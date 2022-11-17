package com.brave.braveandroidplaylist.util

import android.content.Context
import android.content.Intent
import com.brave.braveandroidplaylist.activity.PlaylistPlayerActivity
import com.brave.braveandroidplaylist.model.MediaModel

object PlaylistUtils {
    fun openPlaylistPlayer(context : Context, mediaModel: MediaModel) {
        val playlistPlayerActivityIntent = Intent(context, PlaylistPlayerActivity::class.java)
        playlistPlayerActivityIntent.putExtra("data", mediaModel)
        context.startActivity(playlistPlayerActivityIntent)
    }
}