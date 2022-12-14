package com.brave.playlist.fragment

import android.os.Bundle
import android.util.Log
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
import com.brave.playlist.listener.OnPlaylistListener
import com.brave.playlist.listener.PlaylistOptionsListener
import com.brave.playlist.model.MediaModel
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.model.PlaylistOptionsModel
import com.brave.playlist.util.ConstantUtils
import com.brave.playlist.util.PlaylistUtils
import com.brave.playlist.view.PlaylistToolbar
import com.brave.playlist.view.bottomsheet.PlaylistOptionsBottomSheet
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class AllPlaylistFragment : Fragment(R.layout.fragment_all_playlist), PlaylistOptionsListener,
    OnPlaylistListener {
    private lateinit var playlistViewModel: PlaylistViewModel

    private lateinit var playlistToolbar: PlaylistToolbar
    private lateinit var btAddNewPlaylist: AppCompatButton

    private var playlistModel: PlaylistModel? = null

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
        playlistToolbar.setOptionsButtonClickListener {
            PlaylistOptionsBottomSheet(
                mutableListOf(
                    PlaylistOptionsModel(
                        it.resources.getString(R.string.edit_text),
                        R.drawable.ic_edit_playlist,
                        PlaylistOptions.EDIT_PLAYLIST,
                        playlistModel
                    ), PlaylistOptionsModel(
                        it.resources.getString(R.string.remove_all_offline_data),
                        R.drawable.ic_remove_offline_data_playlist,
                        PlaylistOptions.REMOVE_PLAYLIST_OFFLINE_DATA,
                        playlistModel
                    ), PlaylistOptionsModel(
                        it.resources.getString(R.string.download_all_playlists_for_offline_use),
                        R.drawable.ic_cloud_download,
                        PlaylistOptions.DOWNLOAD_PLAYLIST_FOR_OFFLINE_USE,
                        playlistModel
                    )
                ), this
            ).show(parentFragmentManager, null)
        }

        btAddNewPlaylist = view.findViewById(R.id.btAddNewPlaylist)
        btAddNewPlaylist.setOnClickListener {
            val newPlaylistFragment = NewPlaylistFragment.newInstance(
                null,
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
                val playlistList = mutableListOf<MediaModel>()
                val playlistJsonObject = allPlaylistJsonArray.getJSONObject(i)
                val jsonArray: JSONArray = playlistJsonObject.getJSONArray("items")
                for (j in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(j)
                    val mediaModel = MediaModel(
                        jsonObject.getString("id"),
                        jsonObject.getString("name"),
                        jsonObject.getString("page_source"),
                        jsonObject.getString("media_path"),
                        jsonObject.getString("media_src"),
                        jsonObject.getString("thumbnail_path"),
                        jsonObject.getString("author"),
                        jsonObject.getString("duration")
                    )
                    playlistList.add(mediaModel)
                }

                allPlaylistList.add(
                    PlaylistModel(
                        playlistJsonObject.getString("id"),
                        playlistJsonObject.getString("name"),
                        playlistList
                    )
                )
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
//        if (playlistModel.items.isNotEmpty()) {
//            val playlistJsonObject = JSONObject()
//            try {
//                playlistJsonObject.put("id", playlistModel.id)
//                playlistJsonObject.put("name", playlistModel.name)
//                val playlistItemsJsonArray = JSONArray()
//                for (playlistItem in playlistModel.items) {
//                    val playlistItemObject = JSONObject()
//                    playlistItemObject.put("id", playlistItem.id)
//                    playlistItemObject.put("name", playlistItem.name)
//                    playlistItemObject.put("page_source", playlistItem.pageSource)
//                    playlistItemObject.put("media_path", playlistItem.mediaPath)
//                    playlistItemObject.put("media_src", playlistItem.mediaSrc)
//                    playlistItemObject.put("thumbnail_path", playlistItem.thumbnailPath)
//                    playlistItemObject.put("cached", playlistItem.isCached)
//                    playlistItemObject.put("author", playlistItem.author)
//                    playlistItemObject.put("duration", playlistItem.duration)
//                    playlistItemsJsonArray.put(playlistItemObject)
//                }
//                playlistJsonObject.put("items", playlistItemsJsonArray)
//                playlistViewModel.setPlaylistData(playlistJsonObject.toString(2))
//                val playlistFragment = PlaylistFragment()
//                playlistFragment.setPlaylistOptionsListener(this)
//                parentFragmentManager
//                    .beginTransaction()
//                    .replace(android.R.id.content, playlistFragment)
//                    .addToBackStack(AllPlaylistFragment::class.simpleName)
//                    .commit()
//            } catch (e: JSONException) {
//                Log.e("BravePlaylist", "AllPlaylistFragment -> JSONException error $e")
//            }
//        } else {
//            parentFragmentManager
//                .beginTransaction()
//                .replace(android.R.id.content, EmptyPlaylistFragment())
//                .addToBackStack(AllPlaylistFragment::class.simpleName)
//                .commit()
//        }
    }

    companion object {
        @JvmStatic
        fun newInstance(playlistModel: PlaylistModel, playlistOptions: PlaylistOptions) =
            AllPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ConstantUtils.PLAYLIST_MODEL, playlistModel)
                    putSerializable(ConstantUtils.PLAYLIST_OPTION, playlistOptions)
                }
            }
    }
}