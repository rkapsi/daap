
package de.kapsi.net.daap;

/**
 * This class implements a Version chunk. A Version chunk is 
 * a 32bit int where the two upper 2 bytes are the major version,
 * the 3rd byte minor and the last byte is the patch level.
 * <code>0x00020000 = 2.0.0</code>
 */
public class VersionChunk extends IntChunk {
    
    private int majorVersion;
    private byte minorVersion;
    private byte patchlevel;
    
    protected VersionChunk(String type, String name, int value) {
        super(type, name, value);
    }
    
    protected VersionChunk(String type, String name, int majorVersion, 
            byte minorVersion, byte patchlevel) {
                
        super(type, name, 0);
        
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchlevel = patchlevel;
        
        setValue(createVersion());
    }
    
    private final int createVersion() {
        byte[] dst = new byte[4];
        ByteUtil.toByte16BE(majorVersion, dst, 0);
        dst[2] = minorVersion;
        dst[3] = patchlevel;
        return ByteUtil.toIntBE(dst, 0);
    }
    
    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
        setValue(createVersion());
    }
    
    public void setMinorVersion(byte minorVersion) {
        this.minorVersion = minorVersion;
        setValue(createVersion());
    }
    
    public void setPatchlevel(byte patchlevel) {
        this.patchlevel = patchlevel;
        setValue(createVersion());
    }
    
    public int getMajorVersion() {
        return this.majorVersion;
    }
    
    public byte getMinorVersion() {
        return this.minorVersion;
    }
    
    public byte getPatchlevel() {
        return this.patchlevel;
    }
    
    /**
     * Returns <tt>Chunk.VERSION_TYPE</tt>
     */
    public int getType() {
        return Chunk.VERSION_TYPE;
    }
    
    public String toString() {
        return super.toString() + "=" + majorVersion + "." +
        minorVersion + "." + patchlevel;
    }
}
