package com.brave.playlist.model

import com.brave.playlist.enums.PlaylistEventEnum

data class PlaylistEventModel(val playlistEventEnum: PlaylistEventEnum, val playlistModel: PlaylistModel)
