/*
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004-2010 Roger Kapsi
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

package org.ardverk.daap.chunks;

/**
 * A Chuck is a tagged value (key/value pair). Chunks can either contain other
 * Chunks or data of various types.
 * 
 * @author Roger Kapsi
 */
public interface Chunk {

    // Note this is technically a char as known
    // from C. An 8 bit unsigned value.
    /** Type for unsigned byte Chunks */
    public static final int U_BYTE_TYPE = 1;

    /** Type for signed byte Chunks */
    public static final int BYTE_TYPE = 2;

    /** Length of byte Chunks */
    public static final int BYTE_LENGTH = 1;

    /** Type for unsigned short Chunks */
    public static final int U_SHORT_TYPE = 3;

    /** Type for signed short Chunks */
    public static final int SHORT_TYPE = 4;

    /** Length of short chunks */
    public static final int SHORT_LENGTH = 2;

    /** Type for unsigned int Chunks */
    public static final int U_INT_TYPE = 5;

    /** Type for signed int Chunks */
    public static final int INT_TYPE = 6;

    /** Length of int chunks */
    public static final int INT_LENGTH = 4;

    /** Type for unsigned long Chunks */
    public static final int U_LONG_TYPE = 7;

    /** Type for long Chunks */
    public static final int LONG_TYPE = 8;

    /** Length of long chunks */
    public static final int LONG_LENGTH = 8;

    /** Type for String Chunks (encoded as UTF-8) */
    public static final int STRING_TYPE = 9;

    /** Type for Date Chunks (Time in <u>seconds</u> since 1970) */
    public static final int DATE_TYPE = 10;

    /** Length of date chunks */
    public static final int DATE_LENGTH = 4;

    /**
     * Type for Version Chunks (an int value split up into major, minor and
     * patch level)
     */
    public static final int VERSION_TYPE = 11;

    /** Length of version chunks */
    public static final int VERSION_LENGTH = 4;

    /** Type for Container Chunks. Chunks that contain other Chunks */
    public static final int CONTAINER_TYPE = 12;

    /** */
    public int getContentCode();

    /** */
    public String getContentCodeString();

    /** */
    public String getName();

    /**
     * Returns the type of this Chunk. For example {@see #BOOLEAN_TYPE}.
     */
    public int getType();
}