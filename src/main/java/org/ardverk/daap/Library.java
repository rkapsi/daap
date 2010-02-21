/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004, 2005 Roger Kapsi, info at kapsi dot de
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

package org.ardverk.daap;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ardverk.daap.chunks.ContentCodesResponseImpl;
import org.ardverk.daap.chunks.impl.AuthenticationMethod;
import org.ardverk.daap.chunks.impl.AuthenticationSchemes;
import org.ardverk.daap.chunks.impl.ContainerCount;
import org.ardverk.daap.chunks.impl.ContentCodesResponse;
import org.ardverk.daap.chunks.impl.DaapProtocolVersion;
import org.ardverk.daap.chunks.impl.DatabaseCount;
import org.ardverk.daap.chunks.impl.DeletedIdListing;
import org.ardverk.daap.chunks.impl.DmapProtocolVersion;
import org.ardverk.daap.chunks.impl.ItemCount;
import org.ardverk.daap.chunks.impl.ItemId;
import org.ardverk.daap.chunks.impl.ItemName;
import org.ardverk.daap.chunks.impl.Listing;
import org.ardverk.daap.chunks.impl.ListingItem;
import org.ardverk.daap.chunks.impl.LoginRequired;
import org.ardverk.daap.chunks.impl.LoginResponse;
import org.ardverk.daap.chunks.impl.PersistentId;
import org.ardverk.daap.chunks.impl.ReturnedCount;
import org.ardverk.daap.chunks.impl.ServerDatabases;
import org.ardverk.daap.chunks.impl.ServerInfoResponse;
import org.ardverk.daap.chunks.impl.ServerRevision;
import org.ardverk.daap.chunks.impl.SpecifiedTotalCount;
import org.ardverk.daap.chunks.impl.Status;
import org.ardverk.daap.chunks.impl.SupportsBrowse;
import org.ardverk.daap.chunks.impl.SupportsExtensions;
import org.ardverk.daap.chunks.impl.SupportsIndex;
import org.ardverk.daap.chunks.impl.SupportsPersistentIds;
import org.ardverk.daap.chunks.impl.SupportsQuery;
import org.ardverk.daap.chunks.impl.SupportsUpdate;
import org.ardverk.daap.chunks.impl.TimeoutInterval;
import org.ardverk.daap.chunks.impl.UpdateResponse;
import org.ardverk.daap.chunks.impl.UpdateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Roger Kapsi
 */
public class Library {

    private static final Logger LOG = LoggerFactory.getLogger(Library.class);

    private static long persistentId = 1;
    
    /** The revision of this Library */
    private int revision = 0;
    
    /** Name of this Library */
    private String name;
    
    /** The total number of Databases in this Library */
    private int totalDatabaseCount = 0;
    
    /** Set of Databases */
    private final Set<Database> databases = new HashSet<Database>();
    
    /** Set of deleted Databases */
    private Set<Database> deletedDatabases = null;
    
    /** List of listener */
    private final List<WeakReference<LibraryListener>> listener = new ArrayList<WeakReference<LibraryListener>>();
    
    protected boolean clone = false;
    
    protected Library(Library library, Transaction txn) {
        this.name = library.name;
        this.revision = library.revision;
        
        if (library.deletedDatabases != null) {
            this.deletedDatabases = library.deletedDatabases;
            library.deletedDatabases = null;
        }
        
        for(Database database : library.databases) {
            if (txn.modified(database)) {
                if (deletedDatabases == null || !deletedDatabases.contains(database)) {
                    Database clone = new Database(database, txn);
                    databases.add(clone);
                }
            }
        }
        
        this.totalDatabaseCount = library.totalDatabaseCount;
        this.clone = true;
        
        init();
    }
    
    public Library(String name) {
        this.name = name;
        commit(null);
        
        init();
    }
    
    private void init() {
        
    }
    
    /**
     * Returns the current revision of this library.
     */
    public synchronized int getRevision() {
        return revision;
    }
    
    /**
     * Sets the name of this Library. Note: Library must be open or an
     * <tt>IllegalStateException</tt> will be thrown
     */
    public void setName(Transaction txn, final String name) {
        if (txn != null) {
            txn.addTxn(this, new Txn() {
                public void commit(Transaction txn) {
                    setNameP(txn, name);
                }
            });
        } else {
            setNameP(txn, name);
        }
    }

    private void setNameP(Transaction txn, String name) {
        this.name = name;
    }
    
    /**
     * Returns the name of this Library
     */
    public String getName() {
        return name;
    }
    
    /**
     * 
     * @return
     */
    public Set<Database> getDatabases() {
        return Collections.unmodifiableSet(databases);
    }
    
