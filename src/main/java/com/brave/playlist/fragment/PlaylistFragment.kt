package com.brave.playlist.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.brave.playlist.PlaylistViewModel
import com.brave.playlist.R
import com.brave.playlist.adapter.MediaItemAdapter
import com.brave.playlist.model.MediaModel
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.listener.*
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.model.PlaylistOptionsModel
import com.brave.playlist.view.PlaylistOptionsBottomSheet
import com.brave.playlist.view.PlaylistToolbar
import org.json.JSONArray
import org.json.JSONObject

class PlaylistFragment : Fragment(R.layout.fragment_playlist), OnItemInteractionListener,
    View.OnClickListener, OnStartDragListener, PlaylistOptionsListener,
    OnPlaylistItemClickListener {

    private var playlistModel: PlaylistModel? = null

    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var mediaItemAdapter: MediaItemAdapter
    private lateinit var playlistToolbar: PlaylistToolbar
    private lateinit var rvPlaylist: RecyclerView
    private lateinit var tvTotalMediaCount: TextView
    private lateinit var layoutPlayMedia: LinearLayoutCompat
    private lateinit var ivPlaylistOptions: ImageView
    private lateinit var layoutShuffleMedia: LinearLayoutCompat
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var playlistOptionsListener: PlaylistOptionsListener

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            playlistData = it.getString(PLAYLIST_DATA)
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistViewModel = activity?.let {
            ViewModelProvider(
                it, ViewModelProvider.NewInstanceFactory()
            )
        }!![PlaylistViewModel::class.java]

        playlistToolbar = view.findViewById(R.id.playlistToolbar)
        playlistToolbar.setOptionsButtonClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        playlistToolbar.setExitEditModeClickListener {
            mediaItemAdapter.setEditMode(false)
            playlistToolbar.enableEditMode(false)
        }
        playlistToolbar.setMoveClickListener {
            Toast.makeText(
                activity,
                "Move : " + mediaItemAdapter.getSelectedItems().size,
                Toast.LENGTH_LONG
            ).show()
            mediaItemAdapter.setEditMode(false)
            playlistToolbar.enableEditMode(false)
        }
        playlistToolbar.setDeleteClickListener {
            Toast.makeText(
                activity,
                "Delete : " + mediaItemAdapter.getSelectedItems().size,
                Toast.LENGTH_LONG
            ).show()
            mediaItemAdapter.setEditMode(false)
            playlistToolbar.enableEditMode(false)
        }
        rvPlaylist = view.findViewById(R.id.rvPlaylists)
        tvTotalMediaCount = view.findViewById(R.id.tvTotalMediaCount)
        layoutPlayMedia = view.findViewById(R.id.layoutPlayMedia)
        layoutPlayMedia.isVisible
        layoutShuffleMedia = view.findViewById(R.id.layoutShuffleMedia)
        ivPlaylistOptions = view.findViewById(R.id.ivPlaylistOptions)

        playlistViewModel.playlistData.observe(viewLifecycleOwner) { playlistData ->
            Log.e("BravePlaylist", playlistData.toString())
            val playlistList = mutableListOf<MediaModel>()
            val playlistJsonObject = JSONObject(playlistData)
            val jsonArray: JSONArray = playlistJsonObject.getJSONArray("items")
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val mediaModel = MediaModel(
                    jsonObject.getString("id"),
                    jsonObject.getString("name"),
                    jsonObject.getString("page_source"),
                    jsonObject.getString("media_path"),
                    jsonObject.getString("thumbnail_path"),
                    jsonObject.getString("author"),
                    jsonObject.getString("duration")
                )
                playlistList.add(mediaModel)
            }

            playlistModel = PlaylistModel(
                playlistJsonObject.getString("id"),
                playlistJsonObject.getString("name"),
                playlistList
            )

            if (playlistList.size > 0) {
                layoutPlayMedia.setOnClickListener {
//                    viewModel.setSelectedPlaylistItem(playlistList[0])
                    openPlaylistPlayer(playlistList[0])
                }

                layoutShuffleMedia.setOnClickListener {
//                    viewModel.setSelectedPlaylistItem(playlistList[0])
                    openPlaylistPlayer(playlistList[0])
                }
            }

            tvTotalMediaCount.text = getString(R.string.number_of_items, playlistList.size.toString())

            mediaItemAdapter = MediaItemAdapter(playlistList, this, this)
            val callback = MediaItemGestureHelper(view.context, rvPlaylist, mediaItemAdapter, this)
            itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper.attachToRecyclerView(rvPlaylist)
            rvPlaylist.adapter = mediaItemAdapter
        }

        ivPlaylistOptions.setOnClickListener {
            PlaylistOptionsBottomSheet(
                mutableListOf(
                    PlaylistOptionsModel(
                        it.resources.getString(R.string.edit_text),
                        R.drawable.ic_edit_playlist,
                        PlaylistOptions.EDIT_PLAYLIST,
                        playlistModel
                    ), PlaylistOptionsModel(
                        it.resources.getString(R.string.rename_text),
                        R.drawable.ic_rename_playlist,
                        PlaylistOptions.RENAME_PLAYLIST,
                        playlistModel
                    ), PlaylistOptionsModel(
                        it.resources.getString(R.string.remove_playlist_offline_data),
                        R.drawable.ic_remove_offline_data_playlist,
                        PlaylistOptions.REMOVE_PLAYLIST_OFFLINE_DATA,
                        playlistModel
                    ), PlaylistOptionsModel(
                        it.resources.getString(R.string.download_playlist_for_offline_use),
                        R.drawable.ic_cloud_download,
                        PlaylistOptions.DOWNLOAD_PLAYLIST_FOR_OFFLINE_USE,
                        playlistModel
                    ), PlaylistOptionsModel(
                        it.resources.getString(R.string.delete_playlist),
                        R.drawable.ic_playlist_delete,
                        PlaylistOptions.DELETE_PLAYLIST,
                        playlistModel
                    )
                ), this
            ).show(parentFragmentManager, null)
        }
    }

    override fun onItemDelete() {

    }

    override fun onRemoveFromOffline(position: Int) {

    }

    override fun onUpload(position: Int) {

    }

    override fun onClick(v: View) {
        //perform undo operation by communicating with adapter
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onPlaylistItemClick(mediaModel: MediaModel) {
//        viewModel.setSelectedPlaylistItem(mediaModel)
        openPlaylistPlayer(mediaModel)
    }

    override fun onPlaylistItemClick(count: Int) {
        playlistToolbar.updateSelectedItems(count)
    }

    private fun openPlaylistPlayer(selectedMediaModel: MediaModel) {
        val playlistPlayerFragment =
            playlistModel?.let { PlaylistPlayerFragment.newInstance(it, selectedMediaModel) }
        if (playlistPlayerFragment != null) {
            parentFragmentManager.beginTransaction()
                .replace(android.R.id.content, playlistPlayerFragment)
                .addToBackStack(PlaylistFragment::class.simpleName)
                .commit()
        }
    }

//    companion object {
//        private const val PLAYLIST_DATA = "PLAYLIST_DATA"
//
//        @JvmStatic
//        fun newInstance(playlistData: String) =
//            PlaylistFragment().apply {
//                arguments = Bundle().apply {
//                    putString(PLAYLIST_DATA, playlistData)
//                }
//            }
//    }

    override fun onOptionClicked(playlistOptionsModel: PlaylistOptionsModel) {
        if (playlistOptionsModel.optionType == PlaylistOptions.EDIT_PLAYLIST) {
            mediaItemAdapter.setEditMode(true)
            playlistToolbar.enableEditMode(true)
        } else if (playlistOptionsModel.optionType == PlaylistOptions.RENAME_PLAYLIST) {
            val newPlaylistFragment = playlistModel?.let {
                NewPlaylistFragment.newInstance(
                    it,
                    PlaylistOptions.RENAME_PLAYLIST
                )
            }
            if (newPlaylistFragment != null) {
                parentFragmentManager
                    .beginTransaction()
                    .replace(android.R.id.content, newPlaylistFragment)
                    .addToBackStack(NewPlaylistFragment::class.simpleName)
                    .commit()
            }
        }
//        playlistOptionsListener.onOptionClicked(playlistOptionsModel)
        playlistViewModel.setSelectedOption(playlistOptionsModel.optionType)
    }

    fun setPlaylistOptionsListener(playlistOptionsListener: PlaylistOptionsListener) {
        this.playlistOptionsListener = playlistOptionsListener
    }
}