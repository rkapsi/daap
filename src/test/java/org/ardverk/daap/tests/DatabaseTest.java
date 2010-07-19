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
import org.ardverk.daap.Playlist;
import org.ardverk.daap.Song;
import org.ardverk.daap.Transaction;

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

        assertTrue(library.getDatabaseCount() == 0
                && library.getRevision() == 1);
        Transaction txn = library.beginTransaction();
        library.addDatabase(txn, database);
        txn.commit();
        assertTrue(library.getDatabaseCount() == 1
                && library.getRevision() == 2);
    }

    public void testAddPlaylist() {
        Playlist playlist = new Playlist("Playlist");

        int size = database.getPlaylistCount();
        int revision = library.getRevision();

        Transaction txn = library.beginTransaction();
        database.addPlaylist(txn, playlist);
        txn.commit();

        assertTrue(library.getRevision() == (revision + 1));
        assertTrue(database.getPlaylistCount() == (size + 1));
        assertTrue(database.containsPlaylist(playlist));
    }

    public void testRemovePlaylist() {
        Playlist playlist = new Playlist("Playlist");

        Transaction txn = library.beginTransaction();
        database.addPlaylist(txn, playlist);
        txn.commit();

        int size = database.getPlaylistCount();
        int revision = library.getRevision();

        txn = library.beginTransaction();
        database.removePlaylist(txn, playlist);
        txn.commit();

        assertTrue(library.getRevision() == (revision + 1));
        assertTrue(database.getPlaylistCount() == (size - 1));
        assertFalse(database.containsPlaylist(playlist));
    }

    public void testSetName() {

        int revision = library.getRevision();
        String name = database.getName();

        Transaction txn = library.beginTransaction();
        database.setName(txn, "OK");
        txn.commit();

        assertTrue(library.getRevision() == (revision + 1));
        assertTrue(database.getName().equals("OK"));
    }

    public void testAddSong() {

        int revision = library.getRevision();

        Song song = new Song("Song");
        Playlist playlist = new Playlist("Playlist");

        Transaction txn = library.beginTransaction();
        database.addSong(txn, song);
        database.addPlaylist(txn, playlist);
        txn.commit();

        assertTrue(library.getRevision() == (revision + 1));
        assertTrue(database.getMasterPlaylist().getSongCount() == 1);
        assertTrue(database.getMasterPlaylist().containsSong(song));

        assertFalse(playlist.getSongCount() == 1);
        assertFalse(playlist.containsSong(song));
    }

    public void testRemoveSong() {

        Song song = new Song("Song");
        Playlist playlist = new Playlist("Playlist");

        Transaction txn = library.beginTransaction();
        database.addSong(txn, song);
        database.addPlaylist(txn, playlist);
        txn.commit();

        int revision = library.getRevision();

        txn = library.beginTransaction();
        database.removeSong(txn, song);
        txn.commit();

        assertTrue(library.getRevision() == (revision + 1));
        assertTrue(database.getMasterPlaylist().getSongCount() == 0);
        assertFalse(database.getMasterPlaylist().containsSong(song));

        assertTrue(playlist.getSongCount() == 0);
        assertFalse(playlist.containsSong(song));
    }

    public void testUpdateSong() {

        Song song = new Song("Song");

        Transaction txn = library.beginTransaction();
        database.addSong(txn, song);
        txn.commit();

        int revision = library.getRevision();

        txn = library.beginTransaction();
        song.setName(txn, "Hello World");
        txn.commit();

        assertTrue(library.getRevision() == (revision + 1));
        assertTrue(database.getMasterPlaylist().getSongCount() == 1);
        assertTrue(database.getMasterPlaylist().containsSong(song));
    }
}
