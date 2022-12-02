package com.brave.playlist.model

import com.brave.playlist.enums.PlaylistOptions

class PlaylistOptionsModel(
    val optionTitle: String,
    val optionIcon: Int,
    val optionType: PlaylistOptions,
    val playlistModel:PlaylistModel? = null
)