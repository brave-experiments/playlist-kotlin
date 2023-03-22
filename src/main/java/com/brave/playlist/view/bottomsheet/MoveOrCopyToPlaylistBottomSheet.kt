package com.brave.playlist.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brave.playlist.PlaylistViewModel
import com.brave.playlist.R
import com.brave.playlist.adapter.PlaylistAdapter
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.extension.setTopCornersRounded
import com.brave.playlist.fragment.AllPlaylistFragment
import com.brave.playlist.fragment.NewPlaylistFragment
import com.brave.playlist.listener.PlaylistClickListener
import com.brave.playlist.model.MoveOrCopyModel
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.util.ConstantUtils
import com.brave.playlist.util.PlaylistUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView

class MoveOrCopyToPlaylistBottomSheet :
    BottomSheetDialogFragment(), PlaylistClickListener {

    private lateinit var playlistViewModel: PlaylistViewModel
    private val moveOrCopyModel: MoveOrCopyModel = PlaylistUtils.moveOrCopyModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_add_or_move_to_playlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistViewModel = activity?.let {
            ViewModelProvider(
                it, ViewModelProvider.NewInstanceFactory()
            )
        }!![PlaylistViewModel::class.java]

        val layoutBottomSheet: MaterialCardView = view.findViewById(R.id.layoutBottomSheet)
        layoutBottomSheet.setTopCornersRounded(16)

        var fromPlaylistId = ""
        if (moveOrCopyModel.items.isNotEmpty()) {
            fromPlaylistId = moveOrCopyModel.items[0].playlistId
        }

        playlistViewModel.fetchPlaylistData(ConstantUtils.ALL_PLAYLIST)

        playlistViewModel.allPlaylistData.observe(viewLifecycleOwner) { allPlaylistData ->
            val allPlaylistList = mutableListOf<PlaylistModel>()
            for (allPlaylistModel in allPlaylistData) {
                if (allPlaylistModel.id != fromPlaylistId) {
                    allPlaylistList.add(
                        PlaylistModel(
                            allPlaylistModel.id,
                            allPlaylistModel.name,
                            allPlaylistModel.items
                        )
                    )
                }
            }

            allPlaylistList.add(
                0,
                PlaylistModel(
                    ConstantUtils.NEW_PLAYLIST,
                    getString(R.string.playlist_new_text),
                    arrayListOf()
                )
            )

            val rvPlaylists: RecyclerView = view.findViewById(R.id.rvPlaylists)
            rvPlaylists.layoutManager = LinearLayoutManager(view.context)
            rvPlaylists.adapter = PlaylistAdapter(allPlaylistList, this)
        }

        val behavior = BottomSheetBehavior.from(layoutBottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
    }

    override fun onPlaylistClick(playlistModel: PlaylistModel) {
        if (playlistModel.id == ConstantUtils.NEW_PLAYLIST) {
            PlaylistUtils.moveOrCopyModel =
                MoveOrCopyModel(moveOrCopyModel.playlistOptions, "", moveOrCopyModel.items)
            val newPlaylistFragment = NewPlaylistFragment.newInstance(
                PlaylistOptions.NEW_PLAYLIST,
                shouldMoveOrCopy = true
            )
            parentFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, newPlaylistFragment)
                .addToBackStack(AllPlaylistFragment::class.simpleName)
                .commit()
        } else {
            PlaylistUtils.moveOrCopyModel = MoveOrCopyModel(
                moveOrCopyModel.playlistOptions,
                playlistModel.id,
                moveOrCopyModel.items
            )
            playlistViewModel.performMoveOrCopy(PlaylistUtils.moveOrCopyModel)
        }
        dismiss()
    }
}
