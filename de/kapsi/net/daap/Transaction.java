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
     * 
     * @return
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }
     
    /**
     * 
     * @return
     */
    public synchronized boolean isOpen() {
        return open;
    }
    
    /**
     * 
     * @return
     */
    public synchronized long lastModified() {
        return lastModified;
    }
    
    /**
     * 
     *
     */
    public synchronized void touch() {
        lastModified = System.currentTimeMillis();
    }
    
    /**
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
     * 
     * @param txn
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
     * 
     * @param l
     */
    public synchronized void addTransactionListener(TransactionListener l) {
        listener.add(l);
    }
    
    /**
     * 
     * @param l
     */
    public synchronized void removeTransactionListener(TransactionListener l) {
        listener.remove(l);
    }
    
    /**
     * Objects can attach attributes to transaction objects. The calling thread
     * must be associated with this transaction.
     * 
     * @param key
     * @param value
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
     * 
     * @param key
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
     * 
     * @param key
     * @return @throws
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
     * 
     */
    private static final class AutoCommitTask extends TimerTask {
        
        private ArrayList list = new ArrayList();
        
        private AutoCommitTask() {
            
        }
        
        private void add(Transaction txn) {
            list.add(txn);
        }
        
        public void run() {
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
