
package de.kapsi.net.daap;

import java.io.*;
import java.util.*;

import de.kapsi.net.daap.chunks.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DaapAudioRequestHandler {
	
	private static final Log LOG = LogFactory.getLog(DaapAudioRequestHandler.class);
	
	private Library library;
	private DaapAudioStream audioStream;
	
	public DaapAudioRequestHandler(Library library) {
		this.library = library;
	}
	
	public void setAudioStream(DaapAudioStream audioStream) {
		this.audioStream = audioStream;
	}
	
	public DaapAudioStream getAudioStream() {
		return audioStream;
	}
	
	public boolean processRequest(DaapConnection conn, DaapRequest request) 
		throws IOException {
		
		if (audioStream != null && request.isSongRequest()) {
		
			int[] range = getRange(request);
			
			if (range == null) {
				if (LOG.isInfoEnabled()) {
					LOG.info("getRange returned null");
				}
				return false;
			}
			
			int begin = range[0];
			int end = range[1];
			
			int length = 0;
			
			Song song = (Song)library.select(request);
			
			if (song == null) {
				if (LOG.isInfoEnabled()) {
					LOG.info("Library returned null-Song for request: " + request);
				}
				return false;
			}
			
			if (end == -1) {
				length = song.getSize()-begin;
			} else {
				length = end - begin;
			}
			
			DaapResponse response = DaapResponse.createAudioResponse(length);
			response.processAudioRequest(conn);
			
			//LOG.info("begin: " + begin + ", end: " + end + " => length: " + length);
			
			audioStream.stream(song, conn.getOutputStream(), begin, length);
		}
		
		return false;
	}
	
	private int[] getRange(DaapRequest request) throws IOException {
		
		Header[] headers = request.getHeaders();
		for(int i = 0; i < headers.length; i++) {
			Header header = headers[i];
			if (header.getName().equals("Range")) {
				try {
					StringTokenizer tok = new StringTokenizer(header.getValue(), "=");
					String key = tok.nextToken();
					
					if (key.equals("bytes")==false) { 
						LOG.info("unknown type");
						return null; 
					}
					
					byte[] range = tok.nextToken().getBytes("UTF-8");
					
					int q = 0;
					for(;q<range.length && range[q] != '-';q++);
					
					int begin = Integer.parseInt(new String(range,0,q));
					
					q++;
					int end = -1;
					
					if (range.length-q != 0) {
						end = Integer.parseInt(new String(range,q,range.length-q));
					}
					
					return (new int[]{begin, end});
					
				} catch (NoSuchElementException err) {
					LOG.error(err);
				} catch (NumberFormatException err) {
					LOG.error(err);
				}
			}
		}
		
		return (new int[]{0,-1});
	}
}
