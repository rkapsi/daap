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

package de.kapsi.net.daap.chunks;

public final class ChunkClasses {
    public static final String[] names = {
        "de.kapsi.net.daap.chunks.impl.AuthenticationMethod",
        "de.kapsi.net.daap.chunks.impl.Bag",
        "de.kapsi.net.daap.chunks.impl.BasePlaylist",
        "de.kapsi.net.daap.chunks.impl.BrowseAlbumListing",
        "de.kapsi.net.daap.chunks.impl.BrowseArtistListing",
        "de.kapsi.net.daap.chunks.impl.BrowseComposerListing",
        "de.kapsi.net.daap.chunks.impl.BrowseGenreListing",
        "de.kapsi.net.daap.chunks.impl.Container",
        "de.kapsi.net.daap.chunks.impl.ContainerCount",
        "de.kapsi.net.daap.chunks.impl.ContainerItemId",
        "de.kapsi.net.daap.chunks.impl.ContentCodesName",
        "de.kapsi.net.daap.chunks.impl.ContentCodesNumber",
        "de.kapsi.net.daap.chunks.impl.ContentCodesResponse",
        "de.kapsi.net.daap.chunks.impl.ContentCodesType",
        "de.kapsi.net.daap.chunks.impl.DaapProtocolVersion",
        "de.kapsi.net.daap.chunks.impl.DatabaseBrowse",
        "de.kapsi.net.daap.chunks.impl.DatabaseCount",
        "de.kapsi.net.daap.chunks.impl.DatabasePlaylists",
        "de.kapsi.net.daap.chunks.impl.DatabaseSongs",
        "de.kapsi.net.daap.chunks.impl.DeletedIdListing",
        "de.kapsi.net.daap.chunks.impl.Dictionary",
        "de.kapsi.net.daap.chunks.impl.DmapProtocolVersion",
        "de.kapsi.net.daap.chunks.impl.ItemCount",
        "de.kapsi.net.daap.chunks.impl.ItemId",
        "de.kapsi.net.daap.chunks.impl.ItemKind",
        "de.kapsi.net.daap.chunks.impl.ItemName",
        "de.kapsi.net.daap.chunks.impl.Listing",
        "de.kapsi.net.daap.chunks.impl.ListingItem",
        "de.kapsi.net.daap.chunks.impl.LoginRequired",
        "de.kapsi.net.daap.chunks.impl.LoginResponse",
        "de.kapsi.net.daap.chunks.impl.NormVolume",
        "de.kapsi.net.daap.chunks.impl.ParentContainerId",
        "de.kapsi.net.daap.chunks.impl.PersistentId",
        "de.kapsi.net.daap.chunks.impl.PlaylistSongs",
        "de.kapsi.net.daap.chunks.impl.Resolve",
        "de.kapsi.net.daap.chunks.impl.ResolveInfo",
        "de.kapsi.net.daap.chunks.impl.ReturnedCount",
        "de.kapsi.net.daap.chunks.impl.ServerDatabases",
        "de.kapsi.net.daap.chunks.impl.ServerInfoResponse",
        "de.kapsi.net.daap.chunks.impl.ServerRevision",
        "de.kapsi.net.daap.chunks.impl.SessionId",
        "de.kapsi.net.daap.chunks.impl.SmartPlaylist",
        "de.kapsi.net.daap.chunks.impl.SongAlbum",
        "de.kapsi.net.daap.chunks.impl.SongArtist",
        "de.kapsi.net.daap.chunks.impl.SongBeatsPerMinute",
        "de.kapsi.net.daap.chunks.impl.SongBitrate",
        "de.kapsi.net.daap.chunks.impl.SongComment",
        "de.kapsi.net.daap.chunks.impl.SongCompilation",
        "de.kapsi.net.daap.chunks.impl.SongComposer",
        "de.kapsi.net.daap.chunks.impl.SongDataKind",
        "de.kapsi.net.daap.chunks.impl.SongDataUrl",
        "de.kapsi.net.daap.chunks.impl.SongDateAdded",
        "de.kapsi.net.daap.chunks.impl.SongDateModified",
        "de.kapsi.net.daap.chunks.impl.SongDescription",
        "de.kapsi.net.daap.chunks.impl.SongDisabled",
        "de.kapsi.net.daap.chunks.impl.SongDiscCount",
        "de.kapsi.net.daap.chunks.impl.SongDiscNumber",
        "de.kapsi.net.daap.chunks.impl.SongEqPreset",
        "de.kapsi.net.daap.chunks.impl.SongFormat",
        "de.kapsi.net.daap.chunks.impl.SongGenre",
        "de.kapsi.net.daap.chunks.impl.SongGrouping",
        "de.kapsi.net.daap.chunks.impl.SongRelativeVolume",
        "de.kapsi.net.daap.chunks.impl.SongSampleRate",
        "de.kapsi.net.daap.chunks.impl.SongSize",
        "de.kapsi.net.daap.chunks.impl.SongStartTime",
        "de.kapsi.net.daap.chunks.impl.SongStopTime",
        "de.kapsi.net.daap.chunks.impl.SongTime",
        "de.kapsi.net.daap.chunks.impl.SongTrackCount",
        "de.kapsi.net.daap.chunks.impl.SongTrackNumber",
        "de.kapsi.net.daap.chunks.impl.SongUserRating",
        "de.kapsi.net.daap.chunks.impl.SongYear",
        "de.kapsi.net.daap.chunks.impl.SpecifiedTotalCount",
        "de.kapsi.net.daap.chunks.impl.Status",
        "de.kapsi.net.daap.chunks.impl.StatusString",
        "de.kapsi.net.daap.chunks.impl.SupportsAutoLogout",
        "de.kapsi.net.daap.chunks.impl.SupportsBrowse",
        "de.kapsi.net.daap.chunks.impl.SupportsExtensions",
        "de.kapsi.net.daap.chunks.impl.SupportsIndex",
        "de.kapsi.net.daap.chunks.impl.SupportsPersistentIds",
        "de.kapsi.net.daap.chunks.impl.SupportsQuery",
        "de.kapsi.net.daap.chunks.impl.SupportsResolve",
        "de.kapsi.net.daap.chunks.impl.SupportsUpdate",
        "de.kapsi.net.daap.chunks.impl.TimeoutInterval",
        "de.kapsi.net.daap.chunks.impl.UpdateResponse",
        "de.kapsi.net.daap.chunks.impl.UpdateType"
    };
}
