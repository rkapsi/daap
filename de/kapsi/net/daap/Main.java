
package de.kapsi.net.daap;

import java.io.*;
import java.net.*;
import javax.jmdns.*;
import java.util.*;
import org.apache.commons.httpclient.*;

/**
 * Test and Sample environment for DAAP
 */
public class Main implements DaapAuthenticator, DaapStreamSource {
    
    private static final File SONG = new File("music/American_Analog_Set_The_Only_One.mp3");
    
    private static final String LIBRARY = "My Library123";
    private static final int PORT = 5360;
    
    private static final String[] NAMES = { "Hello World!",
    "This Is A Test!",
    "W00t!",
    "Under the Impression",
    "There Is",
    "Elvelator",
    "Daze Gone By",
    "The Only One" };
    
    private static final String[] ALBUMS = { "My Album",
    "Hypnoised",
    "The Blue Album",
    "Another EP",
    "American Analog Set"
    };
    
    private static final String[] ARTISTS = { "My Artist",
    "Good Charlotte",
    "Blink 182",
    "Sum 41",
    "Know by Heart"
    };
    
    private int index_names = 0;
    private int index_albums = 0;
    private int index_artists = 0;
    
    private Library library;
    
    private Playlist playlist0;
    private Playlist playlist1;
    private Playlist playlist2;
    private Playlist playlist3;
    
    private Song updateSong;
    private Song remove;
    
    private DaapServer server;
    
    public Main() throws Exception {
        JmDNS jmdns = new JmDNS();
        ServiceInfo serviceInfo = new ServiceInfo("_daap._tcp.local.", LIBRARY + "._daap._tcp.local.", PORT, 0, 0, LIBRARY);
        jmdns.registerService(serviceInfo);
        
        library = new Library(LIBRARY);
        
        playlist0 = new Playlist("Rock Music");
        playlist1 = new Playlist("Rock & Roll");
        playlist2 = new Playlist("Punk Music");
        playlist3 = new Playlist("All");
        
        library.open();
        
        library.add(playlist0);
        library.add(playlist1);
        library.add(playlist2);
        library.add(playlist3);
        
        for(int i = 0; i < 100; i++) {
            
            Song song = createSong(i);
            
            if (i % 2 == 0) {
                playlist0.add(song);
            } else if (i % 3 == 0) {
                playlist1.add(song);
            } else {
                playlist2.add(song);
            }
            
            playlist3.add(song);
            
            if (remove == null)
                remove = song;
            
            updateSong = song;
        }
        
        library.close();
        
        server = new DaapServer(library, PORT);
        server.setAuthenticator(this);
        server.setStreamSource(this);
        server.init();
        
        Thread serverThread = new Thread(server, "DaapServerThread");
        serverThread.setDaemon(true);
        serverThread.start();
    }
    
    public Song createSong(int i) {
        Song song = new Song("The Only One " + i);
        song.setArtist("American Analog Set");
        song.setAlbum("Know by Heart");
        song.setGenre("Rock/Pop");
        song.setTrackNumber(2);
        song.setSize((int)SONG.length());
        song.setBitrate(128);
        song.setTime(135000); // milli seconds
        song.setUserRating(i % 100);
        return song;
    }
    
    public boolean requiresAuthentication() {
        return false;
    }
    
    public boolean authenticate(String username, String password) {
        return password.equals("test");
    }
    
    public InputStream getSource(Song song)
    throws IOException {
        
        File file = SONG;
        
        if (file != null && file.isFile()) {
            return new FileInputStream(file);
        }
        
        return null;
    }
    
    public void update() {
        
        if (updateSong != null) {
            synchronized(library) {
                library.open();
                
                updateSong.setName("0 " + NAMES[index_names]);
                updateSong.setArtist("0 " + ARTISTS[index_artists]);
                updateSong.setAlbum("0 " + ALBUMS[index_albums]);
                updateSong.setUserRating((updateSong.getUserRating()+20)%120);
                
                updateSong.update();
                
                library.close();
            }
            
            server.update();
            
            index_names = (index_names + 1) % NAMES.length;
            index_artists = (index_artists + 1) % ARTISTS.length;
            index_albums = (index_albums + 1) % ALBUMS.length;
        }
    }
    
    public static void main(String[] args) {
        
        try {
            Main app = new Main();
            
            while(true) {
                
                Thread.sleep(3000);
                app.update();
                System.out.println("Update Library...");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
