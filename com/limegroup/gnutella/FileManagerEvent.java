
package com.limegroup.gnutella;

import java.util.EventObject;

/**
 * This class implements a FileManagerEvent which is
 * used by FileManager and MetaFileManager to notify
 * the front end about add, remove, rename and change
 * events in the Library.
 */
public class FileManagerEvent extends EventObject {
    
    public static final int ADD     = 1;
    public static final int REMOVE  = 2;
    public static final int RENAME  = 3;
    public static final int CHANGE  = 4;
    
    private final int kind;
    private final FileDesc[] files;
    
    public FileManagerEvent(FileManager manager, int kind) {
        this(manager, kind, null);
    }
    
    public FileManagerEvent(FileManager manager, int kind, FileDesc[] files) {
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
    
    /**
     * Returns true if this event is an ADD event
     */
    public boolean isAddEvent() {
        return (kind==ADD);
    }
    
    /**
     * Returns true if this event is a REMOVE event
     */
    public boolean isRemoveEvent() {
        return (kind==REMOVE);
    }
    
    /**
     * Returns true if this event is a RENAME (MOVE) 
     * event
     */
    public boolean isRenameEvent() {
        return (kind==RENAME);
    }
    
    /**
     * Returns true if this event is a CHANGE (i.e.
     * when ID3 Tags changed) event.
     */
    public boolean isChangeEvent() {
        return (kind==CHANGE);
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer("FileManagerEvent: [event=");
        
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
        
        if (files != null) {
            
            buffer.append(", files=").append(files.length).append("\n");
            
            for(int i = 0; i < files.length; i++) {
                buffer.append(files[i]);
            }
            
        } else {
            buffer.append(", files=null");
        }
        
        return buffer.append("]").toString();
    }
}
