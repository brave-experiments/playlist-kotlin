package com.brave.braveandroidplaylist.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.brave.braveandroidplaylist.fragment.PlaylistOnboardingSingleFragment
import com.brave.braveandroidplaylist.model.PlaylistOnboardingModel

class PlaylistOnboardingFragmentStateAdapter(
    fragmentActivity: FragmentActivity,
    private val onboardingPages: List<PlaylistOnboardingModel>
) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = onboardingPages.size

    override fun createFragment(position: Int): Fragment =
        PlaylistOnboardingSingleFragment.newInstance(onboardingPages[position])

}