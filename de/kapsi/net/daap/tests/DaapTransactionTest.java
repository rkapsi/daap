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
        assertTrue(library.getRevision()==0);
        DaapTransaction transaction = DaapTransaction.open(library);
        Database database = new Database("Database");
        library.add(database);
        transaction.commit();
        assertTrue(library.getRevision()==1);
        assertTrue(library.size()==1);
    }
    
    public void testRollback() {
        assertTrue(library.getRevision()==0);
        DaapTransaction transaction = DaapTransaction.open(library);
        Database database = new Database("Database");
        library.add(database);
        transaction.rollback();
        assertTrue(library.getRevision()==0);
        assertTrue(library.size()==0);
    }
    
    public void testExtendedCommit() {
        Database database = new Database("Database");
        Playlist playlist = new Playlist("Playlist");
        
        assertTrue(library.getRevision()==0);
        assertTrue(library.size()==0);
        assertTrue(database.size()==1); /* masterPlaylist!!! */ 
        
        DaapTransaction transaction = DaapTransaction.open(library);
        database.add(playlist);
        library.add(database);
        transaction.commit();
        
        assertTrue(library.getRevision()==1); /* incremeted with each commit */
        assertTrue(library.size()==1);
        assertTrue(database.size()==2);
        
        DaapTransaction transaction2 = DaapTransaction.open(library);
        database.setName("NewDatabaseName");
        playlist.setName("NewPlaylistName");
        library.setName("NewLibraryName");
        transaction2.commit();
        
        assertTrue(library.getRevision()==2);
        assertTrue(library.size()==1);
        assertTrue(database.size()==2);
        
        assertEquals(library.getName(), "NewLibraryName");
        assertEquals(database.getName(), "NewDatabaseName");
        assertEquals(playlist.getName(), "NewPlaylistName");
        
        Playlist masterPlaylist = database.getMasterPlaylist();
        assertEquals(masterPlaylist.getName(), "NewDatabaseName");
        
        Song song1 = new Song("Song1");
        DaapTransaction transaction3 = DaapTransaction.open(library);
        playlist.add(song1);
        transaction3.commit();
        
        assertTrue(library.getRevision()==3);
        assertTrue(library.size()==1);
        assertTrue(database.size()==2);
        assertTrue(playlist.size()==1 && playlist.contains(song1));
        assertTrue(masterPlaylist.size()==1 && masterPlaylist.contains(song1));
        
        Song song2 = new Song("Song2");
        playlist.setNotifyMasterPlaylistOnAdd(false);
        DaapTransaction transaction4 = DaapTransaction.open(library);
        playlist.add(song2);
        transaction4.commit();
        
        assertTrue(library.getRevision()==4);
        assertTrue(library.size()==1);
        assertTrue(database.size()==2);
        assertTrue(playlist.size()==2 && playlist.contains(song2));
        assertTrue(masterPlaylist.size()==1 && !masterPlaylist.contains(song2));
    }
    
    /**
     * Nested operations are not supported.
     *
     */
    public void testNestedOperation() {
        DaapTransaction transaction = DaapTransaction.open(library);
        
        try {
            DaapTransaction nested = DaapTransaction.open(library);
            assertFalse(nested != null);
        } catch (DaapTransactionException err) {
            assertTrue(true);
        } finally {
            transaction.rollback();
        }
    }
}
