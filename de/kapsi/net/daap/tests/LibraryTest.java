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

public class LibraryTest extends TestCase {
    
    public static TestSuite suite() {
        return new TestSuite(LibraryTest.class);
    }
    
    private Library library;
    
    public LibraryTest(String name) {
        super(name);
    }
    
    public void setUp() {
        library = new Library("TestLibrary");
    }
   
    public void testChangeLibraryName() {
        
        int revision = library.getRevision();
        String name = library.getName();
        
        try {
            library.setName("Error");
            assertTrue(false);
        } catch (DaapTransactionException err) {
            assertTrue(library.getRevision() == revision);
            assertTrue(library.getName().equals(name));
        }
        
        DaapTransaction transaction = DaapTransaction.open(library);
        library.setName("OK");
        transaction.commit();
        assertTrue(library.getRevision() == (revision+1));
        assertTrue(library.getName().equals("OK"));
    }
    
    public void testAddDatabase() {
        Database database = new Database("Database");
        
        int revision = library.getRevision();
        
        try {
            library.add(database);
            assertTrue(false);
        } catch (DaapTransactionException err) {
            assertTrue(library.getRevision()==revision);
            assertTrue(library.size() == 0);
            assertFalse(library.contains(database));
        }
        
        DaapTransaction transaction = DaapTransaction.open(library);
        library.add(database);
        transaction.commit();
        
        assertTrue(library.getRevision() == (revision+1));
        assertTrue(library.size() == 1);
        assertTrue(library.contains(database));
    }
    
    public void testRemoveDatabase() {
        Database database = new Database("Database");
        
        DaapTransaction transaction = DaapTransaction.open(library);
        library.add(database);
        transaction.commit();
        
        int revision = library.getRevision();
        
        try {
            library.remove(database);
            assertTrue(false);
        } catch (DaapTransactionException err) {
            assertTrue(library.getRevision() == revision);
            assertTrue(library.size() == 1);
            assertTrue(library.contains(database));
        }
        
        transaction = DaapTransaction.open(library);
        library.remove(database);
        transaction.commit();
        
        assertTrue(library.getRevision() == (revision+1));
        assertTrue(library.size() == 0);
        assertFalse(library.contains(database));
    }
}
