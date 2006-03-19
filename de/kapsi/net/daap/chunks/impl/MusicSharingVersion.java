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

package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.UIntChunk;
import de.kapsi.net.daap.chunks.VersionChunk;

/**
 * Unknown purpose.
 * 
 * Is for some reason derivated from signed int. Should
 * be a VersionChunk!
 * 
 * @author  Roger Kapsi
 */
public class MusicSharingVersion extends UIntChunk {
    
    /** Creates a new instance of MusicSharingVersion */
    public MusicSharingVersion() {
        this(0);
    }
    
    public MusicSharingVersion(long sharingVersion) {
        super("aeSV", "com.apple.itunes.music-sharing-version", sharingVersion);
    }
    
    public int getMajorVersion() {
        return VersionChunk.getMajorVersion(getValue());
    }
    
    public int getMinorVersion() {
        return VersionChunk.getMinorVersion(getValue());
    }

    public int getMicroVersion() {
        return VersionChunk.getMicroVersion(getValue());
    }

    public String toString(int indent) {
        return indent(indent) + name + "(" + contentCode + "; uint)="
            + getMajorVersion() + "." + getMinorVersion() + "." + getMicroVersion();
    }
}