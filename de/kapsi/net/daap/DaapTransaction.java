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

import java.util.HashMap;

/**
 * A very simple Transaction Manager.
 * 
 * @author Roger Kapsi
 */
public class DaapTransaction {

    private static ThreadLocal CONTEXT = new ThreadLocal();

    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_COMMITING = 2;
    public static final int STATUS_COMMITED = 3;
    public static final int STATUS_ROLLING_BACK = 4;
    public static final int STATUS_ROLLEDBACK = 5;

    /**
     * Create a new transaction and associate it with the current thread. Nested
     * transactions are not supported (i.e. multible transactions per thread).
     * You must complete a transaction (with commit() or rollback()) berfore a
     * new transaction can be started.
     * 
     * @param library
     * @return @throws
     *         DaapTransactionException
     */
    public static DaapTransaction open(Library library)
            throws DaapTransactionException {
        if (isOpen())
            throw new DaapTransactionException(
                    "Nested operations are not supported.");

        DaapTransaction trx = new DaapTransaction(library);
        CONTEXT.set(trx);
        return trx;
    }

    /**
     * Get the transaction object that represents the transaction ontext of the
     * calling thread.
     * 
     * @return
     */
    public static DaapTransaction getTransaction() {
        return (DaapTransaction) CONTEXT.get();
    }

    /**
     * Returns true if a transaction object is associated with the calling
     * thread.
     * 
     * @return
     */
    public static boolean isOpen() {
        return (getTransaction() != null);
    }

    private Library library;
    private int status;
    private HashMap map;

    private DaapTransaction(Library library) {
        this.library = library;

        map = new HashMap();
        status = STATUS_ACTIVE;
    }

    /**
     * Returns the status of this transaction
     * 
     * @return
     */
    public int getStatus() {
        return status;
    }

    /**
     * Complete the transaction associated with the current thread.
     * 
     * @throws DaapTransactionException
     */
    public void commit() throws DaapTransactionException {

        if (!isOpen()) {
            throw new DaapTransactionException(
                    "Current Thread is not associated with a transaction.");
        }

        status = STATUS_COMMITING;

        try {
            library.commit();
        } catch (CloneNotSupportedException err) {
            throw new DaapTransactionException(err.getMessage());
        } finally {
            status = STATUS_COMMITED;
            library = null;
            map.clear();
            map = null;
            CONTEXT.set(null);
        }
    }

    /**
     * Roll back the transaction associated with the current thread.
     * 
     * @throws DaapTransactionException
     */
    public void rollback() throws DaapTransactionException {

        if (!isOpen()) {
            throw new DaapTransactionException(
                    "Current Thread is not associated with a transaction.");
        }

        status = STATUS_ROLLING_BACK;

        try {
            library.rollback();
        } finally {
            status = STATUS_ROLLEDBACK;
            library = null;
            map.clear();
            map = null;
            CONTEXT.set(null);
        }
    }

    /**
     * Objects can attach attributes to transaction objects. The calling thread
     * must be associated with this transaction.
     * 
     * @param key
     * @param value
     * @throws DaapTransactionException
     */
    void setAttribute(Object key, Object value) throws DaapTransactionException {

        if (!isOpen()) {
            throw new DaapTransactionException(
                    "Current Thread is not associated with a transaction.");
        }

        if (value != null) {
            map.put(key, value);
        } else {
            map.remove(key);
        }
    }

    /**
     * 
     * @param key
     * @return @throws
     *         DaapTransactionException
     */
    Object getAttribute(Object key) throws DaapTransactionException {

        if (!isOpen()) {
            throw new DaapTransactionException(
                    "Current Thread is not associated with a transaction.");
        }

        return map.get(key);
    }

    /**
     * 
     * @param key
     * @return @throws
     *         DaapTransactionException
     */
    boolean hasAttribute(Object key) throws DaapTransactionException {

        if (!isOpen()) {
            throw new DaapTransactionException(
                    "Current Thread is not associated with a transaction.");
        }

        return map.containsKey(key);
    }
}