package com.limegroup.gnutella.gui;

import com.sun.java.util.collections.*;
import javax.swing.*;
import java.io.*;
import com.limegroup.gnutella.settings.QuestionsHandler;
import com.limegroup.gnutella.*;
import com.limegroup.gnutella.gui.library.LibraryMediator;
import com.limegroup.gnutella.gui.search.SearchMediator;
import com.limegroup.gnutella.gui.tables.ComponentMediator;
import com.limegroup.gnutella.gui.chat.*;
import com.limegroup.gnutella.chat.*;
import com.limegroup.gnutella.security.User;
import com.limegroup.gnutella.update.gui.*;
import com.limegroup.gnutella.search.*;
import com.limegroup.gnutella.FileManagerEvent;

/**
 * This class is the interface from the backend to the frontend.  It
 * delegates all callbacks to the appropriate frontend classes, and it
 * also handles putting an necessary calls onto the Swing thread.
 * 
 * It implements the <tt>ActivityCallback</tt> callback interface, designed
 * to make it easy to swap UIs.
 */
public final class VisualConnectionCallback implements ActivityCallback {

	/**
	 * Handle to the <tt>GUIMediator</tt> for accessing frontend classes
	 * and for making general calls.
	 */
    private final GUIMediator MEDIATOR = GUIMediator.instance();

    private final String ENGLISH_MESSGE=
        "LimeWire has detected a new version on the network. Please update "+
            "to version ";

	/**
	 * Handle to the class that handles query strings.
	 */
    private final HandleQueryString HANDLE_QUERY_STRING = 
		new HandleQueryString();

	
	/**
	 * Handle to the download window for displaying and updating downloads.
	 */
    private final ComponentMediator  DOWNLOAD_MEDIATOR;

	/**
	 * Handle to the monitor window for displaying searches as they come in.
	 */
	private final MonitorView MONITOR_VIEW;

	/**
	 * Handle to the upload window for displaying and updating uploads.
	 */
    private final ComponentMediator UPLOAD_MEDIATOR;

	/**
	 * Handle to the connection window for displaying and updating connections.
	 */
    private final ComponentMediator CONNECTION_MEDIATOR;

	/**
	 * Handle to the library window to make shared files visible to the user.
	 */
	private final LibraryMediator LIBRARY_MEDIATOR;

    private final iTunesMediator ITUNES_MEDIATOR = iTunesMediator.instance();
    
    private final DaapMediator DAAP_MEDIATOR = DaapMediator.instance();
    
	/**
	 * Sets the references to all of the main gui classes that will handle
	 * the callbacks.
	 */
    VisualConnectionCallback() {
		MainFrame mf = MEDIATOR.getMainFrame();
		DOWNLOAD_MEDIATOR = mf.getDownloadMediator();
		MONITOR_VIEW      = mf.getMonitorView();
		UPLOAD_MEDIATOR   = mf.getUploadMediator();
		CONNECTION_MEDIATOR   = mf.getConnectionMediator();
		LIBRARY_MEDIATOR  = mf.getLibraryMediator();
    }


    /**
     *  Handle a new connection.
     */
    public void connectionInitializing(Connection c)
    {
        Runnable doWorkRunnable = new ConnectionInitializing(c);
        SwingUtilities.invokeLater(doWorkRunnable);
    }

    /**
     *  Change the status of a connection when it's been fully initialized
     */
    public void connectionInitialized(Connection c)
    {
        Runnable doWorkRunnable = new ConnectionInitialized(c);
        SwingUtilities.invokeLater(doWorkRunnable);
    }

    /**
     *  Handle a removed connection.
     */
    public void connectionClosed(Connection c)
    {
        Runnable doWorkRunnable = new ConnectionClosed(c);
        SwingUtilities.invokeLater(doWorkRunnable);
    }


