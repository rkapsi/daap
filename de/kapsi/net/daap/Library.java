/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004 Roger Kapsi, info at kapsi dot de
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

package de.kapsi.net.daap;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.ChunkClasses;
import de.kapsi.net.daap.chunks.ContentCode;
import de.kapsi.net.daap.chunks.impl.ContainerCount;
import de.kapsi.net.daap.chunks.impl.ContentCodesResponse;
import de.kapsi.net.daap.chunks.impl.DaapProtocolVersion;
import de.kapsi.net.daap.chunks.impl.DatabaseCount;
import de.kapsi.net.daap.chunks.impl.DmapProtocolVersion;
import de.kapsi.net.daap.chunks.impl.ItemCount;
import de.kapsi.net.daap.chunks.impl.ItemId;
import de.kapsi.net.daap.chunks.impl.ItemName;
import de.kapsi.net.daap.chunks.impl.Listing;
import de.kapsi.net.daap.chunks.impl.ListingItem;
import de.kapsi.net.daap.chunks.impl.LoginRequired;
import de.kapsi.net.daap.chunks.impl.PersistentId;
import de.kapsi.net.daap.chunks.impl.ReturnedCount;
import de.kapsi.net.daap.chunks.impl.ServerDatabases;
import de.kapsi.net.daap.chunks.impl.ServerInfoResponse;
import de.kapsi.net.daap.chunks.impl.SpecifiedTotalCount;
import de.kapsi.net.daap.chunks.impl.Status;
import de.kapsi.net.daap.chunks.impl.SupportsAutoLogout;
import de.kapsi.net.daap.chunks.impl.SupportsBrowse;
import de.kapsi.net.daap.chunks.impl.SupportsExtensions;
import de.kapsi.net.daap.chunks.impl.SupportsIndex;
import de.kapsi.net.daap.chunks.impl.SupportsPersistentIds;
import de.kapsi.net.daap.chunks.impl.SupportsQuery;
import de.kapsi.net.daap.chunks.impl.SupportsResolve;
import de.kapsi.net.daap.chunks.impl.SupportsUpdate;
import de.kapsi.net.daap.chunks.impl.TimeoutInterval;
import de.kapsi.net.daap.chunks.impl.UpdateType;

/**
 * This class and its internals are the heart of this DAAP
 * implementation. Note: it's very important to synchronize
 * all operations!!!<p>
 * <code>
 * Library library = ...;
 * synchronized(library) {
 *      library.open();
 *      library.add(new Song(...));
 *      library.close();
 *  }
 * </code>
 *
 * @author  Roger Kapsi
 */
public class Library {
	
    private static final Log LOG = LogFactory.getLog(Library.class);
	
    private static final int DATABASE_ID = 1;
    
    public static final int DEFAULT_KEEP_REVISIONS = 10;
    
    private static long persistentId = 0;
    
    private ArrayList revisions = new ArrayList();

    private int keepNumRevisions;
    private String name;

    private Database current;
    private Database temp;
	
    private byte[] serverDatabases;
    private byte[] serverDatabasesUpdate;
    
    private byte[] contentCodes;
    
    //private byte[] serverInfoV1; // nobody uses iTunes 4.0
    private byte[] serverInfoV2;
    private byte[] serverInfoV3;
    
    private boolean open = false;
    
    /**
     * Creates a new Library with the provided <tt>name</tt>
     * and with the default revision history of <tt>DEFAULT_KEEP_REVISIONS</tt>
     */
    public Library(String name) {
        this(name, DEFAULT_KEEP_REVISIONS);
    }

