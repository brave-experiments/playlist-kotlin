package com.brave.playlist.model

import com.brave.playlist.enums.PlaylistOptions

data class MoveOrCopyModel(val playlistOptions: PlaylistOptions, val toPlaylistId: String, val items: List<PlaylistItemModel>)
