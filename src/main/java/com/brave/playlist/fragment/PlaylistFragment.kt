package com.brave.playlist.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.brave.playlist.PlaylistVideoService
import com.brave.playlist.PlaylistViewModel
import com.brave.playlist.R
import com.brave.playlist.adapter.PlaylistItemAdapter
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.listener.ItemInteractionListener
import com.brave.playlist.listener.PlaylistItemClickListener
import com.brave.playlist.listener.PlaylistItemOptionsListener
import com.brave.playlist.listener.PlaylistOptionsListener
import com.brave.playlist.listener.StartDragListener
import com.brave.playlist.model.MoveOrCopyModel
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.model.PlaylistItemOptionModel
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.model.PlaylistOptionsModel
import com.brave.playlist.util.ConnectionUtils
import com.brave.playlist.util.ConstantUtils.DEFAULT_PLAYLIST
import com.brave.playlist.util.MenuUtils
import com.brave.playlist.util.PlaylistItemGestureHelper
import com.brave.playlist.util.PlaylistPreferenceUtils
import com.brave.playlist.util.PlaylistPreferenceUtils.RECENTLY_PLAYED_PLAYLIST
import com.brave.playlist.util.PlaylistPreferenceUtils.get
import com.brave.playlist.util.PlaylistPreferenceUtils.set
import com.brave.playlist.util.PlaylistUtils
import com.brave.playlist.view.PlaylistToolbar
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.util.LinkedList

