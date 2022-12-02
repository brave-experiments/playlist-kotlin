package com.brave.playlist.model

import android.os.Parcel
import android.os.Parcelable

data class MediaModel(
    val id: String,
    val name: String,
    val pageSource: String,
    val mediaPath: String,
    val thumbnailPath: String,
    val author: String,
    val duration: String,
    val isCached:Boolean = false,
    var isSelected: Boolean = false
) : Parcelable {
    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<MediaModel> {
            override fun createFromParcel(parcel: Parcel) = MediaModel(parcel)
            override fun newArray(size: Int) = arrayOfNulls<MediaModel>(size)
        }
    }

    private constructor(parcel: Parcel) : this(
        id = parcel.readString().toString(),
        name = parcel.readString().toString(),
        pageSource = parcel.readString().toString(),
        mediaPath = parcel.readString().toString(),
        thumbnailPath = parcel.readString().toString(),
        author = parcel.readString().toString(),
        duration = parcel.readString().toString(),
        isCached = parcel.readInt() == 1,
        isSelected = parcel.readInt() == 1
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(pageSource)
        parcel.writeString(mediaPath)
        parcel.writeString(thumbnailPath)
        parcel.writeString(author)
        parcel.writeString(duration)
        parcel.writeInt(if(isCached) 1 else 0)
        parcel.writeInt(if(isSelected) 1 else 0)
    }

    override fun describeContents() = 0
}