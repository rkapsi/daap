package com.limegroup.gnutella.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.FileManagerEvent;
import com.limegroup.gnutella.IncompleteFileDesc;
import com.limegroup.gnutella.RouterService;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.filters.IPFilter;
import com.limegroup.gnutella.settings.DaapSettings;
import com.limegroup.gnutella.util.CommonUtils;
import com.limegroup.gnutella.util.FileUtils;
import com.limegroup.gnutella.util.ManagedThread;
import com.limegroup.gnutella.util.NetworkUtils;
import com.limegroup.gnutella.xml.LimeXMLDocument;
import com.limegroup.gnutella.xml.LimeXMLReplyCollection;
import com.limegroup.gnutella.xml.SchemaReplyCollectionMapper;

import de.kapsi.net.daap.DaapAuthenticator;
import de.kapsi.net.daap.DaapConfig;
import de.kapsi.net.daap.DaapFilter;
import de.kapsi.net.daap.DaapServer;
import de.kapsi.net.daap.DaapServerFactory;
import de.kapsi.net.daap.DaapStreamSource;
import de.kapsi.net.daap.DaapThreadFactory;
import de.kapsi.net.daap.DaapTransaction;
import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.Database;
import de.kapsi.net.daap.Library;
import de.kapsi.net.daap.Playlist;
import de.kapsi.net.daap.Song;

/**
 * This class handles the mDNS registration and acts as an
 * interface between LimeWire and DAAP.
 */
public final class DaapManager implements FinalizeListener {
    
    private static final Log LOG = LogFactory.getLog(DaapManager.class);
    private static final DaapManager INSTANCE = new DaapManager();
    
    private static final String AUDIO_SCHEMA = "http://www.limewire.com/schemas/audio.xsd";
    
    public static DaapManager instance() {
        return INSTANCE;
    }

    private SongURNMap map;
    
    private Library library;
    private Database database;
    private Playlist whatsNew;
    private DaapServer server;
    private RendezvousService rendezvous;
    
    private boolean annotateEnabled = false;
    private int maxPlaylistSize;
    
    private DaapManager() {
        if (CommonUtils.isJava14OrLater() == false)
            throw new RuntimeException("Cannot instance DaapManager");
        
        GUIMediator.addFinalizeListener(this);
    }
    
    /**
     * Initializes the Library
     */
    public synchronized void init() {
        
        if (isServerRunning()) {
            setAnnotateEnabled(annotateEnabled);
        }
    }
    
    /**
     * Starts the DAAP Server
     */
    public synchronized void start() throws IOException {
        
        if (!isServerRunning()) {
            
            try {
                
                map = new SongURNMap();
                
                maxPlaylistSize = DaapSettings.DAAP_MAX_LIBRARY_SIZE.getValue();
                
                String name = DaapSettings.DAAP_LIBRARY_NAME.getValue();
                int revisions = DaapSettings.DAAP_LIBRARY_REVISIONS.getValue();
                boolean useLibraryGC = DaapSettings.DAAP_LIBRARY_GC.getValue();
                library = new Library(name, revisions, useLibraryGC);
                
                database = new Database(name);
                whatsNew = new Playlist(GUIMediator.getStringResource("SEARCH_TYPE_WHATSNEW"));
                
                DaapTransaction txn = DaapTransaction.open(library);
                library.add(database);
                database.add(whatsNew);
                whatsNew.setSmartPlaylist(true);
                txn.commit();
                
                LimeConfig config = new LimeConfig();
                
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
                            config.nextPort();
                        } else {
                            throw bindErr;
                        }
                    }
                }
                
                Thread serverThread = new ManagedThread(server, "DaapServerThread");
                serverThread.setDaemon(true);
                serverThread.start();
                
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
        
        if (rendezvous != null)
            rendezvous.close();
        
        if (server != null)
            server.stop();
        
        if (map != null)
            map.clear();
        
        rendezvous = null;
        server = null;
        map = null;
        library = null;
        whatsNew = null;
        database = null;
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
        if (isServerRunning())
            stop();
    
        start();
        init();
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
        
