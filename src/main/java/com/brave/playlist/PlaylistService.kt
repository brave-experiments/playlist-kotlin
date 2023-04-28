package com.brave.playlist

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.brave.playlist.view.PlaylistToolbar
import com.google.android.exoplayer2.ExoPlayer

class PlaylistService : Service() {

    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }
}