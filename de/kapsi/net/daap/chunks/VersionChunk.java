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

package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ByteUtil;
import de.kapsi.net.daap.DaapUtil;

/**
 * This class implements a Version chunk. A Version chunk is 
 * a 32bit int where the two upper 2 bytes are the major version,
 * the 3rd byte minor and the last byte is the patch level.
 * <code>0x00020000 = 2.0.0</code>
 */
public class VersionChunk extends IntChunk {
    
    private int majorVersion;
    private int minorVersion;
    private int patchLevel;
    
    protected VersionChunk(String type, String name, int value) {
        super(type, name, value);
    }
    
    protected VersionChunk(String type, String name, int majorVersion, 
            int minorVersion, int patchLevel) {
                
        super(type, name, 0);
        
        this.majorVersion = majorVersion & 0xFFFF;
        this.minorVersion = minorVersion & 0xFF;
        this.patchLevel = patchLevel & 0xFF;
        
        setValue(createVersion());
    }
    
    private final int createVersion() {
        return DaapUtil.toVersion(majorVersion, minorVersion, patchLevel);
    }
    
    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion & 0xFFFF;
        setValue(createVersion());
    }
    
    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion & 0xFF;
        setValue(createVersion());
    }
    
    public void setPatchlevel(int patchLevel) {
        this.patchLevel = patchLevel & 0xFF;
        setValue(createVersion());
    }
    
    public int getMajorVersion() {
        return majorVersion;
    }
    
    public int getMinorVersion() {
        return minorVersion;
    }
    
    public int getPatchLevel() {
        return patchLevel;
    }
    
    /**
     * Returns <tt>Chunk.VERSION_TYPE</tt>
     */
    public int getType() {
        return Chunk.VERSION_TYPE;
    }
    
    public String toString() {
        return super.toString() + "=" + majorVersion + "." +
        minorVersion + "." + patchLevel;
    }
}
