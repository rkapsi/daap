
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;

public class StringChunk extends AbstractChunk {
	
	private String value;
	
	public StringChunk(String type, String name, String value) {
		super(type, name);
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public int chunkLength() {
		return (value != null) ? value.length() : 0;
	}
	
	public int chunkTypeCode() {
		return 9;
	}
	
	public void serialize(OutputStream out) throws IOException {
		
		super.serialize(out);
		
		if (value != null) {
			byte[] bytes = value.getBytes("UTF-8");
			out.write(bytes, 0, bytes.length);
		}
	}
	
	public String toString() {
		return super.toString() + "=" + value;
	}
}   
