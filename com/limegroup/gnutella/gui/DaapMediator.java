package com.limegroup.gnutella.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.net.InetAddress;
import java.net.BindException;

import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.RouterService;
import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.IncompleteFileDesc;
import com.limegroup.gnutella.util.CommonUtils;
import com.limegroup.gnutella.util.NetworkUtils;
import com.limegroup.gnutella.util.FileUtils;
import com.limegroup.gnutella.util.ManagedThread;
import com.limegroup.gnutella.settings.DaapSettings;
import com.limegroup.gnutella.FileManagerEvent;
import com.limegroup.gnutella.filters.IPFilter;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.FinalizeListener;

import com.limegroup.gnutella.xml.LimeXMLDocument;
import com.limegroup.gnutella.xml.LimeXMLReplyCollection;
import com.limegroup.gnutella.xml.SchemaReplyCollectionMapper;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import de.kapsi.net.daap.Song;
import de.kapsi.net.daap.Playlist;
import de.kapsi.net.daap.Library;
import de.kapsi.net.daap.DaapServer;
import de.kapsi.net.daap.DaapServerFactory;
import de.kapsi.net.daap.SimpleConfig;
import de.kapsi.net.daap.DaapFilter;
import de.kapsi.net.daap.DaapStreamSource;
import de.kapsi.net.daap.DaapAuthenticator;
import de.kapsi.net.daap.DaapThreadFactory;

/**
 * This class handles the mDNS registration and acts as an
 * interface between LimeWire and DAAP.
 */
public final class DaapMediator implements FinalizeListener {
    
    private static final Log LOG = LogFactory.getLog(DaapMediator.class);
    private static final DaapMediator INSTANCE = new DaapMediator();
    
    private static final String AUDIO_SCHEMA = "http://www.limewire.com/schemas/audio.xsd";
    
    public static DaapMediator instance() {
        return INSTANCE;
    }

    private SongURNMap map;
    
    private Library library;
    private Playlist whatsNew;
    private DaapServer server;
    private RendezvousService rendezvous;
    private UpdateWorker updateWorker;
    
    private boolean annotateEnabled = false;
    
    private DaapMediator() {
        if (isSupportedPlatform()) {
            GUIMediator.addFinalizeListener(this);
        }
    }
    
    /**
     * Initializes the Library
     */
    public synchronized void init() {
        
        if (isSupportedPlatform() && isServerRunning()) {
            setAnnotateEnabled(annotateEnabled);
        }
    }
    
