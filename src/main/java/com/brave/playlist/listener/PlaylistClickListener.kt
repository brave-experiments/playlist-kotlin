package com.brave.playlist.listener

import com.brave.playlist.model.PlaylistModel

interface PlaylistClickListener {
    fun onPlaylistClick(playlistModel: PlaylistModel) {}
}