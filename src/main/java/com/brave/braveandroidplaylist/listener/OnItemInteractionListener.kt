package com.brave.braveandroidplaylist.listener

import com.brave.braveandroidplaylist.model.MediaModel

interface OnItemInteractionListener {

    fun onItemDelete()

    fun onRemoveFromOffline(position: Int)

    fun onUpload(position: Int)

    fun onPlaylistItemClick(mediaModel: MediaModel)

    fun onPlaylistItemClick(count:Int)
}