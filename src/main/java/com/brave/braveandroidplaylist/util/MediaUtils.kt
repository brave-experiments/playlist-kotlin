package com.brave.braveandroidplaylist.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.View
import com.brave.braveandroidplaylist.model.SnackBarActionModel
import com.google.android.material.snackbar.Snackbar
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