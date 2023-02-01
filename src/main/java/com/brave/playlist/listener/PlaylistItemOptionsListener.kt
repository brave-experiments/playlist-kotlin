package com.brave.playlist.listener

import com.brave.playlist.model.PlaylistItemOptionModel

interface PlaylistItemOptionsListener {
    fun onOptionClicked(playlistItemOptionModel: PlaylistItemOptionModel) {}
}