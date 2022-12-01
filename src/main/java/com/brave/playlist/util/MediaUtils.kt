package com.brave.playlist.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File

object MediaUtils {
    fun getMediaDuration(context : Context, mediaPath : String): Long? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, Uri.fromFile(File(mediaPath)))
        val time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        mediaMetadataRetriever.release()
       return time?.toLong()
    }
}