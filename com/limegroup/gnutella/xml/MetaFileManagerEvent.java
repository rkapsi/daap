
package com.limegroup.gnutella.xml;

import java.util.EventObject;
import com.limegroup.gnutella.FileDesc;

/**
 * 
 */
public class MetaFileManagerEvent extends EventObject {
    
    public static final int ADD     = 1;
    public static final int REMOVE  = 2;
    public static final int RENAME  = 3;
    public static final int CHANGE  = 4;
    
    private final int kind;
    private final FileDesc[] files;
    
    public MetaFileManagerEvent(MetaFileManager manager, int kind) {
        this(manager, kind, null);
    }
    
    public MetaFileManagerEvent(MetaFileManager manager, int kind, FileDesc[] files) {
        super(manager);
        this.kind = kind;
        this.files = files;
    }
    
    public int getKind() {
        return kind;
    }
    
    /**
     * Note: RENAME and CHANGE events return an array with
     * two elements. The first element is the previous
     * FileDesc and the second is the new FileDesc.
     */
    public FileDesc[] getFileDesc() {
        return files;
    }

    public boolean isAddEvent() {
        return (kind==ADD);
    }
    
    public boolean isRemoveEvent() {
        return (kind==REMOVE);
    }
    
    public boolean isRenameEvent() {
        return (kind==RENAME);
    }
    
    public boolean isChangeEvent() {
        return (kind==CHANGE);
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer("MetaFileManagerEvent: [event=");
        
        switch(kind) {
            case ADD:
                buffer.append("ADD");
                break;
            case REMOVE:
                buffer.append("REMOVE");
                break;
            case RENAME:
                buffer.append("RENAME");
                break;
            case CHANGE:
                buffer.append("CHANGE");
                break;
            default:
                buffer.append("UNKNOWN");
                break;
        }
        
        if (files == null) {
            buffer.append(", files=null");
        } else {
        
            buffer.append(", files=").append(files.length).append("\n");
            
            for(int i = 0; i < files.length; i++) {
                buffer.append(files[i]);
            }
        }
        
        return buffer.append("]").toString();
    }
}
