package com.brave.braveandroidplaylist.model

enum class PlaylistOptions {
    ADD_MEDIA, EDIT_PLAYLIST, RENAME_PLAYLIST, REMOVE_PLAYLIST_OFFLINE_DATA, DOWNLOAD_PLAYLIST_FOR_OFFLINE_USE, DELETE_PLAYLIST, OPEN_PLAYLIST, PLAYLIST_SETTINGS, PLAYLIST_HIDE
}

class PlaylistOptionsModel(
    val optionTitle: String,
    val optionIcon: Int,
    val optionType: PlaylistOptions
)