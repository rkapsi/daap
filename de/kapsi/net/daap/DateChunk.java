
package de.kapsi.net.daap;

/**
 * This class is an implementation of a date chunk.
 * The date is an integer with seconds since 1970
 * (standard UNIX time).
 */
public class DateChunk extends IntChunk {
    
    protected DateChunk(String type, String name, int date) {
        super(type, name, date);
    }
    
    /**
     * Returns <tt>Chunk.DATE_TYPE</tt>
     */
    public int getType() {
        return Chunk.DATE_TYPE;
    }
}
