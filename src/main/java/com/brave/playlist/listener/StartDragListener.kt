package com.brave.playlist.listener

import androidx.recyclerview.widget.RecyclerView

interface StartDragListener {
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {}
}