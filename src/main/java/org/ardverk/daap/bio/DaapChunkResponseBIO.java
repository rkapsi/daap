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

package org.ardverk.daap.bio;

import java.io.IOException;
import java.io.OutputStream;

import org.ardverk.daap.DaapChunkResponse;
import org.ardverk.daap.DaapRequest;

/**
 * A Chunk Response.
 * 
 * @author Roger Kapsi
 */
public class DaapChunkResponseBIO extends DaapChunkResponse {

    private boolean headerWritten = false;
    private boolean dataWritten = false;

    private OutputStream out;

    /** Creates a new instance of DaapChunkResponse */
    public DaapChunkResponseBIO(DaapRequest request, byte[] data) {
        super(request, data);

        DaapConnectionBIO connection = (DaapConnectionBIO) request
                .getConnection();
        out = connection.getOutputStream();
    }

    public boolean hasRemaining() {
        return !(headerWritten && dataWritten);
    }

    /**
     * 
     * @throws IOException
     * @return
     */
    public boolean write() throws IOException {

        if (!headerWritten) {
            out.write(header, 0, header.length);
            out.flush();

            headerWritten = true;
        }

        if (!dataWritten) {

            out.write(data, 0, data.length);
            out.flush();

            dataWritten = true;
        }

        return true;
    }
}