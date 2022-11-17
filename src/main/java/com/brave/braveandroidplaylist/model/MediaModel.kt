package com.brave.braveandroidplaylist.model

import java.io.Serializable

class MediaModel(
    val id:String,
    val name:String,
    val pageSource:String,
    val mediaPath:String,
    val thumbnailPath: String,
//    val mediaTitle: String,
//    val mediaDuration: Long,
//    val mediaFileSize: Long,
    var isSelected: Boolean = false
) : Serializable