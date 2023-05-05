package com.brave.playlist.util

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import com.brave.playlist.PlaylistVideoService
import com.brave.playlist.activity.PlaylistMenuOnboardingActivity
import com.brave.playlist.model.MoveOrCopyModel
import com.brave.playlist.model.PlaylistItemModel
import java.util.Date


object PlaylistUtils {
    @JvmStatic
    lateinit var moveOrCopyModel: MoveOrCopyModel
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                PlaylistVideoService.PLAYLIST_CHANNEL_ID,
                PlaylistVideoService.PLAYLIST_CHANNEL_ID,
                NotificationManager.IMPORTANCE_HIGH
            )
            serviceChannel.lightColor = Color.BLUE
            serviceChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(serviceChannel)
        }
    }

    fun isMediaSourceExpired(mediaSrc: String): Boolean {
        val uri: Uri =
            Uri.parse(mediaSrc)
        if (!uri.getQueryParameter("expire").isNullOrEmpty()) {
            val expireMillis: Long? = uri.getQueryParameter("expire")?.toLong()?.times(1000L)
            return Date() > expireMillis?.let { Date(it) }
        }
        return false
    }

    fun showSharingDialog(context: Context, text: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(intent, "Share with:"))
    }

    fun isPlaylistServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun playlistNotificationIntent(context: Context, playlistItemModel: PlaylistItemModel): Intent? {
        val packageManager = context.packageManager
//        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        return try {
            val intent = Intent(context,Class.forName("org.chromium.chrome.browser.playlist.PlaylistHostActivity"))
            intent.action = "playlist"
            intent.putExtra("playlist_item_id", playlistItemModel.id)
            intent.putExtra("playlist_id", playlistItemModel.playlistId)
            intent.putExtra("name", playlistItemModel.name)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            val componentName = intent.component
//            Intent.makeRestartActivityTask(componentName)
        } catch(ex: ClassNotFoundException) {
            Log.e(ConstantUtils.TAG, "playlistNotificationIntent"+ex.message)
            null
        }
    }

    @JvmStatic
    fun openPlaylistMenuOnboardingActivity(context: Context) {
        val playlistActivityIntent = Intent(context, PlaylistMenuOnboardingActivity::class.java)
        playlistActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(playlistActivityIntent)
    }
}
