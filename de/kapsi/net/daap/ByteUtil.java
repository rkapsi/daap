
package de.kapsi.net.daap;

public class ByteUtil {

	/**
	 *
	 */
	public static final int toInt16BE(byte[] src, int offset) {
        return 	(((src[  offset] & 0xFF) << 8) + 
				  (src[++offset] & 0xFF));
    }
    
	/**
	 *
	 */
	public static final int toByte16BE(int value, byte[] dst, int offset) {
		dst[  offset] = (byte)((value >> 8) & 0xFF);
		dst[++offset] = (byte)(value & 0xFF);
		return 2;
    }
	
	/**
	 *
	 */
    public static final int toIntBE(byte[] src, int offset) {
        return (((src[  offset] & 0xFF) << 24) + 
                ((src[++offset] & 0xFF) << 16) + 
                ((src[++offset] & 0xFF) << 8) + 
				 (src[++offset] & 0xFF));
    }
	
	/**
	 *
	 */
	public static final int toByteBE(String value, byte[] dst, int offset) 
			throws java.io.UnsupportedEncodingException {
			
		byte[] bytes = value.getBytes("UTF-8");
		System.arraycopy(bytes, 0, dst, offset, 4);
		return 4;
	}
	
	/**
	 *
	 */
	public static final int toByteBE(int value, byte[] dst, int offset) {
		dst[  offset] = (byte)((value >> 24) & 0xFF);
		dst[++offset] = (byte)((value >> 16) & 0xFF);
		dst[++offset] = (byte)((value >> 8) & 0xFF);
		dst[++offset] = (byte)(value & 0xFF);
		return 4;
    }
	
	/**
	 * 
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
