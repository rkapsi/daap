
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;

public class BooleanChunk extends AbstractChunk {
	
	private boolean value;
	
	public BooleanChunk(String type, String name, boolean value) {
		super(type, name);
		this.value = value;
	}
	
	public boolean getValue() {
		return value;
	}
	
	public void setValue(boolean value) {
		this.value = value;
	}
	
	public int chunkLength() {
		return 1;
	}
	
	public int chunkTypeCode() {
		return 1;
	}
	
	public void serialize(OutputStream out) throws IOException {
		
		super.serialize(out);
		
		out.write((getValue()) ? (byte)1 : (byte)0);
	}
	
	public String toString() {
		return super.toString() + "=" + value;
	}
}   
