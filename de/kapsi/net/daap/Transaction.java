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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Transaction collects operations you perform on Libraries, 
 * Databases and Playlists and brings them into a suitable order
 * so that the operations can be commited and processed. A DAAP 
 * Transaction does not guarantee full ACID (Atomicy, Consistency, 
 * Isolation and Durability).
 * 
 * <p><b>Atomicy:</b> A commit failure will leave the target object in
 *  an undefined state. A rollback failure has no effect.</p>
 * 
 * <p><b>Consistency:</b> Yep, we have consistency if no failures
 *  happen (see Atomicy). A Transaction transfers the system
 *  from a consitent state into a different consistent state.</p>
 * 
 * <p><b>Isolation:</b> No isolation in sense of the definition of
 *  transactions. Transactions are synchronized but as we
 *  join multible Transations to a single Transaction they
 *  may overwrite their attributes, but that is designedly so!</p>
 * 
 * <p><b>Durability:</b> Yep, we have durability. The changes of
 *  a successfully commited Transaction can neither disappear
 *  nor can be undone.</p>
 * 
 * @author Roger Kapsi
 */
public class Transaction {
    
    private static final long SCHEDULE_INTERVAL = 5*1000;
    
    private static Timer timer;
    private static AutoCommitTask timerTask;
    
    private Library library;
    private Txn rootTxn;
    boolean autoCommit;
    
    private boolean open;
    
    private HashMap attributes = new HashMap();
    private HashSet listener = new HashSet();
    
    private final long txnCreated;
    private long lastModified;
    
    /**
     * Creates a new Transaction
     * 
     * @param library the Library to which this Transaction is associated
     * @param rootTxn a root Txn
     * @param autoCommit auto commit
     */
    Transaction(Library library, Txn rootTxn, boolean autoCommit) {
        this.library = library;
        this.rootTxn = rootTxn;
        this.autoCommit = autoCommit;
        
        this.open = true;
        
        setAttribute(library, rootTxn);
        
        if (autoCommit) {
            synchronized(Transaction.class) {
                if (timer == null) {
                    timer = new Timer(true);
                    
                    timerTask = new AutoCommitTask();
                    timer.scheduleAtFixedRate(timerTask, SCHEDULE_INTERVAL, SCHEDULE_INTERVAL);
                }
                
                timerTask.add(this);
            }
        }
        
        txnCreated = System.currentTimeMillis();
        lastModified = txnCreated;
    }
    
