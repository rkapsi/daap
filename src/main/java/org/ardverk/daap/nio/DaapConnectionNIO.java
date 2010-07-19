/*
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004-2010 Roger Kapsi
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ardverk.daap.nio;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import org.ardverk.daap.DaapConnection;
import org.ardverk.daap.DaapRequest;
import org.ardverk.daap.DaapRequestProcessor;
import org.ardverk.daap.DaapResponse;
import org.ardverk.daap.DaapResponseFactory;
import org.ardverk.daap.DaapSession;
import org.ardverk.daap.DaapUtil;
import org.ardverk.daap.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A NIO based implementation of DaapConnection.
 * 
 * @author Roger Kapsi
 */
public class DaapConnectionNIO extends DaapConnection {

    private static final Logger LOG = LoggerFactory
            .getLogger(DaapConnectionNIO.class);

    private static final DaapResponseFactory FACTORY 
        = new DaapResponseFactoryNIO();
    
    private static final DaapRequestProcessor PROCESSOR 
        = new DaapRequestProcessor(FACTORY);

    private SocketChannel socketChannel;
    private DaapRequestReaderNIO reader;

    private ReadableByteChannel readChannel;
    private WritableByteChannel writeChannel;

    private long timer = System.currentTimeMillis();

    /** Creates a new instance of DaapConnection */
    public DaapConnectionNIO(DaapServerNIO server, SocketChannel channel) {
        super(server);

        this.socketChannel = channel;
        this.readChannel = channel;
        this.writeChannel = channel;
        reader = new DaapRequestReaderNIO(this);
    }

    /**
     * What do you do next?
     * 
     * @return
     */
    public int interrestOps() {

        if (isUndef()) {
            return SelectionKey.OP_READ;

        } else if (isDaapConnection()) {

            int op = SelectionKey.OP_READ;

            if (!writer.isEmpty())
                op |= SelectionKey.OP_WRITE;

            return op;

        } else {
            // isAudioStream
            return SelectionKey.OP_WRITE;
        }
    }

    /**
     * 
     * @return
     */
    public SocketChannel getChannel() {
        return socketChannel;
    }

    /**
     * 
     * @throws IOException
     * @return
     */
    public boolean read() throws IOException {
        if (!isAudioStream()) {
            while (true) {
                if (!processRead()) {
                    break;
                }
            }
            return true;
        }
        return false;
    }

    private boolean processRead() throws IOException {
        timer = System.currentTimeMillis();
        DaapRequest request = reader.read();
        if (request == null)
            return false;

        if (isUndef()) {
            defineConnection(request);
        }

        DaapResponse response = PROCESSOR.process(request);

        if (LOG.isTraceEnabled()) {
            LOG.trace("Request=" + request + ", response=" + response);
        }

        if (response != null) {
            writer.add(response);
        }

        return true;
    }

    private void defineConnection(DaapRequest request) throws IOException {
        if (request.isSongRequest()) {
            setConnectionType(ConnectionType.AUDIO);

            // AudioStreams have a session-id and we must check the id
            SessionId sid = request.getSessionId();
            if (((DaapServerNIO) server).isSessionIdValid(sid) == false) {
                throw new IOException("Unknown Session-ID: " + sid);
            }

            // Get the associated "normal" connection...
            DaapConnection connection = ((DaapServerNIO) server)
                    .getDaapConnection(sid);
            if (connection == null) {
                throw new IOException(
                        "No connection associated with this Session-ID: " + sid);
            }

            // ... and check if there's already an audio connection
            DaapConnection audio = ((DaapServerNIO) server)
                    .getAudioConnection(sid);
            if (audio != null) {
                throw new IOException(
                        "Multiple audio connections not allowed: " + sid);
            }

            // ...and use its protocolVersion for this Audio Stream
            // because Audio Streams do not provide the version in
            // the request header (we could use the User-Agent header
            // but that breaks compatibility to non iTunes hosts and
            // they would have to fake their request header.
            setProtocolVersion(connection.getProtocolVersion());

        } else if (request.isServerInfoRequest()) {

            setConnectionType(ConnectionType.DAAP);
            setProtocolVersion(DaapUtil.getProtocolVersion(request));

        } else {

            // disconnect as the first request must be
            // either a song or server-info request!
            throw new IOException("Illegal first request: " + request);
        }

        if (!DaapUtil.isSupportedProtocolVersion(getProtocolVersion())) {
            throw new IOException("Unsupported Protocol Version: "
                    + getProtocolVersion());
        }

        // All checks passed successfully...
        if (!((DaapServerNIO) server).updateConnection(this)) {
            throw new IOException("Too may connections");
        }
    }

    /**
     * Returns true if Connection type is undef or daap and timeout is exceeded.
     */
    boolean timeout() {
        return (isUndef() && System.currentTimeMillis() - timer >= TIMEOUT)
                || (isDaapConnection() && System.currentTimeMillis() - timer >= LIBRARY_TIMEOUT);
    }

    public void clearLibraryQueue() {
        super.clearLibraryQueue();
        timer = System.currentTimeMillis();
    }

    /**
     * 
     * @throws IOException
     */
    public void update() throws IOException {
        if (isDaapConnection() && !isLocked()) {
            DaapSession session = getSession(false);
            if (session != null) {
                SessionId sessionId = session.getSessionId();

                // client's revision
                // int delta = getFirstInQueue().getRevision();
                int delta = (Integer) session.getAttribute("CLIENT_REVISION");

                // to request
                int revisionNumber = getFirstInQueue().getRevision();

                DaapRequest request = new DaapRequest(this, sessionId,
                        revisionNumber, delta);

                DaapResponse response = PROCESSOR.process(request);
                if (response != null) {
                    writer.add(response);
                }
            }
        }
    }

    public void close() {
        super.close();
        reader = null;
    }

    public String toString() {
        return socketChannel.toString();
    }

    public ReadableByteChannel getReadChannel() {
        return readChannel;
    }

    public void setReadChannel(ReadableByteChannel readChannel) {
        this.readChannel = readChannel;
    }

    public WritableByteChannel getWriteChannel() {
        return writeChannel;
    }

    public void setWriteChannel(WritableByteChannel writeChannel) {
        this.writeChannel = writeChannel;
    }
}