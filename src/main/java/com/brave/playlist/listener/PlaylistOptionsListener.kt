package com.brave.playlist.listener

import com.brave.playlist.model.PlaylistOptionsModel

interface PlaylistOptionsListener {
    fun onOptionClicked(playlistOptionsModel: PlaylistOptionsModel)
}