package com.brave.playlist

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.brave.playlist.local_database.PlaylistRepository
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.util.ConstantUtils.CAST_ACTION
import com.brave.playlist.util.ConstantUtils.CURRENT_PLAYING_ITEM_ACTION
import com.brave.playlist.util.ConstantUtils.CURRENT_PLAYING_ITEM_ID
import com.brave.playlist.util.ConstantUtils.PLAYER_ITEMS
import com.brave.playlist.util.ConstantUtils.PLAYLIST_NAME
import com.brave.playlist.util.ConstantUtils.SHOULD_SHOW_CONTROLS
import com.brave.playlist.util.ConstantUtils.TAG
import com.brave.playlist.util.PlaylistPreferenceUtils
import com.brave.playlist.util.PlaylistPreferenceUtils.continuousListening
import com.brave.playlist.util.PlaylistPreferenceUtils.rememberFilePlaybackPosition
import com.brave.playlist.util.PlaylistPreferenceUtils.rememberListPlaybackPosition
import com.brave.playlist.util.PlaylistPreferenceUtils.setLatestPlaylistItem
import com.brave.playlist.util.PlaylistUtils
import com.brave.playlist.util.PlaylistUtils.createNotificationChannel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private var currentItemIndex: Int = 0
    private var currentPlayer: Player? = null

    private var lastSavedPositionHandler: Handler? = null
    private val playlistRepository: PlaylistRepository by lazy {
        PlaylistRepository(applicationContext)
    }
    private val runnableCode: Runnable = object : Runnable {
        override fun run() {
            if (currentPlayer?.isPlaying == true) {
                Log.e(TAG, "runnableCode")
                val currentPosition = currentPlayer?.currentPosition
                currentPosition?.let { saveLastPosition(currentItemIndex, it) }
            }
            lastSavedPositionHandler?.postDelayed(this, 5000)
        }
    }

    companion object {
        const val PLAYLIST_CHANNEL_ID = "brave_playlist_channel"
        const val NOTIFICATION_ID = 1001
        var CURRENTLY_PLAYED_ITEM_ID: String? = null
    }

    private fun lastSavedPositionTimer() {
        lastSavedPositionHandler = currentPlayer?.applicationLooper?.let { Handler(it) }
        lastSavedPositionHandler?.post(runnableCode)
    }

    private fun cancelLastSavedPositionTimer() {
        lastSavedPositionHandler?.removeCallbacks(runnableCode)
    }

    fun setPlayerView(styledPlayerView: StyledPlayerView) {
        playerView = styledPlayerView
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.e(TAG, "onBind")
        return PlaylistVideoServiceBinder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(applicationContext)
        currentItemIndex = C.INDEX_UNSET
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(32 * 1024, 64 * 1024, 1024, 1024)
            .build()
        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .build()
        localPlayer = ExoPlayer.Builder(applicationContext)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(
                    PlaylistDownloadUtils.getDataSourceFactory(
                        applicationContext
                    )
                )
            )
            .setLoadControl(loadControl)
            .setReleaseTimeoutMs(5000).setAudioAttributes(audioAttributes, true).build()
        localPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        localPlayer?.addListener(this)
        castContext = CastContext.getSharedInstance()
        castPlayer = castContext?.let { CastPlayer(it) }
        castPlayer?.addListener(this)
        castPlayer?.setSessionAvailabilityListener(this)
    }

    private fun release() {
        CURRENTLY_PLAYED_ITEM_ID = null
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
        playlistName = null
        playlistItemsModel = null
        mediaSessionConnector = null
        release()
        cancelLastSavedPositionTimer()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        intent?.let {
            playlistName = it.getStringExtra(PLAYLIST_NAME)
            playlistItemsModel = it.getParcelableArrayListExtra(PLAYER_ITEMS)
        }

        playerNotificationManager = PlayerNotificationManager.Builder(
            applicationContext,
            NOTIFICATION_ID,
            PLAYLIST_CHANNEL_ID
        ).setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence {
                return playlistName.toString()
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                return PendingIntent.getActivity(
                    applicationContext,
                    0,
                    getMediaItemFromPosition(player.currentMediaItemIndex)
                        ?.let { PlaylistUtils.playlistNotificationIntent(applicationContext, it) },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            override fun getCurrentContentText(player: Player): CharSequence {
                return getMediaItemFromPosition(player.currentMediaItemIndex)?.name ?: ""
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                Glide.with(applicationContext)
                    .asBitmap()
                    .load(getMediaItemFromPosition(player.currentMediaItemIndex)?.thumbnailPath)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            callback.onBitmap(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
                return null
            }
        }).setSmallIconResourceId(com.google.android.exoplayer2.ui.R.drawable.exo_icon_pause)
            .build()

//        Log.e(TAG,DownloadUtils.getDownloadManager(applicationContext)?.downloadIndex?.getDownloads(
//            Download.STATE_DOWNLOADING, Download.STATE_FAILED, Download.STATE_QUEUED)?.download?.request?.toMediaItem()?.mediaMetadata.toString())

        Log.e(TAG, "playlistItemsModel?.size : "+playlistItemsModel?.size.toString())
        Log.e(TAG, "playlistItemsModel : "+playlistItemsModel.toString())
        mediaQueue.clear()
        castMediaQueue.clear()
        playlistItemsModel?.forEach { mediaModel ->
            val movieMetadata: MediaMetadata =
                MediaMetadata.Builder().setTitle(mediaModel.name).setArtist(mediaModel.author)
                    .setArtworkUri(Uri.parse(mediaModel.thumbnailPath)).build()
            val onlineMediaItem = MediaItem.Builder()
                .setUri(Uri.parse(if (mediaModel.isCached) mediaModel.mediaPath else mediaModel.mediaSrc))
                .setMediaMetadata(movieMetadata)
                .build()
            val mediaItem : MediaItem = PlaylistDownloadUtils.getMediaItemFromDownloadRequest(applicationContext, mediaModel)?: onlineMediaItem
            val castMediaItem = MediaItem.Builder()
//                .setUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                .setUri(mediaModel.mediaSrc)
                .setMediaMetadata(movieMetadata)
//                .setMimeType(MimeTypes.VIDEO_MP4)
                .build()
            mediaQueue.add(mediaItem)
            castMediaQueue.add(castMediaItem)
        }

        Log.e(TAG, "onStartCommand : setCurrentPlayer")
        setCurrentPlayer(if (castPlayer?.isCastSessionAvailable == true) castPlayer else localPlayer)
        scope.launch {
            lastSavedPositionTimer()
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    inner class PlaylistVideoServiceBinder : Binder() {
        fun getServiceInstance() = this@PlaylistVideoService
    }

    fun getCurrentPlayingItem() = currentPlayer?.currentMediaItemIndex?.let {
        getMediaItemFromPosition(
            it
        )
    }

    // Player.Listener implementation.
    override fun onPlaybackStateChanged(playbackState: @Player.State Int) {
        if (playbackState == Player.STATE_ENDED) {
            Log.e(TAG, "onPlaybackStateChanged")
            saveLastPosition(currentItemIndex, 0)
        }
        updateCurrentItemIndex()
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        when(error.errorCode) {
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> {
                val currentPlaylistItemModel = getCurrentPlayingItem()
                val movieMetadata: MediaMetadata =
                    MediaMetadata.Builder().setTitle(currentPlaylistItemModel?.name).setArtist(currentPlaylistItemModel?.author)
                        .setArtworkUri(Uri.parse(currentPlaylistItemModel?.thumbnailPath)).build()
                val mediaItem = MediaItem.Builder()
//                .setUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                    .setUri(Uri.parse(currentPlaylistItemModel?.mediaSrc))
                    .setMediaMetadata(movieMetadata)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()
                mediaQueue[currentPlayer?.currentMediaItemIndex!!] = mediaItem
                Log.e(TAG, "onPlayerError : setCurrentPlayer")
//                currentPlayer?.clearMediaItems()
//                currentPlayer = null // Reset current player
                setCurrentPlayer(if (castPlayer?.isCastSessionAvailable == true) castPlayer else localPlayer)
            }
            else -> {
                if (currentPlayer?.hasNextMediaItem() == true) {
                    currentPlayer?.nextMediaItemIndex?.let { setCurrentItem(it) }
                }
            }
        }
        Log.e(TAG, "onPlayerError : "+ error.message.toString())
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        Log.e(
            TAG,
            PlaylistVideoService::class.java.name + " : onMediaItemTransition : Reason : " + reason
        )
        if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
            currentPlayer?.playWhenReady =
                PlaylistPreferenceUtils.defaultPrefs(applicationContext).continuousListening
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: @Player.DiscontinuityReason Int
    ) {
        updateCurrentItemIndex()
        val playbackState = currentPlayer?.playbackState
        if (playbackState == PlaybackState.STATE_PLAYING) {
            Log.e(TAG, "onPositionDiscontinuity")
            val size = playlistItemsModel?.size ?: 0
            val previousItemIndex =
                if (currentItemIndex in 1 until size) currentItemIndex - 1 else currentItemIndex
            saveLastPosition(previousItemIndex, 0)
            sendCurrentPlayingItemBroadcast()
        }
    }

    private fun saveLastPosition(itemIndex: Int, currentPosition: Long) {
        scope.launch {
            getMediaItemFromPosition(itemIndex)
                ?.let {
                    it.lastPlayedPosition = currentPosition
                    if (PlaylistPreferenceUtils.defaultPrefs(applicationContext).rememberFilePlaybackPosition) {
                        playlistRepository.insertPlaylistItemModel(it)
                    }
                    if (PlaylistPreferenceUtils.defaultPrefs(applicationContext).rememberListPlaybackPosition) {
                        PlaylistPreferenceUtils.defaultPrefs(applicationContext)
                            .setLatestPlaylistItem(it.playlistId, it.id)
                    }
                }
        }
        sendCurrentPlayingItemBroadcast()
    }

    override fun onTimelineChanged(timeline: Timeline, reason: @Player.TimelineChangeReason Int) {
        updateCurrentItemIndex()
    }

    // CastPlayer.SessionAvailabilityListener implementation.
    override fun onCastSessionAvailable() {
        Log.e(TAG, "onCastSessionUnavailable : setCurrentPlayer")
        setCurrentPlayer(castPlayer)
        sendCastStatusBroadcast(shouldShowControls = false)
    }

    override fun onCastSessionUnavailable() {
        Log.e(TAG, "onCastSessionUnavailable : setCurrentPlayer")
        setCurrentPlayer(localPlayer)
        sendCastStatusBroadcast(shouldShowControls = true)
    }

    private fun sendCastStatusBroadcast(shouldShowControls: Boolean) {
        val intent = Intent()
        intent.action = CAST_ACTION
        intent.putExtra(SHOULD_SHOW_CONTROLS, shouldShowControls)
        sendBroadcast(intent)
    }

    private fun sendCurrentPlayingItemBroadcast() {
        Log.e("NTP", "sendCurrentPlayingItemBroadcast")
        val currentPlayingItemId = currentPlayer?.currentMediaItemIndex?.let {
            getMediaItemFromPosition(
                it
            )?.id
        }
        if (!currentPlayingItemId.isNullOrEmpty()) {
            val intent = Intent()
            intent.action = CURRENT_PLAYING_ITEM_ACTION
            intent.putExtra(CURRENT_PLAYING_ITEM_ID, currentPlayingItemId)
            sendBroadcast(intent)
        }
        CURRENTLY_PLAYED_ITEM_ID = currentPlayingItemId
        Log.e("CURRENTLY_PLAYED_ITEM_ID", CURRENTLY_PLAYED_ITEM_ID.toString())
    }

    private fun getMediaItemFromPosition(position: Int) : PlaylistItemModel? {
//        if ((playlistItemsModel?.size ?: 0) >= position) {
//            return null
//        }
        return playlistItemsModel?.get(position)
    }

    // Internal methods.
    private fun updateCurrentItemIndex() {
        val playbackState = currentPlayer?.playbackState
        maybeSetCurrentItemAndNotify(
            if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) currentPlayer?.currentMediaItemIndex?:C.INDEX_UNSET else C.INDEX_UNSET
        )
    }

    private fun setCurrentPlayer(currentPlayer: Player?) {
//        if (this.currentPlayer === currentPlayer) {
//            return
//        }
        playerView?.player = currentPlayer
        playerView?.controllerHideOnTouch = currentPlayer === localPlayer
        if (currentPlayer === castPlayer) {
            playerView?.controllerShowTimeoutMs = 0
            playerView?.useController = true
            playerView?.controllerHideOnTouch = false
            playerView?.showController()
        } else { // currentPlayer == localPlayer
            playerView?.useController = false
            val mediaSession = MediaSessionCompat(applicationContext, TAG)
            mediaSessionConnector = MediaSessionConnector(mediaSession)
            mediaSessionConnector?.setPlayer(currentPlayer)
            mediaSession.isActive = true
            playerNotificationManager?.setPlayer(currentPlayer)
            playerNotificationManager?.setMediaSessionToken(mediaSession.sessionToken)
            playerView?.controllerShowTimeoutMs = StyledPlayerControlView.DEFAULT_SHOW_TIMEOUT_MS
//            val audioAttributes: AudioAttributes = AudioAttributes.Builder()
//                .setUsage(C.USAGE_MEDIA)
//                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
//                .build()
//
//            localPlayer?.setAudioAttributes(audioAttributes, true)
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

        Log.e(TAG, "before currentPlayer?.mediaItemCount : "+ currentPlayer?.mediaItemCount)
        // Media queue management.
        if (currentPlayer === castPlayer) {
            currentPlayer?.setMediaItems(castMediaQueue, currentItemIndex, playbackPositionMs)
        } else {
            currentPlayer?.setMediaItems(mediaQueue, currentItemIndex, playbackPositionMs)
        }
        Log.e(TAG, "after currentPlayer?.mediaItemCount : "+ currentPlayer?.mediaItemCount)
        currentPlayer?.playWhenReady = playWhenReady
        currentPlayer?.prepare()
        currentPlayer?.play()
    }

    fun setCurrentItem(itemIndex: Int) {
        maybeSetCurrentItemAndNotify(itemIndex)
        if (currentPlayer?.currentTimeline?.windowCount != mediaQueue.size) {
            // This only happens with the cast player. The receiver app in the cast device clears the
            // timeline when the last item of the timeline has been played to end.
            if (currentPlayer === castPlayer) {
                currentPlayer?.setMediaItems(castMediaQueue, itemIndex, C.TIME_UNSET)
            } else {
                currentPlayer?.setMediaItems(mediaQueue, itemIndex, C.TIME_UNSET)
            }
        } else {
            playlistItemsModel?.get(currentItemIndex)?.lastPlayedPosition
                ?.let { currentPlayer?.seekTo(itemIndex, it) }
        }
        currentPlayer?.playWhenReady = true
    }

    private fun maybeSetCurrentItemAndNotify(currentItemIndex: Int) {
        if (this.currentItemIndex != currentItemIndex) {
            val oldIndex = this.currentItemIndex
            this.currentItemIndex = currentItemIndex
            CURRENTLY_PLAYED_ITEM_ID = getCurrentPlayingItem()?.id
            Log.e(TAG,"CURRENTLY_PLAYED_ITEM_ID: "+ CURRENTLY_PLAYED_ITEM_ID.toString())
        }
    }
}
