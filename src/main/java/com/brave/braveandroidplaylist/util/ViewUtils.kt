package com.brave.braveandroidplaylist.util

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.extension.allowMoving
import com.brave.braveandroidplaylist.interpolator.BraveBounceInterpolator
import com.brave.braveandroidplaylist.model.PlaylistOptionsModel
import com.brave.braveandroidplaylist.view.MovableImageButton
import com.brave.braveandroidplaylist.view.PlaylistOnboardingPanel
import com.brave.braveandroidplaylist.view.PlaylistOptionsBottomSheet

object ViewUtils {
    @JvmStatic
    fun showPlaylistButton(context: Context, parent: ViewGroup, shouldShowOnboarding: Boolean) {
        val movableImageButton = MovableImageButton(context)
        movableImageButton.id = R.id.playlist_button_id
        movableImageButton.setBackgroundResource(R.drawable.ic_playlist_floating_button_bg)
        movableImageButton.setImageResource(R.drawable.ic_add_media_to_playlist)
        val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.BOTTOM or Gravity.END
        movableImageButton.layoutParams = params
        movableImageButton.elevation = 8.0f
        movableImageButton.setOnClickListener {
            if (shouldShowOnboarding) {
                PlaylistOnboardingPanel(context as FragmentActivity, movableImageButton, parent)
            } else {
                PlaylistOptionsBottomSheet(
                    mutableListOf(
                        PlaylistOptionsModel(
                            context.getString(R.string.add_media),
                            R.drawable.ic_add_media_to_playlist
                        ),
                        PlaylistOptionsModel(
                            context.getString(R.string.open_playlist),
                            R.drawable.ic_open_playlist
                        ),
                        PlaylistOptionsModel(
                            context.getString(R.string.opne_playlist_settings),
                            R.drawable.ic_playlist_settings
                        ),
                        PlaylistOptionsModel(
                            context.getString(R.string.hide_playlist_button),
                            R.drawable.ic_playlist_hide
                        )
                    )
                ).show((context as FragmentActivity).supportFragmentManager, null)
            }
        }
        movableImageButton.allowMoving(!shouldShowOnboarding)

        parent.removeView(movableImageButton)
        parent.addView(movableImageButton)

        val transition = Slide(Gravity.BOTTOM)
            .addTarget(R.id.playlist_button_id)
            .setDuration(500)
            .setInterpolator(BraveBounceInterpolator())

        TransitionManager.beginDelayedTransition(parent, transition)
    }
}
