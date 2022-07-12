package com.brave.braveandroidplaylist.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.view.PlaylistToolbar

class AddNewPlaylistActivity : AppCompatActivity() {

    private lateinit var etPlaylistName: AppCompatEditText
    private lateinit var playlistToolbar: PlaylistToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_playlist)

        etPlaylistName = findViewById(R.id.etPlaylistName)
        playlistToolbar = findViewById(R.id.playlistToolbar)

        etPlaylistName.requestFocus()
        playlistToolbar.setActionButtonOnClickListener {
            navigateToAllPlaylistActivity()
        }
    }

    private fun navigateToAllPlaylistActivity() {
        startActivity(Intent(this, AllPlaylistActivity::class.java))
        finish()
    }
}