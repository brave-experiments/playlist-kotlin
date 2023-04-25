package com.brave.playlist.activity

import android.os.Bundle
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.viewpager2.widget.ViewPager2
import com.brave.playlist.R
import com.brave.playlist.adapter.PlaylistOnboardingFragmentStateAdapter
import com.brave.playlist.extension.afterMeasured
import com.brave.playlist.extension.showOnboardingGradientBg
import com.brave.playlist.model.PlaylistOnboardingModel
import com.brave.playlist.util.PlaylistViewUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PlaylistMenuOnboardingActivity : AppCompatActivity(R.layout.playlist_onboarding_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onboardingLayout = findViewById<ScrollView>(R.id.onboardingLayout)
        onboardingLayout.afterMeasured {
            showOnboardingGradientBg()
        }

        val playlistOnboardingViewPager: ViewPager2 = findViewById(R.id.playlistOnboardingViewPager)

        val adapter = PlaylistOnboardingFragmentStateAdapter(
            this, PlaylistViewUtils.getOnboardingItemList(this)
        )
        playlistOnboardingViewPager.adapter = adapter

        val nextButton: AppCompatButton = findViewById(R.id.btNextOnboarding)
        nextButton.setOnClickListener {
            if (playlistOnboardingViewPager.currentItem == 2) {
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
                nextButton.text = if (position ==2) getString(R.string.playlist_try_it) else getString(R.string.playlist_next)
                adapter.notifyItemChanged(position)
            }
        })

        val tabLayout: TabLayout = findViewById(R.id.playlistOnboardingTabLayout)
        TabLayoutMediator(tabLayout, playlistOnboardingViewPager) { tab, _ ->
            tab.setIcon(R.drawable.ic_tab_layout_dot_selector)
        }.attach()
    }
}
