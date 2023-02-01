package com.brave.playlist.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.Exception


object MediaUtils {
    fun getMediaDuration(context : Context, mediaPath : String): Long? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, Uri.fromFile(File(mediaPath)))
        val time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        mediaMetadataRetriever.release()
       return time?.toLong()
    }

    fun getFileSizeFromUri(context: Context, uri: Uri): Long {
        var fileSize = 0L
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val bytes = ByteArray(1024)
                var read: Int
                while (inputStream.read(bytes).also { read = it } >= 0) {
                    fileSize += read.toLong()
                }
            }
        } catch (ex: Exception) {
            Log.e("BravePlaylist", ex.message.toString());
        } finally {
            inputStream?.close()
        }
        return fileSize
    }
}