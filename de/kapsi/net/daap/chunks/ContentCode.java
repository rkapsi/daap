
package de.kapsi.net.daap.chunks;

import java.io.OutputStream;
import java.io.IOException;

import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.chunks.impl.ContentCodesNumber;
import de.kapsi.net.daap.chunks.impl.ContentCodesName;
import de.kapsi.net.daap.chunks.impl.ContentCodesType;
import de.kapsi.net.daap.chunks.impl.Dictionary;

/**
 * A content code is essentially a description of a chunk.
 */
public final class ContentCode extends Dictionary {
    
    public ContentCode(String type, String name, int value) {
        super();
        
        add(new ContentCodesNumber(DaapUtil.toContentCodeNumber(type)));
        add(new ContentCodesName(name));
        add(new ContentCodesType(value));
    }
}
