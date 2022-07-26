package com.brave.braveandroidplaylist.listener

interface OnItemInteractionListener {

    fun onItemDelete()

    fun onRemoveFromOffline(position: Int)

    fun onUpload(position: Int)
}