package com.brave.playlist.model

import android.os.Parcel
import android.os.Parcelable

data class PlaylistItemModel(
    val id: String,
    val playlistId: String,
    val name: String,
    val pageSource: String,
    val mediaPath: String,
    val mediaSrc: String,
    val thumbnailPath: String,
    val author: String,
    val duration: String,
    val lastPlayedPosition : Int,
    val isCached:Boolean = false,
    var isSelected: Boolean = false
) : Parcelable {
    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<PlaylistItemModel> {
            override fun createFromParcel(parcel: Parcel) = PlaylistItemModel(parcel)
            override fun newArray(size: Int) = arrayOfNulls<PlaylistItemModel>(size)
        }
    }

    private constructor(parcel: Parcel) : this(
        id = parcel.readString().toString(),
        playlistId = parcel.readString().toString(),
        name = parcel.readString().toString(),
        pageSource = parcel.readString().toString(),
        mediaPath = parcel.readString().toString(),
        mediaSrc = parcel.readString().toString(),
        thumbnailPath = parcel.readString().toString(),
        author = parcel.readString().toString(),
        duration = parcel.readString().toString(),
        lastPlayedPosition = parcel.readInt(),
        isCached = parcel.readInt() == 1,
        isSelected = parcel.readInt() == 1
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(playlistId)
        parcel.writeString(name)
        parcel.writeString(pageSource)
        parcel.writeString(mediaPath)
        parcel.writeString(mediaSrc)
        parcel.writeString(thumbnailPath)
        parcel.writeString(author)
        parcel.writeString(duration)
        parcel.writeInt(lastPlayedPosition)
        parcel.writeInt(if(isCached) 1 else 0)
        parcel.writeInt(if(isSelected) 1 else 0)
    }

    override fun describeContents() = 0
}