package com.brave.braveandroidplaylist.extension

import android.content.Context
import android.view.WindowManager
import android.widget.PopupWindow

fun PopupWindow.addScrimBackground() {
    val rootView = contentView.rootView
    val windowManager: WindowManager =
        rootView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val params = rootView.layoutParams as WindowManager.LayoutParams
    params.flags = params.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
    params.dimAmount = 0.3f
    windowManager.updateViewLayout(rootView, params)
}
