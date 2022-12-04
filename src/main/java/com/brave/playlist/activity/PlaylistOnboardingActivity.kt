package com.brave.playlist.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.brave.playlist.R
import com.brave.playlist.view.PlaylistToolbar


class PlaylistOnboardingActivity : AppCompatActivity(R.layout.activity_playlist_onboarding) {

    private lateinit var btCreateAPlaylist: AppCompatButton
    private lateinit var tvSkipPlaylist: AppCompatTextView
    private lateinit var playlistToolbar: PlaylistToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btCreateAPlaylist = findViewById(R.id.btCreateAPlaylist)
        tvSkipPlaylist = findViewById(R.id.tvSkipPlaylist)
        playlistToolbar = findViewById(R.id.playlistToolbar)

        btCreateAPlaylist.setOnClickListener { navigateToAddNewPlaylistActivity() }
        tvSkipPlaylist.setOnClickListener { navigateToAllPlaylistActivity() }

//        showOnboardingButton();
    }

    private fun navigateToAddNewPlaylistActivity() {
//        startActivity(Intent(this, AddNewPlaylistActivity::class.java))
    }

    private fun navigateToAllPlaylistActivity() {
//        startActivity(Intent(this, AllPlaylistActivity::class.java))
    }

//    private fun showOnboardingButton() {
//        val movableImageButton: View = findViewById(R.id.movableImageButton)
//        val parent = findViewById<ViewGroup>(R.id.parent)
//
//        val transition = Slide(Gravity.BOTTOM)
//            .addTarget(R.id.movableImageButton)
//            .setDuration(500)
//            .setInterpolator(BraveBounceInterpolator())
//
//        TransitionManager.beginDelayedTransition(parent, transition)
//
//        movableImageButton.visibility = View.VISIBLE
//    }
}