package com.brave.braveandroidplaylist.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.model.PlaylistOnboardingModel

class PlaylistOnboardingSingleFragment : Fragment(R.layout.fragment_single_playlist_onboarding) {

    private var playlistOnboardingModel: PlaylistOnboardingModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistOnboardingModel = it.getParcelable(MODEL)
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
            if (it.illustrationBg != 0)
                ivOnboardingIllustration.setBackgroundResource(it.illustrationBg)
        }
    }

    companion object {

        private const val MODEL = "MODEL"

        @JvmStatic
        fun newInstance(model: PlaylistOnboardingModel) =
            PlaylistOnboardingSingleFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(MODEL, model)
                }
            }
    }
}