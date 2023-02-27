package com.brave.playlist.model

import com.brave.playlist.enums.PlaylistEventEnum

data class PlaylistEventModel(val playlistEventEnum: PlaylistEventEnum, val playlistId: String)
