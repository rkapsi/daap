
package de.kapsi.net.daap;

/**
 * 
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
