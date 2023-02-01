package com.brave.playlist.model

import com.brave.playlist.enums.PlaylistOptions

data class PlaylistItemOptionModel(
    val optionTitle: String,
    val optionIcon: Int,
    val optionType: PlaylistOptions,
    val playlistItemModel: PlaylistItemModel?,
    val playlistId: String?
)
