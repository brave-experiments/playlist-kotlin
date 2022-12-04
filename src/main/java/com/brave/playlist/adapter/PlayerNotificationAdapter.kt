package com.brave.playlist.adapter

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.brave.playlist.R
import com.brave.playlist.activity.PlaylistOnboardingActivity
import com.brave.playlist.model.MediaModel
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager


class PlayerNotificationAdapter(
    val context: Context,
    private val playlistItems: ArrayList<MediaModel>?,
    private val playlistName: String?
) : PlayerNotificationManager.MediaDescriptionAdapter {

    override fun getCurrentContentTitle(player: Player): String {
        return playlistName.toString()
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        val intent = Intent(context, PlaylistOnboardingActivity::class.java)
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun getCurrentContentText(player: Player): String {
        return playlistItems?.get(player.currentPeriodIndex)?.name ?: "Description"
    }

    override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
        return BitmapFactory.decodeResource(context.resources, R.drawable.ic_edit_playlist)
    }
}