package com.brave.playlist.listener

import android.view.View
import com.brave.playlist.model.PlaylistItemModel

interface PlaylistItemClickListener {
    fun onPlaylistItemClick(playlistItemModel: PlaylistItemModel) {}
    fun onPlaylistItemClick(count: Int) {}

    fun onPlaylistItemMenuClick(view: View, playlistItemModel: PlaylistItemModel) {}
//    fun onPlaylistItemClick(position:Int, mediaItem: MediaItem) {}
}