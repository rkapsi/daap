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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import de.kapsi.net.daap.*;

public class PlaylistTest extends TestCase {

    public static TestSuite suite() {
        return new TestSuite(PlaylistTest.class);
    }
    
    private Library library;
    private Database database;
    private Playlist playlist;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        library = new Library("PlaylistTestLibrary");
        database = new Database("PlaylistTestDatabase");
        playlist = new Playlist("PlaylistTest");
        
        assertTrue(library.size()==0 && library.getRevision()==0);
        
        DaapTransaction trx = DaapTransaction.open(library);
        database.add(playlist);
        library.add(database);
        trx.commit();
        
        assertTrue(library.size()==1 && library.getRevision()==1);
        assertTrue(database.contains(playlist));
        assertTrue(library.contains(database));
    }
    
    public void testSetSmart() {
        
        int revision = library.getRevision();
        boolean isSmart = playlist.isSmartPlaylist();
        
        try {
            playlist.setSmartPlaylist(!isSmart);
            assertTrue(false);
        } catch (DaapTransactionException err) {
            assertTrue(library.getRevision() == revision);
            assertTrue(playlist.isSmartPlaylist() == isSmart);
        }
        
        DaapTransaction trx = DaapTransaction.open(library);
        playlist.setSmartPlaylist(!isSmart);
        trx.commit();
        
        assertTrue(library.getRevision() == (revision+1));
        assertTrue(playlist.isSmartPlaylist() != isSmart);
    }
    
    public void testAddSong() {
        
        int revision = library.getRevision();
        
        Song song = new Song("Song");
        
        try {
            playlist.add(song);
            assertTrue(false);
        } catch (DaapTransactionException err) {
            assertTrue(library.getRevision() == revision);
            assertFalse(playlist.contains(song));
        }
        
        DaapTransaction trx = DaapTransaction.open(library);
        playlist.add(song);
        trx.commit();
        
        assertTrue(library.getRevision() == (revision+1));
        assertTrue(playlist.contains(song));
    }
    
    public void testRemoveSong() {
        
        Song song = new Song("Song");
        
        DaapTransaction trx = DaapTransaction.open(library);
        playlist.add(song);
        trx.commit();
        
        int revision = library.getRevision();
        
        try {
            playlist.remove(song);
            assertTrue(false);
        } catch (DaapTransactionException err) {
            assertTrue(library.getRevision() == revision);
            assertTrue(playlist.contains(song));
        }
        
        trx = DaapTransaction.open(library);
        playlist.remove(song);
        trx.commit();
        
        assertTrue(library.getRevision() == (revision+1));
        assertFalse(playlist.contains(song));
    }
    
    public void testInteractionWithMasterPlaylist() {
        Playlist masterPlaylist = database.getMasterPlaylist();
        
        Song song1 = new Song("Song1");
        Song song2 = new Song("Song2");
        
        int revision = library.getRevision();
        
        // Test add (notify master playlist, default)
        DaapTransaction trx = DaapTransaction.open(library);
        playlist.add(song1);
        trx.commit();
        
        assertTrue(playlist.contains(song1));
        assertTrue(masterPlaylist.contains(song1));
        
        // Test add (do not notify master playlist)
        playlist.setNotifyMasterPlaylistOnAdd(false);
        trx = DaapTransaction.open(library);
        playlist.add(song2);
        trx.commit();
        
        assertTrue(playlist.contains(song2));
        assertFalse(masterPlaylist.contains(song2));
        
        // Test remove (do not notify master playlist, default)
        trx = DaapTransaction.open(library);
        playlist.remove(song1);
        trx.commit();
        
        assertFalse(playlist.contains(song1));
        assertTrue(masterPlaylist.contains(song1));
        
        // Test remove (notify master playlist)
        playlist.setNotifyMasterPlaylistOnRemove(true);
        trx = DaapTransaction.open(library);
        playlist.remove(song2);
        trx.commit();
        
        assertFalse(playlist.contains(song2));
        assertFalse(masterPlaylist.contains(song2));
        
        // Final state...
        assertTrue(library.getRevision() == (revision+4)); // 4*commit
        assertTrue(playlist.isEmpty()); // both songs were removed
        assertTrue(masterPlaylist.size()==1); // masterPlaylist contains song1
        assertTrue(masterPlaylist.contains(song1));
    }
}
