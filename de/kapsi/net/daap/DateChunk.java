
package de.kapsi.net.daap;

public class DateChunk extends IntChunk {
	
	public DateChunk(String type, String name, int date) {
		super(type, name, date);
	}
	
	public int chunkTypeCode() {
		return 10;
	}
}
