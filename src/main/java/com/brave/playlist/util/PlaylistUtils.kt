package com.brave.playlist.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import com.brave.playlist.PlaylistVideoService
import java.util.*


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

    fun getRoundedCornerBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap? {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.width)
        val rectF = RectF(rect)
        val roundPx = 8f
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    fun isMediaSourceExpired(mediaSrc : String): Boolean {
        val uri: Uri =
            Uri.parse(mediaSrc)
        val expireMillis : Long? = uri.getQueryParameter("expire")?.toLong()?.times(1000L)
        return Date() > expireMillis?.let { Date(it) }
    }

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId: Int = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun showSharingDialog(context: Context, text: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(intent, "Share with:"))
    }
}