package com.brave.playlist.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable

data class PlaylistModel(val id: String, val name: String, val items: List<PlaylistItemModel>) :
    Parcelable {
    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<PlaylistModel> {
            override fun createFromParcel(parcel: Parcel) = PlaylistModel(parcel)
            override fun newArray(size: Int) = arrayOfNulls<PlaylistModel>(size)
        }
    }

    private constructor(parcel: Parcel) : this(
        id = parcel.readString().toString(),
        name = parcel.readString().toString(),
        arrayListOf<PlaylistItemModel>().apply {
            parcel.readList(this, PlaylistItemModel::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        if (Build.VERSION.SDK_INT >= 29) {
            parcel.writeParcelableList(items, flags)
        } else {
            parcel.writeList(items)
        }
    }

    override fun describeContents() = 0
}