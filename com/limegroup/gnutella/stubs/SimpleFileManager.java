package com.limegroup.gnutella.stubs;

import java.io.File;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.FileManager;
import com.limegroup.gnutella.Response;
import com.limegroup.gnutella.messages.QueryRequest;
import com.sun.java.util.collections.List;

/**
 * A file manager that behaves exactly like FileManager would if
 * MetaFileManager didn't exist.
 */
public class SimpleFileManager extends FileManager {
    
    public boolean shouldIncludeXMLInResponse(QueryRequest qr) {
        return false;
    }
    
    public void addXMLToResponse(Response r, FileDesc fd) {
        ;
    }
    
    protected FileDesc addFileIfShared(File f, List docs, boolean notify) {
        return addFileIfShared(f, notify);
    }
}

