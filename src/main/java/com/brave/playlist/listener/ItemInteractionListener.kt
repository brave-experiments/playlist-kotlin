package com.brave.playlist.listener

interface ItemInteractionListener {
    fun onItemDelete(position: Int) {}
    fun onRemoveFromOffline(position: Int) {}

    fun onShare(position: Int) {}
}