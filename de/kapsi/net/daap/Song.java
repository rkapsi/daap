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

package de.kapsi.net.daap;

import java.util.HashMap;

import de.kapsi.net.daap.chunks.AbstractChunk;
import de.kapsi.net.daap.chunks.Chunk;
import de.kapsi.net.daap.chunks.impl.ContainerItemId;
import de.kapsi.net.daap.chunks.impl.ItemId;
import de.kapsi.net.daap.chunks.impl.ItemKind;
import de.kapsi.net.daap.chunks.impl.ItemName;
import de.kapsi.net.daap.chunks.impl.NormVolume;
import de.kapsi.net.daap.chunks.impl.PersistentId;
import de.kapsi.net.daap.chunks.impl.SongAlbum;
import de.kapsi.net.daap.chunks.impl.SongArtist;
import de.kapsi.net.daap.chunks.impl.SongBeatsPerMinute;
import de.kapsi.net.daap.chunks.impl.SongBitrate;
import de.kapsi.net.daap.chunks.impl.SongComment;
import de.kapsi.net.daap.chunks.impl.SongCompilation;
import de.kapsi.net.daap.chunks.impl.SongComposer;
import de.kapsi.net.daap.chunks.impl.SongDataKind;
import de.kapsi.net.daap.chunks.impl.SongDataUrl;
import de.kapsi.net.daap.chunks.impl.SongDateAdded;
import de.kapsi.net.daap.chunks.impl.SongDateModified;
import de.kapsi.net.daap.chunks.impl.SongDescription;
import de.kapsi.net.daap.chunks.impl.SongDisabled;
import de.kapsi.net.daap.chunks.impl.SongDiscCount;
import de.kapsi.net.daap.chunks.impl.SongDiscNumber;
import de.kapsi.net.daap.chunks.impl.SongEqPreset;
import de.kapsi.net.daap.chunks.impl.SongFormat;
import de.kapsi.net.daap.chunks.impl.SongGenre;
import de.kapsi.net.daap.chunks.impl.SongGrouping;
import de.kapsi.net.daap.chunks.impl.SongRelativeVolume;
import de.kapsi.net.daap.chunks.impl.SongSampleRate;
import de.kapsi.net.daap.chunks.impl.SongSize;
import de.kapsi.net.daap.chunks.impl.SongStartTime;
import de.kapsi.net.daap.chunks.impl.SongStopTime;
import de.kapsi.net.daap.chunks.impl.SongTime;
import de.kapsi.net.daap.chunks.impl.SongTrackCount;
import de.kapsi.net.daap.chunks.impl.SongTrackNumber;
import de.kapsi.net.daap.chunks.impl.SongUserRating;
import de.kapsi.net.daap.chunks.impl.SongYear;

/**
 * There isn't much to say: a Song is a Song.
 * <p>Note: although already mentioned in StringChunk I'd like to
 * point out that <code>null</code> is a valid value for DAAP. Use
 * it to reset Strings. See StringChunk for more information!</p>
 *
 * @author  Roger Kapsi
 */
public class Song {
    
    private static int ID = 0;
    
    private final HashMap properties = new HashMap();
    
    private final ItemKind itemKind = new ItemKind(2);
    private final ItemId itemId = new ItemId();
    private final ItemName itemName = new ItemName();
    
    private final SongAlbum album = new SongAlbum();
    private final SongArtist artist = new SongArtist();
    private final SongBeatsPerMinute bpm = new SongBeatsPerMinute();
    private final SongBitrate bitrate = new SongBitrate();
    private final SongComment comment = new SongComment();
    private final SongCompilation compilation = new SongCompilation();
    private final SongComposer composer = new SongComposer();
    private final SongDataKind dataKind = new SongDataKind();
    private final SongDataUrl dataUrl = new SongDataUrl();
    private final SongDateAdded dateAdded = new SongDateAdded();
    private final SongDateModified dateModified = new SongDateModified();
    private final SongDescription description = new SongDescription();
    private final SongDisabled disabled = new SongDisabled();
    private final SongDiscCount discCount = new SongDiscCount();
    private final SongDiscNumber discNumber = new SongDiscNumber();
    private final SongEqPreset eqPreset = new SongEqPreset();
    private final SongFormat format = new SongFormat();
    private final SongGenre genre = new SongGenre();
    private final SongRelativeVolume relativeVolume = new SongRelativeVolume();
    private final SongSampleRate sampleRate = new SongSampleRate();
    private final SongSize size = new SongSize();
    private final SongStartTime startTime = new SongStartTime();
    private final SongStopTime stopTime = new SongStopTime();
    private final SongTime time = new SongTime();
    private final SongTrackCount trackCount = new SongTrackCount();
    private final SongTrackNumber trackNumber = new SongTrackNumber();
    private final SongUserRating userRating = new SongUserRating();
    private final SongYear year = new SongYear();
    private final SongGrouping grouping = new SongGrouping();
    
