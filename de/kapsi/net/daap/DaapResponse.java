
package de.kapsi.net.daap;

import java.io.*;
import java.util.*;
import org.apache.commons.httpclient.Header;

public class DaapResponse {
	
	public static DaapResponse createResponse(byte[] body, String encoding) {
		
		Header[] header = {
			new Header("Date", DaapUtil.now()),
			new Header("DAAP-Server", "iTunes/4.2 (Mac OS X)"),
			new Header("Content-Type", "application/x-dmap-tagged"),
			new Header("Content-Length", Integer.toString(body.length)),
			new Header("Content-Encoding", encoding)
		};
		
		return (new DaapResponse("HTTP/1.1 200 OK", header, body));
	}
	
	public static DaapResponse createResponse(int bodyLength, String encoding) {
		
		Header[] header = {
			new Header("Date", DaapUtil.now()),
			new Header("DAAP-Server", "iTunes/4.2 (Mac OS X)"),
			new Header("Content-Type", "application/x-dmap-tagged"),
			new Header("Content-Length", Integer.toString(bodyLength)),
			new Header("Content-Encoding", encoding)
		};
		
		return (new DaapResponse("HTTP/1.1 200 OK", header, null));
	}
	
	public static DaapResponse createResponse(ChunkSerializer chunk) throws IOException {
		byte[] body = DaapUtil.serialize(chunk);
		return createResponse(body, "gzip");
	}
	
	public static DaapResponse createAuthResponse() {
		
		Header[] header = {
			new Header("Date", DaapUtil.now()),
			new Header("DAAP-Server", "iTunes/4.2 (Mac OS X)"),
			new Header("Content-Type", "text/html"),
			new Header("Content-Length", "0"),
			new Header("WWW-Authenticate", "Basic-realm=\"daap\"")
		};
		
		return (new DaapResponse("HTTP/1.1 401 Authorization Required", header, null));
	}
	
	public static DaapResponse createAudioResponse(int length) {
		
		Header[] header = {
			new Header("Date", DaapUtil.now()),
			new Header("DAAP-Server", "iTunes/4.2 (Mac OS X)"),
			new Header("Content-Type", "application/x-dmap-tagged"),
			new Header("Content-Length", Integer.toString(length)),
			new Header("Accept-Ranges", "bytes")
		};
			
		return (new DaapResponse("HTTP/1.1 200 OK", header, null));
	}
	
	private String statusLine;
	private Header[] header;
	private byte[] body;
	
	public DaapResponse(String statusLine, Header[] header, byte[] body) {
		this.statusLine = statusLine;
		this.header = header;
		this.body = body;
	}
	
	public String getStatusLine() {
		return statusLine;
	}
	
	public boolean processRequest(DaapConnection conn) throws IOException {
		
		ResponseWriter out = conn.getWriter();
		
		out.println(statusLine);
		for(int i = 0; i < header.length; i++) {
			out.print(header[i].toExternalForm());
		}
		out.println();
		
		if (body != null) {
			out.write(body, 0, body.length);
		}
		
		out.flush();
		
		conn.connectionKeepAlive();
		return true;
	}
	
	public void processAudioRequest(DaapConnection conn) throws IOException {
		
		ResponseWriter out = conn.getWriter();
		
		out.println(statusLine);
		for(int i = 0; i < header.length; i++) {
			out.print(header[i].toExternalForm());
		}
		out.println();
		out.flush();
	}
}