    /**
     * Adds database to this Library (<b>NOTE</b>: only one Database per Library
     * is supported by iTunes!)
     * 
     * @param database
     * @throws DaapTransactionException
     */
    public void addDatabase(Transaction txn, final Database database) {
        if (txn != null) {
            txn.addTxn(this, new Txn() {
                public void commit(Transaction txn) {
                    addDatabaseP(txn, database);
                }
            });
            txn.attach(database);
        } else {
            addDatabaseP(txn, database);
        }
    }

    private void addDatabaseP(Transaction txn, Database database) {
        if (!databases.isEmpty()) {
            throw new DaapException("One Database per Library is maximum.");
        }
        
        if (databases.add(database)) {
            totalDatabaseCount = databases.size();
            if (deletedDatabases != null && deletedDatabases.remove(database)
                    && deletedDatabases.isEmpty()) {
                deletedDatabases = null;
            }
        }
    }
    
    /**
     * Removes database from this Library
     * 
     * @param database
     * @throws DaapTransactionException
     */
    public void removeDatabase(Transaction txn, final Database database) {
        if (txn != null) {
            txn.addTxn(this, new Txn() {
                public void commit(Transaction txn) {
                    removeDatabaseP(txn, database);
                }
            });
        } else {
            removeDatabaseP(txn, database);
        }
    }
    
    private void removeDatabaseP(Transaction txn, Database database) {
        if (databases.remove(database)) {
            totalDatabaseCount = databases.size();
            if (deletedDatabases == null) {
                deletedDatabases = new HashSet<Database>();
            }
            deletedDatabases.add(database);
        }
    }
    
    /**
     * Returns true if this Library contains database
     * 
     * @param database
     * @return
     */
    public synchronized boolean containsDatabase(Database database) {
        return databases.contains(database);
    }
    
    public synchronized Transaction beginTransaction() {
        Transaction txn = new Transaction(this);
        return txn;
    }
    
    /**
     * Returns some kind of Object or null if <tt>request</tt> didn't matched
     * for this Library (unknown request, unknown id, whatever). The returned
     * Object could be basically anything but it's in our case either an
     * <tt>java.lang.Integer</tt> or a byte-Array (gzip'ed).
     */
    public synchronized Object select(DaapRequest request) {
        //System.out.println("REQUEST: " + request);
        
        if (request.isServerInfoRequest()) {
            return getServerInfo(request);
        
        } else if (request.isLoginRequest()) {
            return getLoginResponse(request);
            
        } else if (request.isContentCodesRequest()) {
            return getContentCodes(request);
            
        } else if (request.isUpdateRequest()) {
            return getUpdateResponse(request);

        } else if (request.isDatabasesRequest()) {
            return getServerDatabases(request);
            
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
    
    public synchronized void commit(Transaction txn) {
        if (txn == null) {
            txn = new Transaction(this);
            txn.addTxn(this, new Txn());
            txn.commit();
            return;
        }
        
        this.revision++;
        Library diff = new Library(this, txn);
        
        synchronized(listener) {
            Iterator<WeakReference<LibraryListener>> it = listener.iterator();
            while(it.hasNext()) {
                LibraryListener l = it.next().get();
                if (l == null) {
                    it.remove();
                } else {
                    l.libraryChanged(this, diff);
                }
            }
        }
    }
    
    protected synchronized void rollback(Transaction txn) {
        // TODO: add code, actually do nothing...
    }
    
    protected synchronized void close(Transaction txn) {
    }
    
    /**
     * Returns the number of Databases
     * 
     * @return
     */
    public int getDatabaseCount() {
        return databases.size();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Library)) {
            return false;
        }
        
        return o == this;
    }
    
    public String toString() {
        if (!clone) {
            return "Library(" + revision + ")";
        } else {
            return "LibraryPatch(" + revision + ")";
        }
    }
    
    protected static long nextPersistentId() {
        synchronized(Library.class) {
            return persistentId++;
        }
    }

    public void addLibraryListener(LibraryListener l) {
        synchronized(listener) {
            listener.add(new WeakReference<LibraryListener>(l));
        }
    }
    
    public void removeLibraryListener(LibraryListener l) {
        synchronized(listener) {
            Iterator<WeakReference<LibraryListener>> it = listener.iterator();
            while(it.hasNext()) {
                LibraryListener gotten = it.next().get();
                if (gotten == null || gotten == l) {
                    it.remove();
                }
            }
        }
    }
    
    protected Database getDatabase(DaapRequest request) {
        long databaseId = request.getDatabaseId();
        for(Database database : databases) {
            if (database.getItemId() == databaseId) {
                return database;
            }
        }
        return null;
    }
    
