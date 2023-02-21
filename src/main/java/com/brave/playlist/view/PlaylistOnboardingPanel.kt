package com.brave.playlist.view

import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.brave.playlist.R
import com.brave.playlist.adapter.PlaylistOnboardingFragmentStateAdapter
import com.brave.playlist.extension.addScrimBackground
import com.brave.playlist.model.PlaylistOnboardingModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class PlaylistOnboardingPanel(fragmentActivity: FragmentActivity, anchorView: View, parent: View) {

    init {
        val view = View.inflate(fragmentActivity, R.layout.panel_playlist_onboarding, null)
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(view, width, height, true)

        val playlistOnboardingViewPager: ViewPager2 =
            view.findViewById(R.id.playlistOnboardingViewPager)

        val adapter = PlaylistOnboardingFragmentStateAdapter(fragmentActivity, listOf(
            PlaylistOnboardingModel(fragmentActivity.getString(R.string.playlist_onboarding_title_1), fragmentActivity.getString(R.string.playlist_onboarding_text_1), R.drawable.ic_playlist_onboarding_icon, R.drawable.ic_playlist_onboarding_graphic_bg),
            PlaylistOnboardingModel(fragmentActivity.getString(R.string.playlist_onboarding_title_2), fragmentActivity.getString(R.string.playlist_onboarding_text_2), R.drawable.ic_playlist_buttononboard_img2, R.drawable.ic_playlist_buttononboard_img2_bg),
            PlaylistOnboardingModel(fragmentActivity.getString(R.string.playlist_onboarding_title_3), fragmentActivity.getString(R.string.playlist_onboarding_text_3), R.drawable.ic_playlist_buttononboard_img3, R.drawable.ic_playlist_buttononboard_img2_bg)
        ))
        playlistOnboardingViewPager.adapter = adapter

        val nextButton: AppCompatButton = view.findViewById(R.id.btNextOnboarding)
        nextButton.setOnClickListener {
            if (playlistOnboardingViewPager.currentItem==2) {
                PlaylistButtonTooltip(anchorView, parent)
                popupWindow.dismiss()
            } else {
                playlistOnboardingViewPager.currentItem = playlistOnboardingViewPager.currentItem+1
            }
        }

        playlistOnboardingViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position==2) {
                    nextButton.text = fragmentActivity.getString(R.string.playlist_try_it)
                }
                adapter.notifyItemChanged(position)
            }
        })

        val tabLayout: TabLayout = view.findViewById(R.id.playlistOnboardingTabLayout)
        TabLayoutMediator(tabLayout, playlistOnboardingViewPager) { tab, _ ->
            tab.setIcon(R.drawable.ic_tab_layout_dot_selector)
        }.attach()

        view.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )

        popupWindow.animationStyle = R.style.OnboardingWindowAnimation

        val y = anchorView.bottom - view.measuredHeight - 40
        val x = anchorView.right - view.measuredWidth - 20

        popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, x, y)

        popupWindow.addScrimBackground()
    }
}