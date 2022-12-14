package com.brave.playlist.listener

interface OnItemInteractionListener {
    fun onItemDelete(position: Int) {}
    fun onRemoveFromOffline(position: Int) {}
    fun onUpload(position: Int) {}
}