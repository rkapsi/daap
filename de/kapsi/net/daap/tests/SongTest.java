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
import de.kapsi.net.daap.chunks.impl.*;

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
        Song song = new Song("Song");
        assertEquals(song.getName(), "Song");
        
        song.setAlbum("Album");
        assertEquals(song.getAlbum(), "Album");
        
        song.setArtist("Artist");
        assertEquals(song.getArtist(), "Artist");
        
        song.setBeatsPerMinute(123);
        assertEquals(song.getBeatsPerMinute(), 123);
        
        song.setBitrate(192);
        assertEquals(song.getBitrate(), 192);
        
        song.setComment("Comment");
        assertEquals(song.getComment(), "Comment");
        
        song.setCompilation(true);
        assertEquals(song.isCompilation(), true);
        song.setCompilation(false);
        assertEquals(song.isCompilation(), false);
        
        song.setComposer("Composer");
        assertEquals(song.getComposer(), "Composer");
        
        song.setDataKind(SongDataKind.DAAP_STREAM);
        assertEquals(song.getDataKind(), SongDataKind.DAAP_STREAM);
        
        song.setDataUrl("http://www.somewhere.ee");
        assertEquals(song.getDataUrl(), "http://www.somewhere.ee");
        
        song.setDateAdded(9999999);
        assertEquals(song.getDateAdded(), 9999999);
        
        song.setDateModified(6666666);
        assertEquals(song.getDateModified(), 6666666);
        
        song.setDescription("Description");
        assertEquals(song.getDescription(), "Description");
        
        song.setDisabled(true);
        assertEquals(song.isDisabled(), true);
        song.setDisabled(false);
        assertEquals(song.isDisabled(), false);
        
        song.setDiscCount(100);
        assertEquals(song.getDiscCount(), 100);
        
        song.setDiscNumber(99);
        assertEquals(song.getDiscNumber(), 99);
        
        song.setEqPreset(SongEqPreset.LATIN);
        assertEquals(song.getEqPreset(), SongEqPreset.LATIN);
        
        song.setFormat(SongFormat.M4A);
        assertEquals(song.getFormat(), SongFormat.M4A);
        
        song.setGenre(SongGenre.NEW_AGE);
        assertEquals(song.getGenre(), SongGenre.NEW_AGE);
        
        song.setGrouping("Grouping");
        assertEquals(song.getGrouping(), "Grouping");
        
        song.setRelativeVolume(50);
        assertEquals(song.getRelativeVolume(), 50);
        
        song.setSampleRate(44100);
        assertEquals(song.getSampleRate(), 44100);
        
        song.setSize(6*1024*1024);
        assertEquals(song.getSize(), 6*1024*1024);
        
        song.setStartTime(3333333);
        assertEquals(song.getStartTime(), 3333333);
        
        song.setStopTime(4444444);
        assertEquals(song.getStopTime(), 4444444);
        
        song.setTime(5555555);
        assertEquals(song.getTime(), 5555555);
        
        song.setTrackCount(88);
        assertEquals(song.getTrackCount(), 88);
        
        song.setTrackNumber(87);
        assertEquals(song.getTrackNumber(), 87);
        
        song.setUserRating(SongUserRating.FOUR);
        assertEquals(song.getUserRating(), SongUserRating.FOUR);
        
        song.setYear(2004);
        assertEquals(song.getYear(), 2004);
    }
}
