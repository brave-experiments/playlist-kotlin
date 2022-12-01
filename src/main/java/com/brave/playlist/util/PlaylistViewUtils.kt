package com.brave.playlist.util

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.brave.playlist.R
import com.brave.playlist.extension.allowMoving
import com.brave.playlist.interpolator.BraveBounceInterpolator
import com.brave.playlist.listener.PlaylistOptionsListener
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.model.PlaylistOptionsModel
import com.brave.playlist.model.SnackBarActionModel
import com.brave.playlist.view.MovableImageButton
import com.brave.playlist.view.PlaylistOptionsBottomSheet
import com.google.android.material.snackbar.Snackbar

object PlaylistViewUtils {
    @JvmStatic
    fun showPlaylistButton(activity: Activity, parent: ViewGroup, playlistOptionsListener: PlaylistOptionsListener) {
        val movableImageButton = MovableImageButton(activity)
        movableImageButton.id = R.id.playlist_button_id
        movableImageButton.setBackgroundResource(R.drawable.ic_playlist_floating_button_bg)
        movableImageButton.setImageResource(R.drawable.ic_add_media_to_playlist)
        val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.marginEnd=16
        params.bottomMargin = 80
        params.gravity = Gravity.BOTTOM or Gravity.END
        movableImageButton.layoutParams = params
        movableImageButton.elevation = 8.0f
        movableImageButton.visibility=View.GONE
        movableImageButton.setOnClickListener {
            PlaylistOptionsBottomSheet(
                mutableListOf(
                    PlaylistOptionsModel(
                        activity.getString(R.string.add_media),
                        R.drawable.ic_add_media_to_playlist,
                        PlaylistOptions.ADD_MEDIA
                    ),
                    PlaylistOptionsModel(
                        activity.getString(R.string.open_playlist),
                        R.drawable.ic_open_playlist,
                        PlaylistOptions.OPEN_PLAYLIST
                    ),
                    PlaylistOptionsModel(
                        activity.getString(R.string.open_playlist_settings),
                        R.drawable.ic_playlist_settings,
                        PlaylistOptions.PLAYLIST_SETTINGS
                    ),
                    PlaylistOptionsModel(
                        activity.getString(R.string.hide_playlist_button),
                        R.drawable.ic_playlist_hide,
                        PlaylistOptions.PLAYLIST_HIDE
                    )
                )
            , playlistOptionsListener).show((activity as FragmentActivity).supportFragmentManager, null)
        }
        movableImageButton.allowMoving(true)
        if (parent.findViewById<MovableImageButton>(R.id.playlist_button_id) != null) {
            parent.removeView(parent.findViewById<MovableImageButton>(R.id.playlist_button_id))
        }
        parent.addView(movableImageButton)
        val transition = Slide(Gravity.BOTTOM)
            .addTarget(R.id.playlist_button_id)
            .setDuration(500)
            .setInterpolator(BraveBounceInterpolator())

        TransitionManager.beginDelayedTransition(parent, transition)
        movableImageButton.visibility = View.VISIBLE
//        PlaylistOnboardingPanel(activity as FragmentActivity, movableImageButton, parent)
    }

    @JvmStatic
    fun showSnackBarWithActions(view : View,message: String, action : SnackBarActionModel) {
        val snack = Snackbar.make(view,message,Snackbar.LENGTH_LONG)
        snack.setAction(action.actionText, action.onActionClickListener)
        snack.show()
    }
}
