package com.brave.braveandroidplaylist.view

import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.adapter.PlaylistOnboardingFragmentStateAdapter
import com.brave.braveandroidplaylist.extension.addScrimBackground
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

        val adapter = PlaylistOnboardingFragmentStateAdapter(fragmentActivity, listOf())
        playlistOnboardingViewPager.adapter = adapter


        playlistOnboardingViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
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