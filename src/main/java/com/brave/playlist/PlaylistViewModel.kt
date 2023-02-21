package com.brave.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.model.*

class PlaylistViewModel : ViewModel() {
    // Using Livedata for Playlist Data
    private val mutablePlaylistData = MutableLiveData<String>()
    val playlistData: LiveData<String> get() = mutablePlaylistData
    fun setPlaylistData(playlistData: String) {
        mutablePlaylistData.value = playlistData
    }

    // Using Livedata for all Playlist Data
    private val mutableAllPlaylistData = MutableLiveData<String>()
    val allPlaylistData: LiveData<String> get() = mutableAllPlaylistData
    fun setAllPlaylistData(allPlaylistData: String) {
        mutableAllPlaylistData.value = allPlaylistData
    }

    // Using after creating new playlist
    private val mutableCreatePlaylistOption = MutableLiveData<String>()
    val createPlaylistOption: LiveData<String> get() = mutableCreatePlaylistOption
    fun setCreatePlaylistOption(newName: String) {
        mutableCreatePlaylistOption.value = newName
    }

    // Using Livedata to open specific Playlist
    private val mutablePlaylistToOpen = MutableLiveData<String>()
    val playlistToOpen: LiveData<String> get() = mutablePlaylistToOpen
    fun setPlaylistToOpen(playlistId: String) {
        mutablePlaylistToOpen.value = playlistId
    }

    // Using Livedata for playlist menu option
    private val mutablePlaylistOption = MutableLiveData<PlaylistOptionsModel>()
    val playlistOption: LiveData<PlaylistOptionsModel> get() = mutablePlaylistOption
    fun setPlaylistOption(playlistOptionsModel: PlaylistOptionsModel) {
        mutablePlaylistOption.value = playlistOptionsModel
    }

    // Using Livedata for all playlist menu option
    private val mutableAllPlaylistOption = MutableLiveData<PlaylistOptionsModel>()
    val allPlaylistOption: LiveData<PlaylistOptionsModel> get() = mutableAllPlaylistOption
    fun setAllPlaylistOption(playlistOptionsModel: PlaylistOptionsModel) {
        mutableAllPlaylistOption.value = playlistOptionsModel
    }

    // Using Livedata for playlist item menu option
    private val mutablePlaylistItemOption = MutableLiveData<PlaylistItemOptionModel>()
    val playlistItemOption: LiveData<PlaylistItemOptionModel> get() = mutablePlaylistItemOption
    fun setPlaylistItemOption(playlistItemOptionModel: PlaylistItemOptionModel) {
        mutablePlaylistItemOption.value = playlistItemOptionModel
    }

    // Using Livedata for playlist multiple items menu option
    private val mutablePlaylistMultipleItemOption = MutableLiveData<PlaylistOptionsModel>()
    val playlistMultipleItemOption: LiveData<PlaylistOptionsModel> get() = mutablePlaylistMultipleItemOption
    fun setPlaylistMultipleItemOption(playlistMultipleItemOptionModel: PlaylistOptionsModel) {
        mutablePlaylistMultipleItemOption.value = playlistMultipleItemOptionModel
    }

    // Using Livedata to delete multiple items from a playlist
    private val mutableDeletePlaylistItems = MutableLiveData<PlaylistModel>()
    val deletePlaylistItems: LiveData<PlaylistModel> get() = mutableDeletePlaylistItems
    fun setDeletePlaylistItems(playlistItems: PlaylistModel) {
        mutableDeletePlaylistItems.value = playlistItems
    }

    private val mutableRenamePlaylistOption = MutableLiveData<RenamePlaylistModel>()
    val renamePlaylistOption: LiveData<RenamePlaylistModel> get() = mutableRenamePlaylistOption
    fun setRenamePlaylistOption(renamePlaylistModel: RenamePlaylistModel) {
        mutableRenamePlaylistOption.value = renamePlaylistModel
    }

    private val mutableDownloadProgress = MutableLiveData<DownloadProgressModel>()
    val downloadProgress: LiveData<DownloadProgressModel> get() = mutableDownloadProgress
    fun updateDownloadProgress (downloadProgressModel: DownloadProgressModel) {
        mutableDownloadProgress.value = downloadProgressModel
    }

    private val mutableFetchPlaylistData = MutableLiveData<String>()
    val fetchPlaylistData: LiveData<String> get() = mutableFetchPlaylistData
    fun fetchPlaylistData(playlistId: String) {
        mutableFetchPlaylistData.value = playlistId
    }

    private val mutableReorderPlaylistItems = MutableLiveData<List<PlaylistItemModel>>()
    val reorderPlaylistItems: LiveData<List<PlaylistItemModel>> get() = mutableReorderPlaylistItems
    fun reorderPlaylistItems(playlistItems: MutableList<PlaylistItemModel>) {
        mutableReorderPlaylistItems.value = playlistItems
    }
}