    /**
     * Starts the DAAP Server
     */
    public synchronized void start() throws IOException {
        
        if (isSupportedPlatform() && !isServerRunning()) {
            
            try {
                
                map = new SongURNMap();
                library = new Library(DaapSettings.DAAP_LIBRARY_NAME.getValue());
                whatsNew = new Playlist(GUIMediator.getStringResource("SEARCH_TYPE_WHATSNEW"));
                whatsNew.setSmartPlaylist(true);
                
                library.open();
                library.add(whatsNew);
                library.close();
                
                updateWorker = new UpdateWorker();
                
                // Reset PORT to default value to prevent increasing
                // it to infinity
                DaapSettings.DAAP_PORT.revertToDefault();
                
                SimpleConfig config = new SimpleConfig(CommonUtils.getHttpServer(), DaapSettings.DAAP_PORT.getValue());
                config.setMaxConnections(DaapSettings.DAAP_MAX_CONNECTIONS.getValue());
                
                final boolean NIO = DaapSettings.DAAP_USE_NIO.getValue();
                server = DaapServerFactory.createServer(library, config, NIO);
                server.setAuthenticator(new LimeAuthenticator());
                server.setStreamSource(new LimeStreamSource());
                server.setFilter(new LimeFilter());
                
                if (!NIO) {
                    server.setThreadFactory(new LimeThreadFactory());
                }
                
                final int maxAttempts = 10;
                
                for(int i = 0; i < maxAttempts; i++) {
                    try {
                        server.bind();
                        break;
                    } catch (BindException bindErr) {
                        if (i < (maxAttempts-1)) {
                            // try next port...
                            int port = DaapSettings.DAAP_PORT.getValue()+1;
                            config.setInetSocketAddress(port);
                            DaapSettings.DAAP_PORT.setValue(port);
                        } else {
                            throw bindErr;
                        }
                    }
		}
                
                Thread serverThread = new ManagedThread(server, "DaapServerThread");
                serverThread.setDaemon(true);
                serverThread.start();
             
                Thread updateWorkerThread = new ManagedThread(updateWorker, "UpdateWorkerThread");
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
        
        if (isSupportedPlatform()) {
            
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
            whatsNew = null;
        }
    }
    
    /**
     * Restarts the DAAP server and re-registers it via mDNS.
     * This is equivalent to:<p>
     *
     * <code>
     * stop();
     * start();
     * init();
     * </code>
     */
    public synchronized void restart() throws IOException {
        if (isSupportedPlatform()) {
            
            if (isServerRunning())
                stop();
        
            start();
            init();
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
    public synchronized void updateService() throws IOException {
        
        if (isSupportedPlatform() && isServerRunning()) {
            rendezvous.updateService();
            updateWorker.setName(DaapSettings.DAAP_LIBRARY_NAME.getValue());
        }
    }
    
    /**
     * Disconnects all clients
     */
    public synchronized void disconnectAll() {
        if (isSupportedPlatform() && isServerRunning()) {
            server.disconnectAll();
        }
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
     * A helper method.
     */
    private static boolean isSupportedPlatform() {
        return CommonUtils.isJava14OrLater();
    }
    
    /**
     * Returns true if the extension of name is a supported file type.
     */
    private static boolean isSupportedFormat(String name) {
        String[] types = DaapSettings.DAAP_SUPPORTED_FILE_TYPES.getValue();
        for(int i = 0; i < types.length; i++) {
            if (name.endsWith(types[i])) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Called by VisualConnectionCallback
     */
    public void handleFileManagerEvent(FileManagerEvent evt) {
        
        if (isSupportedPlatform() && isServerRunning()) {
              
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
                    if (isSupportedFormat(name)) {
                        
                        Song song = createSong(file);
                        
                        map.put(song, file.getSHA1Urn());
                        
                        updateWorker.add(song, true);
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
        
        if (isSupportedPlatform() && isServerRunning() && enabled) {
            
            // disable updateWorker
            updateWorker.setEnabled(false);
            
            SongURNMap tmpMap = new SongURNMap();
            
            FileDesc[] files = RouterService.getFileManager().getAllSharedFileDescriptors();
            
            for(int i = 0; i < files.length; i++) {
                
                FileDesc file = files[i];
                
                if (!(file instanceof IncompleteFileDesc)) {
                    String name = file.getName().toLowerCase(Locale.US);
                    if (isSupportedFormat(name)) {
                        
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
                                updateWorker.add(song, false);
                            }
                        }
                    }
                }
            }
            
            // See 1)
            // As all known URNs were removed from 'map' and only
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
            
            song.setFormat(ext.toLowerCase(Locale.US));
            
            updateSongMeta(song, desc);
        }
        
        return song;
    }
    
    /**
     * Sets the meta data
     */
    private boolean updateSongMeta(Song song, FileDesc desc) {
        
        SchemaReplyCollectionMapper map = SchemaReplyCollectionMapper.instance();
        LimeXMLReplyCollection collection = map.getReplyCollection(AUDIO_SCHEMA);
        LimeXMLDocument doc = collection.getDocForHash(desc.getSHA1Urn());
        
        if (doc == null)
            return false;
        
        boolean update = false;
        
        String title = doc.getValue("audios__audio__title__");
        String track = doc.getValue("audios__audio__track__");
        String artist = doc.getValue("audios__audio__artist__");
        String album = doc.getValue("audios__audio__album__");
        String genre = doc.getValue("audios__audio__genre__");
        String bitrate = doc.getValue("audios__audio__bitrate__");
        String comments = doc.getValue("audios__audio__comments__");
        String time = doc.getValue("audios__audio__seconds__");
        String year = doc.getValue("audios__audio__year__");
        
        if (title != null) {
            String currentTitle = song.getName();
            if (currentTitle == null || !title.equals(currentTitle)) {
                update = true;
                song.setName(title);
            }
        }
        
        int currentTrack = song.getTrackNumber();
        if (track != null) {
            try {
                int num = Integer.parseInt(track);
                if (num > 0 && num != currentTrack) {
                    update = true;
                    song.setTrackNumber(num);
                }
            } catch (NumberFormatException err) {}
        } else if (currentTrack != 0) {
            update = true;
            song.setTrackNumber(0);
        }
        
        String currentArtist = song.getArtist();
        if (artist != null) {
            if (currentArtist == null || !artist.equals(currentArtist)) {
                update = true;
                song.setArtist(artist);
            }
        } else if (currentArtist != null) {
            update = true;
            song.setArtist(null);
        }
        
        String currentAlbum = song.getAlbum();
        if (album != null) {
            if (currentAlbum == null || !album.equals(currentAlbum)) {
                update = true;
                song.setAlbum(album);
            }
        } else if (currentAlbum != null) {
            update = true;
            song.setAlbum(null);
        }
        
        String currentGenre = song.getGenre();
        if (genre != null) {
            if (currentGenre == null || !genre.equals(currentGenre)) {
                update = true;
                song.setGenre(genre);
            }
        } else if (currentGenre != null) {
            update = true;
            song.setGenre(null);
        }
        
        String currentComments = song.getComment();
        if (comments != null) {
            if (currentComments == null || !comments.equals(currentComments)) {
                update = true;
                song.setComment(comments);
            }
        } else if (currentComments != null) {
            update = true;
            song.setComment(null);
        }
        
        int currentBitrate = song.getBitrate();
        if (bitrate != null) {
            try {
                int num = Integer.parseInt(bitrate);
                if (num > 0 && num != currentBitrate) {
                    update = true;
                    song.setBitrate(num);
                }
            } catch (NumberFormatException err) {}
        } else if (currentBitrate != 0) {
            update = true;
            song.setBitrate(0);
        }
        
        int currentTime = song.getTime();
        if (time != null) {
            try {
                // iTunes expects the song length in milli seconds
                int num = (int)Integer.parseInt(time)*1000;
                if (num > 0 && num != currentTime) {
                    update = true;
                    song.setTime(num);
                }
            } catch (NumberFormatException err) {}
        } else if (currentTime != 0) {
            update = true;
            song.setTime(0);
        }
        
        int currentYear = song.getYear();
        if (year != null) {
            try {
                int num = Integer.parseInt(year);
                if (num > 0 && num != currentYear) {
                    update = true;
                    song.setYear(num);
                }
            } catch (NumberFormatException err) {}
        } else if (currentYear != 0) {
            update = true;
            song.setYear(0);
        }
        
        // iTunes expects the date/time in seconds
        int mod = (int)(desc.lastModified()/1000);
        if (song.getDateModified() != mod) {
            update = true;
            song.setDateModified(mod);
        }
        
        return update;
    }
    
    private final class LimeThreadFactory implements DaapThreadFactory {
        
        public LimeThreadFactory() {    
        }
        
        public Thread createDaapThread(Runnable runner, String name) {
            Thread thread = new ManagedThread(runner, name);
            thread.setDaemon(true);
            return thread;
        }
    }
    
    /**
     * Handles the audio stream
     */
    private final class LimeStreamSource implements DaapStreamSource {
        
        public LimeStreamSource() {
        }
        
        public FileInputStream getSource(Song song) throws IOException {
            URN urn = map.get(song);
            
            if (urn != null) {
                FileDesc fileDesc = RouterService.getFileManager().getFileDescForUrn(urn);
                
                if (fileDesc != null) {
                    File file = fileDesc.getFile();
                    
                    FileInputStream in = new FileInputStream(file);
                    
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
            return DaapSettings.DAAP_REQUIRES_PASSWORD.getValue();
        }
        
        /**
         * Returns true if username and password are correct.<p>
         * Note: iTunes does not support usernames (i.e. it's
         * don't care)!
         */
        public boolean authenticate(String username, String password) {
            return password.equals(DaapSettings.DAAP_PASSWORD.getValue());
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
            
            try {
                // Is address a private address?
                if ( ! NetworkUtils.isPrivateAddress(address))
                    return false;
            } catch (IllegalArgumentException err) {
                LOG.error(err);
                return false;
            }
            
            // Is it a annoying fellow? >:-)
            return IPFilter.instance().allow(address.getAddress());
        }
    }
    
    /**
     * Helps us to publicize and update the DAAP Service via
     * multicast-DNS (aka Rendezvous or Zeroconf)
     */
    private final class RendezvousService {
        
        private static final String VERSION = "Version";
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
            
            String type = DaapSettings.DAAP_TYPE_NAME.getValue();
            String name = DaapSettings.DAAP_SERVICE_NAME.getValue();
            
            int port = DaapSettings.DAAP_PORT.getValue();
            int weight = DaapSettings.DAAP_WEIGHT.getValue();
            int priority = DaapSettings.DAAP_PRIORITY.getValue();
            
            boolean password = DaapSettings.DAAP_REQUIRES_PASSWORD.getValue();
            
            java.util.Hashtable props = new java.util.Hashtable();
            
            // Greys the share and the playlist names when iTunes's
            // protocol version is different from this version. It's
            // only a nice visual effect and has no impact to the
            // ability to connect this server! Disabled because 
            // iTunes 4.2 is still widespread...
            //props.put(VERSION, Integer.toString(DaapUtil.VERSION_3));
            
            // This is the inital share name
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
            
            ServiceInfo service = new ServiceInfo(type, qualifiedName, port, 
                                                     weight, priority, props);
            
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
            
            
            if (service.getPort() != DaapSettings.DAAP_PORT.getValue())
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
        private HashMap /* of Song -> Boolean */ add = new HashMap();
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
        
        public void add(Song song, boolean isNew) {
            synchronized(LOCK) {
                add.put(song, new Boolean(isNew));
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
                if (!add.containsKey(song) &&
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
                                    
                                    final int MAX_SIZE = DaapSettings.DAAP_MAX_LIBRARY_SIZE.getValue();
                                    
                                    Iterator it = add.keySet().iterator();
                                    while(it.hasNext() && running && library.size() < MAX_SIZE) {
                                        Song song = (Song)it.next();
                                        Boolean bool = (Boolean)add.get(song);
                                        
                                        if (bool.booleanValue()) {
                                            // add to What's New playlist
                                            whatsNew.add(song);
                                        } else {
                                            // add to master playlist
                                            library.add(song);
                                        }
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