    //private final SongCodecType codecType = new SongCodecType();
    //private final SongCodecSubtype codecSubtype = new SongCodecSubtype();
    
    private final ContainerItemId containerItemId = new ContainerItemId();
    
    private final PersistentId persistentId = new PersistentId();
    private final NormVolume normVolume = new NormVolume();
    
    /**
     * Creates a new Song
     */
    public Song() {
        
        synchronized(Song.class) {
            itemId.setValue(++ID);
        }
        
        persistentId.setValue(itemId.getValue());
        containerItemId.setValue(itemId.getValue());
        
        add(itemKind);
        add(itemName);
        add(itemId);
        add(containerItemId);
        
        add(album);
        add(artist);
        add(bpm);
        add(bitrate);
        add(comment);
        add(compilation);
        add(composer);
        add(dataKind);
        add(dataUrl);
        add(dateAdded);
        add(dateModified);
        add(description);
        add(disabled);
        add(discCount);
        add(discNumber);
        add(eqPreset);
        add(format);
        add(genre);
        add(relativeVolume);
        add(sampleRate);
        add(size);
        add(startTime);
        add(stopTime);
        add(time);
        add(trackCount);
        add(trackNumber);
        add(userRating);
        add(year);
        add(grouping);
        add(persistentId);
        add(normVolume);
        
        //add(codecType);
        //add(codecSubtype);
    }
    
    /**
     * Creates a new Song with the provided name
     */
    public Song(String name) {
        this();
        itemName.setValue(name);
    }
    
    /**
     * Returns the unique id of this song
     */
    public int getId() {
        return itemId.getValue();
    }
    
    /**
     * Returns the id of this Songs container.
     * Note: same as getId()
     */
    public int getContainerId() {
        return containerItemId.getValue();
    }
    
    /**
     * Returns the name of this Song
     */
    public String getName() {
        return itemName.getValue();
    }
    
    /**
     * Sets the name of this Song
     */
    public void setName(String name) {
        itemName.setValue(name);
    }
    
    /**
     * Sets the album of this Song
     */
    public void setAlbum(String album) {
        this.album.setValue(album);
    }
    
    /**
     * Returns the album of this Song
     */
    public String getAlbum() {
        return album.getValue();
    }
    
    /**
     * Sets the artist of this Song
     */
    public void setArtist(String artist) {
        this.artist.setValue(artist);
    }
    
    /**
     * Returns the artist of this Song
     */
    public String getArtist() {
        return artist.getValue();
    }
    
    /**
     * Sets the beats per minute of this Song
     */
    public void setBeatsPerMinute(int bpm) {
        this.bpm.setValue(bpm);
    }
    
    /**
     * Returns the beats per minute of this Song
     */
    public int getBeatsPerMinute() {
        return bpm.getValue();
    }
    
    /**
     * Sets the bitrate of this Song
     */
    public void setBitrate(int bitrate) {
        this.bitrate.setValue(bitrate);
    }
    
    /**
     * Returns the bitrate of this Song
     */
    public int getBitrate() {
        return bitrate.getValue();
    }
    
    /**
     * Sets the comment of this Song
     */
    public void setComment(String comment) {
        this.comment.setValue(comment);
    }
    
    /**
     * Returns the comment of this Song
     */
    public String getComment() {
        return comment.getValue();
    }
    
