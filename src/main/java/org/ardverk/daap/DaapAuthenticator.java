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

/**
 * This interfce enables us to implement a custom authenticator.
 * 
 * @author Roger Kapsi
 */
public interface DaapAuthenticator {

    /**
     * Return true if username and password are correct. URI and nonce are null
     * if BASIC authentication is selected and DIGEST if they are not null. If
     * DIGEST is selected is the password field equal to the so called result.
     * 
     * DIGEST:
     * 
     * String ha1 = (String)map.get(username); String ha2 =
     * DaapUtil.calculateHA2(uri); String digest = DaapUtil.digest(ha1, ha2,
     * nonce); return digest.equals(password);
     */
    public boolean authenticate(String username, String password, String uri,
            String nonce);
}
