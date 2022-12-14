package com.brave.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brave.playlist.model.MediaModel
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.model.PlaylistModel

class PlaylistViewModel : ViewModel() {
    private val mutablePlaylistData = MutableLiveData<String>()
    val playlistData: LiveData<String> get() = mutablePlaylistData

    fun setPlaylistData(playlistData: String) {
        mutablePlaylistData.value = playlistData
    }

    private val mutableAllPlaylistData = MutableLiveData<String>()
    val allPlaylistData: LiveData<String> get() = mutableAllPlaylistData

    fun setAllPlaylistData(allPlaylistData: String) {
        mutableAllPlaylistData.value = allPlaylistData
    }

    private val mutableSelectedPlaylistItem = MutableLiveData<MediaModel>()
    val selectedPlaylistItem: LiveData<MediaModel> get() = mutableSelectedPlaylistItem

    fun setSelectedPlaylistItem(mediaModel: MediaModel) {
        mutableSelectedPlaylistItem.value = mediaModel
    }

    private val mutableSelectedOption = MutableLiveData<PlaylistOptions>()
    val selectedOption: LiveData<PlaylistOptions> get() = mutableSelectedOption

    fun setSelectedOption(playlistOptions: PlaylistOptions) {
        mutableSelectedOption.value = playlistOptions
    }

    private val mutableCreatePlaylistOption = MutableLiveData<String>()
    val createPlaylistOption: LiveData<String> get() = mutableCreatePlaylistOption

    fun setCreatePlaylistOption(newName: String) {
        mutableCreatePlaylistOption.value = newName
    }

    private val mutablePlaylistToOpen = MutableLiveData<String>()
    val playlistToOpen: LiveData<String> get() = mutablePlaylistToOpen
    fun setPlaylistToOpen(playlistId: String) {
        mutablePlaylistToOpen.value = playlistId
    }

    private val mutableDeletePlaylistItems = MutableLiveData<PlaylistModel>()
    val deletePlaylistItems: LiveData<PlaylistModel> get() = mutableDeletePlaylistItems
    fun setDeletePlaylistItems(playlistItems: PlaylistModel) {
        mutableDeletePlaylistItems.value = playlistItems
    }
}
