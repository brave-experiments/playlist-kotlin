package com.brave.playlist.listener

import com.brave.playlist.model.PlaylistModel

interface OnPlaylistListener {
    fun onPlaylistClick(playlistModel: PlaylistModel) {}
}