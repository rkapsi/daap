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

package org.ardverk.daap;

import java.io.IOException;

/**
 * An interface for either NIO or BIO based DaapRespones
 * 
 * @author Roger Kapsi
 */
public interface DaapResponse {

    /**
     * Returns <code>true</code> if there are more bytes to write.
     * 
     * @return <code>true</code> if response has remining bytes in the buffer
     */
    public boolean hasRemaining();

    /**
     * Returns <code>true</code> when the write() operation is complete and
     * <code>false</code> when some bytes were left which shall be written at
     * the next iteration (NIO view, classic I/O DaapRespones will always return
     * <code>true</code>).
     * 
     * @throws IOException
     * @return <code>true</code> if write() operation is complete
     */
    public boolean write() throws IOException;
}