
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;

public abstract class AbstractChunk implements Chunk {
	
	private String chunkType;
	private String chunkName;
	
	public AbstractChunk(String chunkType, String chunkName) {
	
		if (chunkType.length() != 4) {
			throw new IndexOutOfBoundsException("ChunkType must have 4 characters");
		}
		
		this.chunkType = chunkType;
		this.chunkName = chunkName;
	}
	
	public String getChunkType() {
		return chunkType;
	}
	
	public String getChunkName() {
		return chunkName;
	}
	
	public int chunkSize() {
		return 4+4+chunkLength();
	}
	
	public abstract int chunkLength();
	public abstract int chunkTypeCode();
	
	public void serialize(OutputStream out) throws IOException {
		byte[] buffer = new byte[4+4];
		
		ByteUtil.toByteBE(chunkType, buffer, 0);
		ByteUtil.toByteBE(chunkLength(), buffer, 4);
		
		out.write(buffer, 0, buffer.length);
	}
	
	public String toString() {
		return chunkName + "('" + chunkType + "')";
	}
}
