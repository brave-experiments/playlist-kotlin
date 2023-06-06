package com.brave.playlist.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
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
import com.brave.playlist.PlaylistDownloadUtils
import com.brave.playlist.PlaylistVideoService
import com.brave.playlist.PlaylistViewModel
import com.brave.playlist.R
import com.brave.playlist.adapter.PlaylistItemAdapter
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.extension.afterMeasured
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
import com.brave.playlist.util.ConstantUtils.CURRENT_PLAYING_ITEM_ACTION
import com.brave.playlist.util.ConstantUtils.CURRENT_PLAYING_ITEM_ID
import com.brave.playlist.util.ConstantUtils.DEFAULT_PLAYLIST
import com.brave.playlist.util.ConstantUtils.TAG
import com.brave.playlist.util.MediaUtils
import com.brave.playlist.util.MenuUtils
import com.brave.playlist.util.PlaylistItemGestureHelper
import com.brave.playlist.util.PlaylistPreferenceUtils
import com.brave.playlist.util.PlaylistPreferenceUtils.getLatestPlaylistItem
import com.brave.playlist.util.PlaylistPreferenceUtils.recentlyPlayedPlaylist
import com.brave.playlist.util.PlaylistPreferenceUtils.rememberListPlaybackPosition
import com.brave.playlist.util.PlaylistUtils
import com.brave.playlist.view.PlaylistToolbar
import com.bumptech.glide.Glide
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.LinkedList

