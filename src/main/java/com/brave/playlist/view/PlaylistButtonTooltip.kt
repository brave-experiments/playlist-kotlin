package com.brave.playlist.view

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatImageView
import com.brave.playlist.R

class PlaylistButtonTooltip(anchorView: View, parent: View) {

    init {
        val view =
            View.inflate(anchorView.context, R.layout.tooltip_playlist_onboarding_button, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(view, width, height, true)
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        view.findViewById<AppCompatImageView>(R.id.ivCloseTooltip).setOnClickListener {
            popupWindow.dismiss()
        }

        popupWindow.animationStyle = R.style.OnboardingWindowAnimation

        val y = anchorView.bottom - view.measuredHeight - 40
        val x = anchorView.right - view.measuredWidth - 20

        popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, x, y)
    }
}