
package de.kapsi.net.daap;

import java.util.StringTokenizer;

public class PathTokenizer extends StringTokenizer {
	
	public PathTokenizer(String path) {
		super(path, "/");
	}
	
	public String tail() {
		StringBuffer buffer = new StringBuffer();
		
		while(hasMoreTokens()) {
			buffer.append('/').append(nextToken());
		}
		
		return buffer.toString();
	}
}
