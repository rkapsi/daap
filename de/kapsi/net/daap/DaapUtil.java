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

package de.kapsi.net.daap;

import java.text.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import java.nio.ByteBuffer;

import org.apache.commons.httpclient.Header;

import de.kapsi.net.daap.chunks.Chunk;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Misc methods and constants
 *
 * @author  Roger Kapsi
 */
public final class DaapUtil {
    
    public static final int UNDEF_VALUE = 0;
    
    private static final byte[] CRLF = { (byte)'\r', (byte)'\n' };
    private static final String ISO_8859_1 = "ISO-8859-1";
    
    private static final Log LOG = LogFactory.getLog(DaapUtil.class);
    
    private final static SimpleDateFormat formatter = 
        new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss z", Locale.US);
    
    private final static Random generator = new Random();
    
    static {
        // warm up...
        for(int i = 0; i < 100; i++) {
            generator.nextInt();
        }
    }
    
    public static final String[] DATABASE_SONGS_META = {
        "dmap.itemkind",
        "daap.songalbum",
        "daap.songgrouping",
        "daap.songartist",
        "daap.songbeatsperminute",
        "daap.songbitrate",
        "daap.songcomment",
        "daap.songcompilation",
        "daap.songcomposer",
        "daap.songdateadded",
        "daap.songdatemodified",
        "daap.songdisccount",
        "daap.songdiscnumber",
        "daap.songdatakind",
        "daap.songformat",
        "daap.songeqpreset",
        "daap.songgenre",
        "dmap.itemid",
        "daap.songdescription",
        "dmap.itemname",
        "com.apple.itunes.norm-volume", // Seems to be OK now but needs more testing!
        "dmap.persistentid",
        "daap.songdisabled",
        "daap.songrelativevolume",
        "daap.songsamplerate",
        "daap.songsize",
        "daap.songstarttime",
        "daap.songstoptime",
        "daap.songtime",
        "daap.songtrackcount",
        "daap.songtracknumber",
        "daap.songuserrating",
        "daap.songyear",
        "dmap.containeritemid",
        "daap.songdataurl"
    };
    
    public static final String[] DATABASE_PLAYLISTS_META = {
        "dmap.itemid",
        "dmap.persistentid",
        "dmap.itemname",
        "com.apple.itunes.smart-playlist",
        "dmap.itemcount"
    };
    
    public static final String[] PLAYLIST_SONGS_META = {
        "dmap.itemkind",
        "dmap.itemid",
        "dmap.containeritemid"
    };
    
    public static final int VERSION_1 = 0x00010000; // 1.0.0
    public static final int VERSION_2 = 0x00020000; // 2.0.0
    public static final int VERSION_3 = 0x00030000; // 3.0.0
    
    private static final String CLIENT_DAAP_VERSION = "Client-DAAP-Version";
    private static final String USER_AGENT = "User-Agent";
    
    private DaapUtil() {
    }
    
    public static int toContentCodeNumber(String contentCode) {
        if (contentCode.length() != 4) {
            throw new IllegalArgumentException("content code must have 4 characters!");
        }
        
        try {
            byte[] chars = contentCode.getBytes("UTF-8");
            return ByteUtil.toIntBE(chars, 0);
        } catch (UnsupportedEncodingException err) {
            LOG.error(err);
            return 0;
        }
    }
    
    /**
     * Returns the current Date/Time in "iTunes time format"
     */
    public static final String now() {
        return formatter.format(new Date());
    }
    
    /**
     * Serializes the <tt>chunk</tt> and compresses it optionally.
     * The serialized data is returned as a byte-Array.
     */
    public static final byte[] serialize(Chunk chunk, boolean compress) throws IOException {
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(chunk.getSize());
        
        if (compress) {
            GZIPOutputStream gzip = new GZIPOutputStream(buffer);
            chunk.serialize(gzip);
            gzip.finish();
            gzip.close();
        } else {
            chunk.serialize(buffer);
            buffer.flush();
            buffer.close();
        }
        
        return buffer.toByteArray();
    }
    
    public static final Map parseQuery(String queryString) {
        
        Map map = new HashMap();
        
        if (queryString != null && queryString.length() != 0) {
            StringTokenizer tok = new StringTokenizer(queryString, "&");
            while(tok.hasMoreTokens()) {
                String token = tok.nextToken();
                
                int q = token.indexOf('=');
                if (q != -1 && q != token.length()) {
                    String key = token.substring(0, q);
                    String value = token.substring(++q);
                    map.put(key, value);
                }
            }
        }
        
        return map;
    }
    
    /*private static final ArrayList parseMeta2(String meta) {
        StringTokenizer tok = new StringTokenizer(meta, ",");
        ArrayList list = new ArrayList();
        while(tok.hasMoreTokens()) {
            String token = tok.nextToken();
     
            if (token.equals("dmap.itemkind")) {
                    list.add(0, token);
            } else if (!token.equals("com.apple.itunes.norm-volume")) {
                    list.add(token);
            }
        }
        return list;
    }*/
    
