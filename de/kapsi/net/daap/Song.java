
package de.kapsi.net.daap;

import de.kapsi.net.daap.chunks.*;

import java.util.*;

public class Song {
	
	private static int ID = 1;
	
	private final HashMap properties = new HashMap();
	private final HashSet listener = new HashSet();
	
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
	
	private final ContainerItemId containerItemId = new ContainerItemId();
	
	private final PersistentId persistentId = new PersistentId();
	private final NormVolume normVolume = new NormVolume();
	
	public Song(String name) {

		synchronized(Song.class) {
			itemId.setValue(ID++);
		}
		
		itemName.setValue(name);
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
		
		//setFormat("mp3");
	}
	
	public int getId() {
		return itemId.getValue();
	}
	
	public int getContainerId() {
		return containerItemId.getValue();
	}
	
	public String getName() {
		return itemName.getValue();
	}
	
	public void setName(String name) {
		itemName.setValue(name);
	}
	
	public void setAlbum(String album) {
		this.album.setValue(album);
	}
	
	public String getAlbum() {
		return album.getValue();
	}
	
	public void setArtist(String artist) {
		this.artist.setValue(artist);
	}
	
	public String getArtist() {
		return artist.getValue();
	}
	
	public void setBeatsPerMinute(int bpm) {
		this.bpm.setValue(bpm);
	}
	
	public int getBeatsPerMinute() {
		return bpm.getValue();
	}
	
	public void setBitrate(int bitrate) {
		this.bitrate.setValue(bitrate);
	}
	
	public int getBitrate() {
		return bitrate.getValue();
	}
	
	public void setComment(String comment) {
		this.comment.setValue(comment);
	}
	
	public String getComment() {
		return comment.getValue();
	}

	public void setCompilation(boolean comp) {
		this.compilation.setValue(comp);
	}
	
	public boolean isCompilation() {
		return compilation.getValue();
	}
	
	public void setComposer(String composer) {
		this.composer.setValue(composer);
	}
	
	public String getComposer() {
		return composer.getValue();
	}
	
	public void setDataKind(int dataKind) {
		this.dataKind.setValue(dataKind);
	}
	
	public int getDataKind() {
		return dataKind.getValue();
	}
	
	public void setDataUrl(String dataUrl) {
		this.dataUrl.setValue(dataUrl);
	}
	
	public String getDataUrl() {
		return dataUrl.getValue();
	}
	
	public void setDateAdded(int dateAdded) {
		this.dateAdded.setValue(dateAdded);
	}
	
	public int getDateAdded() {
		return dateAdded.getValue();
	}
	
	public void setDateModified(int dateModified) {
		this.dateModified.setValue(dateModified);
	}
	
	public int getDateModified() {
		return dateModified.getValue();
	}
	
	public void setDescription(String description) {
		this.description.setValue(description);
	}
	
	public String getDescription() {
		return description.getValue();
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled.setValue(disabled);
	}
	
	public boolean isDisabled() {
		return disabled.getValue();
	}
	
	public void setDiscCount(int discCount) {
		this.discCount.setValue(discCount);
	}
	
	public int getDiscCount() {
		return discCount.getValue();
	}
	
	public void setDiscNumber(int discNumber) {
		this.discNumber.setValue(discNumber);
	}
	
	public int getDiscNumber() {
		return discNumber.getValue();
	}
	
	public void setEqPreset(String eqPreset) {
		this.eqPreset.setValue(eqPreset);
	}
	
	public String getEqPreset() {
		return eqPreset.getValue();
	}
	
	public void setFormat(String format) {
		this.format.setValue(format);
	}
	
	public String getFormat() {
		return format.getValue();
	}
	
	public void setGenre(String genre) {
		this.genre.setValue(genre);
	}
	
	public String getGenre() {
		return genre.getValue();
	}
	
	public void setRelativeVolume(int relativeVolume) {
		this.relativeVolume.setValue(relativeVolume);
	}
	
	public int getRelativeVolume() {
		return relativeVolume.getValue();
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate.setValue(sampleRate);
	}
	
	public int getSampleRate() {
		return sampleRate.getValue();
	}
	
	public void setSize(int size) {
		this.size.setValue(size);
	}
	
	public int getSize() {
		return size.getValue();
	}
	
	public void setStartTime(int startTime) {
		this.startTime.setValue(startTime);
	}
	
	public int getStartTime() {
		return startTime.getValue();
	}
	
	public void setStopTime(int stopTime) {
		this.stopTime.setValue(stopTime);
	}
	
	public int getStopTime() {
		return stopTime.getValue();
	}
	
	public void setTime(int time) {
		this.time.setValue(time);
	}
	
	public int getTime() {
		return time.getValue();
	}
	
	public void setTrackCount(int trackCount) {
		this.trackCount.setValue(trackCount);
	}
	
	public int getTrackCount() {
		return trackCount.getValue();
	}
	
	public void setTrackNumber(int trackNumber) {
		this.trackNumber.setValue(trackNumber);
	}
	
	public int getTrackNumber() {
		return trackNumber.getValue();
	}
	
	public void setUserRating(int userRating) {
		this.userRating.setValue(userRating);
	}
	
	public int getUserRating() {
		return userRating.getValue();
	}
	
	public void setYear(int year) {
		this.year.setValue(year);
	}
	
	public int getYear() {
		return year.getValue();
	}
	
	public void setGrouping(String grouping) {
		this.grouping.setValue(grouping);
	}
	
	public String getGrouping() {
		return grouping.getValue();
	}
	
	private void add(AbstractChunk chunk) {
		properties.put(chunk.getChunkName(), chunk);
	}
	
	Chunk getProperty(String property) {
		return (Chunk)properties.get(property);
	}
	
	void addListener(SongListener l) {
		listener.add(l);
	}
	
	void removeListener(SongListener l) {
		listener.remove(l);
	}
	
	public void update() {
		Iterator it = listener.iterator();
		while(it.hasNext()) {
			((SongListener)it.next()).songEvent(this, SongListener.SONG_CHANGED);
		}
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("Name: ").append(getName());
		buffer.append(", id: ").append(getId());
		
		return buffer.toString();
	}
}
