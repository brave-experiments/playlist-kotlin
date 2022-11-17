package com.brave.braveandroidplaylist.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.adapter.MediaItemAdapter
import com.brave.braveandroidplaylist.listener.MediaItemGestureHelper
import com.brave.braveandroidplaylist.listener.OnItemInteractionListener
import com.brave.braveandroidplaylist.listener.OnStartDragListener
import com.brave.braveandroidplaylist.listener.PlaylistOptionsListener
import com.brave.braveandroidplaylist.model.MediaModel
import com.brave.braveandroidplaylist.model.PlaylistOptions
import com.brave.braveandroidplaylist.model.PlaylistOptionsModel
import com.brave.braveandroidplaylist.util.PlaylistUtils
import com.brave.braveandroidplaylist.view.PlaylistOptionsBottomSheet
import com.brave.braveandroidplaylist.view.PlaylistToolbar
import org.json.JSONArray
import org.json.JSONObject


class PlaylistFragment : Fragment(R.layout.fragment_playlist), OnItemInteractionListener,
    View.OnClickListener,
    OnStartDragListener, PlaylistOptionsListener {

    private lateinit var mediaItemAdapter: MediaItemAdapter
    private lateinit var playlistToolbar: PlaylistToolbar
    private lateinit var rvPlaylist: RecyclerView
    private lateinit var tvTotalMediaCount: TextView
    private lateinit var layoutPlayMedia: LinearLayoutCompat
    private lateinit var ivPlaylistOptions: ImageView
    private lateinit var layoutShuffleMedia: LinearLayoutCompat
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var playlistOptionsListener: PlaylistOptionsListener

    private var playlistData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistData = it.getString(PLAYLIST_DATA)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistToolbar = view.findViewById(R.id.playlistToolbar)
//        val ivOptionsToolbarPlaylist: ImageView = playlistToolbar.findViewById(R.id.ivOptionsToolbarPlaylist)
//        ivOptionsToolbarPlaylist.setOnClickListener {
//            activity?.finish()
//        }
        rvPlaylist = view.findViewById(R.id.rvPlaylists)
        tvTotalMediaCount = view.findViewById(R.id.tvTotalMediaCount)
        layoutPlayMedia = view.findViewById(R.id.layoutPlayMedia)
        layoutPlayMedia.isVisible
        layoutShuffleMedia = view.findViewById(R.id.layoutShuffleMedia)
        ivPlaylistOptions = view.findViewById(R.id.ivPlaylistOptions)

        Log.e("BravePlaylist", playlistData.toString())

        val playlistList = mutableListOf<MediaModel>()
        val playlistJsonObject = playlistData?.let { JSONObject(it) }
        val jsonArray: JSONArray = playlistJsonObject!!.getJSONArray("items")
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val mediaModel = MediaModel(
                jsonObject.getString("id"),
                jsonObject.getString("name"),
                jsonObject.getString("page_source"),
                jsonObject.getString("media_path"),
                jsonObject.getString("thumbnail_path")
            )
            playlistList.add(mediaModel)
        }

        if (playlistList.size > 0) {
            layoutPlayMedia.setOnClickListener {
                PlaylistUtils.openPlaylistPlayer(view.context, playlistList[0])
            }

            layoutShuffleMedia.setOnClickListener {
                PlaylistUtils.openPlaylistPlayer(view.context, playlistList[0])
            }
        }

        ivPlaylistOptions.setOnClickListener {
            PlaylistOptionsBottomSheet(
                mutableListOf(
                    PlaylistOptionsModel(
                        it.resources.getString(R.string.edit_text),
                        R.drawable.ic_edit_playlist,
                        PlaylistOptions.EDIT_PLAYLIST
                    ),
                    PlaylistOptionsModel(
                        it.resources.getString(R.string.rename_text),
                        R.drawable.ic_rename_playlist,
                        PlaylistOptions.RENAME_PLAYLIST
                    ),
                    PlaylistOptionsModel(
                        it.resources.getString(R.string.remove_playlist_offline_data),
                        R.drawable.ic_remove_offline_data_playlist,
                        PlaylistOptions.REMOVE_PLAYLIST_OFFLINE_DATA
                    ),
                    PlaylistOptionsModel(
                        it.resources.getString(R.string.download_playlist_for_offline_use),
                        R.drawable.ic_cloud_download,
                        PlaylistOptions.DOWNLOAD_PLAYLIST_FOR_OFFLINE_USE
                    ),
                    PlaylistOptionsModel(
                        it.resources.getString(R.string.delete_playlist),
                        R.drawable.ic_playlist_delete,
                        PlaylistOptions.DELETE_PLAYLIST
                    )
                ), this
            ).show(parentFragmentManager, null)
        }

        tvTotalMediaCount.text = playlistList.size.toString()+" items"

        mediaItemAdapter = MediaItemAdapter(playlistList, this)
        val callback = MediaItemGestureHelper(view.context, rvPlaylist, mediaItemAdapter, this)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(rvPlaylist)
        rvPlaylist.adapter = mediaItemAdapter
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

    companion object {
        private const val PLAYLIST_DATA = "PLAYLIST_DATA"

        @JvmStatic
        fun newInstance(playlistData: String) =
            PlaylistFragment().apply {
                arguments = Bundle().apply {
                    putString(PLAYLIST_DATA, playlistData)
                }
            }
    }

    override fun onOptionClicked(playlistOptionsModel: PlaylistOptionsModel) {
        if (playlistOptionsModel.optionType == PlaylistOptions.EDIT_PLAYLIST) {
            mediaItemAdapter.setEditMode(true)
        }
        playlistOptionsListener.onOptionClicked(playlistOptionsModel)
    }

    fun setPlaylistOptionsListener(playlistOptionsListener: PlaylistOptionsListener) {
        this.playlistOptionsListener = playlistOptionsListener
    }
}