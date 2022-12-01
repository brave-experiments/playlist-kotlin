package com.brave.playlist.listener

import com.brave.playlist.model.MediaModel

interface OnItemInteractionListener {

    fun onItemDelete()

    fun onRemoveFromOffline(position: Int)

    fun onUpload(position: Int)

    fun onPlaylistItemClick(mediaModel: MediaModel)

    fun onPlaylistItemClick(count:Int)
}