    /**
     * Returns <code>true</code> if this Transaction is an
     * autocommiting Transaction
     * 
     * @return <code>true</code> if this Transaction will commit
     *          automatically
     */
    public boolean isAutoCommit() {
        return autoCommit;
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
     * Returns the modification time of this Transaction
     * 
     * @return the time when this Transaction was modified for last time
     */
    public synchronized long lastModified() {
        return lastModified;
    }
    
    /**
     * Sets the modification date to 'now'. By polling this method you can
     * delay the commit of an autocommiting Transaction (it makes no sense
     * tho). 
     */
    public synchronized void touch() {
        lastModified = System.currentTimeMillis();
    }
    
    /**
     * Commit this Transaction
     * 
     * @throws DaapException
     */
    public synchronized void commit() throws DaapException {

        if (!isOpen())
            throw new DaapException();
        
        try {
            rootTxn.commit(this);
   
            Iterator it = listener.iterator();
            while(it.hasNext()) {
                ((TransactionListener)it.next()).commit(this);
            }
        } finally {
            rootTxn.cleanup(this);
            open = false;
            listener.clear();
        }
    }
    
    /**
     * Rollback this Transaction
     * 
     * @throws DaapException
     */
    public synchronized void rollback() throws DaapException {
        
        if (!isOpen())
            throw new DaapException();
        
        try {
            rootTxn.rollback(this);
            
            Iterator it = listener.iterator();
            while(it.hasNext()) {
                ((TransactionListener)it.next()).rollback(this);
            }
        } finally {
            rootTxn.cleanup(this);
            open = false;
            listener.clear();
        }
    }
    
    /**
     * Join this Transaction with txn. In 99.9% of all thinkable cases
     * you do not do this yourself!
     * 
     * @param txn a Transaction
     * @throws DaapException
     */
    public synchronized void join(Transaction txn) throws DaapException {
        
        if (this == txn)
            throw new DaapException("Cannot join 'this' with itself");
        
        if (!isOpen())
            throw new DaapException("Transaction is not open");
        
        if (!txn.isOpen())
            throw new DaapException("The other Transaction is not open");
        
        if (library != txn.library)
            throw new DaapException("Transactions are associated with different Libraries");
        
        listener.addAll(txn.listener);
        
        Iterator it = txn.attributes.keySet().iterator();
        while(it.hasNext()) {
            Object key = it.next();
            Txn value = (Txn)txn.attributes.get(key);
            
            if (!attributes.containsKey(key)) {
                attributes.put(key, value);
            } else {
                Txn thizValue = (Txn)attributes.get(key);
                thizValue.join(value);
            }
        }
    }
    
    /**
     * Adds a TransactionListener to this Transaction
     * 
     * @param l a TransactionListener
     */
    public synchronized void addTransactionListener(TransactionListener l) {
        listener.add(l);
    }
    
    /**
     * Removes a TransactionListener from this Transaction
     * 
     * @param l a TransactionListener
     */
    public synchronized void removeTransactionListener(TransactionListener l) {
        listener.remove(l);
    }
    
    /**
     * Attachs the key/value pair to this Transaction or removes it
     * if value is <code>null</code>
     * 
     * @param key an key Object
     * @param value a Txn Object
     * @throws DaapException
     */
    void setAttribute(Object key, Txn value) throws DaapException {

        if (!isOpen()) {
            throw new DaapException("Transaction is not open");
        }

        if (value != null) {
            attributes.put(key, value);
        } else {
            attributes.remove(key);
        }
        
        touch();
    }

    /**
     * Retrieves an Txn Object that is associated with <code>key</code>.
     * 
     * @param key an key Object
     * @return @throws
     *         DaapException
     */
    Txn getAttribute(Object key) throws DaapException {

        if (!isOpen()) {
            throw new DaapException("Transaction is not open");
        }
        
        touch();
        return (Txn)attributes.get(key);
    }

    /**
     * Returns <code>true</code> if this Transaction has an
     * attribute that is associated with the <code>key</code>
     * Object.
     * 
     * @param key an key Object
     * @return <code>true</code> if this Transaction has a such
     *      attribute
     * @throws
     *         DaapException
     */
    boolean hasAttribute(Object key) throws DaapException {

        if (!isOpen()) {
            throw new DaapException("Transaction is not open");
        }
        
        touch();
        return attributes.containsKey(key);
    }
    
    public String toString() {
        return attributes.toString();
    }
    
    /**
     * The AutoCommitTask commits all Transactions that haven't been
     * modified for a certain period of time. If multible Transactions
     * have been timed out in this manner they'll be joined and
     * commited as a single Transaction.
     */
    private static final class AutoCommitTask extends TimerTask {
        
        private Library library;
        private ArrayList list = new ArrayList();
        
        private AutoCommitTask() {
            
        }
        
        private void add(Transaction txn) {
            list.add(txn);
            
            if(library == null)
                library = txn.library;
            //else if(txn.library != library)
            //  multiple libraries?  huh?  that isn't happening yet!
        }
        
        public void run() {
            synchronized(library) {
                synchronized(Transaction.class) {
                    if (list.isEmpty()) {
                        cancel();
                        timer.cancel();
                        timer = null;
                        timerTask = null;
                    }
                    
                    Iterator it = list.iterator();
                    Transaction root = null;
                    
                    while(it.hasNext()) {
                        Transaction txn = (Transaction)it.next();
                        
                        if (!txn.isOpen()) {
                            it.remove();
                            
                        } else if (System.currentTimeMillis() - txn.lastModified() > SCHEDULE_INTERVAL) {
                            if (root == null) {
                                root = txn;
                            } else {
                                root.join(txn);
                            }
                            it.remove();
                        }
                    }
                    
                    if (root != null)
                        root.commit();
                }
            }
        }
    }
}
