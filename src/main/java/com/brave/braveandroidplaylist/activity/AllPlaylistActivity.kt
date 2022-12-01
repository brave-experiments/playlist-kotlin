package com.brave.braveandroidplaylist.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.adapter.PlaylistAdapter
import com.brave.braveandroidplaylist.adapter.RecentlyPlayedPlaylistAdapter
import com.brave.braveandroidplaylist.view.PlaylistOptionsBottomSheet

class AllPlaylistActivity : AppCompatActivity(R.layout.activity_all_playlist) {
    private lateinit var btAddNewPlaylist: AppCompatButton
    private lateinit var ivPlaylistsOptions: AppCompatImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btAddNewPlaylist = findViewById(R.id.btAddNewPlaylist)
        ivPlaylistsOptions = findViewById(R.id.ivPlaylistsOptions)

        btAddNewPlaylist.setOnClickListener { navigateToAddNewPlaylistActivity() }
        ivPlaylistsOptions.setOnClickListener {
//            PlaylistOptionsBottomSheet(mutableListOf()).show(supportFragmentManager, null)
        }

        val rvRecentlyPlayed: RecyclerView = findViewById(R.id.rvRecentlyPlayed)
        val rvPlaylist: RecyclerView = findViewById(R.id.rvPlaylists)
        rvRecentlyPlayed.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvRecentlyPlayed.adapter = RecentlyPlayedPlaylistAdapter(mutableListOf())
        rvPlaylist.layoutManager = LinearLayoutManager(this)
        rvPlaylist.adapter = PlaylistAdapter(mutableListOf())

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    private fun navigateToAddNewPlaylistActivity() {
//        startActivity(Intent(this, AddNewPlaylistActivity::class.java))
    }
}