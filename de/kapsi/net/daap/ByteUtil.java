/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004 Roger Kapsi, info at kapsi dot de
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


/**
 * Miscellaneous Java primitive to byte array and vice versa
 * methods.
 *
 * <p>Note: All values are in Big-Endian!</p>
 *
 * @author  Roger Kapsi
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
     * Copies the first 4 characters of fourChars to dst
     */
    public static final int toFourCharBytes(String fourChars, byte[] dst, int offset)
            throws IllegalArgumentException {
        
        final int length = fourChars.length();
            
        if (length > 4)
            throw new IllegalArgumentException("Illegal fourChars length: " + length);
        
        for(int i = 0; i < length; i++) {
            dst[offset++] = (byte)(fourChars.charAt(i) & 0xFF);
        }
          
        return 4;
    }
    
    /**
     * This method converts the first 4 characters of fourChars to
     * an integer. 
     */
    public static final int toFourCharCode(String fourChars) 
            throws IllegalArgumentException {
        
        final byte[] dst = new byte[4];
        toFourCharBytes(fourChars, dst, 0);
        return toIntBE(dst, 0);
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
