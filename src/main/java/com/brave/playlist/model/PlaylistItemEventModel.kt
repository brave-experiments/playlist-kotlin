package com.brave.playlist.model

import com.brave.playlist.enums.PlaylistItemEventEnum

data class PlaylistItemEventModel(val playlistItemEventEnum: PlaylistItemEventEnum, val playlistItemModel: PlaylistItemModel)