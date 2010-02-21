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

package org.ardverk.daap;


/**
 * This interfce enables us to implement a custom authenticator.
 *
 * @author  Roger Kapsi
 */
public interface DaapAuthenticator {
    
    /**
     * Return true if username and password are correct. URI and nonce
     * are null if BASIC authentication is selected and DIGEST if they
     * are not null. If DIGEST is selected is the password field equal
     * to the so called result.
     * 
     * DIGEST:
     * 
     * String ha1 = (String)map.get(username);
     * String ha2 = DaapUtil.calculateHA2(uri);
     * String digest = DaapUtil.digest(ha1, ha2, nonce);
     * return digest.equals(password); 
     */
    public boolean authenticate(String username, String password, String uri, String nonce);
}
