package com.brave.braveandroidplaylist.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.fragment.PlaylistFragment

class PlaylistActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        /*
            Fetch playlist
            Check, if it's empty, open EmptyPlaylistFragment else open PlaylistFragment
         */
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view_tag, PlaylistFragment()).commit()
    }
}