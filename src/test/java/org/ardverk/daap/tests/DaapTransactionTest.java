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

import org.ardverk.daap.AutoCommitTransaction;
import org.ardverk.daap.Database;
import org.ardverk.daap.Library;
import org.ardverk.daap.Playlist;
import org.ardverk.daap.Song;
import org.ardverk.daap.Transaction;

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
        assertTrue(library.getRevision() == 1);
        Transaction txn = library.beginTransaction();
        Database database = new Database("Database");
        library.addDatabase(txn, database);
        txn.commit();
        assertTrue(library.getRevision() == 2);
        assertTrue(library.getDatabaseCount() == 1);
    }

    public void testRollback() {
        assertTrue(library.getRevision() == 1);
        Transaction txn = library.beginTransaction();
        Database database = new Database("Database");
        library.addDatabase(txn, database);
        txn.rollback();
        assertTrue(library.getRevision() == 1);
        assertTrue(library.getDatabaseCount() == 0);
    }

    public void testExtendedCommit() {
        Database database = new Database("Database");
        Playlist playlist = new Playlist("Playlist");

        assertTrue(library.getRevision() == 1);
        assertTrue(library.getDatabaseCount() == 0);
        assertTrue(database.getPlaylistCount() == 1); /* masterPlaylist!!! */

        Transaction txn = library.beginTransaction();
        database.addPlaylist(txn, playlist);
        library.addDatabase(txn, database);
        txn.commit();

        assertTrue(library.getRevision() == 2); /* incremeted with each commit */
        assertTrue(library.getDatabaseCount() == 1);
        assertTrue(database.getPlaylistCount() == 2);

        Transaction txn2 = library.beginTransaction();
        database.setName(txn2, "NewDatabaseName");
        playlist.setName(txn2, "NewPlaylistName");
        library.setName(txn2, "NewLibraryName");
        txn2.commit();

        assertTrue(library.getRevision() == 3);
        assertTrue(library.getDatabaseCount() == 1);
        assertTrue(database.getPlaylistCount() == 2);

        assertEquals(library.getName(), "NewLibraryName");
        assertEquals(database.getName(), "NewDatabaseName");
        assertEquals(playlist.getName(), "NewPlaylistName");

        Song song1 = new Song("Song1");
        Transaction txn3 = library.beginTransaction();
        playlist.addSong(txn3, song1);
        txn3.commit();

        assertTrue(library.getRevision() == 4);
        assertTrue(library.getDatabaseCount() == 1);
        assertTrue(database.getPlaylistCount() == 2);
        assertTrue(playlist.getSongCount() == 1 && playlist.containsSong(song1));
        assertTrue(database.getSongCount() == 1 && database.containsSong(song1));

        Playlist masterPlaylist = database.getMasterPlaylist();
        assertFalse(masterPlaylist.getSongCount() == 1
                && masterPlaylist.containsSong(song1));
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
        assertTrue(library.getRevision() == (revision + 1));

        AutoCommitTransaction autoCommitTxn = new AutoCommitTransaction(
                library, 2000, 10);

        // Test timed auto commit

        int databaseSongCount = database.getSongCount();
        int playlistSongCount = playlist.getSongCount();

        for (int i = 0; i < 5; i++) {
            song = new Song("Test Song: " + i);
            playlist.addSong(autoCommitTxn, song);
        }

        try {
            Thread.sleep(3000);
        } catch (Exception err) {
            assertTrue(false);
        }

        assertTrue(database.getSongCount() == (databaseSongCount + 5));
        assertTrue(playlist.getSongCount() == (playlistSongCount + 5));
        assertTrue(database.containsSong(song));
        assertTrue(playlist.containsSong(song));

        // Test enforced commit

        databaseSongCount = database.getSongCount();
        playlistSongCount = playlist.getSongCount();

        // +3 for timed commit
        Song[] songs = new Song[autoCommitTxn.getEnforceCommit() + 3];

        for (int i = 0; i < songs.length; i++) {
            songs[i] = new Song("Test Song: " + i);
            playlist.addSong(autoCommitTxn, songs[i]);
        }

        assertTrue(database.getSongCount() == (databaseSongCount + autoCommitTxn
                .getEnforceCommit()));
        assertTrue(playlist.getSongCount() == (playlistSongCount + autoCommitTxn
                .getEnforceCommit()));
        assertTrue(database.containsSong(songs[9]));
        assertTrue(playlist.containsSong(songs[9]));

        databaseSongCount = database.getSongCount();
        playlistSongCount = playlist.getSongCount();

        try {
            Thread.sleep(3000);
        } catch (Exception err) {
            assertTrue(false);
        }

        assertTrue(database.getSongCount() == (databaseSongCount + 3));
        assertTrue(playlist.getSongCount() == (playlistSongCount + 3));
        assertTrue(database.containsSong(songs[songs.length - 1]));
        assertTrue(playlist.containsSong(songs[songs.length - 1]));
    }
}