class PlaylistFragment : Fragment(R.layout.fragment_playlist), ItemInteractionListener,
    StartDragListener, PlaylistOptionsListener, PlaylistItemOptionsListener,
    PlaylistItemClickListener {
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private lateinit var playlistModel: PlaylistModel
    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var playlistItemAdapter: PlaylistItemAdapter
    private lateinit var playlistToolbar: PlaylistToolbar
    private lateinit var rvPlaylist: RecyclerView
    private lateinit var tvTotalMediaCount: TextView
    private lateinit var tvPlaylistName: TextView
    private lateinit var layoutPlayMedia: LinearLayoutCompat
    private lateinit var ivPlaylistOptions: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutShuffleMedia: LinearLayoutCompat
    private lateinit var ivPlaylistCover: AppCompatImageView
    private lateinit var tvPlaylistTotalSize: AppCompatTextView
    private lateinit var itemTouchHelper: ItemTouchHelper

    private lateinit var emptyView: View
    private lateinit var playlistView: View

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action.equals(CURRENT_PLAYING_ITEM_ACTION)) {
                val currentPlayingItemId = intent.getStringExtra(CURRENT_PLAYING_ITEM_ID)
                if (!currentPlayingItemId.isNullOrEmpty()) {
                    playlistItemAdapter.updatePlayingStatus(currentPlayingItemId)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistViewModel = activity?.let {
            ViewModelProvider(
                it, ViewModelProvider.NewInstanceFactory()
            )
        }!![PlaylistViewModel::class.java]

        val intentFilter = IntentFilter()
        intentFilter.addAction(CURRENT_PLAYING_ITEM_ACTION)
        activity?.registerReceiver(broadcastReceiver, intentFilter)

        emptyView = view.findViewById(R.id.empty_view)
        playlistView = view.findViewById(R.id.playlist_view)

        playlistToolbar = view.findViewById(R.id.playlistToolbar)
        playlistToolbar.setOptionsButtonClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        playlistToolbar.setExitEditModeClickListener {
            playlistItemAdapter.setEditMode(false)
            playlistToolbar.enableEditMode(false)
            ivPlaylistOptions.visibility = View.VISIBLE
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
                for (selectedItem in playlistItemAdapter.getSelectedItems()) {
                    stopVideoPlayerOnDelete(selectedItem)
                }
                playlistViewModel.setDeletePlaylistItems(
                    PlaylistModel(
                        playlistModel.id,
                        playlistModel.name,
                        playlistItemAdapter.getSelectedItems()
                    )
                )
                playlistItemAdapter.setEditMode(false)
                playlistToolbar.enableEditMode(false)
                ivPlaylistOptions.visibility = View.VISIBLE
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
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        rvPlaylist.visibility = View.GONE

        playlistViewModel.playlistData.observe(viewLifecycleOwner) { playlistData ->
//            playlistData.items.toMutableList().reversed() // To have latest item on top
            Log.e(TAG, playlistData.toString())
            var totalFileSize = 0L
            playlistModel = playlistData

            view.findViewById<Button>(R.id.btBrowseForMedia).setOnClickListener {
                requireActivity().finish()
            }

            playlistData.items.forEach {playlistItemModel ->
                PlaylistDownloadUtils.startDownloadRequest(requireContext(), playlistItemModel)
            }

            if (playlistModel.items.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(playlistModel.items[0].thumbnailPath)
                    .placeholder(R.drawable.ic_playlist_placeholder)
                    .error(R.drawable.ic_playlist_placeholder)
                    .into(ivPlaylistCover)
                layoutPlayMedia.setOnClickListener {
                    if (PlaylistPreferenceUtils.defaultPrefs(requireContext()).rememberListPlaybackPosition && !TextUtils.isEmpty(
                            PlaylistPreferenceUtils.defaultPrefs(requireContext())
                                .getLatestPlaylistItem(playlistModel.id)
                        )
                    ) {
                        playlistModel.items.forEach { playlistToOpen ->
                            if (playlistToOpen.id == PlaylistPreferenceUtils.defaultPrefs(
                                    requireContext()
                                ).getLatestPlaylistItem(playlistModel.id)
                            ) {
                                openPlaylistPlayer(playlistToOpen)
                                return@forEach
                            }
                        }
                    } else {
                        openPlaylistPlayer(playlistModel.items[0])
                    }
                }

                layoutShuffleMedia.setOnClickListener {
                    openPlaylistPlayer(
                        playlistModel.items[(0 until playlistModel.items.size).shuffled().last()]
                    )
                }

                tvTotalMediaCount.text =
                    getString(
                        R.string.playlist_number_of_items,
                        playlistModel.items.size.toString()
                    )

                tvPlaylistName.text =
                    if (playlistModel.id == DEFAULT_PLAYLIST) resources.getString(R.string.playlist_play_later) else playlistModel.name

                requireActivity()
                    .onBackPressedDispatcher
                    .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            if (playlistItemAdapter.getEditMode()) {
                                playlistItemAdapter.setEditMode(false)
                                playlistToolbar.enableEditMode(false)
                                ivPlaylistOptions.visibility = View.VISIBLE
                                //Reorder list
                                playlistViewModel.reorderPlaylistItems(playlistItemAdapter.getPlaylistItems())
                            } else {
                                this.remove()
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }
                    }
                    )

                scope.launch {
                    playlistModel.items.forEach {
                        try {
                            if (it.isCached) {
                                val fileSize = MediaUtils.getFileSizeFromUri(
                                    view.context,
                                    Uri.parse(it.mediaPath)
                                )
                                it.fileSize = fileSize
                                totalFileSize += fileSize
                            }
                        } catch (ex: IOException) {
                            Log.e(TAG, ex.message.toString())
                        }
                    }

                    activity?.runOnUiThread {
                        if (totalFileSize > 0) {
                            tvPlaylistTotalSize.text =
                                Formatter.formatShortFileSize(view.context, totalFileSize)
                        }
                        playlistItemAdapter = PlaylistItemAdapter(
                            playlistModel.items.toMutableList(),
                            this@PlaylistFragment,
                            this@PlaylistFragment
                        )
                        val callback =
                            PlaylistItemGestureHelper(
                                view.context,
                                rvPlaylist,
                                playlistItemAdapter,
                                this@PlaylistFragment
                            )
                        itemTouchHelper = ItemTouchHelper(callback)
                        itemTouchHelper.attachToRecyclerView(rvPlaylist)
                        rvPlaylist.adapter = playlistItemAdapter
                        playlistItemAdapter.setEditMode(false)
                        playlistToolbar.enableEditMode(false)
                        ivPlaylistOptions.visibility = View.VISIBLE
                        playlistView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE

                        progressBar.visibility = View.GONE
                        rvPlaylist.visibility = View.VISIBLE

                        playlistViewModel.downloadProgress.observe(viewLifecycleOwner) {
                            playlistItemAdapter.updatePlaylistItemDownloadProgress(it)
                        }

                        playlistViewModel.playlistItemEventUpdate.observe(viewLifecycleOwner) {
                            playlistItemAdapter.updatePlaylistItem(it)
                        }

                        rvPlaylist.afterMeasured {
                            PlaylistVideoService.CURRENTLY_PLAYED_ITEM_ID?.let {
                                playlistItemAdapter.updatePlayingStatus(
                                    it
                                )
                                if (!arguments?.getString(CURRENT_PLAYING_ITEM_ID).isNullOrEmpty() && playlistModel.items.isNotEmpty()) {
                                    playlistModel.items.forEach { item ->
                                        if (item.id == arguments?.getString(CURRENT_PLAYING_ITEM_ID)) {
                                            Log.e(TAG, item.id + " : "+item.name )
                                            openPlaylistPlayer(item)
                                            arguments?.putString(CURRENT_PLAYING_ITEM_ID, "")
                                            return@forEach
                                        }
                                    }
                                }
                            }

//                            playlistVideoService?.getCurrentPlayingItem()?.id?.let {
//                                playlistItemAdapter.updatePlayingStatus(
//                                    it
//                                )
//                            }
                        }
                    }
                }
            } else {
                ivPlaylistCover.setImageResource(R.drawable.ic_playlist_placeholder)
                emptyView.visibility = View.VISIBLE
                playlistView.visibility = View.GONE
            }
        }

        ivPlaylistOptions.setOnClickListener {
            MenuUtils.showPlaylistMenu(
                view.context, parentFragmentManager,
                playlistModel, this, playlistModel.id == DEFAULT_PLAYLIST
            )
        }
    }

    override fun onDestroyView() {
        activity?.unregisterReceiver(broadcastReceiver)
        super.onDestroyView()
    }

    override fun onItemDelete(position: Int) {
        val selectedPlaylistItem = playlistItemAdapter.getPlaylistItems()[position]
        stopVideoPlayerOnDelete(selectedPlaylistItem)
        playlistViewModel.setDeletePlaylistItems(
            PlaylistModel(
                playlistModel.id,
                playlistModel.name,
                arrayListOf(
                    selectedPlaylistItem
                )
            )
        )
    }

    override fun onRemoveFromOffline(position: Int) {
        val selectedPlaylistItem = playlistItemAdapter.getPlaylistItems()[position]
        stopVideoPlayerOnDelete(selectedPlaylistItem)
        val playlistOptionsModel = PlaylistItemOptionModel(
            requireContext().resources.getString(R.string.playlist_delete_item_offline_data),
            R.drawable.ic_remove_offline_data_playlist,
            PlaylistOptions.DELETE_ITEMS_OFFLINE_DATA,
            playlistItemModel = selectedPlaylistItem,
            playlistId = playlistModel.id
        )
        playlistViewModel.setPlaylistItemOption(playlistOptionsModel)
    }

    override fun onShare(position: Int) {
        super.onShare(position)
        val playlistItemModel = playlistModel.items[position]
        //Share model
        PlaylistUtils.showSharingDialog(requireContext(), playlistItemModel.pageSource)
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
            playlistModel.name == DEFAULT_PLAYLIST
        )
    }

    private fun openPlaylistPlayer(selectedPlaylistItemModel: PlaylistItemModel) {
        if (!selectedPlaylistItemModel.isCached && !ConnectionUtils.isDeviceOnline(requireContext())) {
            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_offline_message),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!selectedPlaylistItemModel.isCached && PlaylistUtils.isMediaSourceExpired(selectedPlaylistItemModel.mediaSrc)) {
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
        } else {
            var recentPlaylistIds = LinkedList<String>()
            val recentPlaylistJson =
                PlaylistPreferenceUtils.defaultPrefs(requireContext()).recentlyPlayedPlaylist
            if (!recentPlaylistJson.isNullOrEmpty()) {
                recentPlaylistIds = GsonBuilder().serializeNulls().create().fromJson(
                    recentPlaylistJson,
                    TypeToken.getParameterized(LinkedList::class.java, String::class.java).type
                )
                if (recentPlaylistIds.contains(playlistModel.id)) {
                    recentPlaylistIds.remove(playlistModel.id)
                }
            }
            recentPlaylistIds.addFirst(playlistModel.id)
            PlaylistPreferenceUtils.defaultPrefs(requireContext()).recentlyPlayedPlaylist =
                GsonBuilder().serializeNulls().create().toJson(recentPlaylistIds)

            activity?.stopService(Intent(requireContext(), PlaylistVideoService::class.java))
            val playlistPlayerFragment =
                PlaylistPlayerFragment.newInstance(
                    selectedPlaylistItemModel.id,
                    playlistModel
                )
            parentFragmentManager.beginTransaction()
                .replace(android.R.id.content, playlistPlayerFragment)
                .addToBackStack(PlaylistFragment::class.simpleName)
                .commit()
        }
    }

    override fun onOptionClicked(playlistOptionsModel: PlaylistOptionsModel) {
        when (playlistOptionsModel.optionType) {
            PlaylistOptions.EDIT_PLAYLIST -> {
                playlistItemAdapter.setEditMode(true)
                playlistToolbar.enableEditMode(true)
                ivPlaylistOptions.visibility = View.GONE
            }

            PlaylistOptions.MOVE_PLAYLIST_ITEMS, PlaylistOptions.COPY_PLAYLIST_ITEMS -> {
                PlaylistUtils.moveOrCopyModel = MoveOrCopyModel(
                    playlistOptionsModel.optionType,
                    "",
                    playlistItemAdapter.getSelectedItems()
                )
                playlistItemAdapter.setEditMode(false)
                playlistToolbar.enableEditMode(false)
                ivPlaylistOptions.visibility = View.VISIBLE
            }

            PlaylistOptions.RENAME_PLAYLIST -> {
                val newPlaylistFragment = NewPlaylistFragment.newInstance(
                    PlaylistOptions.RENAME_PLAYLIST,
                    playlistModel
                )
                parentFragmentManager
                    .beginTransaction()
                    .replace(android.R.id.content, newPlaylistFragment)
                    .addToBackStack(PlaylistFragment::class.simpleName)
                    .commit()
            }

            PlaylistOptions.DELETE_PLAYLIST -> {
                activity?.stopService(Intent(requireContext(), PlaylistVideoService::class.java))
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            else -> {
                //Do nothing
            }
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
            } else if (playlistItemOptionModel.optionType == PlaylistOptions.DELETE_ITEMS_OFFLINE_DATA || playlistItemOptionModel.optionType == PlaylistOptions.DELETE_PLAYLIST_ITEM) {
                playlistItemOptionModel.playlistItemModel?.let { stopVideoPlayerOnDelete(it) }
            }
            playlistViewModel.setPlaylistItemOption(playlistItemOptionModel)
        }
    }

    private fun stopVideoPlayerOnDelete(selectedPlaylistItem: PlaylistItemModel) {
        if (PlaylistVideoService.CURRENTLY_PLAYED_ITEM_ID == selectedPlaylistItem.id) {
            activity?.stopService(Intent(requireContext(), PlaylistVideoService::class.java))
        }
    }
}
