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
import org.ardverk.daap.Playlist;
import org.ardverk.daap.Song;
import org.ardverk.daap.Transaction;
import org.ardverk.daap.Txn;
import org.ardverk.daap.chunks.impl.SongDataKind;
import org.ardverk.daap.chunks.impl.SongEqPreset;
import org.ardverk.daap.chunks.impl.SongFormat;
import org.ardverk.daap.chunks.impl.SongGenre;
import org.ardverk.daap.chunks.impl.SongUserRating;

public class SongTest extends TestCase {

    public static TestSuite suite() {
        return new TestSuite(SongTest.class);
    }

    private Library library;
    private Database database;
    private Playlist playlist;

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testAttributes() {
        performTest(null);
    }

    public void testTxnAttributes() {
        performTest(new DummyTransaction());
    }

    private void performTest(Transaction txn) {
        Song song = new Song("Song");
        assertEquals(song.getName(), "Song");

        song.setAlbum(txn, "Album");
        System.out.println("TEST: " + song.getAlbum() + ": " + txn);
        assertEquals(song.getAlbum(), "Album");

        song.setArtist(txn, "Artist");
        assertEquals(song.getArtist(), "Artist");

        song.setBeatsPerMinute(txn, 123);
        assertEquals(song.getBeatsPerMinute(), 123);

        song.setBitrate(txn, 192);
        assertEquals(song.getBitrate(), 192);

        song.setComment(txn, "Comment");
        assertEquals(song.getComment(), "Comment");

        song.setCompilation(txn, true);
        assertEquals(song.isCompilation(), true);
        song.setCompilation(txn, false);
        assertEquals(song.isCompilation(), false);

        song.setComposer(txn, "Composer");
        assertEquals(song.getComposer(), "Composer");

        song.setDataKind(txn, SongDataKind.DAAP_STREAM);
        assertEquals(song.getDataKind(), SongDataKind.DAAP_STREAM);

        song.setDataUrl(txn, "http://www.somewhere.ee");
        assertEquals(song.getDataUrl(), "http://www.somewhere.ee");

        song.setDateAdded(txn, 9999999);
        assertEquals(song.getDateAdded(), 9999999);

        song.setDateModified(txn, 6666666);
        assertEquals(song.getDateModified(), 6666666);

        song.setDescription(txn, "Description");
        assertEquals(song.getDescription(), "Description");

        song.setDisabled(txn, true);
        assertEquals(song.isDisabled(), true);
        song.setDisabled(txn, false);
        assertEquals(song.isDisabled(), false);

        song.setDiscCount(txn, 100);
        assertEquals(song.getDiscCount(), 100);

        song.setDiscNumber(txn, 99);
        assertEquals(song.getDiscNumber(), 99);

        song.setEqPreset(txn, SongEqPreset.LATIN);
        assertEquals(song.getEqPreset(), SongEqPreset.LATIN);

        song.setFormat(txn, SongFormat.M4A);
        assertEquals(song.getFormat(), SongFormat.M4A);

        song.setGenre(txn, SongGenre.NEW_AGE);
        assertEquals(song.getGenre(), SongGenre.NEW_AGE);

        song.setGrouping(txn, "Grouping");
        assertEquals(song.getGrouping(), "Grouping");

        song.setRelativeVolume(txn, 50);
        assertEquals(song.getRelativeVolume(), 50);

        song.setSampleRate(txn, 44100);
        assertEquals(song.getSampleRate(), 44100);

        song.setSize(txn, 6 * 1024 * 1024);
        assertEquals(song.getSize(), 6 * 1024 * 1024);

        song.setStartTime(txn, 3333333);
        assertEquals(song.getStartTime(), 3333333);

        song.setStopTime(txn, 4444444);
        assertEquals(song.getStopTime(), 4444444);

        song.setTime(txn, 5555555);
        assertEquals(song.getTime(), 5555555);

        song.setTrackCount(txn, 88);
        assertEquals(song.getTrackCount(), 88);

        song.setTrackNumber(txn, 87);
        assertEquals(song.getTrackNumber(), 87);

        song.setUserRating(txn, SongUserRating.FOUR);
        assertEquals(song.getUserRating(), SongUserRating.FOUR);

        song.setYear(txn, 2004);
        assertEquals(song.getYear(), 2004);

        song.setITMSArtistId(txn, 123);
        assertEquals(song.getITMSArtistId(), 123);

        song.setITMSComposerId(txn, 333);
        assertEquals(song.getITMSComposerId(), 333);

        song.setITMSGenreId(txn, 444);
        assertEquals(song.getITMSGenreId(), 444);

        song.setITMSPlaylistId(txn, 888);
        assertEquals(song.getITMSPlaylistId(), 888);

        song.setITMSSongId(txn, 666);
        assertEquals(song.getITMSSongId(), 666);

        song.setITMSStorefrontId(txn, 111);
        assertEquals(song.getITMSStrorefrontId(), 111);

        song.setCodecType(txn, 741);
        assertEquals(song.getCodecType(), 741);

        song.setCodecSubtype(txn, 369);
        assertEquals(song.getCodecSubtype(), 369);

        song.setNormVolume(txn, 58442);
        assertEquals(song.getNormVolume(), 58442);

        song.setPodcast(txn, true);
        assertEquals(song.isPodcast(), true);

        song.setDescription(txn, "Description");
        assertEquals(song.getDescription(), "Description");

        song.setContentRating(txn, 210);
        assertEquals(song.getContentRating(), 210);

        song.setContentDescription(txn, "ContentDescription");
        assertEquals(song.getContentDescription(), "ContentDescription");

        song.setKeywords(txn, "Keywords");
        assertEquals(song.getKeywords(), "Keywords");

        song.setLongDescription(txn, "LongDescription");
        assertEquals(song.getLongDescription(), "LongDescription");

        song.setHasVideo(txn, true);
        assertEquals(song.hasVideo(), true);
        song.setHasVideo(txn, false);
        assertEquals(song.hasVideo(), false);
    }

    private class DummyTransaction extends Transaction {

        public DummyTransaction() {
            super(null);
        }

        protected synchronized void addTxn(Object obj, Txn txn) {
            txn.commit(this);
        }

        protected void close() {
        }

        public synchronized void commit() {
        }

        public synchronized boolean isOpen() {
            return true;
        }

        public synchronized void rollback() {
        }
    }
}