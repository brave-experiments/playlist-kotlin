package com.brave.playlist.model

import android.view.View

data class SnackBarActionModel(
    val actionText: String,
    val onActionClickListener: View.OnClickListener? = null
)