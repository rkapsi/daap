
package de.kapsi.net.daap;

import java.io.*;

public interface DaapAudioStream {
	
	public void stream(Song song, OutputStream out, int start, int length) throws IOException;
}
