package com.brave.playlist.util

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import com.brave.playlist.PlaylistVideoService


object PlaylistUtils {
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                PlaylistVideoService.PLAYLIST_CHANNEL_ID,
                PlaylistVideoService.PLAYLIST_CHANNEL_ID,
                NotificationManager.IMPORTANCE_HIGH
            )
            serviceChannel.lightColor = Color.BLUE
            serviceChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(serviceChannel)
        }
    }
}