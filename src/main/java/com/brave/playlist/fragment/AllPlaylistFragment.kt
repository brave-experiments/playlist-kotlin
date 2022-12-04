package com.brave.playlist.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brave.playlist.PlaylistViewModel
import com.brave.playlist.R
import com.brave.playlist.adapter.MediaItemAdapter
import com.brave.playlist.adapter.PlaylistAdapter
import com.brave.playlist.adapter.RecentlyPlayedPlaylistAdapter
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.listener.MediaItemGestureHelper
import com.brave.playlist.model.MediaModel
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.util.ConstantUtils
import com.brave.playlist.view.PlaylistToolbar
import org.json.JSONArray
import org.json.JSONObject

class AllPlaylistFragment : Fragment(R.layout.fragment_all_playlist) {
    private lateinit var playlistViewModel: PlaylistViewModel

    private lateinit var playlistToolbar: PlaylistToolbar
    private lateinit var btAddNewPlaylist: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            playlistModel = it.getParcelable(ConstantUtils.PLAYLIST)
//            playlistOptions = it.getSerializable(ConstantUtils.PLAYLIST_OPTION) as PlaylistOptions
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistViewModel = activity?.let {
            ViewModelProvider(
                it, ViewModelProvider.NewInstanceFactory()
            )
        }!![PlaylistViewModel::class.java]

        playlistToolbar = view.findViewById(R.id.playlistToolbar)
        playlistToolbar.setOptionsButtonClickListener(View.OnClickListener {

        })

        btAddNewPlaylist = view.findViewById(R.id.btAddNewPlaylist)

        btAddNewPlaylist.setOnClickListener {

        }

        playlistViewModel.allPlaylistData.observe(viewLifecycleOwner) { allPlaylistData ->

        }

        val rvRecentlyPlayed: RecyclerView = view.findViewById(R.id.rvRecentlyPlayed)
        val rvPlaylist: RecyclerView = view.findViewById(R.id.rvPlaylists)
        rvRecentlyPlayed.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvRecentlyPlayed.adapter = RecentlyPlayedPlaylistAdapter(mutableListOf())
        rvPlaylist.layoutManager = LinearLayoutManager(requireContext())
        rvPlaylist.adapter = PlaylistAdapter(mutableListOf())
    }

    companion object {
        @JvmStatic
        fun newInstance(playlistModel: PlaylistModel, playlistOptions: PlaylistOptions) =
            AllPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ConstantUtils.PLAYLIST, playlistModel)
                    putSerializable(ConstantUtils.PLAYLIST_OPTION, playlistOptions)
                }
            }
    }
}