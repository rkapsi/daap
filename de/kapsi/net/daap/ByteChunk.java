
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;

public class ByteChunk extends AbstractChunk {
	
	private byte value;
	
	public ByteChunk(String type, String name, int value) {
		super(type, name);
		this.value = (byte)(value & 0xFF);
	}
	
	public byte getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = (byte)(value & 0xFF);
	}
	
	public int chunkLength() {
		return 1;
	}
	
	public int chunkTypeCode() {
		return 1;
	}
	
	public void serialize(OutputStream out) throws IOException {
		super.serialize(out);
		out.write(getValue());
	}
	
	public String toString() {
		return super.toString() + "=" + value;
	}
}   
