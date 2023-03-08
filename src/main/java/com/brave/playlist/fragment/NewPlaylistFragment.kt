package com.brave.playlist.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.brave.playlist.PlaylistViewModel
import com.brave.playlist.R
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.model.CreatePlaylistModel
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.model.RenamePlaylistModel
import com.brave.playlist.util.ConstantUtils.PLAYLIST_MODEL
import com.brave.playlist.util.ConstantUtils.PLAYLIST_OPTION
import com.brave.playlist.util.ConstantUtils.SHOULD_MOVE_OR_COPY
import com.brave.playlist.view.PlaylistToolbar

class NewPlaylistFragment : Fragment(R.layout.fragment_new_playlist) {
    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var etPlaylistName: AppCompatEditText
    private lateinit var playlistToolbar: PlaylistToolbar
    private var playlistModel: PlaylistModel? = null
    private var playlistOptions: PlaylistOptions = PlaylistOptions.NEW_PLAYLIST
    private var shouldMoveOrCopy: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistModel = it.getParcelable(PLAYLIST_MODEL)
            playlistOptions = it.getSerializable(PLAYLIST_OPTION) as PlaylistOptions
            shouldMoveOrCopy = it.getBoolean(SHOULD_MOVE_OR_COPY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistViewModel = activity?.let {
            ViewModelProvider(
                it, ViewModelProvider.NewInstanceFactory()
            )
        }!![PlaylistViewModel::class.java]

        etPlaylistName = view.findViewById(R.id.etPlaylistName)
        if (playlistModel != null) {
            etPlaylistName.setText(playlistModel!!.name)
        }
        playlistToolbar = view.findViewById(R.id.playlistToolbar)
        playlistToolbar.setToolbarTitle(
            if (playlistOptions == PlaylistOptions.NEW_PLAYLIST) getString(
                R.string.playlist_new_text
            ) else getString(R.string.playlist_rename_text)
        )
        playlistToolbar.setActionText(
            if (playlistOptions == PlaylistOptions.NEW_PLAYLIST) getString(
                R.string.playlist_create_toolbar_text
            ) else getString(R.string.playlist_rename_text)
        )
        playlistToolbar.setActionButtonClickListener {
            if (playlistOptions == PlaylistOptions.NEW_PLAYLIST) {
                playlistViewModel.setCreatePlaylistOption(
                    CreatePlaylistModel(
                        etPlaylistName.text.toString(),
                        shouldMoveOrCopy
                    )
                )
            } else {
                playlistViewModel.setRenamePlaylistOption(
                    RenamePlaylistModel(
                        playlistModel?.id,
                        etPlaylistName.text.toString()
                    )
                )
            }
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        etPlaylistName.requestFocus()
    }

    companion object {
        @JvmStatic
        fun newInstance(
            playlistOptions: PlaylistOptions,
            playlistModel: PlaylistModel? = null,
            shouldMoveOrCopy: Boolean = false
        ) =
            NewPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(PLAYLIST_OPTION, playlistOptions)
                    putParcelable(PLAYLIST_MODEL, playlistModel)
                    putBoolean(SHOULD_MOVE_OR_COPY, shouldMoveOrCopy)
                }
            }
    }
}