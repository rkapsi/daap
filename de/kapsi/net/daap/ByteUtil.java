
package de.kapsi.net.daap;

public class ByteUtil {

	public static final int toInt16BE(byte[] src, int offset) {
        return 	(((src[offset] & 0xFF) << 8) + 
				(src[offset+1] & 0xFF));
    }
    
	public static final int toByte16BE(int value, byte[] dst, int offset) {
		dst[offset] = (byte)((value >> 8) & 0xFF);
		dst[offset+1] = (byte)(value& 0xFF);
		return 2;
    }
	
    public static final int toIntBE(byte[] src, int offset) {
        return 	(((src[offset] & 0xFF) << 24) + 
                ((src[offset+1] & 0xFF) << 16) + 
                ((src[offset+2] & 0xFF) << 8) + 
				(src[offset+3] & 0xFF));
    }
	
	public static final int toByteBE(String value, byte[] dst, int offset) 
			throws java.io.UnsupportedEncodingException {
			
		byte[] bytes = value.getBytes("UTF-8");
		System.arraycopy(bytes, 0, dst, offset, 4);
		return 4;
	}
	
	public static final int toByteBE(int value, byte[] dst, int offset) {
		dst[offset] = (byte)((value >> 24) & 0xFF);
		dst[offset+1] = (byte)((value >> 16) & 0xFF);
		dst[offset+2] = (byte)((value >> 8) & 0xFF);
		dst[offset+3] = (byte)(value& 0xFF);
		return 4;
    }
	
	public static final int toByte64BE(long value, byte[] dst, int offset) {
		dst[offset]   = (byte)((value >> 56) & 0xFF);
		dst[offset+1] = (byte)((value >> 48) & 0xFF);
		dst[offset+2] = (byte)((value >> 40) & 0xFF);
		dst[offset+3] = (byte)((value >> 32) & 0xFF);
		dst[offset+4] = (byte)((value >> 24) & 0xFF);
		dst[offset+5] = (byte)((value >> 16) & 0xFF);
		dst[offset+6] = (byte)((value >> 8) & 0xFF);
		dst[offset+7] = (byte)(value & 0xFF);
		return 8;
    }
}
