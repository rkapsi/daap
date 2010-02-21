/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004, 2005 Roger Kapsi, info at kapsi dot de
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ardverk.daap.chunks.impl.HasChildContainers;
import org.ardverk.daap.chunks.impl.ParentContainerId;

/**
 * A Folder is a Playlist of Playlists
 * 
 * @since iTunes 5.0
 * @author Roger Kapsi
 */
public class Folder extends Playlist {
    
    /** */
    private final ParentContainerId parentContainerId = new ParentContainerId();
    
    // @since iTunes 5.0
    private HasChildContainers hasChildContainers = new HasChildContainers(true);
    
    /** */
    private List<Playlist> playlists = null;
    
    protected Folder(Playlist playlist, Transaction txn) {
        super(playlist, txn);
        parentContainerId.setValue(getItemId());
        playlists = ((Folder)playlist).playlists;
        init();
    }

    public Folder(String name) {
        super(name);
        parentContainerId.setValue(getItemId());
        init();
    }
    
    private void init() {
        addChunk(hasChildContainers);
    }
    
    public void addSong(Transaction txn, Song song) {
        throw new UnsupportedOperationException("Songs cannot be added to Folders");
    }
    
    public void removeSong(Transaction txn, Song song) {
        throw new UnsupportedOperationException("Songs cannot be removed from Folders");
    }
    
    public boolean containsSong(Song song) {
        return false;
    }

    public int getSongCount() {
        return 0;
    }

    public List<Song> getSongs() {
        return Collections.emptyList();
    }
    
    public void addPlaylist(Transaction txn, final Playlist playlist) {
        if (playlist instanceof Folder) {
            throw new IllegalArgumentException("Recursion is not supported");
        }
        
        if (txn != null) {
            txn.addTxn(this, new Txn() {
                public void commit(Transaction txn) {
                    addPlaylistP(txn, playlist);
                }
            });
            txn.attach(playlist);
        } else {
            addPlaylistP(txn, playlist);
        }
    }
    
    private void addPlaylistP(Transaction txn, Playlist playlist) {
        if (playlists == null) {
            playlists = new ArrayList<Playlist>();
        }
        
        if (!containsPlaylist(playlist) && playlists.add(playlist)) {
            playlist.addChunk(parentContainerId);
        }
    }
    
    public void removePlaylist(Transaction txn, final Playlist playlist) {
        if (playlist instanceof Folder) {
            return;
        }
        
        if (txn != null) {
            txn.addTxn(this, new Txn() {
                public void commit(Transaction txn) {
                    removePlaylistP(txn, playlist);
                }
            });
            txn.attach(playlist);
        } else {
            removePlaylistP(txn, playlist);
        }
    }
    
    private void removePlaylistP(Transaction txn, Playlist playlist) {
        if (playlists == null) {
            return;
        }
        
        if (playlists.remove(playlist)) {
            playlist.removeChunk(parentContainerId);
            
            if (playlists.isEmpty()) {
                playlists = null;
            }
        }
    }
    
    public int getPlaylistCount() {
        return getPlaylists().size();
    }
    
    public List<Playlist> getPlaylists() {
        if (playlists != null) {
            return Collections.unmodifiableList(playlists);
        } else {
            return Collections.emptyList();
        }
    }
    
    public boolean containsPlaylist(Playlist playlist) {
        return getPlaylists().contains(playlist);
    }
}
