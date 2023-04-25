package com.brave.playlist.model

import android.os.Parcel
import android.os.Parcelable

data class PlaylistOnboardingModel(
    val title: String,
    val message: String,
    val illustration: Int
) : Parcelable {
    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<PlaylistOnboardingModel> {
            override fun createFromParcel(parcel: Parcel) = PlaylistOnboardingModel(parcel)
            override fun newArray(size: Int) = arrayOfNulls<PlaylistOnboardingModel>(size)
        }
    }

    private constructor(parcel: Parcel) : this(
        title = parcel.readString().toString(),
        message = parcel.readString().toString(),
        illustration = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(message)
        parcel.writeInt(illustration)
    }

    override fun describeContents() = 0
}