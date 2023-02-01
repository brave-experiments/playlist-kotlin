package com.brave.playlist.fragment

import android.os.Bundle
import android.view.View
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
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.model.PlaylistOptionsModel
import com.brave.playlist.util.MenuUtils
import com.brave.playlist.view.PlaylistToolbar
import org.json.JSONArray

class AllPlaylistFragment : Fragment(R.layout.fragment_all_playlist), PlaylistOptionsListener,
    PlaylistClickListener {
    private lateinit var playlistViewModel: PlaylistViewModel

    private lateinit var playlistToolbar: PlaylistToolbar
    private lateinit var btAddNewPlaylist: AppCompatButton

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

        playlistViewModel.allPlaylistData.observe(viewLifecycleOwner) { allPlaylistData ->
            val allPlaylistList = mutableListOf<PlaylistModel>()
            val allPlaylistJsonArray = JSONArray(allPlaylistData)
            for (i in 0 until allPlaylistJsonArray.length()) {
                val playlistList = mutableListOf<PlaylistItemModel>()
                val playlistJsonObject = allPlaylistJsonArray.getJSONObject(i)
                val jsonArray: JSONArray = playlistJsonObject.getJSONArray("items")
                for (j in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(j)
                    val playlistItemModel = PlaylistItemModel(
                        jsonObject.getString("id"),
                        playlistJsonObject.getString("id"),
                        jsonObject.getString("name"),
                        jsonObject.getString("page_source"),
                        jsonObject.getString("media_path"),
                        jsonObject.getString("media_src"),
                        jsonObject.getString("thumbnail_path"),
                        jsonObject.getString("author"),
                        jsonObject.getString("duration"),
                        jsonObject.getBoolean("cached")
                    )
                    playlistList.add(playlistItemModel)
                }

                allPlaylistList.add(
                    PlaylistModel(
                        playlistJsonObject.getString("id"),
                        playlistJsonObject.getString("name"),
                        playlistList
                    )
                )
            }

            playlistToolbar.setOptionsButtonClickListener {
                MenuUtils.showAllPlaylistsMenu(it.context, parentFragmentManager,allPlaylistList , this)
            }

            val rvRecentlyPlayed: RecyclerView = view.findViewById(R.id.rvRecentlyPlayed)
            val rvPlaylist: RecyclerView = view.findViewById(R.id.rvPlaylists)
            rvRecentlyPlayed.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvRecentlyPlayed.adapter = RecentlyPlayedPlaylistAdapter(mutableListOf())
            rvPlaylist.layoutManager = LinearLayoutManager(requireContext())
            rvPlaylist.adapter = PlaylistAdapter(allPlaylistList, this)
        }
    }

    override fun onOptionClicked(playlistOptionsModel: PlaylistOptionsModel) {
        playlistViewModel.setAllPlaylistOption(playlistOptionsModel)
    }

    override fun onPlaylistClick(playlistModel: PlaylistModel) {
        if (playlistModel.items.isNotEmpty()) {
            playlistViewModel.setPlaylistToOpen(playlistModel.id)
        } else {
            parentFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, EmptyPlaylistFragment())
                .addToBackStack(AllPlaylistFragment::class.simpleName)
                .commit()
        }
    }
}