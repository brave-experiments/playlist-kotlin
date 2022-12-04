package com.brave.playlist

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.brave.playlist.adapter.PlayerNotificationAdapter
import com.brave.playlist.model.MediaModel
import com.brave.playlist.util.ConstantUtils
import com.brave.playlist.util.ConstantUtils.PLAYER_ITEMS
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class PlaylistVideoService : Service() {
    private var exoPlayer: Player? = null
    private var playlistName: String? = null
    private var playlistItems: ArrayList<MediaModel>? = arrayListOf()
    private var playerNotificationManager: PlayerNotificationManager? = null

    companion object {
        const val PLAYLIST_CHANNEL_ID = "Playlist Channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder {
        intent?.let {
            playlistName = it.getStringExtra(ConstantUtils.PLAYLIST_NAME)
            playlistItems = it.getParcelableArrayListExtra(PLAYER_ITEMS)
        }

        val playerNotificationAdapter =
            PlayerNotificationAdapter(applicationContext, playlistItems, playlistName)
        playerNotificationManager = PlayerNotificationManager.Builder(
            applicationContext,
            NOTIFICATION_ID,
            PLAYLIST_CHANNEL_ID
        ).setMediaDescriptionAdapter(playerNotificationAdapter).build()

        return PlaylistVideoServiceBinder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        playlistName = null
        playlistItems = null
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        val trackSelector = DefaultTrackSelector(applicationContext).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        exoPlayer = ExoPlayer.Builder(applicationContext).setTrackSelector(trackSelector).build()
    }

    override fun onDestroy() {
        playerNotificationManager?.setPlayer(null)
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    inner class PlaylistVideoServiceBinder : Binder() {
        fun getExoPlayerInstance() = exoPlayer
        fun getPlayerNotificationManagerInstance() = playerNotificationManager
    }
}