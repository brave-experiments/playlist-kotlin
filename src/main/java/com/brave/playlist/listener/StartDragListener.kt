package com.brave.playlist.listener

import androidx.recyclerview.widget.RecyclerView

interface StartDragListener {
    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {}
}