package com.brave.playlist.listener

import com.brave.playlist.model.PlaylistItemModel

interface PlaylistItemClickListener {
    fun onPlaylistItemClick(playlistItemModel: PlaylistItemModel) {}
    fun onPlaylistItemClick(count:Int) {}
//    fun onPlaylistItemClick(position:Int, mediaItem: MediaItem) {}
}