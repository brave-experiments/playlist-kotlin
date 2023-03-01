package com.brave.playlist.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import com.brave.playlist.PlaylistVideoService
import com.brave.playlist.model.MoveOrCopyModel
import java.util.Date


object PlaylistUtils {
    @JvmStatic lateinit var moveOrCopyModel: MoveOrCopyModel
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

    fun isMediaSourceExpired(mediaSrc : String): Boolean {
        val uri: Uri =
            Uri.parse(mediaSrc)
        val expireMillis : Long? = uri.getQueryParameter("expire")?.toLong()?.times(1000L)
        return Date() > expireMillis?.let { Date(it) }
    }

    fun showSharingDialog(context: Context, text: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(intent, "Share with:"))
    }
}