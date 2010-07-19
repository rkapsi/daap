/*
 * Digital Audio Access Protocol (DAAP) Library
 * Copyright (C) 2004-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     * Transaction will either auto commit after <code>timeout</code> or after
     * <code>enforceCommit</code> modifications of the Library.
     * 
     * @param library
     * @param timeout
     * @param enforceTimeout
     */
    public AutoCommitTransaction(Library library, long timeout,
            int enforceCommit) {
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
            synchronized (AutoCommitTransaction.this) {
                if (System.currentTimeMillis() - touched >= timeout) {
                    commit();
                }
            }
        }
    }
}