    /**
     * Creates a new Library with the provided <tt>name</tt> and
     * the max number of revisions.
     */
    public Library(String name, int keepNumRevisions) {
        
        if (keepNumRevisions <= 0)
            throw new IllegalArgumentException("keepNumRevisions must be >= 1");
        
        this.name = name;
        this.keepNumRevisions = keepNumRevisions;
        
        contentCodes = new ContentCodesResponseImpl().getBytes();
        
        // 1.0.0 (iTunes 4.0)
        //serverInfoV1 = new ServerInfoResponseImpl(name, DaapUtil.VERSION_1).getBytes();
        
        // 2.0.0 (iTunes 4.1 and 4.2)
        serverInfoV2 = new ServerInfoResponseImpl(name, DaapUtil.VERSION_2).getBytes();
        
        // 3.0.0 (iTunes 4.5)
        serverInfoV3 = new ServerInfoResponseImpl(name, DaapUtil.VERSION_3).getBytes();
    }
    
    /**
     * Returns the current revision of this library. Everytime
     * you open() and close() the library the revision will be
     * increased by one
     */
    public int getRevision() {
        if (current == null) {
            return 0;
        } else {
            return current.getRevision();
        }
    }

    /**
     * Sets the name of this Library. Note: Library must be
     * open or an <tt>IllegalStateException</tt> will be thrown
     */
    public void setName(String name) {
        if (!isOpen()) {
            throw new IllegalStateException("Library is not open");
        }

        temp.setName(name);
    }

    /**
     * Returns the name of this Library
     */
    public String getName() {
        if (current == null) {
            return name;
        } else {
            return current.getName();
        }
    }

