package com.limegroup.gnutella.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Locale;
import javax.swing.SwingUtilities;

import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.ErrorService;
import com.limegroup.gnutella.util.CommonUtils;
import com.limegroup.gnutella.util.FileUtils;
import com.limegroup.gnutella.settings.iTunesSettings;
import com.limegroup.gnutella.settings.SharingSettings;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.jmdns.*;
import de.kapsi.net.daap.*;
import org.apache.commons.httpclient.*;
import com.sun.java.util.collections.*;
import com.limegroup.gnutella.*;
import com.limegroup.gnutella.xml.*;
import java.io.*;

public final class DaapMediator implements DaapAuthenticator, DaapAudioStream {
    
	private static final Log LOG = LogFactory.getLog(DaapMediator.class);
        
	private static final DaapMediator INSTANCE = new DaapMediator();
    
	public static DaapMediator instance() {
		return INSTANCE;
	}
	
	private Map songToDesc;
	private Map descToSong;
	
	private Library library;
	
	private DaapServer server;
	private JmDNS zeroConf;
	private ServiceInfo serviceInfo;
	
	private SongUpdateThread updateThread;
	
    private DaapMediator() {
	}
    
	private static boolean check() {
		return ( ! CommonUtils.isJava13OrLater() 
				|| ! iTunesSettings.DAAP_SUPPORT_ENABLED.getValue());
	}
	
	public void startServer() throws IOException {
	
		if (check()) {
			return;
		}
		
		if (server == null) {
			
			songToDesc = new HashMap();
			descToSong = new HashMap();
			
			library = new Library(iTunesSettings.DAAP_SERVICE_NAME.getValue());
			
			int port = iTunesSettings.DAAP_PORT.getValue();
			server = new DaapServer(library, port);
			server.setAuthenticator(this);
			server.setAudioStream(this);
			server.start();
			
			updateThread = new SongUpdateThread();
			updateThread.start();
		}
	}
	
	public void stopServer() throws IOException {
		
		if (check()) {
			return;
		}
		
		if (server != null) {
		
			updateThread.stopThread();
			while(updateThread.isAlive());
			
			updateThread = null;
			
			server.stop();
			server = null;
			
			library = null;
			
			songToDesc.clear();
			songToDesc = null;
			
			descToSong.clear();
			descToSong = null;
		}
	}
	
	public void registerService() throws IOException {
		
		if (check()) {
			return;
		}
		
		if (zeroConf == null && library != null) {
			zeroConf = new JmDNS();
			
			String type = iTunesSettings.DAAP_TYPE_NAME.getValue();
			String name = iTunesSettings.DAAP_SERVICE_NAME.getValue() + "." + type;
			String description = iTunesSettings.DAAP_SERVICE_DESCRIPTION.getValue();
			
			int port = iTunesSettings.DAAP_PORT.getValue();
			int weight = iTunesSettings.DAAP_WEIGHT.getValue();
			int priority = iTunesSettings.DAAP_PRIORITY.getValue();
			
			//"_daap._tcp.local.", "LimeWire._daap._tcp.local."
			serviceInfo = new ServiceInfo(type, name, port, weight, priority, description);
			zeroConf.registerService(serviceInfo);
		}
	}
	
	public void unregisterService() {
		
		if (check()) {
			return;
		}
		
		if (zeroConf != null) {
			zeroConf.unregisterService(serviceInfo);
			zeroConf.close();
			zeroConf = null;
			serviceInfo = null;
		}
	}
	
	public boolean isServerRunning() {
		if (server != null) {
			return server.isRunning();
		}
		return false;
	}
	
	public boolean requiresAuthentication() {
		return iTunesSettings.DAAP_REQUIRES_PASSWORD.getValue();
	}
	
	// username is don't care (not supported by iTunes)
	public boolean authenticate(String username, String password) {
		if (password != null) {
			return password.equals(iTunesSettings.DAAP_PASSWORD.getValue());
		}
		return false;
	}
	
	public void stream(Song song, OutputStream out, int begin, int length) 
		throws IOException {
		
		FileDesc fileDesc = (FileDesc)songToDesc.get(song);
		
		if (fileDesc != null) {
			
			File file = fileDesc.getFile();
			
			BufferedInputStream in = null;
			
			try {
				
				in = new BufferedInputStream(new FileInputStream(file));
				byte[] buffer = new byte[4069*16];
				
				int total = 0;
				int len = -1;
				
				if (begin != 0) {
					in.skip(begin);
				}
				
				while((len = in.read(buffer, 0, buffer.length)) != -1 && total < length) {
					out.write(buffer, 0, len);
					
					total += len;
				}
				
				out.flush();
				in.close();
				in = null;
				
			} finally {
				if (in != null) {
					in.close();
				}
			}
		}
	}
	
	/**
     * Returns true if the extension of name is a supported file type.
     */
    private static boolean isSupported(String name) {
        String[] types = iTunesSettings.ITUNES_SUPPORTED_FILE_TYPES.getValue();        
        for(int i = 0; i < types.length; i++)
            if (name.endsWith(types[i]))
                return true;
        return false;
    }
	
	private class SongUpdateThread extends Thread {
		
		private boolean run = false;
		
		private HashSet newDescs = new HashSet();
		private HashSet deletedDescs = new HashSet();
		
		public SongUpdateThread() {
			super("SongUpdateThread");
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY+2);
		}
		
