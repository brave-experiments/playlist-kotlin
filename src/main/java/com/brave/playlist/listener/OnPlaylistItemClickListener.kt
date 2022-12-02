package com.brave.playlist.listener

import com.brave.playlist.model.MediaModel

interface OnPlaylistItemClickListener {
    fun onPlaylistItemClick(mediaModel: MediaModel)
    fun onPlaylistItemClick(count:Int)
}