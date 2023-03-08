package com.brave.playlist

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import com.brave.playlist.adapter.PlayerNotificationAdapter
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.util.ConstantUtils.PLAYER_ITEMS
import com.brave.playlist.util.ConstantUtils.PLAYLIST_NAME
import com.brave.playlist.util.PlaylistUtils.createNotificationChannel
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.gms.cast.framework.CastContext


class PlaylistVideoService : Service(), Player.Listener, SessionAvailabilityListener {
    private var playlistName: String? = null
    private var playlistItemsModel: ArrayList<PlaylistItemModel>? = arrayListOf()
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var localPlayer: ExoPlayer? = null
    private var castPlayer: CastPlayer? = null
    private var castContext: CastContext? = null
    private val mediaQueue: ArrayList<MediaItem> = ArrayList()
    private val castMediaQueue: ArrayList<MediaItem> = ArrayList()
    private var playerView: StyledPlayerView? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    private var currentItemIndex: Int = 0
    private var currentPlayer: Player? = null

    private val images =
        arrayListOf<String>("https://picsum.photos/200", "https://picsum.photos/id/237/200")

    companion object {
        const val PLAYLIST_CHANNEL_ID = "Playlist Channel"
        const val NOTIFICATION_ID = 1
    }

    fun setPlayerView(styledPlayerView: StyledPlayerView) {
        playerView = styledPlayerView
    }

    override fun onBind(intent: Intent?): IBinder {
        intent?.let {
            playlistName = it.getStringExtra(PLAYLIST_NAME)
            playlistItemsModel = it.getParcelableArrayListExtra(PLAYER_ITEMS)
        }

        val playerNotificationAdapter =
            PlayerNotificationAdapter(applicationContext, playlistItemsModel, playlistName)
        playerNotificationManager = PlayerNotificationManager.Builder(
            applicationContext,
            NOTIFICATION_ID,
            PLAYLIST_CHANNEL_ID
        ).setMediaDescriptionAdapter(playerNotificationAdapter).build()

        playlistItemsModel?.forEach { mediaModel ->
            val movieMetadata: MediaMetadata =
                MediaMetadata.Builder().setTitle(mediaModel.name).setArtist(mediaModel.author)
                    .setArtworkUri(Uri.parse(mediaModel.thumbnailPath)).build()
            val mediaUri = if (mediaModel.isCached) mediaModel.mediaPath else mediaModel.mediaSrc
            val mediaItem = MediaItem.Builder()
//                .setUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                .setUri(Uri.parse(mediaUri))
                .setMediaMetadata(movieMetadata)
                .setMimeType(MimeTypes.VIDEO_MP4).build()
            val castMediaItem = MediaItem.Builder()
                .setUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                .setUri(mediaModel.mediaSrc)
                .setMediaMetadata(movieMetadata)
                .setMimeType(MimeTypes.VIDEO_MP4).build()
            mediaQueue.add(mediaItem)
            castMediaQueue.add(castMediaItem)
        }

        setCurrentPlayer(if (castPlayer?.isCastSessionAvailable == true) castPlayer else localPlayer)

        return PlaylistVideoServiceBinder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        playlistName = null
        playlistItemsModel = null
        mediaSessionConnector = null
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(applicationContext)
        currentItemIndex = C.INDEX_UNSET
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(32 * 1024, 64 * 1024, 1024, 1024)
            .build()
        localPlayer = ExoPlayer.Builder(applicationContext).setLoadControl(loadControl)
            .setReleaseTimeoutMs(5000).build()
        localPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        localPlayer?.addListener(this)
        castContext = CastContext.getSharedInstance()
        castPlayer = castContext?.let { CastPlayer(it) }
        castPlayer?.addListener(this)
        castPlayer?.setSessionAvailabilityListener(this)
    }

    private fun release() {
        currentItemIndex = C.INDEX_UNSET
        mediaQueue.clear()
        castMediaQueue.clear()
        playerNotificationManager?.setPlayer(null)
        castPlayer?.setSessionAvailabilityListener(null)
        castPlayer?.release()
        playerView?.player = null
        localPlayer?.release()
    }

    fun getCurrentPlayer() = currentPlayer

