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

package de.kapsi.net.daap.tools;

import java.lang.reflect.*;

import java.io.*;
import java.util.*;

import de.kapsi.net.daap.*;
import de.kapsi.net.daap.chunks.*;
import de.kapsi.net.daap.chunks.impl.*;

/**
 * This tool will help you to detect changes in the DAAP protocol.<p>
 *
 * <ol>
 * <li>install ethereal
 * <li>capture the /content-codes request between two iTunes hosts
 * <li>save the data as foobar.gz
 * <li>run gzip -d foobar.gz
 * <li>open foobar with this tool and it will tell you what's new!
 * </ol>
 *
 * @author  Roger Kapsi
 */
public class ContentCodesAnalyzer {
    
    /** Creates a new instance of ContentCodesAnalyzer */
    public ContentCodesAnalyzer() {
    }
    
    private static HashMap getKnownChunks(ChunkClasses chunkClasses) {
       HashMap map = new HashMap();
       
       String[] names = ChunkClasses.names;

        final Class[] arg1 = new Class[]{};
        final Object[] arg2 = new Object[]{};

        for(int i = 0; i < names.length; i++) {
            try {
                Class clazz = Class.forName(names[i]);

                Method methodContentCode = clazz.getMethod("getContentCode", arg1);
                Method methodName = clazz.getMethod("getName", arg1);
                Method methodType = clazz.getMethod("getType", arg1);

                Object inst = clazz.newInstance();

                String contentCode = (String)methodContentCode.invoke(inst, arg2);
                String name = (String)methodName.invoke(inst, arg2);
                int type = ((Integer)methodType.invoke(inst, arg2)).intValue();
                
                
                map.put(contentCode, new ContentCode(contentCode, name, type));
                
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
       
       return map;
    }
    
    private static HashMap readNewChunks(File file) throws IOException {
        HashMap map = new HashMap();
        
        FileInputStream in = new FileInputStream(file);
        
        try {
            in.skip(0x14); // skip header
            in.skip(0x04); // skip 'mdcl'
            
            byte[] lenBuf = new byte[4];
            
            while(in.read(lenBuf, 0, lenBuf.length) != -1) {
                int len = ByteUtil.toIntBE(lenBuf, 0);
                byte[] buf = new byte[len];
                if (in.read(buf, 0, buf.length) == -1)
                    break;
                
                int pos = 0;
                
                pos += 4; // skip 'mcnm'
                len = ByteUtil.toIntBE(buf, pos);
                pos += 4;
                String contentCode = new String(buf, pos, len);
                pos += len;
                
                pos += 4; // skip 'mcna'
                len = ByteUtil.toIntBE(buf, pos);
                pos += 4;
                String name = new String(buf, pos, len);
                pos += len;
                
                pos += 4; // skip 'mcty'
                len = ByteUtil.toIntBE(buf, pos);
                pos += 4;
                int type = ByteUtil.toInt16BE(buf, pos);
                pos += len;
                
                map.put(contentCode, new ContentCode(contentCode, name, type));
                
                in.skip(0x04); // skip 'mdcl' of the next chunk
            }
            
        } finally {
            in.close();
        }
        
        return map;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        if (args.length == 0) {
            System.out.println("ContentCodesAnalyzer <fileWithContentCodes>");
            System.exit(-1);
        }
        
        Object chunkClasses = null;
        
        try {
            chunkClasses = Class.forName("de.kapsi.net.daap.chunks.ChunkClasses").newInstance();
        } catch (Exception err) {
            
        }
        
        HashMap knownChnunks = null;
        
        if (chunkClasses != null) {
            knownChnunks = getKnownChunks((ChunkClasses)chunkClasses);
        } else {
            knownChnunks = new HashMap();
        }
        
        HashMap newChunks = readNewChunks(new File(args[0]));
        
        Iterator it = null;
        
        /*System.out.println("\n+++ KNOWN CHUNKS +++\n");
        
        it = knownChnunks.keySet().iterator();
        while(it.hasNext()) {
            System.out.println(knownChnunks.get(it.next()));
        }
        
        System.out.println("\n+++ NEW CHUNKS +++\n");
        
        
        it = newChunks.keySet().iterator();
        while(it.hasNext()) {
            System.out.println(newChunks.get(it.next()));
        }*/
        
        List added = new ArrayList();
        List removed = new ArrayList();
        List changed = new ArrayList();
        
        it = newChunks.keySet().iterator();
        while(it.hasNext()) {
            Object key = it.next();
            Object obj = newChunks.get(key);
            
            if (knownChnunks.containsKey(key) == false) {
                added.add(obj);
            } else {
                Object obj2 = knownChnunks.get(key);
                if (obj2.equals(obj) == false) {
                    changed.add(new Object[]{obj, obj2});
                }
            }
        }
        
        it = knownChnunks.keySet().iterator();
        while(it.hasNext()) {
            Object key = it.next();
            Object obj = knownChnunks.get(key);
            if (newChunks.containsKey(key) == false) {
                removed.add(obj);
            }
        }
        
        System.out.println("\n+++ NEW CHUNKS +++\n");
        
        it = added.iterator();
        while(it.hasNext()) {
            System.out.println(it.next());
        }
        
        System.out.println("\n+++ REMOVED CHUNKS +++\n");
        
        it = removed.iterator();
        while(it.hasNext()) {
            System.out.println(it.next());
        }
        
        System.out.println("\n+++ CHANGED CHUNKS +++\n");
        
        it = changed.iterator();
        while(it.hasNext()) {
            Object[] obj = (Object[])it.next();
            
            System.out.println("NEW: " + obj[0]);
            System.out.println("OLD: " + obj[1]);
        }
        
        FileOutputStream os = new FileOutputStream(new File("/Users/roger/foobar.txt"));
        byte[] dst = new byte[4];
        ByteUtil.toByteBE(SongCodecType.MPEG, dst, 0);
        os.write(dst,0,dst.length);
    }
    
    private static final class ContentCode {
        
        private String contentCode;
        private String name;
        private int type;
        
        private ContentCode(String contentCode, String name, int type) {
            this.contentCode = contentCode;
            this.name = name;
            this.type = type;
        }
        
        public boolean equals(Object o) {
            ContentCode other = (ContentCode)o;
            
            return (contentCode.equals(other.contentCode) && 
                    name.equals(other.name) && 
                    type == other.type);
        }
        
        public String toString() {
            StringBuffer buf = new StringBuffer();
            
            buf.append("dmap.dictionary = {\n");
            buf.append("    dmap.contentcodesnumber = ").append(contentCode).append("\n");
            buf.append("    dmap.contentcodesname = ").append(name).append("\n");
            buf.append("    dmap.contentcodestype = ").append(type).append("\n");
            buf.append("}\n");
            
            return buf.toString();
        }
    }
}
