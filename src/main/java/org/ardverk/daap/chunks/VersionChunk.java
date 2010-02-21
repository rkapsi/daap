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

import org.ardverk.daap.DaapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a Version chunk. A Version chunk is a 32bit int where
 * the two upper 2 bytes are the major version, the 3rd byte minor and the last
 * byte is the micro version. <code>0x00020000 = 2.0.0</code>
 * 
 * @author Roger Kapsi
 */
public abstract class VersionChunk extends AbstractChunk {

    private static final Logger LOG = LoggerFactory
            .getLogger(VersionChunk.class);

    public static final long MIN_VALUE = 0l;
    public static final long MAX_VALUE = 0xFFFFFFFFl;

    protected int version = 0;

    public VersionChunk(int type, String name, long value) {
        super(type, name);
        setValue(value);
    }

    public VersionChunk(String type, String name, long value) {
        super(type, name);
        setValue(value);
    }

    protected VersionChunk(String type, String name, int majorVersion,
            int minorVersion, int microVersion) {
        this(type, name, DaapUtil.toVersion(majorVersion, minorVersion,
                microVersion));
    }

    public void setValue(long version) {
        this.version = (int) checkVersionRange(version);
    }

    public long getValue() {
        return version & MAX_VALUE;
    }

    public void setMajorVersion(int majorVersion) {
        long version = getValue() & 0x0000FFFFl;
        version |= (majorVersion & 0xFFFF) << 16;
        setValue(version);
    }

    public void setMinorVersion(int minorVersion) {
        long version = getValue() & 0xFFFF00FFl;
        version |= (minorVersion & 0xFF) << 8;
        setValue(version);
    }

    public void setMicroVersion(int microVersion) {
        long version = getValue() & 0xFFFFFF00l;
        version |= (microVersion & 0xFF);
        setValue(version);
    }

    public int getMajorVersion() {
        return (int) ((getValue() >> 16) & 0xFFFF);
    }

    public int getMinorVersion() {
        return (int) ((getValue() >> 8) & 0xFF);
    }

    public int getMicroVersion() {
        return (int) (getValue() & 0xFF);
    }

    /**
     * Checks if #MIN_VALUE <= value <= #MAX_VALUE and if not an
     * IllegalArgumentException is thrown.
     */
    public static long checkVersionRange(long value)
            throws IllegalArgumentException {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Value is outside of Version range: " + value);
            }
        }
        return value;
    }

    /**
     * Returns {@see #VERSION_TYPE}
     */
    public int getType() {
        return Chunk.VERSION_TYPE;
    }

    public String toString(int indent) {
        return indent(indent) + name + "(" + getContentCodeString()
                + "; version)=" + getMajorVersion() + "." + getMinorVersion()
                + "." + getMicroVersion();
    }

    public static final int getMajorVersion(int version) {
        return (version & 0xFFFF0000) >> 16;
    }

    public static final int getMinorVersion(int version) {
        return (version & 0xFF00) >> 8;
    }

    public static final int getMicroVersion(int version) {
        return version & 0xFF;
    }
}