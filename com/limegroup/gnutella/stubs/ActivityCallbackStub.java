package com.limegroup.gnutella.stubs;

import java.io.File;

import com.limegroup.gnutella.ActivityCallback;
import com.limegroup.gnutella.Connection;
import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.Endpoint;
import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.FileManagerEvent;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.Uploader;
import com.limegroup.gnutella.chat.Chatter;
import com.limegroup.gnutella.search.HostData;
import com.limegroup.gnutella.security.User;
import com.sun.java.util.collections.Set;

/**
 * A stub for ActivityCallback.  Does nothing.
 */
public class ActivityCallbackStub implements ActivityCallback {
    
    //don't delete corrupt file on detection
    public static boolean delCorrupt = false;
    //if corruptness was queried
    public static boolean corruptChecked = false;

    public void componentLoading(String component) {}
    public void connectionInitializing(Connection c) { }
    public void connectionInitialized(Connection c) { }
    public void connectionClosed(Connection c) { }
    public void knownHost(Endpoint e) { }
    //public void handleQueryReply( QueryReply qr ) { }

	public void handleQueryResult(RemoteFileDesc rfd, 
	                              HostData data,
	                              Set alts) {}
    public void handleQueryString( String query ) { }    
    public void addDownload(Downloader d) { }    
    public void removeDownload(Downloader d) { }    
    public void addUpload(Uploader u) { }
    public void removeUpload(Uploader u) { }    	
	public void acceptChat(Chatter ctr) { }
	public void receiveMessage(Chatter chr) { }	
	public void chatUnavailable(Chatter chatter) { }	
	public void chatErrorMessage(Chatter chatter, String str) { }
    public void addSharedDirectory(final File directory, final File parent) { }
    public void addSharedFile(final FileDesc file, final File parent) { }
	public void clearSharedFiles() { }           
    public void downloadsComplete() { }
    public void uploadsComplete() { }
    public void error(int errorCode) { }
    public void error(int errorCode, Throwable t) { }
    public void error(Throwable t) { }
    public User getUserAuthenticationInfo(String host) { 
        return null;
    }    
    public void promptAboutCorruptDownload(Downloader dloader) {
        corruptChecked = true;
        dloader.discardCorruptDownload(delCorrupt);
    }    
    public void browseHostFailed(GUID guid) {}
	public void restoreApplication() {}
	public void showDownloads() {}
    public void setAnnotateEnabled(boolean enabled) {}
    public String getHostValue(String key) { return null;}
    public void handleSharedFileUpdate(File file) { }
    public void handleFileManagerEvent(FileManagerEvent evt) {}
    public void notifyUserAboutUpdate(String version, boolean isPro, boolean l){
    }
    public void fileManagerLoaded() {}
    public void indicateNewVersion() {}
    public void showError(String message, String messageKey) {}
    public boolean isQueryAlive(GUID guid) {
        return false;
    }
}
