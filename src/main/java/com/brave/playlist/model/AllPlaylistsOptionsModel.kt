package com.brave.playlist.model

import com.brave.playlist.enums.PlaylistOptions

data class AllPlaylistsOptionsModel(
    val optionTitle: String,
    val optionIcon: Int,
    val optionType: PlaylistOptions
)