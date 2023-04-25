package com.brave.playlist.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.brave.playlist.R
import com.brave.playlist.model.PlaylistOnboardingModel
import com.brave.playlist.util.ConstantUtils.ONBOARDING_MODEL

class PlaylistOnboardingSingleFragment : Fragment(R.layout.fragment_single_playlist_onboarding) {

    private var playlistOnboardingModel: PlaylistOnboardingModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistOnboardingModel = it.getParcelable(ONBOARDING_MODEL)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ivOnboardingIllustration: AppCompatImageView =
            view.findViewById(R.id.ivOnboardingIllustration)
        val tvOnboardingTitle: AppCompatTextView = view.findViewById(R.id.tvOnboardingTitle)
        val tvOnboardingMessage: AppCompatTextView = view.findViewById(R.id.tvOnboardingMessage)

        playlistOnboardingModel?.let {
            tvOnboardingTitle.text = it.title
            tvOnboardingMessage.text = it.message
            ivOnboardingIllustration.setImageResource(it.illustration)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(model: PlaylistOnboardingModel) =
            PlaylistOnboardingSingleFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ONBOARDING_MODEL, model)
                }
            }
    }
}
