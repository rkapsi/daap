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

package org.ardverk.daap.tests;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ardverk.daap.Database;
import org.ardverk.daap.Library;
import org.ardverk.daap.Transaction;

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

Transaction txn = library.beginTransaction();
library.setName(txn, "OK");
txn.commit();
assertTrue(library.getRevision() == (revision+1));
assertTrue(library.getName().equals("OK"));
}

public void testAddDatabase() {
Database database = new Database("Database");

int revision = library.getRevision();

Transaction txn = library.beginTransaction();
library.addDatabase(txn, database);
txn.commit();

assertTrue(library.getRevision() == (revision+1));
assertTrue(library.getDatabaseCount() == 1);
assertTrue(library.containsDatabase(database));
}

public void testRemoveDatabase() {
Database database = new Database("Database");

Transaction txn = library.beginTransaction();
library.addDatabase(txn, database);
txn.commit();

int revision = library.getRevision();

txn = library.beginTransaction();
library.removeDatabase(txn, database);
txn.commit();

assertTrue(library.getRevision() == (revision+1));
assertTrue(library.getDatabaseCount() == 0);
assertFalse(library.containsDatabase(database));
}
}