package com.brave.braveandroidplaylist.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.adapter.MediaItemAdapter
import com.brave.braveandroidplaylist.listener.MediaItemGestureHelper
import com.brave.braveandroidplaylist.listener.OnItemInteractionListener
import com.brave.braveandroidplaylist.listener.OnStartDragListener
import com.brave.braveandroidplaylist.view.PlaylistToolbar


class PlaylistFragment : Fragment(R.layout.fragment_playlist), OnItemInteractionListener,
    View.OnClickListener,
    OnStartDragListener {

    private lateinit var playlistToolbar: PlaylistToolbar
    private lateinit var rvPlaylist: RecyclerView
    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistToolbar = view.findViewById(R.id.playlistToolbar)
        rvPlaylist = view.findViewById(R.id.rvPlaylists)
        val adapter = MediaItemAdapter(mutableListOf(), this)
        val callback = MediaItemGestureHelper(view.context, rvPlaylist, adapter, this)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(rvPlaylist)
        rvPlaylist.adapter = adapter
    }

    override fun onItemDelete() {

    }

    override fun onRemoveFromOffline(position: Int) {

    }

    override fun onUpload(position: Int) {

    }

    override fun onClick(v: View) {
        //perform undo operation by communicating with adapter
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }
}