    public static final ArrayList parseMeta(String meta) {
        StringTokenizer tok = new StringTokenizer(meta, ",");
        ArrayList list = new ArrayList();
        while(tok.hasMoreTokens()) {
            String token = tok.nextToken();
            
            //if (token.equals("dmap.itemkind")) {
            //	list.add(0, token);
            //} else if (!token.equals("com.apple.itunes.norm-volume")) {
            list.add(token);
            //}
        }
        return list;
    }
    
    public static Integer createSessionId(Set knownIDs) {
        Integer sessionId = null;
        
        while(sessionId == null || knownIDs.contains(sessionId)) {
            int tmp = generator.nextInt();
            
            if (tmp == 0) {
                continue;
            } else if (tmp < 0) {
                tmp = -tmp;
            }
            
            sessionId = new Integer(tmp);
        }
        
        return sessionId;
    }
    
    public static int toVersion(int major) {
        return toVersion(major, 0, 0);
    }
    
    public static int toVersion(int major, int minor) {
        return toVersion(major, minor, 0);
    }
    
    /**
     * Converts major, minor and patch to a DAAP version.
     * Version 2.0.0 is for example 0x00020000
     */
    public static int toVersion(int major, int minor, int patch) {
        byte[] dst = new byte[4];
        ByteUtil.toByte16BE((major & 0xFFFF), dst, 0);
        dst[2] = (byte)(minor & 0xFF);
        dst[3] = (byte)(patch & 0xFF);
        return ByteUtil.toIntBE(dst, 0);
    }
    
    /**
     * This method tries the determinate the protocol version
     * and returns it or UNDEF_VALUE if version could not be
     * estimated...
     */
    public static int getProtocolVersion(DaapRequest request) {
        
        if (request.isUnknownRequest())
            return DaapUtil.UNDEF_VALUE;
        
        Header header = request.getHeader(CLIENT_DAAP_VERSION);
        
        if (header == null && request.isSongRequest()) {
            header = request.getHeader(USER_AGENT);
        }
        
        if (header == null)
            return DaapUtil.UNDEF_VALUE;
        
        String name = header.getName();
        String value = header.getValue();

        // Unfortunately song requests do not have a Client-DAAP-Version
        // header. As a workaround we can estimate the protocol version
        // by User-Agent but that is weak an may break with non iTunes
        // hosts...
        if ( request.isSongRequest() && name.equals(USER_AGENT)) {
            
            if (value.startsWith("iTunes/4.5"))
                return DaapUtil.VERSION_3;
            else if (value.startsWith("iTunes/4.2") || value.startsWith("iTunes/4.1"))
                return DaapUtil.VERSION_2;
            else if (value.startsWith("iTunes/4.0"))
                return DaapUtil.VERSION_1;
            else
                return DaapUtil.UNDEF_VALUE;
            
        } else {
            
            StringTokenizer tokenizer = new StringTokenizer(value, ".");
            int count = tokenizer.countTokens();
            
            if (count >= 2 && count <= 3) {
                try {

                    int major = DaapUtil.UNDEF_VALUE;
                    int minor = DaapUtil.UNDEF_VALUE;
                    int patch = DaapUtil.UNDEF_VALUE;

                    major = Integer.parseInt(tokenizer.nextToken());
                    minor = Integer.parseInt(tokenizer.nextToken());

                    if (count == 3)
                        patch = Integer.parseInt(tokenizer.nextToken());

                    return DaapUtil.toVersion(major, minor, patch);

                } catch (NumberFormatException err) {
                }
            }
        }
        
        return DaapUtil.UNDEF_VALUE;
    }
    
    // Creates the ChunkClasses.java file...
   /*public static void main(String[] args) throws IOException {
    
        final String name = "ChunkClasses";
        final String javaname = name + ".java";
        final String pakage = "de.kapsi.net.daap.chunks.impl";
    
        File fin = new File(pakage.replace('.', '/'));
    
        if (!fin.exists() || fin.isFile()) {
            throw new IOException();
        }
    
        File fout = (new File(fin, name + ".java")).getParentFile();
    
        BufferedWriter out = new BufferedWriter(new FileWriter(fout));
        StringBuffer buffer = new StringBuffer();
    
        buffer.append("// This class is machine-made!").append("\n\n");
        buffer.append("package ").append(pakage).append(";\n\n");
        buffer.append("public final class ").append(name).append(" {\n");
        buffer.append("\tpublic static final String[] names = {\n");
    
        String[] list = fin.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".java") && !javaname.equals(name);
            }
        });
    
        for(int i = 0; i < list.length; i++) {
            buffer.append("\t\t").append("\"");
    
            String clazz = list[i];
            int q = clazz.lastIndexOf(".");
    
            buffer.append(pakage + "." + clazz.substring(0, q));
            buffer.append("\"");
    
            if (i < list.length-1) {
                buffer.append(",");
            }
    
            buffer.append("\n");
        }
        buffer.append("\t};\n");
        buffer.append("}\n");
    
        out.write(buffer.toString());
        out.close();
    }*/
}
