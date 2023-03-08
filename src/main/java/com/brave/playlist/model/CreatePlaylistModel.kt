package com.brave.playlist.model

data class CreatePlaylistModel(val newPlaylistId: String, val isMoveOrCopy: Boolean = false)
