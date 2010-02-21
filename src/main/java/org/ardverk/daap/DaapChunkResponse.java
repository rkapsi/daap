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

/**
* An abstract base class for DaapChunkRespones. A Chunk is either a single
* piece of data or a set of Chunks. Derived classes implement the actual
* DaapChunkResponse.
*
* @author  Roger Kapsi
*/
public abstract class DaapChunkResponse implements DaapResponse {

protected final DaapRequest request;
protected final byte[] data;
protected final byte[] header;

/** Creates a new instance of DaapChunkResponse */
public DaapChunkResponse(DaapRequest request, byte[] data) {
this.request = request;
this.data = data;

header = DaapHeaderConstructor.createChunkHeader(request, data.length);
}

public String toString() {
return (new String(header));
}
}