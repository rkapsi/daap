
package de.kapsi.net.daap;

import java.io.*;
import java.net.*;
import javax.jmdns.*;
import java.util.*;
import org.apache.commons.httpclient.*;

/**
 * Test and Sample environment for DAAP
 */
public class Main implements DaapAuthenticator, DaapAudioStream {
	
    private static final File SONG = new File("music/02 The Only One.mp3");
    
    private static final String LIBRARY = "My Library";
	private static final int PORT = 5353;
	
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
    
	private Song updateSong;
	private Song removeSong;
    
	private DaapServer server;
	
	public Main() throws Exception {
		JmDNS jmdns = new JmDNS();
		ServiceInfo serviceInfo = new ServiceInfo("_daap._tcp.local.", LIBRARY + "._daap._tcp.local.", PORT, 0, 0, LIBRARY);
		jmdns.registerService(serviceInfo);
		
		library = new Library(LIBRARY);
        
        playlist0 = new Playlist("Rock Music");
        playlist1 = new Playlist("Rock & Roll");
        playlist2 = new Playlist("Punk Music");
        
		library.open();
		
        library.addPlaylist(playlist0);
        library.addPlaylist(playlist1);
        library.addPlaylist(playlist2);
        
        for(int i = 0; i < 100; i++) {
            
            Song song = createSong(i);
                
            if (i % 2 == 0) {
                playlist0.addSong(song);
            } else if (i % 3 == 0) {
                playlist1.addSong(song);
            } else {
                playlist2.addSong(song);
            }
            
            if (removeSong == null)
                removeSong = song;
                
            updateSong = song;
        }
		
		library.close();
		
		server = new DaapServer(library, PORT);
		server.setAuthenticator(this);
		server.setAudioStream(this);
		
		server.start();
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
	
	public void stream(Song song, OutputStream out, int begin, int length) 
		throws IOException {
		
		File file = SONG;
		
		if (file != null && file.isFile()) {
			
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
				
			} finally {
				if (in != null) {
					in.close();
				}
			}
		}
	}
	
	public void update() {
		
        if (updateSong != null) {
            synchronized(library) {
                library.open();
                
                updateSong.setName("0 " + NAMES[index_names]);
                updateSong.setArtist("0 " + ARTISTS[index_artists]);
                updateSong.setAlbum("0 " + ALBUMS[index_albums]);
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
