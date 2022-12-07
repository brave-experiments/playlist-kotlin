package com.brave.playlist.listener

import com.brave.playlist.model.MediaModel
import com.google.android.exoplayer2.MediaItem

interface OnPlaylistItemClickListener {
    fun onPlaylistItemClick(mediaModel: MediaModel) {}
    fun onPlaylistItemClick(count:Int) {}
//    fun onPlaylistItemClick(position:Int, mediaItem: MediaItem) {}
}