    /**
     * Sets if this Song is a compilation
     */
    public void setCompilation(boolean comp) {
        this.compilation.setValue(comp);
    }
    
    /**
     * Returns <tt>true</tt> if this Song is a
     * compilation
     */
    public boolean isCompilation() {
        return compilation.getValue();
    }
    
    /**
     * Sets the composer of this Song
     **/
    public void setComposer(String composer) {
        this.composer.setValue(composer);
    }
    
    /** 
     * Returns the composer of this Song
     */
    public String getComposer() {
        return composer.getValue();
    }
    
    /**
     * Sets whether this Song is a Radio or a DAAP
     * stream. See SongDataKind for more information.
     * Note: you must set the DataUrl with setDataUrl()
     * if dataKind is Radio!
     */
    public void setDataKind(int dataKind) {
        this.dataKind.setValue(dataKind);
    }
    
    /**
     * Returns the kind of this Song
     */
    public int getDataKind() {
        return dataKind.getValue();
    }
    
    /**
     * Sets the URL of this Song
     */
    public void setDataUrl(String dataUrl) {
        this.dataUrl.setValue(dataUrl);
    }
    
    /**
     * Returns the URL of this Song
     */
    public String getDataUrl() {
        return dataUrl.getValue();
    }
    
    /**
     * Sets the date when this Song was added to the
     * Library. Note: the date is in seconds since
     * 1970.
     * <code>(int)(System.currentTimeMillis()/1000)</code>
     */
    public void setDateAdded(int dateAdded) {
        this.dateAdded.setValue(dateAdded);
    }
    
    /**
     * Returns the date when this Song was added to 
     * the Library
     */
    public int getDateAdded() {
        return dateAdded.getValue();
    }
    
    /**
     * Sets the date when this Song was modified.
     * Note: the date is in seconds since 1970.
     * <code>(int)(System.currentTimeMillis()/1000)</code>
     */
    public void setDateModified(int dateModified) {
        this.dateModified.setValue(dateModified);
    }
    
    /**
     * Returns the date when this song was modified
     */
    public int getDateModified() {
        return dateModified.getValue();
    }
    
    /**
     * Sets the description of this Song.
     * Note: the description of a Song is its
     * file format. The description of a MP3
     * file is for example 'MPEG Audio file'. 
     * See SongDescription for more information.
     */
    public void setDescription(String description) {
        this.description.setValue(description);
    }
    
    /**
     * Returns the description of this Song
     */
    public String getDescription() {
        return description.getValue();
    }
    
    /**
     * Sets if this Song is either disabled or enabled.
     * This is indicated in iTunes by the small checkbox
     * next to the Song name.
     */
    public void setDisabled(boolean disabled) {
        this.disabled.setValue(disabled);
    }
    
    /**
     * Returns <tt>true</tt> if this Song is disabled
     */
    public boolean isDisabled() {
        return disabled.getValue();
    }
    
    /**
     * Sets the number of discs of this Song
     */
    public void setDiscCount(int discCount) {
        this.discCount.setValue(discCount);
    }
    
    /**
     * Returns the number of discs
     */
    public int getDiscCount() {
        return discCount.getValue();
    }
    
    /**
     * Sets the disc number of this Song
     */
    public void setDiscNumber(int discNumber) {
        this.discNumber.setValue(discNumber);
    }
    
    /**
     * Returns the disc number of this Song
     */
    public int getDiscNumber() {
        return discNumber.getValue();
    }
    
    /**
     * Sets the equalizer of this Song.
     * Note: See SongEqPreset for more information
     */
    public void setEqPreset(String eqPreset) {
        this.eqPreset.setValue(eqPreset);
    }
    
    /**
     * Returns the equalizer of this Song
     */
    public String getEqPreset() {
        return eqPreset.getValue();
    }
    
    /**
     * Sets the format of this Song.
     * Note: See SongFormat for more information
     */
    public void setFormat(String format) {
        this.format.setValue(format);
    }
    
    /**
     * Returns the format of this Song
     */
    public String getFormat() {
        return format.getValue();
    }
    
