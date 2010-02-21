/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2005 Roger Kapsi, info at kapsi dot de
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

import java.util.Timer;
import java.util.TimerTask;

/**
 * An auto commiting Transaction.
 * 
 * @author Roger Kapsi
 */
public class AutoCommitTransaction extends Transaction {
    
    public static final long TIMEOUT = 10000; // 10 Seconds
    public static final int ENFORCE_COMMIT = 100;
    
    protected Timer timer;
    protected TimerTask commitTask;
    
    protected Transaction transaction;
    
    protected long touched = 0;
    protected int txnCounter = 0;
    
    protected long timeout;
    protected int enforceCommit;
    
    public AutoCommitTransaction(Library library) {
        this(library, TIMEOUT, ENFORCE_COMMIT);
    }
    
    /**
     * Transaction will either auto commit after <code>timeout</code> 
     * or after <code>enforceCommit</code> modifications of the Library.
     * 
     * @param library
     * @param timeout
     * @param enforceTimeout
     */
    public AutoCommitTransaction(Library library, long timeout, int enforceCommit) {
        super(library);
        
        this.timeout = timeout;
        this.enforceCommit = enforceCommit;
    }
    
    public int getEnforceCommit() {
        return enforceCommit;
    }
    
    public synchronized void setEnforceCommit(int enforceCommit) {
        this.enforceCommit = enforceCommit;
    }
    
    public long getTimeout() {
        return timeout;
    }
    
    public synchronized void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    
    private synchronized void createTransactionIfNecessary() {
        touch();
        
        if (transaction == null) {
            transaction = library.beginTransaction();
            
            if (timer == null) {
                timer = new Timer();
            }
            
            if (commitTask == null) {
                commitTask = new CommitTask();
                timer.scheduleAtFixedRate(commitTask, 1000, 500);
            }
        }
    }
    
    protected synchronized void addTxn(Object obj, Txn txn) {
        createTransactionIfNecessary();
        transaction.addTxn(obj, txn);
        
        if (enforceCommit > 0) {
            txnCounter++;
        
            if (txnCounter >= enforceCommit) {
                commit();
            }
        }
    }
    
    protected synchronized void attach(Object obj) {
        createTransactionIfNecessary();
        transaction.attach(obj);
    }

    protected synchronized boolean modified(Database database) {
        createTransactionIfNecessary();
        return transaction.modified(database);
    }

    protected synchronized boolean modified(Library library) {
        createTransactionIfNecessary();
        return transaction.modified(library);
    }

    protected synchronized boolean modified(Playlist playlist) {
        createTransactionIfNecessary();
        return transaction.modified(playlist);
    }

    protected synchronized boolean modified(Song song) {
        createTransactionIfNecessary();
        return transaction.modified(song);
    }

    public synchronized void commit() {
        if (transaction != null) {
            
            transaction.commit();
            
            commitTask.cancel();
            timer.cancel();
            
            transaction = null;
            commitTask = null;
            timer = null;
            txnCounter = 0;
            touched = 0;
        }
    }
    
    public synchronized void rollback() {
        if (transaction != null) {
            transaction.rollback();
            
            commitTask.cancel();
            timer.cancel();
            
            transaction = null;
            commitTask = null;
            timer = null;
            txnCounter = 0;
            touched = 0;
        }
    }
    
    protected synchronized void touch() {
        touched = System.currentTimeMillis();
    }
    
    private class CommitTask extends TimerTask {
        public void run() {
            synchronized(AutoCommitTransaction.this) {
                if (System.currentTimeMillis() - touched >= timeout) {
                    commit();
                }
            }
        }
    }
}
