/*
 * DaapConnection.java
 *
 * Created on April 2, 2004, 2:25 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import de.kapsi.net.daap.Library;
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapSession;

/**
 *
 * @author  roger
 */
public class DaapConnection {
    
    private static final int UNDEF  = 0;
    private static final int NORMAL = 1;
    private static final int AUDIO  = 2;
    
    private DaapServerNIO server;
    private SocketChannel channel;
    
    private DaapProcessor processor;
    private DaapRequestReader reader;
    private DaapResponseWriter writer;
    private DaapSession session;
    
    private int type = UNDEF;
    
    /** Creates a new instance of DaapConnection */
    public DaapConnection(DaapServerNIO server, SocketChannel channel) {
        this.server = server;
        this.channel = channel;
        
        processor = new DaapProcessor(this);
        
        reader = new DaapRequestReader(channel);
        writer = new DaapResponseWriter();
    }
    
    /**
     *
     * @param create
     * @return
     */    
    public DaapSession getSession(boolean create) {
        
        if (session == null && create) {
            session = new DaapSession(server.createSessionId());
        }
        
        return session;
    }
    
    /**
     *
     * @return
     */    
    public boolean isAudioStream() {
        return (type==AUDIO);
    }
    
    /**
     *
     * @return
     */    
    public boolean isNormal() {
        return (type==NORMAL);
    }
    
    public boolean isUndef() {
        return (type == UNDEF);
    }
    
    /**
     *
     * @return
     */    
    public int interrestOps() {
        
        if (isUndef()) {
            return SelectionKey.OP_READ;
        
        } else if (isNormal()) {
            
            int op = SelectionKey.OP_READ;
            
            if (!writer.isEmpty())
                op |= SelectionKey.OP_WRITE;
            
            return op;
        
        } else {
            return SelectionKey.OP_WRITE;
        }
    }
    
    /**
     *
     * @return
     */    
    public DaapServerNIO getServer() {
        return server;
    }
    
    /**
     *
     * @return
     */    
    public SocketChannel getChannel() {
        return channel;
    }
    
    /**
     *
     * @throws IOException
     * @return
     */    
    public boolean read() throws IOException {
        
        if (!isAudioStream()) {
            
            DaapRequest request = reader.read();
            
            if (request != null) {
                
                if (type == UNDEF) {
                    type = (request.isSongRequest()) ? AUDIO : NORMAL;
                }
                
                DaapResponse response = processor.process(request);
                if (response != null) {
                    writer.add(response);
                }
                
                return true;
            }
            
        }
        
        return false;
    }
    
    /**
     *
     * @throws IOException
     * @return
     */    
    public boolean write() throws IOException {
        
        if (writer.write()) {
            
            if (isAudioStream()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     *
     * @throws IOException
     */    
    public void update() throws IOException {
        
        if (isNormal()) {
            DaapSession session = getSession(false);

            if (session != null) {

                Integer sessionId = session.getSessionId();
                Integer delta = (Integer)session.getAttribute("DELTA");
                Integer revisionNumber = (Integer)session.getAttribute("REVISION-NUMBER");

                if (delta != null && revisionNumber != null) {

                    DaapRequest request =
                        new DaapRequest(sessionId.intValue(),
                            revisionNumber.intValue(), delta.intValue());

                    DaapResponse response = processor.process(request);

                    if (response != null) {
                        writer.add(response);
                    }
                }
            }
        }
    }
    
    /**
     *
     * @return
     */    
    public String toString() {
        return channel.toString();
    }
}