    /**
     * Sets the genre of this Song.
     * Note: See SongGenre for more information
     */
    public void setGenre(String genre) {
        this.genre.setValue(genre);
    }
    
    /**
     * Returns the genre of this Song
     */
    public String getGenre() {
        return genre.getValue();
    }
    
    /**
     * Unknown purpose
     */
    public void setRelativeVolume(int relativeVolume) {
        this.relativeVolume.setValue(relativeVolume);
    }
    
    /**
     * Unknown purpose
     */
    public int getRelativeVolume() {
        return relativeVolume.getValue();
    }
    
    /**
     * Sets the sample rate of this Song in kHz
     */
    public void setSampleRate(int sampleRate) {
        this.sampleRate.setValue(sampleRate);
    }
    
    /**
     * Returns the sample rate of this Song
     */
    public int getSampleRate() {
        return sampleRate.getValue();
    }
    
    /**
     * Sets the file size of this Song
     */
    public void setSize(int size) {
        this.size.setValue(size);
    }
    
    /**
     * Returns the file size of this Song
     */
    public int getSize() {
        return size.getValue();
    }
    
    /**
     * Sets the start time of this Song in 
     * <tt>milliseconds</tt>.
     */
    public void setStartTime(int startTime) {
        this.startTime.setValue(startTime);
    }
    
    /**
     * Returns the start time of this Song
     */
    public int getStartTime() {
        return startTime.getValue();
    }
    
    /**
     * Sets the stop time of this Song in 
     * <tt>milliseconds</tt>.
     */
    public void setStopTime(int stopTime) {
        this.stopTime.setValue(stopTime);
    }
    
    /**
     * Returns the stop time of this Song
     */
    public int getStopTime() {
        return stopTime.getValue();
    }
    
    /**
     * Sets the time (length) of this Song in
     * <tt>milliseconds</tt>.
     */
    public void setTime(int time) {
        this.time.setValue(time);
    }
    
    /**
     * Returns the time (length) of this Song
     */
    public int getTime() {
        return time.getValue();
    }
    
    /**
     * Sets the track count of this Song
     */
    public void setTrackCount(int trackCount) {
        this.trackCount.setValue(trackCount);
    }
    
    /**
     * Returns the track count of this Song
     */
    public int getTrackCount() {
        return trackCount.getValue();
    }
    
    /**
     * Sets the track number of this Song
     */
    public void setTrackNumber(int trackNumber) {
        this.trackNumber.setValue(trackNumber);
    }
    
    /**
     * Returns the track number of this Song
     */
    public int getTrackNumber() {
        return trackNumber.getValue();
    }
    
    /**
     * Sets the user rating of this Song.
     * Note: See SongUserRating for more informations
     */
    public void setUserRating(int userRating) {
        this.userRating.setValue(userRating);
    }
    
    /**
     * Returns the user rating of this Song
     */
    public int getUserRating() {
        return userRating.getValue();
    }
    
    /**
     * Sets the year of this Song
     */
    public void setYear(int year) {
        this.year.setValue(year);
    }
    
    /**
     * Returns the year of this Song
     */
    public int getYear() {
        return year.getValue();
    }
    
    /**
     * Sets the grouping of this Song
     */
    public void setGrouping(String grouping) {
        this.grouping.setValue(grouping);
    }
    
    /**
     * Returns the grouping of this Song
     */
    public String getGrouping() {
        return grouping.getValue();
    }
    
    /**
     * 
     */
    private void add(AbstractChunk chunk) {
        properties.put(chunk.getName(), chunk);
    }
    
    /**
     * Used by Playlist to get the underlying Chunks
     */
    public Chunk getProperty(String property) {
        return (Chunk)properties.get(property);
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append("Name: ").append(getName()).append("\n");
        buffer.append("ID: ").append(getId()).append("\n");
        buffer.append("Artist: ").append(getArtist()).append("\n");
        buffer.append("Album: ").append(getAlbum()).append("\n");
        buffer.append("Bitrate: ").append(getBitrate()).append("\n");
        buffer.append("Genre: ").append(getGenre()).append("\n");
        buffer.append("Comment: ").append(getComment()).append("\n");
        
        return buffer.append("\n").toString();
    }
}
