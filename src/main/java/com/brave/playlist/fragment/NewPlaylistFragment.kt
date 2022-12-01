package com.brave.playlist.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.brave.playlist.PlaylistViewModel
import com.brave.playlist.R
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.view.PlaylistToolbar

class NewPlaylistFragment : Fragment(R.layout.fragment_new_playlist) {

    private lateinit var viewModel: PlaylistViewModel
    private lateinit var etPlaylistName: AppCompatEditText
    private lateinit var playlistToolbar: PlaylistToolbar
    private var playlistModel: PlaylistModel? = null
    private var playlistOptions: PlaylistOptions = PlaylistOptions.NEW_PLAYLIST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistModel = it.getParcelable(PLAYLIST)
            playlistOptions = it.getSerializable(PLAYLIST_OPTION) as PlaylistOptions
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = activity?.let {
            ViewModelProvider(
                it, ViewModelProvider.NewInstanceFactory()
            )
        }!![PlaylistViewModel::class.java]

        etPlaylistName = view.findViewById(R.id.etPlaylistName)
        if (playlistModel != null) {
            etPlaylistName.setText(playlistModel!!.name)
        }
        playlistToolbar = view.findViewById(R.id.playlistToolbar)
        playlistToolbar.setToolbarTitle(if (playlistOptions == PlaylistOptions.NEW_PLAYLIST) getString(R.string.new_playlist) else getString(R.string.rename_text))
        playlistToolbar.setActionText(if (playlistOptions == PlaylistOptions.NEW_PLAYLIST) getString(R.string.create_toolbar_playlist) else getString(R.string.rename_text))
        playlistToolbar.setActionButtonClickListener {

        }

        etPlaylistName.requestFocus()
    }

    companion object {
        const val PLAYLIST = "playlist"
        const val PLAYLIST_OPTION = "playlist_option"

        @JvmStatic
        fun newInstance(playlistModel: PlaylistModel, playlistOptions: PlaylistOptions) =
            NewPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PLAYLIST, playlistModel)
                    putSerializable(PLAYLIST_OPTION, playlistOptions)
                }
            }
    }
}