
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;

public class IntChunk extends AbstractChunk {
	
	private int value;
	
	public IntChunk(String type, String name, int value) {
		super(type, name);
		
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public int chunkLength() {
		return 4;
	}
	
	public int chunkTypeCode() {
		return 5;
	}
	
	public void serialize(OutputStream out) throws IOException {
		
		super.serialize(out);
		
		byte[] dst = new byte[4];
		ByteUtil.toByteBE(value, dst, 0);
		
		out.write(dst, 0, dst.length);
	}
	
	public String toString() {
		return super.toString() + "=" + value;
	}
}   
