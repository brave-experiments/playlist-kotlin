package com.brave.playlist.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brave.playlist.R
import com.brave.playlist.adapter.PlaylistOptionsBottomSheetAdapter
import com.brave.playlist.extension.setTopCornersRounded
import com.brave.playlist.listener.PlaylistOptionsListener
import com.brave.playlist.model.PlaylistOptionsModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView

class PlaylistOptionsBottomSheet(
    private val playlistOptionsModel: MutableList<PlaylistOptionsModel>,
    private val playlistOptionsListener: PlaylistOptionsListener
) :
    BottomSheetDialogFragment(), PlaylistOptionsListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_playlist_options, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutBottomSheet: MaterialCardView = view.findViewById(R.id.layoutBottomSheet)
        layoutBottomSheet.setTopCornersRounded(16)

        val rvBottomSheet: RecyclerView = view.findViewById(R.id.rvBottomSheet)
        rvBottomSheet.layoutManager = LinearLayoutManager(view.context)
        rvBottomSheet.adapter = PlaylistOptionsBottomSheetAdapter(playlistOptionsModel, this)

        val behavior = BottomSheetBehavior.from(layoutBottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false

        dialog?.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(android.R.color.transparent)
    }

    override fun onOptionClicked(playlistOptionsModel: PlaylistOptionsModel) {
        playlistOptionsListener.onOptionClicked(playlistOptionsModel)
        dismiss()
    }
}
