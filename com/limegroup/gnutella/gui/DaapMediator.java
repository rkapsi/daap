package com.limegroup.gnutella.gui;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.net.InetAddress;
import java.net.BindException;
import javax.swing.SwingUtilities;

import com.sun.java.util.collections.List;

import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.ErrorService;
import com.limegroup.gnutella.RouterService;
import com.limegroup.gnutella.FileManager;
import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.IncompleteFileDesc;
import com.limegroup.gnutella.util.CommonUtils;
import com.limegroup.gnutella.util.NetworkUtils;
import com.limegroup.gnutella.util.FileUtils;
import com.limegroup.gnutella.settings.iTunesSettings;
import com.limegroup.gnutella.xml.LimeXMLDocument;
import com.limegroup.gnutella.FileManagerEvent;
import com.limegroup.gnutella.filters.IPFilter;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.FinalizeListener;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import de.kapsi.net.daap.Song;
import de.kapsi.net.daap.Library;
import de.kapsi.net.daap.DaapServer;
import de.kapsi.net.daap.DaapConfig;
import de.kapsi.net.daap.DaapFilter;
import de.kapsi.net.daap.DaapStreamSource;
import de.kapsi.net.daap.DaapAuthenticator;

/**
 *
 */
public final class DaapMediator implements FinalizeListener {
    
    private static final Log LOG = LogFactory.getLog(DaapMediator.class);
    
    private static final DaapMediator INSTANCE = new DaapMediator();
    
    public static DaapMediator instance() {
        return INSTANCE;
    }
    
    private SongURNMap map;
    
    private Library library;
    private DaapServer server;
    private RendezvousService rendezvous;
    private UpdateWorker updateWorker;
    
    private boolean annotateEnabled = false;
    
    private DaapMediator() {
        if (CommonUtils.isJava14OrLater())
            GUIMediator.addFinalizeListener(this);
    }
    
    /**
     * Initializes the Library
     */
    public synchronized void init() {
        
        if (CommonUtils.isJava14OrLater() && isServerRunning()) {
            setAnnotateEnabled(annotateEnabled);
        }
    }
    
