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

public class DatabaseTest extends TestCase {

    public static TestSuite suite() {
        return new TestSuite(DatabaseTest.class);
    }
    
    private Library library;
    private Database database;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        library = new Library("DatabaseTestLibrary");
        database = new Database("DatabaseTest");
        
        assertTrue(library.size()==0 && library.getRevision()==0);
        DaapTransaction trx = DaapTransaction.open(library);
        library.add(database);
        trx.commit();
        assertTrue(library.size()==1 && library.getRevision()==1);
    }
    
    public void testAddPlaylist() {
        Playlist playlist = new Playlist("Playlist");
        
        int size = database.size();
        int revision = library.getRevision();
        
        try {
            database.add(playlist);
            assertTrue(false);
        } catch (DaapTransactionException err) {
            assertTrue(library.getRevision()==revision);
            assertTrue(database.size()==size); 
        }
        
        DaapTransaction trx = DaapTransaction.open(library);
        database.add(playlist);
        trx.commit();
        
        assertTrue(library.getRevision() == (revision+1));
        assertTrue(database.size() == (size+1));
        assertTrue(database.contains(playlist));
    }
    
    public void testRemovePlaylist() {
        Playlist playlist = new Playlist("Playlist");
        
        DaapTransaction trx = DaapTransaction.open(library);
        database.add(playlist);
        trx.commit();
        
        int size = database.size();
        int revision = library.getRevision();

        try {
            database.remove(playlist);
            assertTrue(false);
        } catch (DaapTransactionException err) {
            assertTrue(library.getRevision()==revision);
            assertTrue(database.size()==size);
            assertTrue(database.contains(playlist));
        }
        
        trx = DaapTransaction.open(library);
        database.remove(playlist);
        trx.commit();
        
        assertTrue(library.getRevision() == (revision+1));
        assertTrue(database.size() == (size-1));
        assertFalse(database.contains(playlist));
    }
    
    public void testSetName() {
        
        int revision = library.getRevision();
        String name = database.getName();
        
        try {
            database.setName("ERROR");
            assertTrue(false);
        } catch (DaapTransactionException err) {
            assertTrue(library.getRevision()==revision);
            assertTrue(database.getName().equals(name));
            assertTrue(database.getMasterPlaylist().getName().equals(name));
        }
        
        DaapTransaction trx = DaapTransaction.open(library);
        database.setName("OK");
        trx.commit();
        
        assertTrue(library.getRevision() == (revision+1));
        assertTrue(database.getName().equals("OK"));
        assertTrue(database.getMasterPlaylist().getName().equals("OK"));
    }
    
    public void testAddSong() {
        
        int revision = library.getRevision();
        
        Song song = new Song("Song");
        Playlist playlist = new Playlist("Playlist");
        
        try {
            database.add(song);
            assertTrue(false);
        } catch (DaapTransactionException err) {
            assertTrue(library.getRevision() == revision);
            assertTrue(database.getMasterPlaylist().size()==0);
            assertFalse(database.getMasterPlaylist().contains(song));
        }
        
        DaapTransaction trx = DaapTransaction.open(library);
        database.add(song);
        database.add(playlist);
        trx.commit();
        
        assertTrue(library.getRevision()==(revision+1));
        assertTrue(database.getMasterPlaylist().size()==1);
        assertTrue(database.getMasterPlaylist().contains(song));
        
        assertTrue(playlist.size()==1);
        assertTrue(playlist.contains(song));
    }
    
    public void testRemoveSong() {
        
        Song song = new Song("Song");
        Playlist playlist = new Playlist("Playlist");
        
        DaapTransaction trx = DaapTransaction.open(library);
        database.add(song);
        database.add(playlist);
        trx.commit();
        
        int revision = library.getRevision();
        
        try {
            database.remove(song);
            assertTrue(false);
        } catch (DaapTransactionException err) {
            assertTrue(library.getRevision() == revision);
            assertTrue(database.getMasterPlaylist().size()==1);
            assertTrue(database.getMasterPlaylist().contains(song));
        }
        
        trx = DaapTransaction.open(library);
        database.remove(song);
        trx.commit();
        
        assertTrue(library.getRevision()==(revision+1));
        assertTrue(database.getMasterPlaylist().size()==0);
        assertFalse(database.getMasterPlaylist().contains(song));
        
        assertTrue(playlist.size()==0);
        assertFalse(playlist.contains(song));
    }
    
    public void testUpdateSong() {
        
        Song song = new Song("Song");
        
        DaapTransaction trx = DaapTransaction.open(library);
        database.add(song);
        trx.commit();
        
        song.setName("Hello World");
        int revision = library.getRevision();
        
        trx = DaapTransaction.open(library);
        database.update(song);
        trx.commit();
        
        assertTrue(library.getRevision()==(revision+1));
        assertTrue(database.getMasterPlaylist().size()==1);
        assertTrue(database.getMasterPlaylist().contains(song));
    }
}
