
package de.kapsi.net.daap;

import java.io.UnsupportedEncodingException;

/**
 * Miscellaneous Java primitive to byte array and vice versa
 * methods.
 *
 * Note: All values are in Big-Endian!
 */
public final class ByteUtil {
    
    private ByteUtil() {
    }
    
	/**
	 * 16bit to int
	 */
	public static final int toInt16BE(byte[] src, int offset) {
        return 	(((src[  offset] & 0xFF) << 8) + 
				  (src[++offset] & 0xFF));
    }
    
	/**
	 * int to 16bit
	 */
	public static final int toByte16BE(int value, byte[] dst, int offset) {
		dst[  offset] = (byte)((value >> 8) & 0xFF);
		dst[++offset] = (byte)(value & 0xFF);
		return 2;
    }
	
	/**
	 * 32bit to int
	 */
    public static final int toIntBE(byte[] src, int offset) {
        return (((src[  offset] & 0xFF) << 24) + 
                ((src[++offset] & 0xFF) << 16) + 
                ((src[++offset] & 0xFF) << 8) + 
				 (src[++offset] & 0xFF));
    }
	
	/**
	 * Used to copy the bytes of a content code (a four character string)
     * to dst...
	 */
	public static final int toContentCodeBytes(String value, byte[] dst, int offset) 
			throws UnsupportedEncodingException {
			
		byte[] bytes = value.getBytes("UTF-8");
        
        if (bytes.length != 4)
            throw new UnsupportedEncodingException("Illegal content code length");
            
		System.arraycopy(bytes, 0, dst, offset, 4);
		return 4;
	}
	
	/**
	 * int to 32bit
	 */
	public static final int toByteBE(int value, byte[] dst, int offset) {
		dst[  offset] = (byte)((value >> 24) & 0xFF);
		dst[++offset] = (byte)((value >> 16) & 0xFF);
		dst[++offset] = (byte)((value >> 8) & 0xFF);
		dst[++offset] = (byte)(value & 0xFF);
		return 4;
    }
	
	/**
	 * long to 64bit
	 */
	public static final int toByte64BE(long value, byte[] dst, int offset) {
		dst[  offset] = (byte)((value >> 56) & 0xFF);
		dst[++offset] = (byte)((value >> 48) & 0xFF);
		dst[++offset] = (byte)((value >> 40) & 0xFF);
		dst[++offset] = (byte)((value >> 32) & 0xFF);
		dst[++offset] = (byte)((value >> 24) & 0xFF);
		dst[++offset] = (byte)((value >> 16) & 0xFF);
		dst[++offset] = (byte)((value >> 8) & 0xFF);
		dst[++offset] = (byte)(value & 0xFF);
		return 8;
    }
}
