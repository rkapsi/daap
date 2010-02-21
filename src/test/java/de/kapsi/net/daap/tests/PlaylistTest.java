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
        
        assertTrue(library.getDatabaseCount()==0 && library.getRevision()==1);
        
        Transaction txn = library.beginTransaction();
        database.addPlaylist(txn, playlist);
        library.addDatabase(txn, database);
        txn.commit();
        
        assertTrue(library.getDatabaseCount()==1 && library.getRevision()==2);
        assertTrue(database.containsPlaylist(playlist));
        assertTrue(library.containsDatabase(database));
    }
    
    public void testSetName() {
        
        int revision = library.getRevision();
        
        Transaction txn = library.beginTransaction();
        playlist.setName(txn, "Hello World!");
        txn.commit();
        
        assertTrue(library.getRevision() == (revision+1));
        assertEquals(playlist.getName(), "Hello World!");
    }

    public void testSetSmart() {
        
        int revision = library.getRevision();
        boolean isSmart = playlist.isSmartPlaylist();
        
        Transaction txn = library.beginTransaction();
        playlist.setSmartPlaylist(txn, !isSmart);
        txn.commit();
        
        assertTrue(library.getRevision() == (revision+1));
        assertTrue(playlist.isSmartPlaylist() != isSmart);
    }
    
    public void testSetPodcast() {
        
        int revision = library.getRevision();
        boolean podcast = playlist.isPodcastPlaylist();
        
        Transaction txn = library.beginTransaction();
        playlist.setPodcastPlaylist(txn, !podcast);
        txn.commit();
        
        assertTrue(library.getRevision() == (revision+1));
        assertTrue(playlist.isPodcastPlaylist() != podcast);
    }

    public void testAddSong() {
        
        int revision = library.getRevision();
        
        Song song = new Song("Song");
        
        Transaction txn = library.beginTransaction();
        playlist.addSong(txn, song);
        txn.commit();
        
        assertTrue(library.getRevision() == (revision+1));
        assertTrue(playlist.containsSong(song));
    }
    
    public void testRemoveSong() {
        
        Song song = new Song("Song");
        
        Transaction txn = library.beginTransaction();
        playlist.addSong(txn, song);
        txn.commit();
        
        int revision = library.getRevision();
        
        txn = library.beginTransaction();
        playlist.removeSong(txn, song);
        txn.commit();
        
        assertTrue(library.getRevision() == (revision+1));
        assertFalse(playlist.containsSong(song));
        assertFalse(database.containsSong(song));
    }
}
