package com.limegroup.gnutella.gui;

import java.io.File;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Locale;

import java.net.InetAddress;

import javax.swing.SwingUtilities;

import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.ErrorService;
import com.limegroup.gnutella.util.CommonUtils;
import com.limegroup.gnutella.util.FileUtils;
import com.limegroup.gnutella.settings.iTunesSettings;
import com.limegroup.gnutella.settings.SharingSettings;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.jmdns.*;
import de.kapsi.net.daap.*;
import org.apache.commons.httpclient.*;
import com.sun.java.util.collections.*;
import com.limegroup.gnutella.*;
import com.limegroup.gnutella.xml.*;

public final class DaapMediator implements DaapAuthenticator, DaapAudioStream {
    
	private static final Log LOG = LogFactory.getLog(DaapMediator.class);
        
	private static final DaapMediator INSTANCE = new DaapMediator();
    
	public static DaapMediator instance() {
		return INSTANCE;
	}
	
	private SongURNMap map;
    
    private UpdateWorker updateWorker;
    
	private Library library;
	
	private DaapServer server;
    private LimeConfig config;
	private RendezvousService rendezvous;
	private boolean annotateEnabled = false;
    
    private DaapMediator() {
    }
    
	private static boolean isEnabled() {
		return (CommonUtils.isJava14OrLater() 
				&& iTunesSettings.DAAP_SUPPORT_ENABLED.getValue());
	}
	
    public synchronized void init() {
        
        if (CommonUtils.isJava14OrLater() && isServerRunning()) {
            if (annotateEnabled)
                setAnnotateEnabled(true);
        }
    }
    
	public synchronized void start() 
            throws IOException {
        
        if (CommonUtils.isJava14OrLater() && !isServerRunning()) {
            
            try {
            
                map = new SongURNMap();
                library = new Library(iTunesSettings.DAAP_LIBRARY_NAME.getValue());
                config = new LimeConfig();
                updateWorker = new UpdateWorker();

                server = new DaapServer(library, config);
                server.setAuthenticator(this);
                server.setAudioStream(this);
                        
                server.start();
                
                Thread updateWorkerThread = new Thread(updateWorker, "UpdateWorkerThread");
                updateWorkerThread.setDaemon(true);
                updateWorkerThread.setPriority(Thread.MIN_PRIORITY+2);
                updateWorkerThread.start();
                
                rendezvous = new RendezvousService();
                rendezvous.registerService();
                
            } catch (IOException err) {
                stop();
                throw err;
            }
        }
	}
	
	public synchronized void stop() {
		
        if (CommonUtils.isJava14OrLater()) {
            
            if (rendezvous != null)
                rendezvous.close();
            
            if (updateWorker != null)
                updateWorker.stop();
            
            if (server != null)
                server.stop();
                
            if (map != null)
                map.clear();
            
            rendezvous = null;
            server = null;
            updateWorker = null;
            map = null;
            library = null;
            config = null;
        }
	}
	
    public synchronized void updateService() 
            throws IOException {
            
        if (CommonUtils.isJava14OrLater() && isServerRunning())
            rendezvous.updateService();
    }

	public synchronized boolean isServerRunning() {
		if (server != null) {
			return server.isRunning();
		}
		return false;
	}
	
	public boolean requiresAuthentication() {
		return iTunesSettings.DAAP_REQUIRES_PASSWORD.getValue();
	}
	
	// username is don't care (not supported by iTunes)
	public boolean authenticate(String username, String password) {
        return password.equals(iTunesSettings.DAAP_PASSWORD.getValue());
	}
	
