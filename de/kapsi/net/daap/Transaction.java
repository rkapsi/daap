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

package de.kapsi.net.daap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * The Transaction object is the handle for a transaction. 
 * Methods off the transaction handle are used to abort 
 * and commit the transaction.
 * 
 * @author Roger Kapsi
 */
public class Transaction {
    
    protected Library library;
    protected boolean open = false;
    
    protected HashMap txnMap = new HashMap();
    
    protected Transaction(Library library) {
        this.library = library;
        this.open = true;
    }
     
    public Library getLibrary() {
        return library;
    }
    
    /**
     * Returns <code>true</code> if this Transaction is open
     * what means that it hasn't been commited yet.
     * 
     * @return <code>true</code> if this Transaction is open
     */
    public synchronized boolean isOpen() {
        return open;
    }
    
    /**
     * Commit this Transaction
     */
    public synchronized void commit() {
        if (!isOpen())
            throw new DaapException("Transaction is not open");
        
        try {
            if (!txnMap.isEmpty()) {
                synchronized(library) {
                    
                    Iterator it = txnMap.keySet().iterator();
                    while(it.hasNext()) {
                        Object key = it.next();
                        Iterator txn = ((List)txnMap.get(key)).iterator();
                        while(txn.hasNext()) {
                            ((Txn)txn.next()).commit(this);
                        }
                    }
    
                    library.commit(this);
                }
            }
        } finally {
            close();
        }
    }
    
    /**
     * Rollback this Transaction
     */
    public synchronized void rollback() {
        if (!isOpen())
            throw new DaapException("Transaction is not open");
        
        try {
            if (!txnMap.isEmpty()) {
                synchronized(library) {
                    Iterator it = txnMap.keySet().iterator();
                    while(it.hasNext()) {
                        Iterator txn = ((List)it.next()).iterator();
                        while(txn.hasNext()) {
                            ((Txn)txn.next()).rollback(this);
                        }
                    }
                    
                    library.rollback(this);
                }
            }
        } finally {
            close();
        }
    }
    
    /**
     * Attaches Object and Txn to this Transaction. Object must be
     * an instance of Song, Playlist, Database or Library!
     */
    protected synchronized void addTxn(Object obj, Txn txn) {
        //if (!isOpen()) {
        //    throw new DaapException("Transaction is not open");
        //}
        
        List list = (List)txnMap.get(obj);
        if (list == null || list == Collections.EMPTY_LIST) {
            list = new ArrayList();
            txnMap.put(obj, list);
        }
        list.add(txn);
    }
    
    /**
     * Attach Object to Transaction. This is necessary for
     * Objects that were constructed/modified independently
     * from this transaction.
     */
    protected synchronized void attach(Object obj) {
        if (!txnMap.containsKey(obj)) {
            txnMap.put(obj, Collections.EMPTY_LIST);
        }
    }
    
    /**
     * Returns true if Library or one of its Databases was modified
     */
    protected synchronized boolean modified(Library library) {
        if (txnMap.containsKey(library)) {
            return true;
        }
        
        Iterator it = library.getDatabases().iterator();
        while(it.hasNext()) {
            if (modified((Database)it.next())) {
                txnMap.put(library, Collections.EMPTY_LIST);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns true if Database or one of its Playlists was modified
     */
    protected synchronized boolean modified(Database database) {
        if (txnMap.containsKey(database)) {
            return true;
        }
        
        Iterator it = database.getPlaylists().iterator();
        while(it.hasNext()) {
            if (modified((Playlist)it.next())) {
                txnMap.put(database, Collections.EMPTY_LIST);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns true if Playlist or one of its Songs was modified
     */
    protected synchronized boolean modified(Playlist playlist) {
        if (txnMap.containsKey(playlist)) {
            return true;
        }
        
        Iterator it = playlist.getSongs().iterator();
        while(it.hasNext()) {
            if (modified((Song)it.next())) {
                txnMap.put(playlist, Collections.EMPTY_LIST);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns true if Song was modified
     */
    protected synchronized boolean modified(Song song) {
        return txnMap.containsKey(song);
    }
    
    /**
     * Cleanup
     */
    protected void close() {
        if (open) {
            
            library.close(this);
            
            open = false;
            
            if (txnMap != null) {
                txnMap.clear();
                txnMap = null;
            }
        }
    }
    
    public String toString() {
        return "Transaction: " + library;
    }
}
