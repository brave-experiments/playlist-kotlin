package com.brave.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brave.playlist.model.MediaModel
import com.brave.playlist.enums.PlaylistOptions

class PlaylistViewModel : ViewModel() {
    private val mutablePlaylistData = MutableLiveData<String>()
    val playlistData: LiveData<String> get() = mutablePlaylistData

    fun setPlaylistData(playlistData: String) {
        mutablePlaylistData.value = playlistData
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
}
