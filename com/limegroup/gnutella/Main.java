package com.limegroup.gnutella;

import java.io.*;
import com.sun.java.util.collections.*;
import com.limegroup.gnutella.chat.*;
import com.limegroup.gnutella.security.User;
import com.limegroup.gnutella.search.*;

/**
 * The command-line UI for the Gnutella servent.
 */
public class Main implements ActivityCallback, ErrorCallback {
    public static void main(String args[]) {
		ActivityCallback callback = new Main();
		//RouterService.setCallback(callback);
		RouterService service = new RouterService(callback);
		service.start();    


		System.out.println("For a command list type help.");
		BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
		for ( ; ;) {
			System.out.print("LimeRouter> ");
			try {
				String command=in.readLine();
				if (command==null)
					break;
				else if (command.equals("help")) {
					System.out.println("catcher                  "+
									   "Print host catcher.");
					System.out.println("connect <host> [<port>]  "+
									   "Connect to a host[:port].");
					System.out.println("help                     "+
									   "Print this message.");
					System.out.println("listen <port>            "+
									   "Set the port you are listening on.");
					//  			System.out.println("push                     "+
					//  			  "Print push routes.");
					System.out.println("query <string>           "+
									   "Send a query to the network.");
					System.out.println("quit                     "+
									   "Quit the application.");
					//  			System.out.println("route                    "+
					//  			  "Print routing tables.");
					//  			System.out.println("stat                     "+
					//  			  "Print statistics.");
					System.out.println("update                   "+
									   "Send pings to update the statistics.");
				}
				else if (command.equals("quit"))
					break;
				//          //Print routing tables
				//          else if (command.equals("route"))
				//              RouterService.dumpRouteTable();
				//          //Print connections
				//          else if (command.equals("push"))
				//              RouterService.dumpPushRouteTable();
				//Print push route
				else if (command.equals("stat")) {
					//              RouterService.dumpConnections();
					System.out.println("Number of hosts: "+
                        RouterService.getNumHosts());
					System.out.println("Number of files: "+
                        RouterService.getNumFiles());
					System.out.println("Size of files: "+
                        RouterService.getTotalFileSize());
				}
				//Send pings to everyone
				else if (command.equals("update"))
					RouterService.updateHorizon();
				String[] commands=split(command);
				//Connect to remote host (establish outgoing connection)
				if (commands.length>=2 && commands[0].equals("connect")) {
					try {
						int port=6346;
						if (commands.length>=3)
							port=Integer.parseInt(commands[2]);
						RouterService.connectToHostBlocking(commands[1], port);
					} catch (IOException e) {
						System.out.println("Couldn't establish connection.");
					} catch (NumberFormatException e) {
						System.out.println("Please specify a valid port.");
					}
				} else if (commands.length>=2 && commands[0].equals("query")) {
					//Get query string from command (possibly multiple words)
					int i=command.indexOf(' ');
					Assert.that(i!=-1 && i<command.length());
					String query=command.substring(i+1);
					RouterService.query(RouterService.newQueryGUID(), query);
				} else if (commands.length==2 && commands[0].equals("listen")) {
					try {
						int port=Integer.parseInt(commands[1]);
						RouterService.setListeningPort(port);
					} catch (NumberFormatException e) {
						System.out.println("Please specify a valid port.");
					} catch (IOException e) {
						System.out.println("Couldn't change port.  Try another value.");
					}
				}
			} catch (IOException e) {
				System.exit(1);
			}
		}
		System.out.println("Good bye.");
		RouterService.shutdown(); //write gnutella.net
    }

    /////////////////////////// ActivityCallback methods //////////////////////

    public void connectionInitializing(Connection c) {
    }

    public void connectionInitialized(Connection c) {
//		String host = c.getOrigHost();
//		int    port = c.getOrigPort();
		;//System.out.println("Connected to "+host+":"+port+".");
    }

    public void connectionClosed(Connection c) {
//		String host = c.getOrigHost();
//		int    port = c.getOrigPort();
		//System.out.println("Connection to "+host+":"+port+" closed.");
    }

