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
 * An abstract base class for DaapAuthResponses which is from client's
 * perspective an request for authentication. Derived classes implement
 * the actual DaapAuthResponse.
 *
 * @author  Roger Kapsi
 */
public abstract class DaapAuthResponse implements DaapResponse {
    
    protected final DaapRequest request;
    protected final byte[] header;
    
    /** Creates a new instance of DaapAuthResponse */
    public DaapAuthResponse(DaapRequest request) {
        this.request = request;
        
        DaapServer server = request.getServer();
        DaapConfig config = server.getConfig();
        Object scheme = config.getAuthenticationScheme();
        
        if (scheme.equals(DaapConfig.BASIC_SCHEME)) {
            header = DaapHeaderConstructor.createBasicAuthHeader(request);
        } else {
            header = DaapHeaderConstructor.createDigestAuthHeader(request);
        }
    }
    
    public String toString() {
        return (new String(header));
    }
}
