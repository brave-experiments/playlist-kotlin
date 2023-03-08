package com.brave.playlist.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.viewpager2.widget.ViewPager2
import com.brave.playlist.R
import com.brave.playlist.adapter.PlaylistOnboardingFragmentStateAdapter
import com.brave.playlist.model.PlaylistOnboardingModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PlaylistMenuOnboardingActivity : AppCompatActivity(R.layout.playlist_onboarding_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playlistOnboardingViewPager: ViewPager2 = findViewById(R.id.playlistOnboardingViewPager)

        val adapter = PlaylistOnboardingFragmentStateAdapter(
            this, listOf(
                PlaylistOnboardingModel(
                    getString(R.string.playlist_onboarding_title_1),
                    getString(R.string.playlist_onboarding_text_1),
                    R.drawable.ic_playlist_onboarding_icon,
                    R.drawable.ic_playlist_onboarding_graphic_bg
                ),
                PlaylistOnboardingModel(
                    getString(R.string.playlist_onboarding_title_3),
                    getString(R.string.playlist_onboarding_text_3),
                    R.drawable.ic_playlist_buttononboard_img3,
                    R.drawable.ic_playlist_buttononboard_img2_bg
                )
            )
        )
        playlistOnboardingViewPager.adapter = adapter

        val nextButton: AppCompatButton = findViewById(R.id.btNextOnboarding)
        nextButton.setOnClickListener {
            if (playlistOnboardingViewPager.currentItem == 1) {
                finish()
            } else {
                playlistOnboardingViewPager.currentItem =
                    playlistOnboardingViewPager.currentItem + 1
            }
        }

        playlistOnboardingViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 1) {
                    nextButton.text = getString(R.string.playlist_browse_for_media)
                } else {
                    nextButton.text = getString(R.string.playlist_next)
                }
                adapter.notifyItemChanged(position)
            }
        })

        val tabLayout: TabLayout = findViewById(R.id.playlistOnboardingTabLayout)
        TabLayoutMediator(tabLayout, playlistOnboardingViewPager) { tab, _ ->
            tab.setIcon(R.drawable.ic_tab_layout_dot_selector)
        }.attach()
    }
}
