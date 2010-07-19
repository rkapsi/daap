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

        assertTrue(library.getDatabaseCount() == 0
                && library.getRevision() == 1);

        Transaction txn = library.beginTransaction();
        database.addPlaylist(txn, playlist);
        library.addDatabase(txn, database);
        txn.commit();

        assertTrue(library.getDatabaseCount() == 1
                && library.getRevision() == 2);
        assertTrue(database.containsPlaylist(playlist));
        assertTrue(library.containsDatabase(database));
    }

    public void testSetName() {

        int revision = library.getRevision();

        Transaction txn = library.beginTransaction();
        playlist.setName(txn, "Hello World!");
        txn.commit();

        assertTrue(library.getRevision() == (revision + 1));
        assertEquals(playlist.getName(), "Hello World!");
    }

    public void testSetSmart() {

        int revision = library.getRevision();
        boolean isSmart = playlist.isSmartPlaylist();

        Transaction txn = library.beginTransaction();
        playlist.setSmartPlaylist(txn, !isSmart);
        txn.commit();

        assertTrue(library.getRevision() == (revision + 1));
        assertTrue(playlist.isSmartPlaylist() != isSmart);
    }

    public void testSetPodcast() {

        int revision = library.getRevision();
        boolean podcast = playlist.isPodcastPlaylist();

        Transaction txn = library.beginTransaction();
        playlist.setPodcastPlaylist(txn, !podcast);
        txn.commit();

        assertTrue(library.getRevision() == (revision + 1));
        assertTrue(playlist.isPodcastPlaylist() != podcast);
    }

    public void testAddSong() {

        int revision = library.getRevision();

        Song song = new Song("Song");

        Transaction txn = library.beginTransaction();
        playlist.addSong(txn, song);
        txn.commit();

        assertTrue(library.getRevision() == (revision + 1));
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

        assertTrue(library.getRevision() == (revision + 1));
        assertFalse(playlist.containsSong(song));
        assertFalse(database.containsSong(song));
    }
}
