package com.brave.playlist.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brave.playlist.PlaylistViewModel
import com.brave.playlist.R
import com.brave.playlist.adapter.PlaylistAdapter
import com.brave.playlist.adapter.RecentlyPlayedPlaylistAdapter
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.listener.PlaylistClickListener
import com.brave.playlist.listener.PlaylistOptionsListener
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.model.PlaylistOptionsModel
import com.brave.playlist.util.ConstantUtils
import com.brave.playlist.util.ConstantUtils.DEFAULT_PLAYLIST
import com.brave.playlist.util.MenuUtils
import com.brave.playlist.util.PlaylistPreferenceUtils
import com.brave.playlist.util.PlaylistPreferenceUtils.recentlyPlayedPlaylist
import com.brave.playlist.view.PlaylistToolbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.LinkedList

class AllPlaylistFragment : Fragment(R.layout.fragment_all_playlist), PlaylistOptionsListener,
    PlaylistClickListener {
    private lateinit var playlistViewModel: PlaylistViewModel

    private lateinit var playlistToolbar: PlaylistToolbar
    private lateinit var btAddNewPlaylist: AppCompatButton
    private lateinit var rvRecentlyPlayed: RecyclerView
    private lateinit var rvPlaylist: RecyclerView
    private lateinit var tvRecentlyPlayed: TextView
    private lateinit var tvPlaylistHeader: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistViewModel = activity?.let {
            ViewModelProvider(
                it, ViewModelProvider.NewInstanceFactory()
            )
        }!![PlaylistViewModel::class.java]

        playlistToolbar = view.findViewById(R.id.playlistToolbar)

        btAddNewPlaylist = view.findViewById(R.id.btAddNewPlaylist)
        btAddNewPlaylist.setOnClickListener {
            val newPlaylistFragment = NewPlaylistFragment.newInstance(
                PlaylistOptions.NEW_PLAYLIST
            )
            parentFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, newPlaylistFragment)
                .addToBackStack(AllPlaylistFragment::class.simpleName)
                .commit()
        }
        rvRecentlyPlayed = view.findViewById(R.id.rvRecentlyPlayed)
        rvPlaylist = view.findViewById(R.id.rvPlaylists)

        tvRecentlyPlayed = view.findViewById(R.id.tvRecentlyPlayed)
        tvPlaylistHeader = view.findViewById(R.id.tvPlaylistHeader)

        playlistViewModel.fetchPlaylistData(ConstantUtils.ALL_PLAYLIST)

        playlistViewModel.allPlaylistData.observe(viewLifecycleOwner) { allPlaylistData ->
            Log.e("NTP", allPlaylistData.toString())
            val allPlaylistList = mutableListOf<PlaylistModel>()

//            val allPlaylistJson : String = GsonBuilder().serializeNulls().create().toJson(allPlaylistData, TypeToken.getParameterized(List::class.java, PlaylistModel::class.java).type)
//            Log.e("BravePlaylist", allPlaylistJson)

            var defaultPlaylistModel: PlaylistModel? = null
            for (allPlaylistModel in allPlaylistData) {
                val playlistModel = PlaylistModel(
                    allPlaylistModel.id,
                    allPlaylistModel.name,
                    allPlaylistModel.items
                )

                if (playlistModel.id == DEFAULT_PLAYLIST) {
                    defaultPlaylistModel = playlistModel
                } else {
                    allPlaylistList.add(
                        playlistModel
                    )
                }
            }
            defaultPlaylistModel?.let { allPlaylistList.add(0, it) }

            val recentPlaylistJson =
                PlaylistPreferenceUtils.defaultPrefs(requireContext()).recentlyPlayedPlaylist
            if (!recentPlaylistJson.isNullOrEmpty()) {
                val recentPlaylist = LinkedList<PlaylistModel>()
                val recentPlaylistIds: LinkedList<String> = GsonBuilder().create().fromJson(
                    recentPlaylistJson,
                    TypeToken.getParameterized(LinkedList::class.java, String::class.java).type
                )
                if (recentPlaylistIds.size > 0) {
                    recentPlaylistIds.forEach ids@{
                        allPlaylistList.forEach models@{ model ->
                            if (model.id == it && model.items.isNotEmpty()) {
                                recentPlaylist.add(model)
                                return@models
                            }
                        }
                    }
                }
                rvRecentlyPlayed.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                rvRecentlyPlayed.adapter = RecentlyPlayedPlaylistAdapter(recentPlaylist, this)
                rvRecentlyPlayed.visibility =
                    if (recentPlaylist.isNotEmpty()) View.VISIBLE else View.GONE
                tvRecentlyPlayed.visibility =
                    if (recentPlaylist.isNotEmpty()) View.VISIBLE else View.GONE
                tvPlaylistHeader.visibility =
                    if (recentPlaylist.isNotEmpty()) View.VISIBLE else View.GONE
            }

            playlistToolbar.setOptionsButtonClickListener {
                MenuUtils.showAllPlaylistsMenu(
                    it.context,
                    parentFragmentManager,
                    allPlaylistList,
                    this
                )
            }

            rvPlaylist.layoutManager = LinearLayoutManager(requireContext())
            rvPlaylist.adapter = PlaylistAdapter(allPlaylistList, this)
        }
    }

    override fun onOptionClicked(playlistOptionsModel: PlaylistOptionsModel) {
        playlistViewModel.setAllPlaylistOption(playlistOptionsModel)
    }

    override fun onPlaylistClick(playlistModel: PlaylistModel) {
        playlistViewModel.setPlaylistToOpen(playlistModel.id)
    }
}
