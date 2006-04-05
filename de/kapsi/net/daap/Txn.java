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

/**
 * A Txn is a internal implementation of a Transaction.
 * As a user of these classes you can safely ignore this!
 * 
 * @author Roger Kapsi
 */
/* public */ class Txn {
    
    /**
     * Called by Transaction with a reference to
     * itself
     * 
     * @param txn a Transaction
     */
    public void commit(Transaction txn) {
        // OVERWRITE IN SUBCLASSES
    }
    
    /**
     * Called by Transaction with a reference to
     * itself
     * 
     * @param txn a Transaction
     */
    public void rollback(Transaction txn) {
        // OVERWRITE IN SUBCLASSES
    }
}