    override fun onDestroy() {
        release()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    inner class PlaylistVideoServiceBinder : Binder() {
        fun getServiceInstance() = this@PlaylistVideoService
        fun getCurrentPlayer() = currentPlayer
    }

//    private fun addItem(item: MediaItem?) {
//        mediaQueue.add(item!!)
//        currentPlayer?.addMediaItem(item)
//    }

    // Player.Listener implementation.
    override fun onPlaybackStateChanged(playbackState: @Player.State Int) {
        updateCurrentItemIndex()
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: @Player.DiscontinuityReason Int
    ) {
        updateCurrentItemIndex()
    }

    override fun onTimelineChanged(timeline: Timeline, reason: @Player.TimelineChangeReason Int) {
        updateCurrentItemIndex()
    }

    // CastPlayer.SessionAvailabilityListener implementation.
    override fun onCastSessionAvailable() {
        setCurrentPlayer(castPlayer)
        val intent = Intent()
        intent.action = "action"
        intent.putExtra("should_show_controls", false)
        sendBroadcast(intent)
    }

    override fun onCastSessionUnavailable() {
        setCurrentPlayer(localPlayer)
        val intent = Intent()
        intent.action = "action"
        intent.putExtra("should_show_controls", true)
        sendBroadcast(intent)
    }

    // Internal methods.
    private fun updateCurrentItemIndex() {
        val playbackState = currentPlayer!!.playbackState
        maybeSetCurrentItemAndNotify(
            if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) currentPlayer!!.currentMediaItemIndex else C.INDEX_UNSET
        )
    }

    private fun setCurrentPlayer(currentPlayer: Player?) {
        if (this.currentPlayer === currentPlayer) {
            return
        }
        playerView?.player = currentPlayer
        playerView?.controllerHideOnTouch = currentPlayer === localPlayer
        if (currentPlayer === castPlayer) {
            playerView?.controllerShowTimeoutMs = 0
            playerView?.useController = true
            playerView?.controllerHideOnTouch = false
            playerView?.showController()
        } else { // currentPlayer == localPlayer
            playerView?.useController = false
            val mediaSession = MediaSessionCompat(applicationContext, "Player")
            mediaSessionConnector = MediaSessionConnector(mediaSession)
            mediaSessionConnector?.setPlayer(currentPlayer)
            mediaSession.isActive = true
            playerNotificationManager?.setPlayer(currentPlayer)
            playerNotificationManager?.setMediaSessionToken(mediaSession.sessionToken)
            playerView?.controllerShowTimeoutMs = StyledPlayerControlView.DEFAULT_SHOW_TIMEOUT_MS
        }

        // Player state management.
        var playbackPositionMs = C.TIME_UNSET
        var currentItemIndex = C.INDEX_UNSET
        var playWhenReady = true
        val previousPlayer = this.currentPlayer
        if (previousPlayer != null) {
            // Save state from the previous player.
            val playbackState = previousPlayer.playbackState
            if (playbackState != Player.STATE_ENDED) {
                playbackPositionMs = previousPlayer.currentPosition
                playWhenReady = previousPlayer.playWhenReady
                currentItemIndex = previousPlayer.currentMediaItemIndex
                if (currentItemIndex != this.currentItemIndex) {
                    playbackPositionMs = C.TIME_UNSET
                    currentItemIndex = this.currentItemIndex
                }
            }
            previousPlayer.stop()
            previousPlayer.clearMediaItems()
        }
        this.currentPlayer = currentPlayer

        // Media queue management.
        if (currentPlayer === castPlayer) {
            currentPlayer?.setMediaItems(castMediaQueue, currentItemIndex, playbackPositionMs)
        } else {
            currentPlayer?.setMediaItems(mediaQueue, currentItemIndex, playbackPositionMs)
        }
        currentPlayer?.playWhenReady = playWhenReady
        currentPlayer?.prepare()
    }

    fun setCurrentItem(itemIndex: Int) {
        maybeSetCurrentItemAndNotify(itemIndex)
        if (currentPlayer!!.currentTimeline.windowCount != mediaQueue.size) {
            // This only happens with the cast player. The receiver app in the cast device clears the
            // timeline when the last item of the timeline has been played to end.
            if (currentPlayer === castPlayer) {
                currentPlayer!!.setMediaItems(castMediaQueue, itemIndex, C.TIME_UNSET)
            } else {
                currentPlayer!!.setMediaItems(mediaQueue, itemIndex, C.TIME_UNSET)
            }
        } else {
            playlistItemsModel?.get(currentItemIndex)?.lastPlayedPosition?.toLong()
                ?.let { currentPlayer!!.seekTo(itemIndex, it) }
        }
        currentPlayer!!.playWhenReady = true
    }

    fun playNextItem() {
        currentPlayer?.let {
            if (it.hasNextMediaItem()) {
                it.seekToNextMediaItem()
            }
        }
    }

    private fun maybeSetCurrentItemAndNotify(currentItemIndex: Int) {
        if (this.currentItemIndex != currentItemIndex) {
            val oldIndex = this.currentItemIndex
            this.currentItemIndex = currentItemIndex
        }
    }
}