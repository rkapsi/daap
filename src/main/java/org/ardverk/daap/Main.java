/*
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004-2010 Roger Kapsi
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ardverk.daap;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Test and Sample environment for DAAP
 * 
 * @author Roger Kapsi
 */
public class Main extends TimerTask implements DaapAuthenticator,
        DaapStreamSource {

    private static final File SONG = new File(new File(System
            .getProperty("user.home")), "Music/song.mp3");

    private static final String LIBRARY = "My Library123";

    private static final int PORT = 5360;

    private static final String[] NAMES = { "Hello World!", "This Is A Test!",
            "W00t!", "Under the Impression", "There Is", "Elvelator",
            "Daze Gone By", "The Only One" };

    private static final String[] ALBUMS = { "My Album", "Hypnoised",
            "The Blue Album", "Another EP", "American Analog Set" };

    private static final String[] ARTISTS = { "My Artist", "Good Charlotte",
            "Blink 182", "Sum 41", "Know by Heart" };

    private int index_names = 0;

    private int index_albums = 0;

    private int index_artists = 0;

    private Library library;
    private Database database;

    private Playlist playlist0;
    private Playlist playlist1;
    private Playlist playlist2;
    private Playlist playlist3;
    private Folder folder;

    private Song updateSong;
    private Song addRemove;

    private DaapServer server;

    private Transaction autoCommitTxn;

    public Main() throws Exception {

        /*
         * System.out.println(SONG);
         * 
         * JmDNS jmdns = new JmDNS(); ServiceInfo serviceInfo = new
         * ServiceInfo("_daap._tcp.local.", LIBRARY + "._daap._tcp.local.",
         * PORT, 0, 0, LIBRARY); jmdns.registerService(serviceInfo);
         * 
         * library = new Library(LIBRARY);
         * 
         * playlist0 = new Playlist(LIBRARY); database = new Database(LIBRARY,
         * playlist0);
         * 
         * playlist1 = new Playlist("Rock & Roll"); playlist2 = new
         * Playlist("Punk Music"); playlist3 = new Playlist("All"); folder = new
         * Folder("Hello World");
         * 
         * playlist1.setSmartPlaylist(null, true);
         * playlist3.setSmartPlaylist(null, true);
         * 
         * library.addDatabase(null, database);
         * 
         * database.addPlaylist(null, playlist1); database.addPlaylist(null,
         * playlist2); database.addPlaylist(null, playlist3);
         * database.addPlaylist(null, folder); folder.addPlaylist(null,
         * playlist3);
         * 
         * Playlist masterPlaylist = database.getMasterPlaylist();
         * 
         * for (int i = 0; i < 100; i++) {
         * 
         * Song song = createSong(i);
         * 
         * if (i % 2 == 0) { playlist0.addSong(null, song); } else if (i % 3 ==
         * 0) { playlist1.addSong(null, song); } else { playlist2.addSong(null,
         * song); }
         * 
         * masterPlaylist.addSong(null, song);
         * 
         * updateSong = song; }
         * 
         * Song testSong = new Song("Foo"); //playlist1.addSong(null, testSong);
         * playlist3.addSong(null, testSong);
         * 
         * addRemove = new Song("0 REMOVE SONG"); playlist0.addSong(null,
         * addRemove); playlist3.addSong(null, addRemove);
         * 
         * library.commit(null);
         * 
         * DaapConfig config = new DaapConfig();
         * config.setInetSocketAddress(PORT); config.setMaxConnections(2);
         * //config.setAuthenticationMethod(DaapConfig.USERNAME_AND_PASSWORD);
         * 
         * server = DaapServerFactory.createNIOServer(library, config);
         * server.setStreamSource(this); server.setAuthenticator(this);
         * server.bind();
         * 
         * Thread serverThread = new Thread(server, "DaapServerThread");
         * serverThread.setDaemon(true); serverThread.start();
         * 
         * autoCommitTxn = new AutoCommitTransaction(library);
         */
    }

    public Song createSong(int i) {
        Song song = new Song("The Only One " + i);
        song.setArtist(null, "American Analog Set");
        song.setAlbum(null, "Know by Heart");
        song.setGenre(null, "Rock/Pop");
        song.setTrackNumber(null, 2);
        song.setSize(null, (int) SONG.length());
        song.setBitrate(null, 128);
        song.setTime(null, 135000); // milli seconds
        song.setUserRating(null, i % 100);
        song.setFormat(null, "mp3");
        // song.setITMSArtistId(null, 0x0038e920);
        // song.setRelativeVolume(null, -100);
        return song;
    }

    public boolean authenticate(String username, String password, String uri,
            String nonce) {
        return password.equals("test");
    }

    public Object getSource(Song song) throws IOException {

        /*
         * File file = SONG;
         * 
         * if (file != null && file.isFile()) { return new
         * FileInputStream(file); }
         * 
         * return null;
         */
        return SONG;
    }

    private int counter = 0;

    // private long t = -1;

    public void run() {

        // Transaction old = autoCommitTxn;
        // autoCommitTxn = library.beginTransaction();

        System.out.println("*** UPDATE ***");

        updateSong.setName(autoCommitTxn, "0 " + counter);// NAMES[index_names]);
        updateSong.setArtist(autoCommitTxn, "0 " + ARTISTS[index_artists]);
        updateSong.setAlbum(autoCommitTxn, "0 " + ALBUMS[index_albums]);
        updateSong.setUserRating(autoCommitTxn,
                (updateSong.getUserRating() + 20) % 120);

        boolean isSmartPlaylist = playlist1.isSmartPlaylist();
        playlist1.setSmartPlaylist(autoCommitTxn, !playlist2.isSmartPlaylist());
        playlist2.setSmartPlaylist(autoCommitTxn, !playlist3.isSmartPlaylist());
        playlist3.setSmartPlaylist(autoCommitTxn, !isSmartPlaylist);

        String p1 = playlist1.getName();
        playlist1.setName(autoCommitTxn, playlist2.getName());
        playlist2.setName(autoCommitTxn, playlist3.getName());
        playlist3.setName(autoCommitTxn, p1);

        /*
         * if (playlist3.containsSong(addRemove)) {
         * playlist3.removeSong(autoCommitTxn, addRemove); } else {
         * playlist3.addSong(autoCommitTxn, addRemove); }
         */

        if (folder.containsPlaylist(playlist3)) {
            System.out.println("**** REMOVE ****");
            folder.removePlaylist(autoCommitTxn, playlist3);
        } else {
            System.out.println("**** ADD ****");
            folder.addPlaylist(autoCommitTxn, playlist3);
        }

        index_names = (index_names + 1) % NAMES.length;
        index_artists = (index_artists + 1) % ARTISTS.length;
        index_albums = (index_albums + 1) % ALBUMS.length;

        counter++;

        /*
         * if (counter == 10) { database.addPlaylist(autoCommitTxn, playlist4);
         * } else if (counter == 20) { database.removePlaylist(autoCommitTxn,
         * playlist4); cancel(); }
         */

        // if (counter == 2000)
        // cancel();

        /*
         * if (counter == 1) { database.removePlaylist(autoCommitTxn,
         * playlist1); } else if (counter == 2) {
         * database.removePlaylist(autoCommitTxn, playlist2); } else if (counter
         * == 3) { database.removePlaylist(autoCommitTxn, playlist3); }
         * 
         * counter++;
         * 
         * if (counter == 4) { cancel(); }
         */

        // autoCommitTxn.commit();
        // autoCommitTxn = old;
    }

    public static void main(String[] args) throws Exception {
        Main app = new Main();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(app, 10000, 500);
    }
}