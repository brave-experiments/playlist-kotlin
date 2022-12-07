package com.brave.playlist.listener

interface OnItemInteractionListener {
    fun onItemDelete() {}
    fun onRemoveFromOffline(position: Int) {}
    fun onUpload(position: Int) {}
}