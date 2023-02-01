package com.brave.playlist.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.io.IOException


object MediaUtils {
    fun getMediaDuration(context : Context, mediaPath : String): Long? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, Uri.fromFile(File(mediaPath)))
        val time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        mediaMetadataRetriever.release()
       return time?.toLong()
    }

    @Throws(IOException::class)
    fun getFileSizeFromUri(context: Context, uri: Uri): Long {
        var fileSize = 0L
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val bytes = ByteArray(1024)
            var read = -1
            while (inputStream.read(bytes).also { read = it } >= 0) {
                fileSize += read.toLong()
            }
        }
        inputStream!!.close()
        return fileSize
    }
}