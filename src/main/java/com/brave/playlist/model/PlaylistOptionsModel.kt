package com.brave.playlist.model

import com.brave.playlist.enums.PlaylistOptions

data class PlaylistOptionsModel(
    val optionTitle: String,
    val optionIcon: Int,
    val optionType: PlaylistOptions,
    val allPlaylistModels: MutableList<PlaylistModel> = mutableListOf(),
    val playlistModel:PlaylistModel? = null,
    val playlistItemModels: ArrayList<PlaylistItemModel> = arrayListOf() // Used for multiple items
)