package com.brave.braveandroidplaylist.model

class MediaModel(
    val thumbnailUrl: String,
    val mediaTitle: String,
    val mediaDuration: Long,
    val mediaFileSize: Long,
    var isSelected: Boolean = false
)