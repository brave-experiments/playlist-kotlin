package com.brave.braveandroidplaylist.model

import android.view.View

class PlaylistOptionsModel(
    val optionTitle: String,
    val optionIcon: Int,
    val onOptionClickListener: View.OnClickListener? = null
)