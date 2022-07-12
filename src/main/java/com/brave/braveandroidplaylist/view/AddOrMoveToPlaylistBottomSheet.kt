package com.brave.braveandroidplaylist.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.adapter.PlaylistAdapter
import com.brave.braveandroidplaylist.model.PlaylistModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddOrMoveToPlaylistBottomSheet(private val playlists: MutableList<PlaylistModel>) :
    BottomSheetDialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_add_or_move_to_playlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvPlaylists: RecyclerView = view.findViewById(R.id.rvPlaylists)
        rvPlaylists.layoutManager = LinearLayoutManager(view.context)
        rvPlaylists.adapter = PlaylistAdapter(playlists)

        val behavior = BottomSheetBehavior.from(view.findViewById(R.id.layoutBottomSheet))
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false

    }
}