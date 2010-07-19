/*
 * Digital Audio Access Protocol (DAAP) Library
 * Copyright (C) 2004-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        assertTrue(library.getRevision() == (revision + 1));
        assertTrue(library.getName().equals("OK"));
    }

    public void testAddDatabase() {
        Database database = new Database("Database");

        int revision = library.getRevision();

        Transaction txn = library.beginTransaction();
        library.addDatabase(txn, database);
        txn.commit();

        assertTrue(library.getRevision() == (revision + 1));
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

        assertTrue(library.getRevision() == (revision + 1));
        assertTrue(library.getDatabaseCount() == 0);
        assertFalse(library.containsDatabase(database));
    }
}
