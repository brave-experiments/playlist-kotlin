package com.brave.braveandroidplaylist.listener

import com.brave.braveandroidplaylist.model.PlaylistModel

interface AllPlaylistItemListener {

    fun onClickPlaylist(playlistModel: PlaylistModel)
}