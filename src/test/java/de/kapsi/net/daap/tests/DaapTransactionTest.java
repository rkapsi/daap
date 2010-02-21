/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004 Roger Kapsi, info at kapsi dot de
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

package de.kapsi.net.daap.tests;

import junit.framework.*;
import de.kapsi.net.daap.*;

public class DaapTransactionTest extends TestCase {
    
    public static TestSuite suite() {
        return new TestSuite(DaapTransactionTest.class);
    }
    
    private Library library;
    
    public DaapTransactionTest(String arg0) {
        super(arg0);
    }
    
    protected void setUp() throws Exception {
        library = new Library("DaapTransactionTest");
    }
    
    public void testCommit() {
        assertTrue(library.getRevision()==1);
        Transaction txn = library.beginTransaction();
        Database database = new Database("Database");
        library.addDatabase(txn, database);
        txn.commit();
        assertTrue(library.getRevision()==2);
        assertTrue(library.getDatabaseCount()==1);
    }
    
    public void testRollback() {
        assertTrue(library.getRevision()==1);
        Transaction txn = library.beginTransaction();
        Database database = new Database("Database");
        library.addDatabase(txn, database);
        txn.rollback();
        assertTrue(library.getRevision()==1);
        assertTrue(library.getDatabaseCount()==0);
    }
    
    public void testExtendedCommit() {
        Database database = new Database("Database");
        Playlist playlist = new Playlist("Playlist");
        
        assertTrue(library.getRevision()==1);
        assertTrue(library.getDatabaseCount()==0);
        assertTrue(database.getPlaylistCount()==1); /* masterPlaylist!!! */ 
        
        Transaction txn = library.beginTransaction();
        database.addPlaylist(txn, playlist);
        library.addDatabase(txn, database);
        txn.commit();
        
        assertTrue(library.getRevision()==2); /* incremeted with each commit */
        assertTrue(library.getDatabaseCount()==1);
        assertTrue(database.getPlaylistCount()==2);
        
        Transaction txn2 = library.beginTransaction();
        database.setName(txn2, "NewDatabaseName");
        playlist.setName(txn2, "NewPlaylistName");
        library.setName(txn2, "NewLibraryName");
        txn2.commit();
        
        assertTrue(library.getRevision()==3);
        assertTrue(library.getDatabaseCount()==1);
        assertTrue(database.getPlaylistCount()==2);
        
        assertEquals(library.getName(), "NewLibraryName");
        assertEquals(database.getName(), "NewDatabaseName");
        assertEquals(playlist.getName(), "NewPlaylistName");
        
        Song song1 = new Song("Song1");
        Transaction txn3 = library.beginTransaction();
        playlist.addSong(txn3, song1);
        txn3.commit();
        
        assertTrue(library.getRevision()==4);
        assertTrue(library.getDatabaseCount()==1);
        assertTrue(database.getPlaylistCount()==2);
        assertTrue(playlist.getSongCount()==1 && playlist.containsSong(song1));
        assertTrue(database.getSongCount()==1 && database.containsSong(song1));
        
        Playlist masterPlaylist = database.getMasterPlaylist();
        assertFalse(masterPlaylist.getSongCount()==1 && masterPlaylist.containsSong(song1));
    }
    
    public void testAutoCommit() {
        
        int revision = library.getRevision();
        
        Database database = new Database("Database");
        Playlist playlist = new Playlist("Playlist");
        Song song = new Song("Song");
        
        library.addDatabase(null, database);
        database.addPlaylist(null, playlist);
        playlist.addSong(null, song);
        library.commit(null);
        
        assertTrue(library.containsDatabase(database));
        assertTrue(database.containsPlaylist(playlist));
        assertTrue(database.containsSong(song));
        assertTrue(playlist.containsSong(song));
        assertTrue(library.getRevision() == (revision+1));
        
        AutoCommitTransaction autoCommitTxn = new AutoCommitTransaction(library, 2000, 10);
        
        // Test timed auto commit
        
        int databaseSongCount = database.getSongCount();
        int playlistSongCount = playlist.getSongCount();
        
        for(int i = 0; i < 5; i++) {
            song = new Song("Test Song: " + i);
            playlist.addSong(autoCommitTxn, song);
        }
        
        try { Thread.sleep(3000); } catch (Exception err) { assertTrue(false); }
        
        assertTrue(database.getSongCount() == (databaseSongCount + 5));
        assertTrue(playlist.getSongCount() == (playlistSongCount + 5));
        assertTrue(database.containsSong(song));
        assertTrue(playlist.containsSong(song));
        
        // Test enforced commit
        
        databaseSongCount = database.getSongCount();
        playlistSongCount = playlist.getSongCount();
        
        // +3 for timed commit
        Song[] songs = new Song[autoCommitTxn.getEnforceCommit() + 3]; 
        
        for(int i = 0; i < songs.length; i++) {
            songs[i] = new Song("Test Song: " + i);
            playlist.addSong(autoCommitTxn, songs[i]);
        }
        
        assertTrue(database.getSongCount() == (databaseSongCount + autoCommitTxn.getEnforceCommit()));
        assertTrue(playlist.getSongCount() == (playlistSongCount + autoCommitTxn.getEnforceCommit()));
        assertTrue(database.containsSong(songs[9]));
        assertTrue(playlist.containsSong(songs[9]));
        
        databaseSongCount = database.getSongCount();
        playlistSongCount = playlist.getSongCount();
        
        try { Thread.sleep(3000); } catch (Exception err) { assertTrue(false); }
        
        assertTrue(database.getSongCount() == (databaseSongCount + 3));
        assertTrue(playlist.getSongCount() == (playlistSongCount + 3));
        assertTrue(database.containsSong(songs[songs.length-1]));
        assertTrue(playlist.containsSong(songs[songs.length-1]));
    }
}
