
package de.kapsi.net.daap;

import java.math.BigInteger;
import java.io.OutputStream;
import java.io.IOException;

public class LongChunk extends AbstractChunk {
	
	private static final BigInteger MAX_VALUE = new BigInteger("FFFFFFFFFFFFFFFF", 16);
	
	private BigInteger value;
	
	public LongChunk(String type, String name, long value) {
		super(type, name);
		setValue(value);
	}
	
	public LongChunk(String type, String name, String value) {
		super(type, name);
		setValue(value);
	}
	
	public LongChunk(String type, String name, BigInteger value) {
		super(type, name);
		setValue(value);
	}
	
	public BigInteger getValue() {
		return value;
	}
	
	public void setValue(long value) {
		setValue(new BigInteger(Long.toHexString(value), 16));
	}
	
	public void setValue(String value) {
		setValue(new BigInteger(value, 16));
	}
	
	public void setValue(BigInteger value) {
		if (value == null || value.compareTo(MAX_VALUE) > 0) {
			throw new IllegalArgumentException();
		}
		
		this.value = value;
	}
	
	public int chunkLength() {
		return 8;
	}
	
	public int chunkTypeCode() {
		return 7;
	}
	
	public void serialize(OutputStream out) throws IOException {
		
		super.serialize(out);
		
		byte[] bytes = value.toByteArray();
		byte[] tmp = new byte[chunkLength()];
		
		int i = chunkLength()-1;
		int j = bytes.length-1;
		
		while(i >= 0 && j >= 0) {
			tmp[i] = bytes[j];
			
			i--;
			j--;
		}
		
		out.write(tmp, 0, tmp.length);
	}
	
	public String toString() {
		return super.toString() + "=" + getValue();
	}
}   