    /**
     *  Add a query reply to a query screen
     */
    public void handleQueryResult(final RemoteFileDesc rfd,
                                  final HostData data,
                                  final Set locs) 
    {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    try {
				    SearchMediator.handleQueryResult(rfd, data, locs);
				} catch(Throwable e) {
				    GUIMediator.showInternalError(e, "handleQueryResult");
				} 
			}
		});
    }

    /**
     *  Add a query string to the monitor screen
     */
    public void handleQueryString( String query )
    {
        HANDLE_QUERY_STRING.addQueryString(query);
    }
    
    /**
     * File manager finished loading.
     */
    public void fileManagerLoaded() {
        // does nothing now.
    }

    public void addSharedDirectory(final File dir, final File parent) 
    {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    try {
				    LIBRARY_MEDIATOR.addSharedDirectory(dir, parent);
				} catch(Throwable e) {
				    GUIMediator.showInternalError(e, "addSharedDirectory");
				} 
			}
		});
    }

    /**
     * NOTE:  We have to share the FileDesc if we want to be able to 
     *        to display any qualities of the shared file
     * Things that could be displayed range from...
     *   - number of alternate locations
     *   - number of hits
     *   - number of attempted uploads
     *   - number of completed uploads
     *   - average D/L time for this file
     *   - etc...
     * Of course, the FileDesc must support this.
     **/     
    public void addSharedFile(final FileDesc file, final File parent) 
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    try {
				    LIBRARY_MEDIATOR.addSharedFile(file, parent);
				} catch(Throwable e) {
				    GUIMediator.showInternalError(e, "addSharedFile");
				}
			}
		});
    }
    
	/** 
	 * This method notifies the frontend that the data for the 
	 * specified shared <tt>File</tt> instance has been 
	 * updated.
	 *
	 * @param file the <tt>File</tt> instance for the shared file whose
	 *  data has been updated
	 */
    public void handleSharedFileUpdate(final File file)
    {
        /**
         * NOTE: Pass this off directly to the library
         * so it can discard the update if the directory
         * of the file isn't selected.
         * This reduces the amount of Runnables created
         * by a very large amount.
         */
         LIBRARY_MEDIATOR.updateSharedFile(file);
    }
        
    public void handleFileManagerEvent(FileManagerEvent evt) {
        DAAP_MEDIATOR.handleFileManagerEvent(evt);
    }
    
	public void clearSharedFiles() 
	{
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            try {
		            LIBRARY_MEDIATOR.clearLibrary();
		        } catch(Throwable e) {
		            GUIMediator.showInternalError(e, "clearSharedFiles");
		        }
		    }
		 });
	}

	public void setAnnotateEnabled(final boolean enabled) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					LIBRARY_MEDIATOR.setAnnotateEnabled(enabled);
				} catch(Throwable e) {
					GUIMediator.showInternalError(e, "setAnnotateEnabled");
				}
			}
		});
        
        DAAP_MEDIATOR.setAnnotateEnabled(enabled);
	}


    public void addDownload(Downloader mgr)
    {
        Runnable doWorkRunnable = new AddDownload(mgr);
        SwingUtilities.invokeLater(doWorkRunnable);
    }

    public void removeDownload(Downloader mgr)
    {
        Runnable doWorkRunnable = new RemoveDownload(mgr);
        SwingUtilities.invokeLater(doWorkRunnable);
        
        if (mgr.getState()==Downloader.COMPLETE) {
            ITUNES_MEDIATOR.handleCompleteDownload(mgr);
        }
    }
    
    public void downloadsComplete() {
        Finalizer.setDownloadsComplete();
    }

    public void addUpload(Uploader mgr)
    {
        Runnable doWorkRunnable = new AddUpload(mgr);
        SwingUtilities.invokeLater(doWorkRunnable);
    }

    public void removeUpload(Uploader mgr)
    {
        Runnable doWorkRunnable = new RemoveUpload(mgr);
        SwingUtilities.invokeLater(doWorkRunnable);
    }
    
    public void uploadsComplete() 
    {
        Finalizer.setUploadsComplete();
    }
	
    /**
     * called by backend when it realizes a new version is available
     */ 
    public void notifyUserAboutUpdate(final String m,
        final boolean isPro, final boolean loc) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GUIMediator.instance().showUpdateNotification(false);//no blink
                String message;
                if(loc) //use locale?
                    //note: If there is no value for UPDATE_MESSGE in the user's
                    //lanaguage, we could end up with the string being half in 
                    //English in this case
                    message =
                        GUIMediator.getStringResource("UPDATE_MESSAGE")+" "+m;
                else
                    message = ENGLISH_MESSGE+m;
                boolean update = 
        			UpdateCallback.showUpdatePromptWithParentFrame(message);
                if(update) {
                    String url = isPro ?
                        "http://www.limewire.com/update" :
                        "http://www.limewire.com/download";
                    //open a browser with the url
                    try {
                        GUIMediator.openURL(url);
                    } catch(IOException ignored) {
                        // TODO:: display a message the user??
                    }
                }
            }
        }); 
    }
    
    /**
     *  Handle a new connection.
     */
    private class ConnectionInitializing implements Runnable
    {
        private Connection  c;
        public ConnectionInitializing(Connection c) {
            this.c      = c;
        }
        public void run() { 
            try {
                CONNECTION_MEDIATOR.add(c); 
            } catch(Throwable e) {
                GUIMediator.showInternalError(e, "ConnectionInitializing");    
            }
        }
    }

    /**
     *  Change the status of a connection when it's been fully initialized
     */
    private class ConnectionInitialized implements Runnable
    {
        private Connection  c;
        public ConnectionInitialized(Connection c) {
            this.c = c;
        }
        public void run() { 
            try {
			    CONNECTION_MEDIATOR.update(c);
			    //MEDIATOR.setConnected(true);
			} catch(Throwable e) {
                GUIMediator.showInternalError(e, "ConnectionInitialized");
			}
		}
    }

    /**
     *  Handle a removed connection.
     */
    private class ConnectionClosed implements Runnable
    {
        private Connection  c;
        public ConnectionClosed(Connection c) {
            this.c = c;
        }
        public void run() { 
            try {
			    CONNECTION_MEDIATOR.remove(c);
			} catch(Throwable e) {
                GUIMediator.showInternalError(e, "ConnectionClosed");
			}
		}
    }


    /**
     *  Add a query string to the monitor screen
     */
    private class HandleQueryString implements Runnable 
	{
        private Vector  list;
        private boolean active;

        public HandleQueryString( ) {
            list   = new Vector();
            active = false;
        }

        public void addQueryString(String query) {
            list.add(query);
            if(active == false) {
                active = true;
                SwingUtilities.invokeLater(this);
            }
        }

        public void run() {
            try {
                String query;
                while (list.size() > 0) {
                    query = (String) list.elementAt(0);
                    list.remove(0);
				    MONITOR_VIEW.handleQueryString(query);
                }
                active = false;
             } catch(Throwable e) {
                GUIMediator.showInternalError(e, "HandleQueryString");
			 }
        }
    }

    private class AddDownload implements Runnable
    {
        private Downloader mgr;
        public AddDownload(Downloader mgr) {
            this.mgr = mgr;
        }
        public void run() {
            try {
                DOWNLOAD_MEDIATOR.add(mgr);
            } catch(Throwable e) {
                GUIMediator.showInternalError(e, "AddDownload");
			}
		}
    }

    private class RemoveDownload implements Runnable
    {
        private Downloader mgr;
        public RemoveDownload( Downloader mgr ) {
            this.mgr = mgr;
        }
        public void run() {
            try {
                DOWNLOAD_MEDIATOR.remove(mgr);
                LIBRARY_MEDIATOR.refreshIfIncomplete();
                SearchMediator.updateResults();
            } catch(Throwable e) {
                GUIMediator.showInternalError(e, "RemoveDownload");
			}
	    }
            
    }

    private class AddUpload implements Runnable
    {
        private Uploader up;
        public AddUpload( Uploader up ) {
            this.up = up;
        }
        public void run() {
            try {
                UPLOAD_MEDIATOR.add(up);
            } catch(Throwable e) {
                GUIMediator.showInternalError(e, "AddUpload");
			}
		}
    }

    private class RemoveUpload implements Runnable
    {
        private Uploader mgr;
        public RemoveUpload( Uploader mgr ) {
            this.mgr = mgr;
        }
        public void run() {
            try {
                UPLOAD_MEDIATOR.remove(mgr);
            } catch(Throwable e) {
                GUIMediator.showInternalError(e, "RemoveUpload");
			}
	    }
    }

	/** 
	 * Adds a new chat session, encapsulated in the specified 
	 * <tt>Chatter</tt> instance.
	 *
	 * @param chatter the <tt>Chatter</tt> instance that provides all
	 *  data access regarding the chat session
	 */
	public void acceptChat(final Chatter chatter) {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            try {
		            ChatUIManager.instance().acceptChat(chatter);
		        } catch(Throwable e) {
                    GUIMediator.showInternalError(e, "acceptChat");
			    }
			}
	    });
	}
	
	/**
	 * Receives a new chat message for a specific <tt>Chatter</tt>
	 * instance.
	 * 
	 * @param chatter the <tt>Chatter</tt> instance that is receiving
	 *  a new message
	 */
	public void receiveMessage(final Chatter chatter) {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            try {
		            ChatUIManager.instance().recieveMessage(chatter);
		        } catch(Throwable e) {
                    GUIMediator.showInternalError(e, "receiveMessage");
			    }
			}
	    });
	}

	/** 
	 * Specifies that the given chat host is no longer available, thereby
	 * ending the chat session.
	 *
	 * @param chatter the <tt>Chatter</tt> instance for the chat session
	 *  that is terminating 
	 */
	public void chatUnavailable(final Chatter chatter) {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            try {
		            ChatUIManager.instance().chatUnavailable(chatter);
		        } catch(Throwable e) {
                    GUIMediator.showInternalError(e, "chatUnavailable");
			    }
			}
	    });
	}

	/** 
	 * Display an error message for the specified chat session.
	 *
	 * @param chatter the <tt>Chatter</tt> instance to show an error for
	 * @param str the error to display
	 */
	public void chatErrorMessage(final Chatter chatter, final String str) {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            try {
		            ChatUIManager.instance().chatErrorMessage(chatter, str);
		        } catch(Throwable e) {
                    GUIMediator.showInternalError(e, "chatErrorMessage");
			    }
			}
	    });
	}


    /**
     * Display an error message for a ResultPanel (if it still exists)
     * @param guid The GUID of the ResultPanel.
     */
    public void browseHostFailed(final GUID guid) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
                SearchMediator.browseHostFailed(guid);
			}
		});
    }

    //Authentication callbacks
    /**
     * Asks user to authenticate, and returns the information received from
     * user
     * @param host The host who is requesting authentication
     * @return The authentication information input by user
     */
    public User getUserAuthenticationInfo(String host){
        //a lock object on which to receive notification, once user
        //inputs the username/password
        Object lock = new Object();
        //create a runnable object that will get the authentication input
        //from the user
        PasswordDialogRunnable passDialog 
            = new PasswordDialogRunnable(host,lock);
        //let it be run in the Swing thread
        SwingUtilities.invokeLater(passDialog);
        //wait to receive notification
        synchronized(lock){
            while(passDialog.notFinished()){
                try{
                    lock.wait();
                }catch(InterruptedException ie){
                    //do nothing
                }
            }
        }
        //once the notification is received, return the authentication
        //information to the caller
        return passDialog.getUser();
    }

    /**
     * Shows the user a message informing her that a file being downloaded 
     * is corrupt.
     * <p>
     * This method MUST call dloader.discardCorruptDownload(boolean b) 
     * otherwise there will be threads piling up waiting for a notification
     */
    public void promptAboutCorruptDownload(Downloader downloader) {    
        final Downloader dloader = downloader;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int resp=GUIMediator.showYesNoMessage(
                              "MESSAGE_FILE_CORRUPT",
                              dloader.getFileName(),
                              "MESSAGE_CONTINUE_DOWNLOAD",
                              QuestionsHandler.CORRUPT_DOWNLOAD);
                    
                // discard if they didn't want to save.                              
                dloader.discardCorruptDownload(
                    resp == MessageService.NO_OPTION);
            }
        });
    }

	/**
	 *  Tell the GUI to deiconify.
	 */  
	public void restoreApplication() {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
		        GUIMediator.restoreView();
            }
        });
		    
	}
	
	/**
	 * Notification of a component loading.
	 */
	public void componentLoading(String component) {
        GUIMediator.setSplashScreenString(
            GUIMediator.getStringResource("SPLASH_STATUS_COMPONENT_LOADING_" +
                                          component));
    }       

	/**
	 *  Show active downloads
	 */
	public void showDownloads() {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
		        GUIMediator.instance().setWindow(GUIMediator.SEARCH_INDEX);	
            }
        });
	}	
    
    /**
     * @return true if the guid is still viewable to the user, else false.
     */
    public boolean isQueryAlive(GUID guid) {
        return SearchMediator.queryIsAlive(guid);
    }

    public void indicateNewVersion() {
        GUIMediator.instance().showUpdateNotification(true);//blinking
    }
	

    public String getHostValue(String key) {
        return GUIMediator.getStringResource(key);
    }

    /**
     * A runnable form of PasswordDialog class
     */
    private class PasswordDialogRunnable implements Runnable {
        
        /** The title for the Password Dialog box*/
        private String _title;
        /** 
         * The object to notify when the authentication
         * information is received from the user
         */
        private Object _lock;
        
        /**
         * a flag indicating whether we finished getting input from user
         */
        private volatile boolean _notFinished = true;
        
        /** Dialog box to display to user */
        private PasswordDialog _passwordDialog;
        
        /** Flag indicating if user pressed 'cancel' in the dialog box */
        private volatile boolean _cancelled = false;
        
        /**
         * Creates a new instance of this class
         * @param title The title for the Password Dialog box
         * @param lock The object to notify when the authentication
         * information is received from the user
         */
        public PasswordDialogRunnable(String title, Object lock) {
            //initialize member fields
            this._title = title;
            this._lock = lock;
            //create the password dialog box
            _passwordDialog = new PasswordDialog(title);
        }

        public void run() {
            try {
                //show the dialog box to user
                int returnCode = MEDIATOR.showPasswordDialog(_passwordDialog);
                //set the cancel flag, based upon user action
                if(returnCode == PasswordDialog.CANCELLED)
                    _cancelled = true;
                //notify the threads waiting for this action
                synchronized(_lock){
                    //unset the not-finished flag
                    _notFinished = false;
                    //notify any thread waiting for this event to occur
                    _lock.notifyAll();
                }
            } catch(Throwable e) {
                GUIMediator.showInternalError(e, "PasswordDialog");
            }
        }
        
        /**
         * Tells if we have already received input from the user or not.
         * @return true, if we have received input from the user, false
         * otherwise
         */
        public boolean notFinished(){
            return _notFinished;
        }
        
        /**
         * Returns the authentication information input by user
         * @return the authentication information input by user
         */
        public User getUser(){
            //if cancelled
            if(_cancelled)
                return new User("","");
            else
            //get information from the dialog box and return
            return new User(_passwordDialog.getUsername(),
                _passwordDialog.getPassword());
        }
    }
}