		public void run() {
			try {
				while(!Thread.interrupted() && run) {
					
					FileManager fileManager = RouterService.getFileManager();
					FileDesc[] descs = fileManager.getAllSharedFileDescriptors();
					checkForNewFileDescs(descs);
					checkForDeletedFileDescs(descs);
					
					updateLibrary();
					server.update();
					
					newDescs.clear();
					deletedDescs.clear();
					
					if (run) {
						Thread.sleep(10*1000); // 10 seconds
					}
				}
			} catch (InterruptedException err) {
				LOG.error(err);
			}
		}
		
		private void checkForNewFileDescs(FileDesc[] descs) {
			for(int i = 0; i < descs.length; i++) {
				if (descToSong.containsKey(descs[i]) == false) {
					if (isSupported(descs[i].getName().toLowerCase(Locale.US))) {
						newDescs.add(descs[i]);
					}
				}
			}
		}
		
		private void checkForDeletedFileDescs(FileDesc[] descs) {
	
			Iterator it = descToSong.keySet().iterator();
			while(it.hasNext()) {
				FileDesc desc = (FileDesc)it.next();
				boolean contains = false;
				
				for(int i = 0; i < descs.length; i++) {
					if (desc == descs[i]) {
						contains = true;
						break;
					}
				}
				
				if (!contains) {
					deletedDescs.add(desc);
				}
			}
		}
		
		private void updateLibrary() {
			
			library.open();
			
			String name = iTunesSettings.DAAP_SERVICE_NAME.getValue();
			if (name != null && name.length() != 0 && !library.getName().equals(name)) {
				library.setName(name);
			}
			
			if (newDescs.size() != 0 || deletedDescs.size() != 0) {
				
				Iterator it = newDescs.iterator();
				while(it.hasNext()) {
					FileDesc desc = (FileDesc)it.next();
					
					if (!(desc instanceof IncompleteFileDesc)) {
						Song song = new Song(desc.getName());
						song.setSize((int)desc.getSize());
						
						library.addSong(song);
						
						songToDesc.put(song, desc);
						descToSong.put(desc, song);
					}
				}
				
				it = deletedDescs.iterator();
				while(it.hasNext()) {
					FileDesc desc = (FileDesc)it.next();
					Song song = (Song)descToSong.remove(desc);
					if (song != null) {
						songToDesc.remove(song);
						library.removeSong(song);
					}
				}
			}
			
			Iterator it = songToDesc.keySet().iterator();
			while(it.hasNext()) {
				Song song = (Song)it.next();
				FileDesc desc = (FileDesc)songToDesc.get(song);
				
				updateSong(song, desc);
			}
			
			library.close();
		}
		
		private void updateSong(Song song, FileDesc desc) {
		
			com.sun.java.util.collections.List docs = desc.getLimeXMLDocuments();
			
			boolean update = false;
			
			String title = null;
			String track = null;
			String artist = null;
			String album = null;
			String genre = null;
			String bitrate = null;
			String comments = null;
			String time = null;
			String year = null;
			
			for(int j = 0; j < docs.size(); j++) {
				LimeXMLDocument doc = (LimeXMLDocument)docs.get(j);
				
				if (title == null)
					title = doc.getValue("audios__audio__title__");
					
				if (track == null) 
					track = doc.getValue("audios__audio__track__");
					
				if (artist == null)
					artist = doc.getValue("audios__audio__artist__");
				
				if (album == null)
					album = doc.getValue("audios__audio__album__");
					
				if (genre == null)
					genre = doc.getValue("audios__audio__genre__");
					
				if (bitrate == null)
					bitrate = doc.getValue("audios__audio__bitrate__");
					
				if (comments == null)
					comments = doc.getValue("audios__audio__comments__");
					
				if (time == null)
					time = doc.getValue("audios__audio__seconds__");
					
				if (year == null) 
					year = doc.getValue("audios__audio__year__");
			}
			
			if (title != null) {
				String currentTitle = song.getName();
				if (currentTitle == null || !title.equals(currentTitle)) {
					update = true;
					song.setName(title);
				}
			}
			
			if (track != null) {
				int num = Integer.parseInt(track);
				if (num != song.getTrackNumber()) {
					update = true;
					song.setTrackNumber(num);
				}
			}
			
			if (artist != null) {
				String currentArtist = song.getArtist();
				if (currentArtist == null || !artist.equals(currentArtist)) {
					update = true;
					song.setArtist(artist);
				}
			}
			
			if (album != null) {
				String currentAlbum = song.getAlbum();
				if (currentAlbum == null || !album.equals(currentAlbum)) {
					update = true;
					song.setAlbum(album);
				}
			}
			
			if (genre != null) {
				String currentGenre = song.getGenre();
				if (currentGenre == null || !genre.equals(currentGenre)) {
					update = true;
					song.setGenre(genre);
				}
			}
			
			if (comments != null) {
				String currentComments = song.getComment();
				if (currentComments == null || !comments.equals(currentComments)) {
					update = true;
					song.setComment(comments);
				}
			}
			
			if (bitrate != null) {
				int num = Integer.parseInt(bitrate);
				if (num != song.getBitrate()) {
					update = true;
					song.setBitrate(num);
				}
			}
			
			if (time != null) {
				int num = (int)Integer.parseInt(time)*1000;
				if (num != song.getTime()) {
					update = true;
					song.setTime(num);
				}
			}
			
			if (year != null) {
				int num = Integer.parseInt(year);
				if (num != song.getYear()) {
					update = true;
					song.setYear(num);
				}
			}
			
			int mod = (int)(desc.lastModified()/1000);
			if (song.getDateModified() != mod) {
				update = true;
				song.setDateModified(mod);
			}
			
			if (update) {
				song.update();
			}
		}
		
		public void start() {
			run = true;
			super.start();
		}
		
		public void stopThread() {
			run = false;
		}
	}
}