class PlaylistFragment : Fragment(R.layout.fragment_playlist), ItemInteractionListener,
    View.OnClickListener, StartDragListener, PlaylistOptionsListener, PlaylistItemOptionsListener,
    PlaylistItemClickListener {

    private var playlistModel: PlaylistModel? = null

    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var playlistItemAdapter: PlaylistItemAdapter
    private lateinit var playlistToolbar: PlaylistToolbar
    private lateinit var rvPlaylist: RecyclerView
    private lateinit var tvTotalMediaCount: TextView
    private lateinit var tvPlaylistName: TextView
    private lateinit var layoutPlayMedia: LinearLayoutCompat
    private lateinit var ivPlaylistOptions: ImageView
    private lateinit var layoutShuffleMedia: LinearLayoutCompat
    private lateinit var ivPlaylistCover: AppCompatImageView
    private lateinit var tvPlaylistTotalSize: AppCompatTextView
    private lateinit var itemTouchHelper: ItemTouchHelper

    private lateinit var emptyView: View
    private lateinit var playlistView: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistViewModel = activity?.let {
            ViewModelProvider(
                it, ViewModelProvider.NewInstanceFactory()
            )
        }!![PlaylistViewModel::class.java]

        emptyView = view.findViewById(R.id.empty_view)
        playlistView = view.findViewById(R.id.playlist_view)

        playlistToolbar = view.findViewById(R.id.playlistToolbar)
        playlistToolbar.setOptionsButtonClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        playlistToolbar.setExitEditModeClickListener {
            playlistItemAdapter.setEditMode(false)
            playlistToolbar.enableEditMode(false)
            //Reorder list
            playlistViewModel.reorderPlaylistItems(playlistItemAdapter.getPlaylistItems())
        }
        playlistToolbar.setMoveClickListener {
            if (playlistItemAdapter.getSelectedItems().size > 0) {
                MenuUtils.showMoveOrCopyMenu(
                    it,
                    parentFragmentManager,
                    playlistItemAdapter.getSelectedItems(),
                    this
                )
//                playlistItemAdapter.setEditMode(false)
//                playlistToolbar.enableEditMode(false)
            } else {
                Toast.makeText(
                    activity,
                    getString(R.string.playlist_please_select_items),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        playlistToolbar.setDeleteClickListener {
            if (playlistItemAdapter.getSelectedItems().size > 0) {
                playlistViewModel.setDeletePlaylistItems(
                    PlaylistModel(
                        playlistModel?.id.toString(),
                        playlistModel?.name.toString(),
                        playlistItemAdapter.getSelectedItems()
                    )
                )
                playlistItemAdapter.setEditMode(false)
                playlistToolbar.enableEditMode(false)
            } else {
                Toast.makeText(
                    activity,
                    getString(R.string.playlist_please_select_items),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        rvPlaylist = view.findViewById(R.id.rvPlaylists)
        tvTotalMediaCount = view.findViewById(R.id.tvTotalMediaCount)
        tvPlaylistName = view.findViewById(R.id.tvPlaylistName)
        layoutPlayMedia = view.findViewById(R.id.layoutPlayMedia)
        layoutPlayMedia.isVisible
        layoutShuffleMedia = view.findViewById(R.id.layoutShuffleMedia)
        ivPlaylistOptions = view.findViewById(R.id.ivPlaylistOptions)
        ivPlaylistCover = view.findViewById(R.id.ivPlaylistCover)
        tvPlaylistTotalSize = view.findViewById(R.id.tvPlaylistTotalSize)

        playlistViewModel.playlistData.observe(viewLifecycleOwner) { playlistData ->
            Log.e("BravePlaylist", playlistData.toString())
            var totalFileSize = 0L
            val playlistList = mutableListOf<PlaylistItemModel>()
            val playlistJsonObject = JSONObject(playlistData)
            val jsonArray: JSONArray = playlistJsonObject.getJSONArray("items")
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
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

            playlistModel = PlaylistModel(
                playlistJsonObject.getString("id"),
                playlistJsonObject.getString("name"),
                playlistList
            )

            view.findViewById<Button>(R.id.btBrowseForMedia).setOnClickListener {
                requireActivity().finish()
//                if (playlistModel != null) {
//                    playlistViewModel.setDefaultPlaylist(playlistModel!!.id)
//                }
            }

//            Thread {
//                playlistList.forEach {
//                    try {
//                        if (it.isCached) {
//                            val fileSize = MediaUtils.getFileSizeFromUri(view.context, Uri.parse(it.mediaPath))
//                            totalFileSize += fileSize
//                        }
//                    } catch (ex: IOException) {
//                        Log.e("BravePlaylist", ex.message.toString());
//                    }
//                }
//                activity?.runOnUiThread {
//                    if (totalFileSize > 0) {
//                        tvPlaylistTotalSize.text = Formatter.formatShortFileSize(view.context, totalFileSize)
//                    }
//                }
//            }.start()

//            playlistList.forEach {
//                if (it.isCached) {
//                    val fileSize =
//                        MediaUtils.getFileSizeFromUri(view.context, Uri.parse(it.mediaPath))
//                    totalFileSize += fileSize
//                }
//            }
//            if (totalFileSize > 0) {
//                tvPlaylistTotalSize.text = Formatter.formatShortFileSize(view.context, totalFileSize)
//            }

            if (playlistList.size > 0) {
                Glide.with(requireContext())
                    .load(playlistList[0].thumbnailPath)
                    .placeholder(R.drawable.ic_playlist_placeholder)
                    .error(R.drawable.ic_playlist_placeholder)
                    .into(ivPlaylistCover)
                layoutPlayMedia.setOnClickListener {
                    openPlaylistPlayer(playlistList[0])
                }

                layoutShuffleMedia.setOnClickListener {
                    openPlaylistPlayer(playlistList[(0 until playlistList.size).shuffled().last()])
                }

                tvTotalMediaCount.text =
                    getString(R.string.playlist_number_of_items, playlistList.size.toString())

                tvPlaylistName.text =
                    if (playlistModel?.id.equals(DEFAULT_PLAYLIST)) resources.getString(R.string.playlist_play_later) else playlistModel?.name

                playlistItemAdapter = PlaylistItemAdapter(playlistList, this, this)
                val callback =
                    PlaylistItemGestureHelper(view.context, rvPlaylist, playlistItemAdapter, this)
                itemTouchHelper = ItemTouchHelper(callback)
                itemTouchHelper.attachToRecyclerView(rvPlaylist)
                rvPlaylist.adapter = playlistItemAdapter
                playlistItemAdapter.setEditMode(false)
                playlistToolbar.enableEditMode(false)
                playlistView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE

                playlistViewModel.downloadProgress.observe(viewLifecycleOwner) {
                    playlistItemAdapter.updatePlaylistItemDownloadProgress(it)
                }

                playlistViewModel.playlistEventUpdate.observe(viewLifecycleOwner) {
                    playlistItemAdapter.updatePlaylistItem(it)
                }

                requireActivity()
                    .onBackPressedDispatcher
                    .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            if (playlistItemAdapter.getEditMode()) {
                                playlistItemAdapter.setEditMode(false)
                                playlistToolbar.enableEditMode(false)
                                //Reorder list
                                playlistViewModel.reorderPlaylistItems(playlistItemAdapter.getPlaylistItems())
                            } else {
                                this.remove()
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }
                    }
                    )
            } else {
                ivPlaylistCover.setImageResource(R.drawable.ic_playlist_placeholder)
                emptyView.visibility = View.VISIBLE
                playlistView.visibility = View.GONE
            }
        }

        ivPlaylistOptions.setOnClickListener {
            playlistModel?.let { model ->
                MenuUtils.showPlaylistMenu(
                    view.context, parentFragmentManager,
                    model, this, model.id == DEFAULT_PLAYLIST
                )
            }
        }
    }

    override fun onItemDelete(position: Int) {
        playlistViewModel.setDeletePlaylistItems(
            PlaylistModel(
                playlistModel?.id.toString(),
                playlistModel?.name.toString(),
                arrayListOf(
                    playlistItemAdapter.getPlaylistItems()[position]
                )
            )
        )
    }

    override fun onRemoveFromOffline(position: Int) {
//        playlistViewModel.setDeletePlaylistItems(
//            PlaylistModel(
//                playlistModel?.id.toString(),
//                playlistModel?.name.toString(),
//                arrayListOf(
//                    playlistModel?.items!![position]
//                )
//            )
//        )
        val playlistOptionsModel = PlaylistItemOptionModel(
            requireContext().resources.getString(R.string.playlist_delete_item_offline_data),
            R.drawable.ic_remove_offline_data_playlist,
            PlaylistOptions.DELETE_ITEMS_OFFLINE_DATA,
            playlistItemModel = playlistItemAdapter.getPlaylistItems()[position],
            playlistId = playlistModel?.id.toString()
        )
        playlistViewModel.setPlaylistItemOption(playlistOptionsModel)
    }

    override fun onShare(position: Int) {
        super.onShare(position)
        val playlistItemModel = playlistModel?.items!![position]
        //Share model
        PlaylistUtils.showSharingDialog(requireContext(), playlistItemModel.pageSource)
    }

    override fun onClick(v: View) {
        //perform undo operation by communicating with adapter
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onPlaylistItemClick(playlistItemModel: PlaylistItemModel) {
        openPlaylistPlayer(playlistItemModel)
    }

    override fun onPlaylistItemClick(count: Int) {
        playlistToolbar.updateSelectedItems(count)
    }

    override fun onPlaylistItemMenuClick(view: View, playlistItemModel: PlaylistItemModel) {
        MenuUtils.showPlaylistItemMenu(
            view.context,
            parentFragmentManager,
            playlistItemModel = playlistItemModel,
            playlistId = playlistItemModel.playlistId,
            playlistItemOptionsListener = this,
            playlistModel?.name == DEFAULT_PLAYLIST
        )
    }

    private fun openPlaylistPlayer(selectedPlaylistItemModel: PlaylistItemModel) {
//        var isExpired = false
//        val webView: WebView? = activity?.let { WebView(it) }
////        val webSettings : WebSettings? = webView?.settings
////        webSettings?.javaScriptEnabled = true
//        val webViewClientImpl = WebViewClientImpl(webViewResponseListener = object :
//            WebViewResponseListener {
//            override fun onPageLoadFinished() {
//                Log.e("BravePlaylist", "onPageLoadFinished");
//                if (isExpired) {
//                    Log.e("BravePlaylist", "expired");
//                    // Download new content and open playlist player
//                    Toast.makeText(requireContext(), "Playlist item is expired", Toast.LENGTH_SHORT).show()
//                } else {
//                    var recentPlaylistIds = LinkedList<String>()
//                    val recentPlaylistJson = PreferenceUtils.defaultPrefs(requireContext())[RECENTLY_PLAYED_PLAYLIST, ""]
//                    if (recentPlaylistJson.isNotEmpty()) {
//                        Log.e("recent_playlist", "recentPlaylistJson : "+recentPlaylistJson)
////            val type: Type = object : TypeToken<LinkedList<String>>() {}.type
////            recentPlaylist = Gson().fromJson(recentPlaylistJson, type)
//                        recentPlaylistIds = GsonBuilder().create().fromJson(
//                            recentPlaylistJson,
//                            TypeToken.getParameterized(LinkedList::class.java, String::class.java).type
//                        )
//                        if (recentPlaylistIds.contains(playlistModel?.id)) {
//                            recentPlaylistIds.remove(playlistModel?.id)
//                        }
//                    }
//                    recentPlaylistIds.addFirst(playlistModel?.id)
//
//                    Log.e("recent_playlist", "after recentPlaylistJson : "+Gson().toJson(recentPlaylistIds))
//                    PreferenceUtils.defaultPrefs(requireContext())[RECENTLY_PLAYED_PLAYLIST] = Gson().toJson(recentPlaylistIds)
//
//                    activity?.stopService(Intent(requireContext(), PlaylistVideoService::class.java))
//                    val playlistPlayerFragment =
//                        playlistModel?.let { PlaylistPlayerFragment.newInstance(selectedPlaylistItemModel.id, it) }
//                    if (playlistPlayerFragment != null) {
//                        parentFragmentManager.beginTransaction()
//                            .replace(android.R.id.content, playlistPlayerFragment)
//                            .addToBackStack(PlaylistFragment::class.simpleName)
//                            .commit()
//                    }
//                }
//                webView?.destroy()
//            }
//            override fun onError() {
//                Log.e("BravePlaylist", "error");
//                isExpired = true
//            }
//        })
//        webView?.webViewClient = webViewClientImpl
//        webView?.loadUrl(selectedPlaylistItemModel.mediaSrc)
        if (!selectedPlaylistItemModel.isCached && !ConnectionUtils.isDeviceOnline(requireContext())) {
            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_offline_message),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!PlaylistUtils.isMediaSourceExpired(selectedPlaylistItemModel.mediaSrc)) {
            var recentPlaylistIds = LinkedList<String>()
            val recentPlaylistJson =
                PlaylistPreferenceUtils.defaultPrefs(requireContext())[RECENTLY_PLAYED_PLAYLIST, ""]
            if (recentPlaylistJson.isNotEmpty()) {
                Log.e("recent_playlist", "recentPlaylistJson : " + recentPlaylistJson)
//            val type: Type = object : TypeToken<LinkedList<String>>() {}.type
//            recentPlaylist = Gson().fromJson(recentPlaylistJson, type)
                recentPlaylistIds = GsonBuilder().create().fromJson(
                    recentPlaylistJson,
                    TypeToken.getParameterized(LinkedList::class.java, String::class.java).type
                )
                if (recentPlaylistIds.contains(playlistModel?.id)) {
                    recentPlaylistIds.remove(playlistModel?.id)
                }
            }
            recentPlaylistIds.addFirst(playlistModel?.id)

            Log.e(
                "recent_playlist",
                "after recentPlaylistJson : " + Gson().toJson(recentPlaylistIds)
            )
            PlaylistPreferenceUtils.defaultPrefs(requireContext())[RECENTLY_PLAYED_PLAYLIST] =
                Gson().toJson(recentPlaylistIds)

            activity?.stopService(Intent(requireContext(), PlaylistVideoService::class.java))
            val playlistPlayerFragment =
                playlistModel?.let {
                    PlaylistPlayerFragment.newInstance(
                        selectedPlaylistItemModel.id,
                        it
                    )
                }
            if (playlistPlayerFragment != null) {
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, playlistPlayerFragment)
                    .addToBackStack(PlaylistFragment::class.simpleName)
                    .commit()
            }
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_item_expired_message),
                Toast.LENGTH_SHORT
            ).show()
            val playlistItemOptionModel = PlaylistItemOptionModel(
                requireContext().resources.getString(R.string.playlist_open_in_private_tab),
                R.drawable.ic_private_tab,
                PlaylistOptions.RECOVER_PLAYLIST_ITEM,
                playlistItemModel = selectedPlaylistItemModel,
                playlistId = selectedPlaylistItemModel.playlistId
            )
            playlistViewModel.setPlaylistItemOption(playlistItemOptionModel)
        }
    }

    override fun onOptionClicked(playlistOptionsModel: PlaylistOptionsModel) {
        if (playlistOptionsModel.optionType == PlaylistOptions.EDIT_PLAYLIST) {
            playlistItemAdapter.setEditMode(true)
            playlistToolbar.enableEditMode(true)
        } else if (playlistOptionsModel.optionType == PlaylistOptions.MOVE_PLAYLIST_ITEMS || playlistOptionsModel.optionType == PlaylistOptions.COPY_PLAYLIST_ITEMS) {
            PlaylistUtils.moveOrCopyModel = MoveOrCopyModel(
                playlistOptionsModel.optionType,
                "",
                playlistItemAdapter.getSelectedItems()
            )
            playlistItemAdapter.setEditMode(false)
            playlistToolbar.enableEditMode(false)
        } else if (playlistOptionsModel.optionType == PlaylistOptions.RENAME_PLAYLIST) {
            val newPlaylistFragment = playlistModel?.let {
                NewPlaylistFragment.newInstance(
                    PlaylistOptions.RENAME_PLAYLIST,
                    it
                )
            }
            if (newPlaylistFragment != null) {
                parentFragmentManager
                    .beginTransaction()
                    .replace(android.R.id.content, newPlaylistFragment)
                    .addToBackStack(PlaylistFragment::class.simpleName)
                    .commit()
            }
        } else if (playlistOptionsModel.optionType == PlaylistOptions.DELETE_PLAYLIST) {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        playlistViewModel.setPlaylistOption(playlistOptionsModel)
    }

    override fun onOptionClicked(playlistItemOptionModel: PlaylistItemOptionModel) {
        if (playlistItemOptionModel.optionType == PlaylistOptions.SHARE_PLAYLIST_ITEM) {
            playlistItemOptionModel.playlistItemModel?.pageSource?.let {
                PlaylistUtils.showSharingDialog(
                    requireContext(),
                    it
                )
            }
        } else {
            if (playlistItemOptionModel.optionType == PlaylistOptions.MOVE_PLAYLIST_ITEM || playlistItemOptionModel.optionType == PlaylistOptions.COPY_PLAYLIST_ITEM) {
                val moveOrCopyItems = ArrayList<PlaylistItemModel>()
                playlistItemOptionModel.playlistItemModel?.let { moveOrCopyItems.add(it) }
                PlaylistUtils.moveOrCopyModel =
                    MoveOrCopyModel(playlistItemOptionModel.optionType, "", moveOrCopyItems)
            }
            playlistViewModel.setPlaylistItemOption(playlistItemOptionModel)
        }
    }
}
