package com.brave.braveandroidplaylist.listener

import com.brave.braveandroidplaylist.model.PlaylistOptionsModel

interface PlaylistOptionsListener {
    fun onOptionClicked(playlistOptionsModel: PlaylistOptionsModel)
}