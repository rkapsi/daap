
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