    private LoginResponse getLoginResponse(DaapRequest request) {
        SessionId sessionId = request.getSessionId();
        
        if (sessionId.equals(SessionId.INVALID)) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unknown SessionId, check Server code!");
            }
            return null;
        }
        
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.add(new Status(200));
        loginResponse.add(new org.ardverk.daap.chunks.impl.SessionId(sessionId.intValue()));
        return loginResponse;
    }
    
    private ContentCodesResponse getContentCodes(DaapRequest request) {
        return new ContentCodesResponseImpl();
    }
    
    private UpdateResponse getUpdateResponse(DaapRequest request) {
        UpdateResponse updateResponse = new UpdateResponse();
        updateResponse.add(new Status(200));
        updateResponse.add(new ServerRevision(getRevision()));
        return updateResponse;
    }
    
    private ServerDatabases getServerDatabases(DaapRequest request) {
        ServerDatabases serverDatabases = new ServerDatabases();
        
        serverDatabases.add(new Status(200));
        serverDatabases.add(new UpdateType(request.isUpdateType() ? 1 : 0));
        serverDatabases.add(new SpecifiedTotalCount(totalDatabaseCount));
        
        serverDatabases.add(new ReturnedCount(databases.size()));

        Listing listing = new Listing();

        for(Database database : databases) {
            ListingItem listingItem = new ListingItem();
            listingItem.add(new ItemId(database.getItemId()));
            listingItem.add(new PersistentId(database.getPersistentId()));
            listingItem.add(new ItemName(database.getName()));
            listingItem.add(new ItemCount(database.getSongCount()));
            listingItem.add(new ContainerCount(database.getPlaylistCount()));

            listing.add(listingItem);
        }

        serverDatabases.add(listing);
        
        if (request.isUpdateType() && deletedDatabases != null) {
            DeletedIdListing deletedListing = new DeletedIdListing();
            
            for(Database database : deletedDatabases)
                deletedListing.add(new ItemId(database.getItemId()));

            serverDatabases.add(deletedListing);
        }
        
        return serverDatabases;
    }
    
    private ServerInfoResponse getServerInfo(DaapRequest request) {
        
        DaapConnection connection = request.getConnection();
        int version = connection.getProtocolVersion();

        if (version < DaapUtil.DAAP_VERSION_3) {
            return null;
        }
        
        DaapServer server = request.getServer();
        DaapConfig config = server.getConfig();
        
        ServerInfoResponse serverInfoResponse = new ServerInfoResponse();
        
        serverInfoResponse.add(new Status(200));
        serverInfoResponse.add(new TimeoutInterval(1800));
        serverInfoResponse.add(new DmapProtocolVersion(DaapUtil.DMAP_VERSION_201));
        serverInfoResponse.add(new DaapProtocolVersion(DaapUtil.DAAP_VERSION_3));
        //serverInfoResponse.add(new MusicSharingVersion(DaapUtil.MUSIC_SHARING_VERSION_201));
        serverInfoResponse.add(new ItemName(name));
        
        // NOTE: the value of the following boolean chunks does not matter!
        // They are either present (=true) or not (=false).
        
        // client should perform /login request (create session)
        serverInfoResponse.add(new LoginRequired(true));
        serverInfoResponse.add(new SupportsBrowse(true));
        serverInfoResponse.add(new SupportsPersistentIds(false));
        serverInfoResponse.add(new SupportsIndex(true));
        serverInfoResponse.add(new SupportsQuery(true));
        serverInfoResponse.add(new SupportsUpdate(true));
        
        //serverInfoResponse.add(new SupportsAutoLogout(true));
        
        // TODO: figure out what is an extension and what not.
        // /content-codes request
        serverInfoResponse.add(new SupportsExtensions(true));
        
        /*serverInfoResponse.add(new SupportsBrowse(true));
        serverInfoResponse.add(new SupportsQuery(true));
        serverInfoResponse.add(new SupportsIndex(true));
        serverInfoResponse.add(new SupportsResolve(true));*/
        
        Object authenticationMethod = config.getAuthenticationMethod();
        if (!authenticationMethod.equals(DaapConfig.NO_PASSWORD)) {
            if (authenticationMethod.equals(DaapConfig.PASSWORD)) {
                serverInfoResponse.add(new AuthenticationMethod(AuthenticationMethod.PASSWORD_METHOD));
            } else {
                serverInfoResponse.add(new AuthenticationMethod(AuthenticationMethod.USERNAME_PASSWORD_METHOD));
            }
            
            serverInfoResponse.add(new AuthenticationSchemes(AuthenticationSchemes.BASIC_SCHEME | AuthenticationSchemes.DIGEST_SCHEME));
        }
        
        serverInfoResponse.add(new DatabaseCount(getDatabaseCount()));
        return serverInfoResponse;
    }
}