        if (isServerRunning()) {
            rendezvous.updateService();

            DaapTransaction txn = DaapTransaction.open(library);
            String name = DaapSettings.DAAP_LIBRARY_NAME.getValue();
            library.setName(name);
            database.setName(name);
            txn.commit();
            server.update();
        }
    }
    
    /**
     * Disconnects all clients
     */
    public synchronized void disconnectAll() {
        if (isServerRunning()) {
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
    public synchronized void handleFileManagerEvent(FileManagerEvent evt) {

        if (!isServerRunning())
            return;
              
        if (evt.isChangeEvent()) {
            
            FileDesc oldDesc = evt.getFileDesc()[0];
            
            Song song = map.remove(oldDesc.getSHA1Urn());
            
            if (song != null) {
                
                FileDesc newDesc = evt.getFileDesc()[1];
                map.put(song, newDesc.getSHA1Urn());
                String format = song.getFormat();
                
                // Any changes in the meta data?
                if ( updateSongMeta(song, newDesc) ) {
                    DaapTransaction txn = DaapTransaction.open(library);
                    database.update(song);
                    txn.commit();
                    server.update();
                }
            }
            
        } else if (evt.isAddEvent()) {
            
            if (database.getMasterPlaylist().size() >= maxPlaylistSize) {
                
                return;
            }
            
            FileDesc file = evt.getFileDesc()[0];
            
            if (!(file instanceof IncompleteFileDesc)) {
                String name = file.getName().toLowerCase(Locale.US);
                if (isSupportedFormat(name)) {
                    
                    Song song = createSong(file);
                    map.put(song, file.getSHA1Urn());
                    
                    DaapTransaction txn = DaapTransaction.open(library);
                    database.add(song);
                    txn.commit();
                    server.update();
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
                DaapTransaction txn = DaapTransaction.open(library);
                database.remove(song);
                txn.commit();
                server.update();
            }
        }
    }
    
    /**
     * Called by VisualConnectionCallback/MetaFileManager.
     */
    public synchronized void setAnnotateEnabled(boolean enabled) {
        
        this.annotateEnabled = enabled;
        
        if (!isServerRunning() || !enabled)
            return;
         
        int size = database.getMasterPlaylist().size();
        
        DaapTransaction txn = DaapTransaction.open(library);
        
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
                                database.update(song);
                            }

                        } else if (size < maxPlaylistSize){
                            
                            // URN was unknown and we must create a
                            // new Song for this URN...
                            
                            song = createSong(file);
                            tmpMap.put(song, urn);
                            database.getMasterPlaylist().add(song);
                            size++;
                        }
                    }
                }
            }
        }
        
        // See 1)
        // As all known URNs were removed from 'map' only
        // deleted FileDesc URNs can be leftover! We must 
        // remove the associated Songs from the Library now
        Iterator it = map.getSongIterator();
        while(it.hasNext()) {
            Song song = (Song)it.next();
            database.remove(song);
        }
        
        map.clear();
        map = tmpMap; // tempMap is the new 'map'
        
        txn.commit();
        server.update();
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
        
        if (collection == null) {
            LOG.error("LimeXMLReplyCollection is null");
            return false;
        }
        
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
                // iTunes expects the song length in milliseconds
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
    
    /**
     * This factory creates ManagedThreads for the DAAP server
     */
    private final class LimeThreadFactory implements DaapThreadFactory {
               
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
     * A LimeWire specific implementation of DaapConfig
     */
    private final class LimeConfig implements DaapConfig {
        
        public LimeConfig() {
            // Reset PORT to default value to prevent increasing
            // it to infinity
            DaapSettings.DAAP_PORT.revertToDefault();
        }
        
        public String getServerName() {
            return CommonUtils.getHttpServer();
        }
        
        public void nextPort() {
            int port = DaapSettings.DAAP_PORT.getValue();
            DaapSettings.DAAP_PORT.setValue(port+1);
        }
        
        public int getBacklog() {
            return 0;
        }
        
        public InetSocketAddress getInetSocketAddress() {
            int port = DaapSettings.DAAP_PORT.getValue();
            return new InetSocketAddress(port);
        }
        
        public int getMaxConnections() {
            return DaapSettings.DAAP_MAX_CONNECTIONS.getValue();
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
            props.put(VERSION, Integer.toString(DaapUtil.VERSION_3));
            
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
