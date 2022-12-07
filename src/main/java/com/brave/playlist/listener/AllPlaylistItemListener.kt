package com.brave.playlist.listener

import com.brave.playlist.model.PlaylistModel

interface AllPlaylistItemListener {
    fun onClickPlaylist(playlistModel: PlaylistModel) {}
}