    public void knownHost(Endpoint e) {
		//Do nothing.
    }

//     public void handleQueryReply( QueryReply qr ) {
// 		synchronized(System.out) {
// 			System.out.println("Query reply from "+qr.getIP()+":"+qr.getPort()+":");
// 			try {
// 				for (Iterator iter=qr.getResults(); iter.hasNext(); )
// 					System.out.println("   "+((Response)iter.next()).getName());
// 			} catch (BadPacketException e) { }
// 		}
//     }

	public void handleQueryResult(RemoteFileDesc rfd ,HostData data, Set loc) {
		synchronized(System.out) {
			System.out.println("Query hit from "+rfd.getHost()+":"+rfd.getPort()+":");
			System.out.println("   "+rfd.getFileName());
		}
	}

    /**
     *  Add a query string to the monitor screen
     */
    public void handleQueryString( String query ) {
    }


	public void error(int errorCode) {
		error(errorCode, null);
    }
    
    public void error(Throwable problem, String msg) {
        problem.printStackTrace();
        System.out.println(msg);
    }

	/**
	 * Implements ActivityCallback.
	 */
    public void error(Throwable problem) {
		problem.printStackTrace();
	}

    public void error(int message, Throwable t) {
		System.out.println("Error: "+message);
		t.printStackTrace();
    }

    ///////////////////////////////////////////////////////////////////////////


    /** Returns an array of strings containing the words of s, where
     *  a word is any sequence of characters not containing a space.
     */
    public static String[] split(String s) {
		s=s.trim();
		int n=s.length();
		if (n==0)
			return new String[0];
		Vector buf=new Vector();

		//s[i] is the start of the word to add to buf
		//s[j] is just past the end of the word
		for (int i=0; i<n; ) {
			Assert.that(s.charAt(i)!=' ');
			int j=s.indexOf(' ',i+1);
			if (j==-1)
				j=n;
			buf.add(s.substring(i,j));
			//Skip past whitespace (if any) following s[j]
			for (i=j+1; j<n ; ) {
				if (s.charAt(i)!=' ')
					break;
				i++;
			}
		}
		String[] ret=new String[buf.size()];
		for (int i=0; i<ret.length; i++)
			ret[i]=(String)buf.get(i);
		return ret;
    }


    public boolean overwriteFile(String file) {return false;};

    public void addDownload(Downloader mgr) {}

    public void removeDownload(Downloader mgr) {}

    public void addUpload(Uploader mgr) {}

    public void removeUpload(Uploader mgr) {}

    public void setPort(int port){}

    public int getNumUploads(){ return 0; }

	public void addSharedDirectory(File file, File parent) {}

	public void addSharedFile(FileDesc file, File parent) {}
	
	public void handleFileManagerEvent(FileManagerEvent evt) {}
	
	public void handleSharedFileUpdate(File file) {}

	public void clearSharedFiles() {}

	public void acceptChat(Chatter chat) {}

	public void receiveMessage(Chatter chat) {}
	
	public void chatUnavailable(Chatter chatter) {}

	public void chatErrorMessage(Chatter chatter, String st) {}
        
    public void downloadsComplete() {}    
    
    public void fileManagerLoaded() {}    
    
    public User getUserAuthenticationInfo(String host){
        return null;
    }

    public void uploadsComplete() {}

    public void promptAboutCorruptDownload(Downloader dloader) {
        dloader.discardCorruptDownload(false);
    }

	public void restoreApplication() {}

	public void showDownloads() {}

    public String getHostValue(String key){
        return null;
    }
    public void browseHostFailed(GUID guid) {}

	public void setAnnotateEnabled(boolean enabled) {}

    public void notifyUserAboutUpdate(String message,boolean isPro,boolean loc){
    }

    public void indicateNewVersion() {}

    public boolean isQueryAlive(GUID guid) {
        return false;
    }
    
    public void componentLoading(String component) {
        System.out.println("Loading component: " + component);
    }

}
