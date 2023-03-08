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
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.model.PlaylistOptionsModel
import com.brave.playlist.util.ConstantUtils
import com.brave.playlist.util.ConstantUtils.DEFAULT_PLAYLIST
import com.brave.playlist.util.MenuUtils
import com.brave.playlist.util.PlaylistPreferenceUtils
import com.brave.playlist.util.PlaylistPreferenceUtils.get
import com.brave.playlist.view.PlaylistToolbar
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
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
    }

    override fun onResume() {
        super.onResume()
        playlistViewModel.fetchPlaylistData(ConstantUtils.ALL_PLAYLIST)

        playlistViewModel.allPlaylistData.observe(viewLifecycleOwner) { allPlaylistData ->
            val allPlaylistList = mutableListOf<PlaylistModel>()
            val allPlaylistJsonArray = JSONArray(allPlaylistData)

            var recentPlaylistIds = LinkedList<String>()
            val recentPlaylist = LinkedList<PlaylistModel>()
            val recentPlaylistJson =
                PlaylistPreferenceUtils.defaultPrefs(requireContext())[PlaylistPreferenceUtils.RECENTLY_PLAYED_PLAYLIST, ""]
            if (recentPlaylistJson.isNotEmpty()) {
                recentPlaylistIds = GsonBuilder().create().fromJson(
                    recentPlaylistJson,
                    TypeToken.getParameterized(LinkedList::class.java, String::class.java).type
                )
                Log.e("recent_playlist", "All playlist : recentPlaylistJson : " + recentPlaylistIds)
            }

            var defaultPlaylistModel: PlaylistModel? = null
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
                        jsonObject.getInt("last_played_position"),
                        jsonObject.getBoolean("cached")
                    )
                    playlistList.add(playlistItemModel)
                }

                val playlistModel = PlaylistModel(
                    playlistJsonObject.getString("id"),
                    playlistJsonObject.getString("name"),
                    playlistList
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

            recentPlaylist.forEach {
                Log.e("recent_playlist", "\nafter All playlist : recentPlaylistJson : " + it.id)
            }

            playlistToolbar.setOptionsButtonClickListener {
                MenuUtils.showAllPlaylistsMenu(
                    it.context,
                    parentFragmentManager,
                    allPlaylistList,
                    this
                )
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