    /**
     * Returns <tt>true</tt> if Library is open
     * an can be edited
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Deletes everything from the Library. Note: you
     * should shutdown the server before doing this!
     */
    public void delete() {
        if (isOpen()) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Library is open.");
            }
            return;
        }

        revisions.clear();

        current = null;
        temp = null;

        serverDatabases = null;
        serverDatabasesUpdate = null;
    }

    /**
     * Creates an empty Library
     */
    public void init() {
        if (getRevision()==0) {
            open();
            close();
        }
    }
    
    /**
     * Open the Library for edit
     */
    public void open() {

        if (isOpen()) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Library is already opened for edit");
            }
            return;
        }

        if (current == null) {
            // current is initialized on close()! 
            temp = new Database(DATABASE_ID, name, nextPersistentId());

        } else {

            temp = current;
            current = temp.createSnapshot();

            temp.open();
        }

        open = true;
    }

    /**
     * Closes the Library
     */
    public void close() {
        if (!isOpen()) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Library is already closed");
            }
            return;
        }

        if (current != null) {
            revisions.add(current);
     
            if (current.getName().equals(temp.getName()) == false) {
                //serverInfoV1 = new ServerInfoResponseImpl(temp.getName(), DaapUtil.VERSION_1).getBytes();
                serverInfoV2 = new ServerInfoResponseImpl(temp.getName(), DaapUtil.VERSION_2).getBytes();
                serverInfoV3 = new ServerInfoResponseImpl(temp.getName(), DaapUtil.VERSION_3).getBytes();
            }
        }
        
        current = temp;
        current.close();
        temp = null;

        ArrayList databases = new ArrayList();
        databases.add(current);

        serverDatabases = (new ServerDatabasesImpl(databases, false)).getBytes();
        serverDatabasesUpdate = (new ServerDatabasesImpl(databases, true)).getBytes();

        if (revisions.size() >= keepNumRevisions) {
            Database old = (Database)revisions.remove(0);
            old.destroy();
        }

        open = false;
    }
    
    /**
     * Returns some kind of Object or null if <tt>request</tt>
     * didn't matched for this Library (unknown request, unknown id,
     * whatever). The returned Object could be basically anything
     * but it's in our case either an <tt>java.lang.Integer</tt> or
     * a byte-Array (gzip'ed).
     */
    public synchronized Object select(DaapRequest request) {

        if (request.isServerInfoRequest()) {
            
            DaapConnection connection = request.getConnection();
            int version = connection.getProtocolVersion();
              
            if (version == DaapUtil.VERSION_2) {
                return serverInfoV2;
            } else if (version >= DaapUtil.VERSION_3) {
                return serverInfoV3;
            
            } else { // Undef or VERSION_1
                return null;
            }
            
        } else if (request.isContentCodesRequest()) {
            return contentCodes;
            
        } else if (request.isUpdateRequest()) {

            int delta = request.getDelta();

            // What's the next revision of the database 
            // iTunes should ask for?
            if (delta == DaapUtil.UNDEF_VALUE) { 
                 
                // 1st. request, iTunes should/will 
                // ask for the current revision
                return (new Integer(getRevision()));

            } else if (delta < getRevision()) {
                
                // ask for the next revision
                return (new Integer(++delta)); 

            } else {

                // iTunes is up-to-date
                return (new Integer(delta));
            }

        } else if (request.isDatabasesRequest()) {

            Database database = getDatabase(request);

            if (database == null) {

                if (LOG.isInfoEnabled()) {
                    LOG.info("No database with this revision known: " 
                        + request.getRevisionNumber());
                }

                return null;
            }

            if (request.isUpdateType()) {
                return serverDatabasesUpdate;
            } else {
                return serverDatabases;
            }

        } else if (request.isSongRequest() 
                            || request.isDatabaseSongsRequest() 
                            || request.isDatabasePlaylistsRequest()
                            || request.isPlaylistSongsRequest()) {

            Database database = getDatabase(request);
            if (database == null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("No database with this revision known: " 
                        + request.getRevisionNumber());
                }

                return null;
            }

            return database.select(request);

        } else {
            if (LOG.isInfoEnabled()) {
                    LOG.info("Unknown request: " + request);
            }
            return null;
        }
    }

    /**
     * Returns a Database for the <tt>request</tt>. The
     * requested Database is determinated by the Database ID.
     */
    private Database getDatabase(DaapRequest request) {

        if (current == null) {
            return null;
        }

        int revisionNumber = request.getRevisionNumber();
        
        if (revisionNumber == DaapUtil.UNDEF_VALUE || 
                       revisionNumber == current.getRevision()) {
                           
            return current;
        }

        Iterator it = revisions.iterator();
        while(it.hasNext()) {
            Database database = (Database)it.next();
            if (database.getRevision() == revisionNumber) {
                return database;
            }
        }

        return null;
    }

    /**
     * Adds <tt>song</tt> to the Master Playlist.
     */
    public void add(Song song) {

        if (!isOpen()) {
            throw new IllegalStateException("Library is not open");
        }

        temp.add(song);
    }

    /**
     * Removes <tt>song</tt> from the Master Playlist.
     */
    public boolean remove(Song song) {

        if (!isOpen()) {
            throw new IllegalStateException("Library is not open");
        }

        return temp.remove(song);
    }

    /**
     * Adds <tt>playlist</tt> to the Library
     */
    public void add(Playlist playlist) {

        if (!isOpen()) {
            throw new IllegalStateException("Library is not open");
        }

        temp.add(playlist);
    }

    /**
     * Removes <tt>playlist</tt> from the Library
     */
    public boolean remove(Playlist playlist) {

        if (!isOpen()) {
            throw new IllegalStateException("Library is not open");
        }

        return temp.remove(playlist);
    }

    /**
     * Returns the number of Songs in this Library
     */
    public int size() {
        if (current==null) {
            return 0;
        } else {

            Playlist masterPlaylist = current.getMasterPlaylist();
            if (masterPlaylist == null && temp != null)
                masterPlaylist = temp.getMasterPlaylist();

            if (masterPlaylist != null) {
                return masterPlaylist.size();
            } else {
                return 0;
            }
        }
    }

    static synchronized long nextPersistentId() {
        return ++persistentId;
    }
    
    public String toString() {
        return getName();
    }
    
    private final class ServerDatabasesImpl extends ServerDatabases {
   
        public ServerDatabasesImpl(List databases, boolean updateType) {
            super();

            add(new Status(200));
            add(new UpdateType(updateType));

            add(new SpecifiedTotalCount(databases.size()));
            add(new ReturnedCount(databases.size()));

            Listing listing = new Listing();

            Iterator it = databases.iterator();
            while(it.hasNext()) {
                ListingItem listingItem = new ListingItem();

                Database database = (Database)it.next();

                listingItem.add(new ItemId(database.getId()));
                listingItem.add(new PersistentId(database.getPersistentId()));
                listingItem.add(new ItemName(database.getName()));

                Playlist playlist = database.getMasterPlaylist();
                int itemCount = ((updateType) ? playlist.getNewSongs() : playlist.getSongs()).size();
                int containerCount = database.getPlaylists().size();

                listingItem.add(new ItemCount(itemCount));
                listingItem.add(new ContainerCount(containerCount));

                listing.add(listingItem);
            }

            add(listing);
        }

        public byte[] getBytes() {
            return getBytes(true);
        }
        
        public byte[] getBytes(boolean compress) {
            try {
                return DaapUtil.serialize(this, compress);
            } catch (IOException err) {
                LOG.error(err);
                return null;
            }
        }
    }
    
    /**
    * Groups many ContentCodes to one single chunk
    */
    private static final class ContentCodesResponseImpl extends ContentCodesResponse {
        
        public ContentCodesResponseImpl() {
            super();

            add(new Status(200));

            String[] names = ChunkClasses.names;

            final Class[] arg1 = new Class[]{};
            final Object[] arg2 = new Object[]{};

            for(int i = 0; i < names.length; i++) {
                try {
                    Class clazz = Class.forName(names[i]);

                    Method methodContentCode = clazz.getMethod("getContentCode", arg1);
                    Method methodName = clazz.getMethod("getName", arg1);
                    Method methodType = clazz.getMethod("getType", arg1);

                    Object inst = clazz.newInstance();

                    String contentCode = (String)methodContentCode.invoke(inst, arg2);
                    String name = (String)methodName.invoke(inst, arg2);
                    int type = ((Integer)methodType.invoke(inst, arg2)).intValue();

                    add(new ContentCode(contentCode, name, type));

                } catch (ClassNotFoundException err) {
                    LOG.error(err);
                } catch (NoSuchMethodException err) {
                    LOG.error(err);
                } catch (InstantiationException err) {
                    LOG.error(err);
                } catch (IllegalAccessException err) {
                    LOG.error(err);
                } catch (IllegalArgumentException err) {
                    LOG.error(err);
                } catch (InvocationTargetException err) {
                    LOG.error(err);
                } catch (SecurityException err) {
                    LOG.error(err);
                }
            }
        }
        
        public byte[] getBytes() {
            return getBytes(true);
        }
        
        public byte[] getBytes(boolean compress) {
            try {
                return DaapUtil.serialize(this, compress);
            } catch (IOException err) {
                LOG.error(err);
                return null;
            }
        }
    }
    
    /**
     * This class implements the ServerInfoResponse
     */
    private static final class ServerInfoResponseImpl extends ServerInfoResponse {

        public ServerInfoResponseImpl(String name, int version) {
            super();

            add(new Status(200));
            add(new TimeoutInterval(1800));
            add(new DmapProtocolVersion(version));
            add(new DaapProtocolVersion(version));
            add(new ItemName(name));
            add(new LoginRequired(false));
            add(new SupportsAutoLogout(false));
            add(new SupportsUpdate(false));
            add(new SupportsPersistentIds(false));
            add(new SupportsExtensions(false));
            add(new SupportsBrowse(false));
            add(new SupportsQuery(false));
            add(new SupportsIndex(false));
            add(new SupportsResolve(false));
            add(new DatabaseCount(1));
        }
        
        public byte[] getBytes() {
            return getBytes(true);
        }
        
        public byte[] getBytes(boolean compress) {
            try {
                return DaapUtil.serialize(this, compress);
            } catch (IOException err) {
                LOG.error(err);
                return null;
            }
        }
    }
}