	public void stream(Song song, OutputStream out, int begin, int length) 
		throws IOException {
		
        URN urn = map.get(song);
        
        if (urn != null) {
            FileDesc fileDesc = RouterService.getFileManager().getFileDescForUrn(urn);
            
            if (fileDesc != null) {
                
                File file = fileDesc.getFile();
                
                BufferedInputStream in = null;
                
                try {
                    
                    in = new BufferedInputStream(new FileInputStream(file));
                    byte[] buffer = new byte[4069*16];
                    
                    int total = 0;
                    int len = -1;
                    
                    if (begin != 0) {
                        in.skip(begin);
                    }
                    
                    while((len = in.read(buffer, 0, buffer.length)) != -1 && total < length) {
                        out.write(buffer, 0, len);
                        
                        total += len;
                    }
                    
                    out.flush();
                    in.close();
                    in = null;
                    
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
        }
	}
	
	/**
     * Returns true if the extension of name is a supported file type.
     */
    private static boolean isSupported(String name) {
        String[] types = iTunesSettings.DAAP_SUPPORTED_FILE_TYPES.getValue();        
        for(int i = 0; i < types.length; i++)
            if (name.endsWith(types[i]))
                return true;
        return false;
        
        //return name.endsWith(".mp3");
    }
    
    public void handleMetaFileManagerEvent(MetaFileManagerEvent evt) {
        
        if (!isEnabled())
            return;
        
        //System.out.println("handleMetaFileManagerEvent");
        
        if (evt.isChangeEvent()) {
        
            FileDesc oldDesc = evt.getFileDesc()[0];
            
            Song song = map.remove(oldDesc.getSHA1Urn());
            
            if (song != null) {
                
                FileDesc newDesc = evt.getFileDesc()[1];
                
                map.put(song, newDesc.getSHA1Urn());
                
                if ( updateSongMeta(song, newDesc) ) {
                    updateWorker.update(song);
                }
            }
            
        } else if (evt.isAddEvent()) {
            
            FileDesc file = evt.getFileDesc()[0];
            
            if (!(file instanceof IncompleteFileDesc)) {
                String name = file.getName().toLowerCase(Locale.US);
                if (isSupported(name)) {
                    
                    Song song = createSong(file);
                    
                    map.put(song, file.getSHA1Urn());
                    
                    updateWorker.add(song);
                }
            }
            
        } else if (evt.isRenameEvent()) {
        
            FileDesc oldDesc = evt.getFileDesc()[0];
            Song song = map.remove(oldDesc.getSHA1Urn());
            
            if (song != null) {
                FileDesc newDesc = evt.getFileDesc()[1];
                map.put(song, newDesc.getSHA1Urn());
            }
            
        } else if (evt.isRemoveEvent()) {
            
            FileDesc file = evt.getFileDesc()[0];
            Song song = map.remove(file.getSHA1Urn());
            
            if (song != null) {
                updateWorker.remove(song);
            }
        }
    }
    
    public void setAnnotateEnabled(boolean enabled) {
        
        this.annotateEnabled = enabled;
        
        if (!isEnabled() || !enabled)
            return;
        
        // disable updateWorker
        updateWorker.setEnabled(false);
        
        SongURNMap tmpMap = new SongURNMap();
        
        FileDesc[] files = RouterService.getFileManager().getAllSharedFileDescriptors();
        
        for(int i = 0; i < files.length; i++) {
            
            FileDesc file = files[i];
            
            if (!(file instanceof IncompleteFileDesc)) {
                String name = file.getName().toLowerCase(Locale.US);
                if (isSupported(name)) {
                    
                    // 1)
                    // _Remove_ URN from the current 'map'...
                    Song song = map.remove(file.getSHA1Urn());
                    
                    // This URN was already mapped with a Song.
                    // Save the Song (again) and update the meta 
                    // data if necessary
                    if (song != null) {
                        
                        tmpMap.put(song, file.getSHA1Urn());
                        
                        // Any changes in the meta data?
                        if ( updateSongMeta(song, file) ) {
                            updateWorker.update(song);
                        }
                    
                    // URN was unknown and we must create a
                    // new Song for this URN...
                    } else {
                    
                        song = createSong(file);
                        tmpMap.put(song, file.getSHA1Urn());
                        updateWorker.add(song);
                    }
                }
            }
        }
        
        // See 1)
        // As all known URNs were removed from 'map' only
        // deleted FileDesc URNs can be leftover and we
        // must remove the associated Songs from the Library
        Iterator it = map.getSongIterator();
        while(it.hasNext()) {
            Song song = (Song)it.next();
            updateWorker.remove(song);
        }
        
        map.clear();
        map = tmpMap; // tempMap is the new 'map'
        
        // enable updateWorker
        updateWorker.setEnabled(true);
    }
    
    /**
     * Create a Song and sets its meta data with
     * the data which is retrieved from the FileDesc
     */
    private Song createSong(FileDesc desc) {
        
        Song song = new Song(desc.getName());
        song.setSize((int)desc.getSize());
        song.setDateAdded((int)(System.currentTimeMillis()/1000));
        
        String ext = FileUtils.getFileExtension(desc.getFile());
        
        if (ext != null) {
        
            // Note: This is required for formats other than MP3
            // For example AAC (.m4a) files won't play if no
            // format is set. As far as I can tell from the iTunes 
            // 'Get Info' dialog are Songs assumed as MP3 until
            // a format is set explicit.
            
            song.setFormat(ext);
            
            // 
            /*if (ext.equals("mp3") || ext.equals("m4a") || ext.equals("wav")) {
                song.setFormat(ext);
                
            } else if (ext.equals("aif") || ext.equals("aiff")) {
            
                song.setFormat("aiff");
            }*/
        }
        
        // get the meta data...
        updateSongMeta(song, desc);
        
        return song;
    }
    
    /**
     * Sets the meta data
     */
    private boolean updateSongMeta(Song song, FileDesc desc) {
		
        com.sun.java.util.collections.List docs = desc.getLimeXMLDocuments();
        
        boolean update = false;
        
        String title = null;
        String track = null;
        String artist = null;
        String album = null;
        String genre = null;
        String bitrate = null;
        String comments = null;
        String time = null;
        String year = null;
        
        for(int i = 0; i < docs.size(); i++) {
            LimeXMLDocument doc = (LimeXMLDocument)docs.get(i);
            
            if (title == null)
                title = doc.getValue("audios__audio__title__");
                
            if (track == null) 
                track = doc.getValue("audios__audio__track__");
                
            if (artist == null)
                artist = doc.getValue("audios__audio__artist__");
            
            if (album == null)
                album = doc.getValue("audios__audio__album__");
                
            if (genre == null)
                genre = doc.getValue("audios__audio__genre__");
                
            if (bitrate == null)
                bitrate = doc.getValue("audios__audio__bitrate__");
                
            if (comments == null)
                comments = doc.getValue("audios__audio__comments__");
                
            if (time == null)
                time = doc.getValue("audios__audio__seconds__");
                
            if (year == null) 
                year = doc.getValue("audios__audio__year__");
        }
        
        if (title != null) {
            String currentTitle = song.getName();
            if (currentTitle == null || !title.equals(currentTitle)) {
                update = true;
                song.setName(title);
            }
        }
        
        if (track != null) {
            try {
                int num = Integer.parseInt(track);
                if (num > 0 && num != song.getTrackNumber()) {
                    update = true;
                    song.setTrackNumber(num);
                }
            } catch (NumberFormatException err) {
            }
        }
        
        if (artist != null) {
            String currentArtist = song.getArtist();
            if (currentArtist == null || !artist.equals(currentArtist)) {
                update = true;
                song.setArtist(artist);
            }
        }
        
        if (album != null) {
            String currentAlbum = song.getAlbum();
            if (currentAlbum == null || !album.equals(currentAlbum)) {
                update = true;
                song.setAlbum(album);
            }
        }
        
        if (genre != null) {
            String currentGenre = song.getGenre();
            if (currentGenre == null || !genre.equals(currentGenre)) {
                update = true;
                song.setGenre(genre);
            }
        }
        
        if (comments != null) {
            String currentComments = song.getComment();
            if (currentComments == null || !comments.equals(currentComments)) {
                update = true;
                song.setComment(comments);
            }
        }
        
        if (bitrate != null) {
            try {
                int num = Integer.parseInt(bitrate);
                if (num > 0 && num != song.getBitrate()) {
                    update = true;
                    song.setBitrate(num);
                }
            } catch (NumberFormatException err) {
            }
        }
        
        if (time != null) {
            try {
                int num = (int)Integer.parseInt(time)*1000;
                if (num > 0 && num != song.getTime()) {
                    update = true;
                    song.setTime(num);
                }
            } catch (NumberFormatException err) {
            }
        }
        
        if (year != null) {
            try {
                int num = Integer.parseInt(year);
                if (num > 0 && num != song.getYear()) {
                    update = true;
                    song.setYear(num);
                }
            } catch (NumberFormatException err) {
            }
        }
        
        int mod = (int)(desc.lastModified()/1000);
        if (song.getDateModified() != mod) {
            update = true;
            song.setDateModified(mod);
        }
        
        return update;
    }
    
    private final class LimeConfig implements DaapConfig {
        
        public LimeConfig() {
        }
        
        public String getServerName() {
            return iTunesSettings.DAAP_LIBRARY_NAME.getValue();
        }
        
        public int getPort() {
            return iTunesSettings.DAAP_PORT.getValue();
        }
        
        public int getBacklog() {
            return 0;
        }
        
        public InetAddress getBindAddress() {
            return null;
        }

        public int getMaxConnections() {
            return DaapConfig.DEFAULT_MAX_CONNECTIONS;
        }
    }
    
    private final class RendezvousService {
        
        private static final String MACHINE_NAME = "Machine Name";
        private static final String PASSWORD = "Password";
        
        private JmDNS zeroConf;
        private ServiceInfo service;
        private boolean registered = false;
        
        public RendezvousService() throws IOException {
            zeroConf = new JmDNS();
        }
        
        public synchronized boolean isRegistered() {
            return registered;
        }
        
        private ServiceInfo createServiceInfo() {
            
            String type = iTunesSettings.DAAP_TYPE_NAME.getValue();
			String name = iTunesSettings.DAAP_SERVICE_NAME.getValue();
			
			int port = iTunesSettings.DAAP_PORT.getValue();
			int weight = iTunesSettings.DAAP_WEIGHT.getValue();
			int priority = iTunesSettings.DAAP_PRIORITY.getValue();
			
            boolean password = iTunesSettings.DAAP_REQUIRES_PASSWORD.getValue();
            
			java.util.Hashtable props = new java.util.Hashtable();
            props.put(MACHINE_NAME, name);
            props.put(PASSWORD, Boolean.toString(password)); // shows the small lock 
                                                            // if Service is protected 
                                                            // by a password!
            
            String qualifiedName = null;
            
            // This isn't really required but as iTunes
            // does it in this way I'm doing it too...
            if (password) {
                qualifiedName = name + "_PW." + type;
            } else {
                qualifiedName = name + "." + type;
            }
            
			ServiceInfo service = new ServiceInfo(type, qualifiedName, port, weight, priority, props);
            
            return service;
        }
        
        public synchronized void registerService() throws IOException {
            
            if (isRegistered())
                throw new IOException();
            
            service = createServiceInfo();
			zeroConf.registerService(service);
            
            registered = true;
        }
        
        public synchronized void unregisterService() {
            if (!isRegistered())
                return;
            
            zeroConf.unregisterService(service);
            
            registered = false;
        }
        
        public synchronized void updateService() throws IOException {
            if (!isRegistered())
                throw new IOException();
                
            int currentPort = service.getPort();
            int port = iTunesSettings.DAAP_PORT.getValue();
            
            if (currentPort != port)
                unregisterService();
                
			service = createServiceInfo();
			zeroConf.registerService(service);
            
            registered = true;
        }
        
        public synchronized void close() {
            unregisterService();
            zeroConf.close();
        }
    }
    
    /**
     * The job of UpdateWorker is to collect Library operations
     * and to perform them as one single operation on the Library.
     * This reduces the overall network/cpu load to notify the
     * clients about the changes and reduces the risk to run out 
     * of revisions in principle to zero.
     */
    private final class UpdateWorker implements Runnable {
        
        private final Object LOCK = new Object();
        
        private HashSet add = new HashSet();
        private HashSet remove = new HashSet();
        private HashSet update = new HashSet();
        
        private boolean running = false;
        private boolean enabled = true;
        
        public UpdateWorker() {
        }
        
        public void add(Song song) {
            synchronized(LOCK) {
                add.add(song);
                remove.remove(song);
                update.remove(song);
            }
        }
        
        public void remove(Song song) {
            synchronized(LOCK) {
                remove.add(song);
                add.remove(song);
                update.remove(song);
            }
        }
        
        public void update(Song song) {
            synchronized(LOCK) {
                if (!add.contains(song) && 
                        !remove.contains(song)) {
                        
                    update.add(song);
                }
            }
        }
        
        public void run() {
            
            running = true;
            
            try {

                do {
                
                    Thread.sleep(5*1000);
                    
                    if (running && enabled) {
                        
                        boolean extraSleep = false;
                        
                        synchronized(LOCK) {
                            if (add.size() != 0 || 
                                    remove.size() != 0 || 
                                    update.size() != 0) {
                                    
                                synchronized(library) {
                                    library.open();
                                    
                                    // It makes no sense to remove or update
                                    // songs if Library is empty...
                                    if (library.size() != 0) {
                                    
                                        Iterator it = remove.iterator();
                                        while(it.hasNext()) {
                                            library.remove((Song)it.next());
                                        }
                                        
                                        it = update.iterator();
                                        while(it.hasNext()) {
                                            ((Song)it.next()).update();
                                        }
                                    }
                                    
                                    Iterator it = add.iterator();
                                    while(it.hasNext()) {
                                        library.add((Song)it.next());
                                    }
                                    
                                    library.close();
                                }
                                
                                server.update();
                                
                                add.clear();
                                remove.clear();
                                update.clear();
                                
                                // OK, we've updated the Library
                                // Let's take an additional sleep
                                extraSleep = true;
                            }
                        }
                        
                        if (extraSleep && running)
                            Thread.sleep(5*1000);
                    }
                    
                } while(running);
                
            } catch (InterruptedException err) {
            
            } finally {
                stop();
            }
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public boolean isRunning() {
            return running;
        }
        
        public void stop() {
            running = false;
        }
    }
    
    /**
     * A simple wrapper for a two way mapping as we have to
     * deal in both directions with FileManager and DaapServer
     * <p>
     * Song -> URN
     * URN -> Song
     */
    private final class SongURNMap {
        
        private HashMap songToUrn = new HashMap();
        private HashMap urnToSong = new HashMap();
        
        public SongURNMap() {
        }
        
        public void put(Song song, URN urn) {
            songToUrn.put(song, urn);
            urnToSong.put(urn, song);
        }
        
        public URN get(Song song) {
            return (URN)songToUrn.get(song);
        }
        
        public Song get(URN urn) {
            return (Song)urnToSong.get(urn);
        }
        
        public Song remove(URN urn) {
            Song song = (Song)urnToSong.remove(urn);
            if (song != null)
                songToUrn.remove(song);
            return song;
        }
        
        public URN remove(Song song) {
            URN urn = (URN)songToUrn.remove(song);
            if (urn != null)
                urnToSong.remove(urn);
            return urn;
        }
        
        public Iterator getSongIterator() {
            return songToUrn.keySet().iterator();
        }
        
        public Iterator getURNIterator() {
            return urnToSong.keySet().iterator();
        }
        
        public void clear() {
            urnToSong.clear();
            songToUrn.clear();
        }
        
        public int size() {
            // NOTE: songToUrn.size() == urnToSong.size()
            return songToUrn.size();
        }
    }
}
