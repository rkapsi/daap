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
 * This interfce enables us to implement a custom authenticator.
 *
 * @author  Roger Kapsi
 */
public interface DaapAuthenticator {
    
    /**
     * Return <tt>true</tt> if authentication is required
     */
    public boolean requiresAuthentication();
    
    /**
     * Return <tt>true</tt> if username and password are
     * correct. Note: iTunes doesn't support usernames
     * currently!
     */
    public boolean authenticate(String username, String password);
}
