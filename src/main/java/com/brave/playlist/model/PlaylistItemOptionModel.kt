package com.brave.playlist.model

import com.brave.playlist.enums.PlaylistOptions

data class PlaylistItemOptionModel(val playlistOptions: PlaylistOptions, val playlistItemModel: PlaylistItemModel?, val playlistId : String?)
