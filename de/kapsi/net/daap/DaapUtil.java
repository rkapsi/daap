
package de.kapsi.net.daap;

import java.text.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Misc methods and constants
 */
public final class DaapUtil {
	
	private static final Log LOG = LogFactory.getLog(DaapUtil.class);
	
	private final static SimpleDateFormat formatter 
		= new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss z", Locale.US);
	
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
			//"com.apple.itunes.norm-volume", // DO NOT ENABLE!
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
			//"com.apple.itunes.smart-playlist",
			"dmap.containeritemid",
			"daap.songdataurl"
		};
		
	public static final String[] DATABASE_PLAYLISTS_META = {
			"dmap.itemid",
			"dmap.itemname",
			"dmap.persistentid"//,
			//"com.apple.itunes.smart-playlist"
		};
	
	public static final String[] PLAYLIST_SONGS_META = {
			"dmap.itemkind",
			"dmap.itemid",
			"dmap.containeritemid"
		};
	
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
	
	public static final String now() {
		return formatter.format(new Date());
	}
	
	public static final byte[] serialize(Chunk chunk, boolean compress) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
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
	
	/*public static final void dump(String file, byte[] bytes) throws IOException {
		FileOutputStream out = new FileOutputStream(new File(file));
		out.write(bytes, 0, bytes.length);
		out.close();
	}
	
	public static final void dumpungz(String file, byte[] bytes) throws IOException {
		GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bytes));
		byte[] dst = new byte[4096];
		int len = -1;
		FileOutputStream out = new FileOutputStream(new File(file));
		
		while((len = in.read(dst, 0, dst.length)) != -1) {
			out.write(dst, 0, len);
		}
		
		in.close();
		out.close();
	}*/
	
	/*public static void main(String[] args) throws IOException {
		
		final String name = "ChunkClasses";
		final String javaname = name + ".java";
		final String pakage = "de.kapsi.net.daap.chunks";
		
		File fin = new File(pakage.replace('.', '/'));
		
		if (!fin.exists() || fin.isFile()) {
			throw new IOException();
		}
		
		File fout = new File(fin, name + ".java");
		
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
