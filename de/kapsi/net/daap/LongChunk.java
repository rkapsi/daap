
package de.kapsi.net.daap;

import java.math.BigInteger;
import java.io.OutputStream;
import java.io.IOException;

public class LongChunk extends AbstractChunk {
	
	private byte[] value;
	
	public LongChunk(String type, String name, long value) {
		super(type, name);
		setValue(value);
	}
	
	public LongChunk(String type, String name, String value) {
		super(type, name);
		setValue(value);
	}
	
	public BigInteger getValue() {
		return new BigInteger(value);
	}
	
	public void setValue(long value) {
		this.value = new byte[chunkLength()];
		ByteUtil.toByte64BE(value, this.value, 0);
	}
	
	public void setValue(String value) {
		if (value.length() > 16) {
			throw new IllegalArgumentException();
		}
		
		setValue(new BigInteger(value, 16));
	}
	
	public void setValue(BigInteger value) {
		
		if (value.compareTo(new BigInteger("FFFFFFFFFFFFFFFF", 16)) > 0) {
			throw new IllegalArgumentException();
		}
		
		byte[] bytes = value.toByteArray();
		this.value = new byte[chunkLength()];
		
		int i = chunkLength()-1;
		int j = bytes.length-1;
		
		while(i >= 0 && j >= 0) {
			this.value[i] = bytes[j];
			
			i--;
			j--;
		}
	}
	
	public int chunkLength() {
		return 8;
	}
	
	public int chunkTypeCode() {
		return 7;
	}
	
	public void serialize(OutputStream out) throws IOException {
		
		super.serialize(out);
		
		out.write(value, 0, value.length);
	}
	
	public String toString() {
		return super.toString() + "=" + getValue();
	}
}   
