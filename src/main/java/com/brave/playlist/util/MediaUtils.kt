package com.brave.playlist.util

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.InputStream

object MediaUtils {
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
            Log.e(ConstantUtils.TAG, ex.message.toString())
        } finally {
            inputStream?.close()
        }
        return fileSize
    }
}
