package com.limegroup.gnutella;

import java.io.File;

import com.limegroup.gnutella.chat.Chatter;
import com.limegroup.gnutella.search.HostData;
import com.limegroup.gnutella.security.User;

import com.sun.java.util.collections.Set;

/**
 *  Callback to notify the GUI of asynchronous backend events.
 *  The methods in this fall into the following categories:
 *
 *  <ul>
 *  <li>Query replies (for displaying results) and query strings 
 *     (for the monitor)
 *  <li>Update in shared file statistics
 *  <li>Change of connection state
 *  <li>New or dead uploads or downloads
 *  <li>New chat requests and chat messages
 *  <li>Error messages
 *  </ul>
 */
public interface ActivityCallback
{
    /**
     *  Add a new unitialized connection.
     */
    public void connectionInitializing(Connection c);

    /**
     *  Mark a connection as initialized
     */
    public void connectionInitialized(Connection c);

    /**
     *  Mark a connection as closed
     */
    public void connectionClosed(Connection c);

    /**
     * Notifies the UI that a new query result has come in to the backend.
     * 
     * @param rfd the descriptor for the remote file
     * @param data the data for the host returning the result
     * @param locs the <tt>Set</tt> of alternate locations for the file
     */
	public void handleQueryResult(RemoteFileDesc rfd, HostData data, Set locs);

    /**
     * Add a query string to the monitor screen
     */
    public void handleQueryString( String query );

    /** Add a file to the download window */
    public void addDownload(Downloader d);

    /** Remove a downloader from the download window. */
    public void removeDownload(Downloader d);

    /** Add an uploader to the upload window */
    public void addUpload(Uploader u);

    /** Remove an uploader from the upload window. */
    public void removeUpload(Uploader u);    

	/** Add a new incoming chat connection */
	public void acceptChat(Chatter ctr);

    /** A new message is available from the given chatter */
	public void receiveMessage(Chatter chr);

	/** The given chatter is no longer available */
	public void chatUnavailable(Chatter chatter);

	/** display an error message in the chat gui */
	public void chatErrorMessage(Chatter chatter, String str);

    /** display an error message since the browse host failed. 
     *  @param guid The GUID of the browse host.
     */    
    public void browseHostFailed(GUID guid);

    /**
     * A new version of LimeWire is available, notify user.
     */
    public void notifyUserAboutUpdate(String message, boolean isPro, boolean l);
    
    /**
     * Notifies a user that the filemanager has completely loaded.
     */
    public void fileManagerLoaded();


    /**
     * Notifies the GUI that the given directory has been shared.  This method
     * is called exactly once per directory per change to the shared directory
     * and extension settings.  Note that the files in directory are not
     * necessarily yet indexed at the time of this call.
     *
     * @param directory MUST be a directory
     * @param parent MUST be the parent of directory, or null if no parent
     *  exists. 
     */
    public void addSharedDirectory(final File directory, final File parent);

    /**
     * Notifies the GUI that the given file has been shared.  This method is
     * called exactly once per file per change to the shared directory and
     * extension settings.
     *
     * @param file MUST be a file descriptor for the file.
     * @param parent MUST be the parent of directory, or null if no parent
     *  exists. 
     */
    public void addSharedFile(final FileDesc file, final File parent);
    
    /**
     * Notifies the GUI when a FileDesc was either added, removed, 
     * changed or renamed. This event is triggered by FileManager
     * or MetaFileManager.
     */
    public void handleFileManagerEvent(FileManagerEvent evt);
    
    /**
     * Notifies the GUI that the given shared file has new information.
     *
     * @param file The File that needs updating
     */    
    public void handleSharedFileUpdate(File file);

	/**
	 * The list of shared files has been emptied.
	 */
	public void clearSharedFiles();           


	/**
	 * Sets the enabled/disabled state of file annotation.
	 */
	public void setAnnotateEnabled(boolean enabled);

     /** 
      * Notifies the GUI that all active downloads have been completed.
      */   
    public void downloadsComplete();
    
    /** 
     * Notifies the GUI that all active uploads have been completed.
     */  
    public void uploadsComplete();


    //authentication callbacks
    /**
     * Asks user to authenticate, and returns the information received from
     * user
     * @param host The host who is requesting authentication
     * @return The authentication information input by user
     */
    public User getUserAuthenticationInfo(String host);
    
    /**
     * Shows the user a message informing her that a file being downloaded 
     * is corrupt.
     * <p>
     * This method MUST call dloader.discardCorruptDownload(boolean b) 
     * otherwise there will be threads piling up waiting for a notification
     */
    public void promptAboutCorruptDownload(Downloader dloader);

	/**
	 *  Tell the GUI to deiconify.
	 */
	public void restoreApplication();

	/**
	 *  Show active downloads
	 */
	public void showDownloads();

    /**
     * @return true If the guid that maps to a query result screen is still
     * available/viewable to the user.
     */
    public boolean isQueryAlive(GUID guid);


    public String getHostValue(String key);

    public void indicateNewVersion();
    
    /**
     * Indicates a component is loading.
     */
    public void componentLoading(String component);
}
