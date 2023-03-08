package com.brave.playlist.model

data class DownloadProgressModel(
    val id: String,
    val totalBytes: Long,
    val receivedBytes: Long,
    val percentComplete: Byte,
    val timeRemaining: String
)
