package com.brave.playlist.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.brave.playlist.R
import com.brave.playlist.activity.PlaylistMenuOnboardingActivity
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.extension.allowMoving
import com.brave.playlist.interpolator.BraveBounceInterpolator
import com.brave.playlist.listener.PlaylistOptionsListener
import com.brave.playlist.model.PlaylistOnboardingModel
import com.brave.playlist.model.PlaylistOptionsModel
import com.brave.playlist.model.SnackBarActionModel
import com.brave.playlist.util.PlaylistPreferenceUtils.shouldShowOnboarding
import com.brave.playlist.view.MovableImageButton
import com.brave.playlist.view.bottomsheet.PlaylistOptionsBottomSheet
import com.google.android.material.snackbar.Snackbar

object PlaylistViewUtils {
    @JvmStatic
    fun showPlaylistButton(
        activity: Activity,
        parent: ViewGroup,
        playlistOptionsListener: PlaylistOptionsListener
    ) {
        val movableImageButton = MovableImageButton(activity)
        movableImageButton.id = R.id.playlist_button_id
        movableImageButton.setBackgroundResource(R.drawable.ic_playlist_floating_button_bg)
        movableImageButton.setImageResource(R.drawable.ic_add_media_to_playlist)
        val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.marginEnd = 16
        params.bottomMargin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            40F,
            activity.resources.displayMetrics
        ).toInt()
        params.gravity = Gravity.BOTTOM or Gravity.END
        movableImageButton.layoutParams = params
        movableImageButton.elevation = 8.0f
        movableImageButton.visibility = View.GONE
        movableImageButton.setOnClickListener {
            val shouldShowOnboarding: Boolean =
                PlaylistPreferenceUtils.defaultPrefs(activity).shouldShowOnboarding
            if (shouldShowOnboarding) {
                val playlistActivityIntent =
                    Intent(activity, PlaylistMenuOnboardingActivity::class.java)
                playlistActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(playlistActivityIntent)
                PlaylistPreferenceUtils.defaultPrefs(activity).shouldShowOnboarding =
                    false
            } else {
                PlaylistOptionsBottomSheet(
                    mutableListOf(
                        PlaylistOptionsModel(
                            activity.getString(R.string.playlist_add_media),
                            R.drawable.ic_add_media_to_playlist,
                            PlaylistOptions.ADD_MEDIA
                        ),
                        PlaylistOptionsModel(
                            activity.getString(R.string.playlist_open_playlist),
                            R.drawable.ic_open_playlist,
                            PlaylistOptions.OPEN_PLAYLIST
                        ),
                        PlaylistOptionsModel(
                            activity.getString(R.string.playlist_open_playlist_settings),
                            R.drawable.ic_playlist_settings,
                            PlaylistOptions.PLAYLIST_SETTINGS
                        ),
//                        PlaylistOptionsModel(
//                            activity.getString(R.string.hide_playlist_button),
//                            R.drawable.ic_playlist_hide,
//                            PlaylistOptions.PLAYLIST_HIDE
//                        )
                    ), playlistOptionsListener
                ).show((activity as FragmentActivity).supportFragmentManager, null)
            }
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
    }

    @JvmStatic
    fun showSnackBarWithActions(view: View, message: String, action: SnackBarActionModel) {
        val snack = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snack.setAction(action.actionText, action.onActionClickListener)
        snack.show()
    }

    fun getOnboardingItemList(context: Context): List<PlaylistOnboardingModel> {
        return listOf(
            PlaylistOnboardingModel(
                context.getString(R.string.playlist_onboarding_title_1),
                context.getString(R.string.playlist_onboarding_text_1),
                R.drawable.ic_playlist_graphic_1
            ),
            PlaylistOnboardingModel(
                context.getString(R.string.playlist_onboarding_title_2),
                context.getString(R.string.playlist_onboarding_text_2),
                R.drawable.ic_playlist_graphic_2
            ),
            PlaylistOnboardingModel(
                context.getString(R.string.playlist_onboarding_title_3),
                context.getString(R.string.playlist_onboarding_text_3),
                R.drawable.ic_playlist_graphic_3
            )
        )
    }
}
