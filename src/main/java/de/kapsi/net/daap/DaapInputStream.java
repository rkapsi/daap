/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2005 Roger Kapsi, info at kapsi dot de
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

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.ByteChunk;
import de.kapsi.net.daap.chunks.Chunk;
import de.kapsi.net.daap.chunks.ChunkFactory;
import de.kapsi.net.daap.chunks.ContainerChunk;
import de.kapsi.net.daap.chunks.DateChunk;
import de.kapsi.net.daap.chunks.IntChunk;
import de.kapsi.net.daap.chunks.LongChunk;
import de.kapsi.net.daap.chunks.ShortChunk;
import de.kapsi.net.daap.chunks.StringChunk;
import de.kapsi.net.daap.chunks.VersionChunk;

public class DaapInputStream extends FilterInputStream {
    
    private static final Log LOG = LogFactory.getLog(DaapInputStream.class);
    
    private ChunkFactory factory = null;
    
    public DaapInputStream(InputStream in) {
        super(in);
    }
    
    public int read() throws IOException {
        int b = super.read();
        if (b < 0) {
            throw new EOFException();
        }
        return b;
    }
    
    /*
     * Re: skip(length-Chunk.XYZ_LENGTH);
     * 
     * iTunes states in Content-Codes responses that
     * Chunk X is of type Y and has hence the length Z.
     * A Byte has for example the length 1. But in some
     * cases iTunes uses a different length for Bytes!
     * It's probably a bug in iTunes...
     */
    
    public int read(int length) throws IOException {
        skip(length-Chunk.BYTE_LENGTH);
        return read();
    }
    
    public int readShort(int length) throws IOException {
        skip(length-Chunk.SHORT_LENGTH);
        return (read() << 8) | read();
    }
    
    public int readInt(int length) throws IOException {
        skip(length-Chunk.INT_LENGTH);
        return    (read() << 24) 
                | (read() << 16) 
                | (read() <<  8) 
                | read();
    }
    
    public long readLong(int length) throws IOException {
        skip(length-Chunk.LONG_LENGTH);
        return    (read() << 54l)
                | (read() << 48l) 
                | (read() << 40l) 
                | (read() << 32l)
                | (read() << 24l) 
                | (read() << 16l) 
                | (read() <<  8l) 
                | read();
    }
    
    public String readString(int length) throws IOException {
        if (length == 0) {
            return null;
        }
        
        byte[] b = new byte[length];
        read(b, 0, b.length);
        return new String(b, DaapUtil.UTF_8);
    }
    
    public int readContentCode() throws IOException {
        return readInt(Chunk.INT_LENGTH);
    }
    
    public int readLength() throws IOException {
        return readInt(Chunk.INT_LENGTH);
    }
    
    public Chunk readChunk() throws IOException {
        int contentCode = readContentCode();
        int length = readLength();
        
        if (factory == null) {
            factory = new ChunkFactory();
        }

        Chunk chunk = factory.newChunk(contentCode);
        
        if (length > 0) {
            if (chunk instanceof ByteChunk) {
                checkLength(chunk, Chunk.BYTE_LENGTH, length);
                ((ByteChunk)chunk).setValue(read(length));
            } else if (chunk instanceof ShortChunk) {
                checkLength(chunk, Chunk.SHORT_LENGTH, length);
                ((ShortChunk)chunk).setValue(readShort(length));
            } else if (chunk instanceof IntChunk) {
                checkLength(chunk, Chunk.INT_LENGTH, length);
                ((IntChunk)chunk).setValue(readInt(length));
            } else if (chunk instanceof LongChunk) {
                checkLength(chunk, Chunk.LONG_LENGTH, length);
                ((LongChunk)chunk).setValue(readLong(length));
            } else if (chunk instanceof StringChunk) {
                ((StringChunk)chunk).setValue(readString(length));
            } else if (chunk instanceof DateChunk) {
                checkLength(chunk, Chunk.DATE_LENGTH, length);
                ((DateChunk)chunk).setValue(readInt(length));
            } else if (chunk instanceof VersionChunk) {
                checkLength(chunk, Chunk.VERSION_LENGTH, length);
                ((VersionChunk)chunk).setValue(readInt(length));
            } else if (chunk instanceof ContainerChunk) {
                byte[] b = new byte[length];
                read(b, 0, b.length);
                DaapInputStream in = new DaapInputStream(new ByteArrayInputStream(b));
                while(in.available() > 0) {
                    ((ContainerChunk)chunk).add(in.readChunk());
                }
                in.close();
            } else {
                throw new IOException("Unknown Chunk Type: " + chunk);
            }
        }
        
        return chunk;
    }
    
    /**
     * Throws an IOE if expected differs from length
     */
    private static void checkLength(Chunk chunk, int expected, int length) {
        if (expected != length) {
            //throw new IOException("Expected a chunk with length " + expected + " but got " + length + " (" + chunk.getContentCodeString() + ")");
            
            if (LOG.isWarnEnabled()) {
                LOG.warn("Expected a chunk with length " + expected + " but got " + length + " (" + chunk.getContentCodeString() + ")");
            }
        }
    }
}