    /**
     * Starts the DAAP Server
     */
    public synchronized void start()
    throws IOException {
        
        if (CommonUtils.isJava14OrLater() && !isServerRunning()) {
            
            try {
                
                map = new SongURNMap();
                library = new Library(iTunesSettings.DAAP_LIBRARY_NAME.getValue());
                library.init();
                
                updateWorker = new UpdateWorker();
                
                LimeConfig config = new LimeConfig();
                server = new DaapServer(library, config);
                server.setAuthenticator(new LimeAuthenticator());
                server.setStreamSource(new LimeStreamSource());
                server.setFilter(new LimeFilter());
                
                final int attempts = 10;
                
                for(int i = 0; i < attempts; i++) {
                    try {
                        server.start();
                        break;
                    } catch (BindException bindErr) {
                        if (i == attempts-1)
                            throw bindErr;
                        else
                            config.nextPort();
                    }
                }
                
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
    
    /**
     * Stops the DAAP Server and releases all resources
     */
    public synchronized void stop() {
        
        if (CommonUtils.isJava14OrLater()) {
            
            if (updateWorker != null)
                updateWorker.stop();
            
            if (rendezvous != null)
                rendezvous.close();
            
            if (server != null)
                server.stop();
            
            if (map != null)
                map.clear();
            
            rendezvous = null;
            server = null;
            updateWorker = null;
            map = null;
            library = null;
        }
    }
    
    /**
     * Shutdown the DAAP service properly. In this case
     * is the main focus on mDNS (Rendezvous) as in
     * some rare cases iTunes doesn't recognize that
     * LimeWire/DAAP is no longer online.
     */
    public void doFinalize() {
        stop();
    }
    
    /**
     * Updates the multicast-DNS servive info
     */
    public synchronized void updateService()
    throws IOException {
        
        if (CommonUtils.isJava14OrLater() && isServerRunning()) {
            rendezvous.updateService();
            updateWorker.setName(iTunesSettings.DAAP_LIBRARY_NAME.getValue());
        }
    }
    
    /**
     * Disconnects all clients
     */
    public synchronized void disconnectAll() {
        if (CommonUtils.isJava14OrLater() && isServerRunning())
            server.disconnectAll();
    }
    
    /**
     * Returns <tt>true</tt> if server is running
     */
    public synchronized boolean isServerRunning() {
        if (server != null) {
            return server.isRunning();
        }
        return false;
    }
    
    /**
     * Returns true if the extension of name is a supported file type.
     */
    private static boolean isSupportedFileType(String name) {
        String[] types = iTunesSettings.DAAP_SUPPORTED_FILE_TYPES.getValue();
        for(int i = 0; i < types.length; i++)
            if (name.endsWith(types[i]))
                return true;
        return false;
    }
    
    /**
     * Called by VisualConnectionCallback/MetaFileManager.
     */
    public synchronized void handleFileManagerEvent(FileManagerEvent evt) {
        
        if (CommonUtils.isJava14OrLater() && isServerRunning()) {
            
            if (evt.isChangeEvent()) {
                
                FileDesc oldDesc = evt.getFileDesc()[0];
                
                Song song = map.remove(oldDesc.getSHA1Urn());
                
                if (song != null) {
                    
                    FileDesc newDesc = evt.getFileDesc()[1];
                    map.put(song, newDesc.getSHA1Urn());
                    String format = song.getFormat();
                    
                    // Any changes in the meta data?
                    if ( updateSongMeta(song, newDesc) ) {
                        updateWorker.update(song);
                    }
                }
                
            } else if (evt.isAddEvent()) {
                
                FileDesc file = evt.getFileDesc()[0];
                
                if (!(file instanceof IncompleteFileDesc)) {
                    String name = file.getName().toLowerCase(Locale.US);
                    if (isSupportedFileType(name)) {
                        
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
    }
    
    /**
     * Called by VisualConnectionCallback/MetaFileManager.
     */
    public synchronized void setAnnotateEnabled(boolean enabled) {
        
        this.annotateEnabled = enabled;
        
        if (CommonUtils.isJava14OrLater() &&
        isServerRunning() && enabled) {
            
            // disable updateWorker
            updateWorker.setEnabled(false);
            
            SongURNMap tmpMap = new SongURNMap();
            
            FileDesc[] files = RouterService.getFileManager().getAllSharedFileDescriptors();
            
            for(int i = 0; i < files.length; i++) {
                
                FileDesc file = files[i];
                
                if (!(file instanceof IncompleteFileDesc)) {
                    String name = file.getName().toLowerCase(Locale.US);
                    if (isSupportedFileType(name)) {
                        
                        URN urn = file.getSHA1Urn();
                        
                        // 1)
                        // _Remove_ URN from the current 'map'...
                        Song song = map.remove(urn);
                            
                        // Check if URN is already in the tmpMap.
                        // If so do nothing as we don't want add 
                        // the same file multible times...
                        if (tmpMap.contains(urn) == false) {
                            
                            // This URN was already mapped with a Song.
                            // Save the Song (again) and update the meta
                            // data if necessary
                            if (song != null) {
                                
                                tmpMap.put(song, urn);

                                // Any changes in the meta data?
                                if ( updateSongMeta(song, file) ) {
                                    updateWorker.update(song);
                                }

                            } else {
                                
                                // URN was unknown and we must create a
                                // new Song for this URN...
                                
                                song = createSong(file);
                                tmpMap.put(song, urn);
                                updateWorker.add(song);
                            }
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
    }
    
    /**
     * Create a Song and sets its meta data with
     * the data which is retrieved from the FileDesc
     */
    private Song createSong(FileDesc desc) {
        
        Song song = new Song(desc.getName());
        song.setSize((int)desc.getSize());
        song.setDateAdded((int)(System.currentTimeMillis()/1000));
        
        File file = desc.getFile();
        String ext = FileUtils.getFileExtension(file);
        
        if (ext != null) {
            
            // Note: This is required for formats other than MP3
            // For example AAC (.m4a) files won't play if no
            // format is set. As far as I can tell from the iTunes
            // 'Get Info' dialog are Songs assumed as MP3 until
            // a format is set explicit.
            
            song.setFormat(ext);
            
            updateSongMeta(song, desc);
        }
        
        return song;
    }
    
    /**
     * Sets the meta data
     */
    private boolean updateSongMeta(Song song, FileDesc desc) {
        
        String format = song.getFormat();
        
        // Meta Data is only available for MP3s
        if (format == null || format.equals("mp3") == false)
            return false;
        
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
                // iTunes expects the song length in milli seconds
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
        
        // iTunes expects the date/time in seconds
        int mod = (int)(desc.lastModified()/1000);
        if (song.getDateModified() != mod) {
            update = true;
            song.setDateModified(mod);
        }
        
        return update;
    }
    
    /**
     * Handles the audio stream
     */
    private final class LimeStreamSource implements DaapStreamSource {
        
        public LimeStreamSource() {
        }
        
        public InputStream getSource(Song song) throws IOException {
            URN urn = map.get(song);
            
            if (urn != null) {
                FileDesc fileDesc = RouterService.getFileManager().getFileDescForUrn(urn);
                
                if (fileDesc != null) {
                    File file = fileDesc.getFile();
                    
                    BufferedInputStream in
                    = new BufferedInputStream(new FileInputStream(file));
                    
                    return in;
                }
            }
            
            return null;
        }
    }
    
    /**
     * Implements the DaapAuthenticator
     */
    private final class LimeAuthenticator implements DaapAuthenticator {
        
        public LimeAuthenticator() {
        }
        
        public boolean requiresAuthentication() {
            return iTunesSettings.DAAP_REQUIRES_PASSWORD.getValue();
        }
        
        /**
         * Returns true if username and password are correct.<p>
         * Note: iTunes does not support usernames (i.e. it's
         * don't care)!
         */
        public boolean authenticate(String username, String password) {
            return password.equals(iTunesSettings.DAAP_PASSWORD.getValue());
        }
    }
    
    /**
     * The DAAP Library should be only accessable from the LAN
     * as we can not guarantee for the required bandwidth and it
     * could be used to bypass Gnutella etc. Note: iTunes can't
     * connect to DAAP Libraries outside of the LAN but certain
     * iTunes download tools can.
     */
    private final class LimeFilter implements DaapFilter {
        
        public LimeFilter() {
        }
        
        /**
         * Returns true if <tt>address</tt> is a private address
         */
        public boolean accept(InetAddress address) {
            
            // Is address a private address?
            if ( ! NetworkUtils.isPrivateAddress(address))
                return false;
            
            // Is it a annoying fellow? >:-)
            return IPFilter.instance().allow(address.getAddress());
        }
    }
    
    /**
     * A LimeWire specific implementation of DaapConfig
     */
    private final class LimeConfig implements DaapConfig {
        
        public LimeConfig() {
            // Reset PORT to default value to prevent increasing
            // it to infinity
            iTunesSettings.DAAP_PORT.revertToDefault();
        }
        
        public String getServerName() {
            return CommonUtils.getHttpServer();
        }
        
        public int getPort() {
            return iTunesSettings.DAAP_PORT.getValue();
        }
        
        public void nextPort() {
            iTunesSettings.DAAP_PORT.setValue(
            iTunesSettings.DAAP_PORT.getValue()+1);
        }
        
        public int getBacklog() {
            return 0;
        }
        
        public InetAddress getBindAddress() {
            return null;
        }
        
        public int getMaxConnections() {
            return iTunesSettings.DAAP_MAX_CONNECTIONS.getValue();
        }
    }
    
    /**
     * Helps us to publicize and update the DAAP Service via
     * multicast-DNS (aka Rendezvous or Zeroconf)
     */
    private final class RendezvousService {
        
        private static final String MACHINE_NAME = "Machine Name";
        private static final String PASSWORD = "Password";
        
        private JmDNS zeroConf;
        private ServiceInfo service;
        
        public RendezvousService() throws IOException {
            zeroConf = new JmDNS();
        }
        
        public boolean isRegistered() {
            return (service != null);
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
            
            // shows the small lock if Service is protected
            // by a password!
            props.put(PASSWORD, Boolean.toString(password)); 
            
            String qualifiedName = null;
            
            // This isn't really required but as iTunes
            // does it in this way I'm doing it too...
            if (password) {
                qualifiedName = name + "_PW." + type;
            } else {
                qualifiedName = name + "." + type;
            }
            
            ServiceInfo service =
            new ServiceInfo(type, qualifiedName, port, weight, priority, props);
            
            return service;
        }
        
        public void registerService() throws IOException {
            
            if (isRegistered())
                throw new IOException();
            
            ServiceInfo service = createServiceInfo();
            zeroConf.registerService(service);
            this.service = service;
        }
        
        public void unregisterService() {
            if (!isRegistered())
                return;
            
            zeroConf.unregisterService(service);
            service = null;
        }
        
        public void updateService() throws IOException {
            if (!isRegistered())
                throw new IOException();
            
            
            if (service.getPort() != iTunesSettings.DAAP_PORT.getValue())
                unregisterService();
            
            ServiceInfo service = createServiceInfo();
            zeroConf.registerService(service);
            
            this.service = service;
        }
        
        public void close() {
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
        
        private static final int SLEEP = 5*1000; // 5 seconds
        
        private final Object LOCK = new Object();
        
        private String name = null;
        private HashSet /* of Song */ add = new HashSet();
        private HashSet /* of Song */ remove = new HashSet();
        private HashSet /* of Song */ update = new HashSet();
        
        private boolean running = false;
        private boolean enabled = true;
        
        public UpdateWorker() {
        }
        
        public void setName(String name) {
            synchronized(LOCK) {
                this.name = name;
            }
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
                    
                    Thread.sleep(SLEEP);
                    
                    if (running && enabled) {
                        
                        boolean extraSleep = false;
                        
                        synchronized(LOCK) {
                            if (add.size() != 0 ||
                            remove.size() != 0 ||
                            update.size() != 0 ||
                            name != null) {
                                
                                synchronized(library) {
                                    library.open();
                                    
                                    if (name != null) {
                                        library.setName(name);
                                        name = null;
                                    }
                                    
                                    // It makes no sense to remove or update
                                    // songs if Library is empty...
                                    if (library.size() != 0) {
                                        
                                        Iterator it = remove.iterator();
                                        while(it.hasNext() && running) {
                                            library.remove((Song)it.next());
                                        }
                                        
                                        it = update.iterator();
                                        while(it.hasNext() && running) {
                                            ((Song)it.next()).update();
                                        }
                                    }
                                    
                                    Iterator it = add.iterator();
                                    while(it.hasNext() && running) {
                                        library.add((Song)it.next());
                                    }
                                    
                                    library.close();
                                }
                                
                                if (running)
                                    server.update();
                                
                                add.clear();
                                remove.clear();
                                update.clear();
                                
                                // OK, we've updated the Library
                                // Let's take an additional sleep
                                if (running)
                                    extraSleep = true;
                            }
                        }
                        
                        if (extraSleep && running)
                            Thread.sleep(SLEEP);
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
        
        public void clear() {
            synchronized(LOCK) {
                add.clear();
                remove.clear();
                update.clear();
                name = null;
            }
        }
        
        public void stop() {
            running = false;
            clear();
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
        
        private HashMap /* Song -> URN */ songToUrn = new HashMap();
        private HashMap /* URN -> Song */ urnToSong = new HashMap();
        
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
        
        public boolean contains(URN urn) {
            return urnToSong.containsKey(urn);
        }
        
        public boolean contains(Song song) {
            return songToUrn.containsKey(song);
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
