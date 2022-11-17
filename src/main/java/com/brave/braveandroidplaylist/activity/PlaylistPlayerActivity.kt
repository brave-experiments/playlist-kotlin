package com.brave.braveandroidplaylist.activity

import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.model.MediaModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView


class PlaylistPlayerActivity : AppCompatActivity(R.layout.activity_playlist_player),
    Player.Listener {

    private var exoPlayer: Player? = null
    private var duration: Long = 0
    private var isUserTrackingTouch = false
    private var currentMediaIndex = 0
    private var playbackPosition = 0L
    private var isShuffleOn = false
    private var playWhenReady = true
    private var playbackSpeed = 1f
    private var repeatMode = Player.REPEAT_MODE_OFF
    private var updatePositionDelayMs = 1000L

    private val styledPlayerView: StyledPlayerView by lazy {
        findViewById(R.id.styledPlayerView)
    }

    private val videoSeekBar: SeekBar by lazy {
        findViewById(R.id.videoSeekBar)
    }

    private val tvVideoTimeElapsed: AppCompatTextView by lazy {
        findViewById(R.id.tvVideoTimeElapsed)
    }

    private val tvVideoTimeRemaining: AppCompatTextView by lazy {
        findViewById(R.id.tvVideoTimeRemaining)
    }

    override fun onResume() {
        super.onResume()
        initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        init()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == ExoPlayer.STATE_READY) {
            exoPlayer?.let {
                duration = it.duration
                updateTime(it.currentPosition)
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying && !isUserTrackingTouch)
            styledPlayerView.postDelayed(this::setCurrentPlayerPosition, updatePositionDelayMs)
    }

    private fun init() {
        setMediaStats()
        setNextMedia()
        setPrevMedia()
        setPlayAndPause()
        setSeekForward()
        setSeekBack()
        setSeekBarListener()
        setPlaylistShuffle()
        playWhenReady()
        setPlaylistRepeatMode()
        setPlaybackSpeed()
    }

    private fun setPlaybackSpeed() {
        val ivPlaylistMediaSpeed: AppCompatImageView = findViewById(R.id.ivPlaylistMediaSpeed)
        ivPlaylistMediaSpeed.setOnClickListener {
            playbackSpeed += 0.5f
            if (playbackSpeed > 2)
                playbackSpeed = 1f
            when (playbackSpeed) {
                1f -> ivPlaylistMediaSpeed.setImageResource(R.drawable.ic_playlist_speed_1x)
                1.5f -> ivPlaylistMediaSpeed.setImageResource(R.drawable.ic_playlist_speed_1_point_5_x)
                2f -> ivPlaylistMediaSpeed.setImageResource(R.drawable.ic_playlist_speed_2x)
            }
            updatePositionDelayMs = (updatePositionDelayMs / playbackSpeed).toLong()
            exoPlayer?.setPlaybackSpeed(playbackSpeed)
        }
    }

    private fun setPlaylistRepeatMode() {
        val ivPlaylistRepeat: AppCompatImageView = findViewById(R.id.ivPlaylistRepeat)
        ivPlaylistRepeat.setOnClickListener {
            when (repeatMode) {
                Player.REPEAT_MODE_OFF -> {
                    repeatMode = Player.REPEAT_MODE_ALL
                    ivPlaylistRepeat.setImageResource(R.drawable.ic_playlist_repeat_all_on)
                }
                Player.REPEAT_MODE_ALL -> {
                    repeatMode = Player.REPEAT_MODE_ONE
                    ivPlaylistRepeat.setImageResource(R.drawable.ic_playlist_repeat_1)
                }
                Player.REPEAT_MODE_ONE -> {
                    repeatMode = Player.REPEAT_MODE_OFF
                    ivPlaylistRepeat.setImageResource(R.drawable.ic_playlist_repeat_all_off)
                }
            }
            exoPlayer?.repeatMode = repeatMode
        }
    }

    private fun setPlaylistShuffle() {
        val ivPlaylistShuffle: AppCompatImageView = findViewById(R.id.ivPlaylistShuffle)
        ivPlaylistShuffle.setOnClickListener {
            isShuffleOn = !isShuffleOn
            exoPlayer?.shuffleModeEnabled = isShuffleOn
            ivPlaylistShuffle.setImageResource(
                if (isShuffleOn)
                    R.drawable.ic_playlist_shuffle_on
                else
                    R.drawable.ic_playlist_shuffle_off
            )
        }
    }

    private fun setMediaStats() {

    }

    private fun playWhenReady() {
        exoPlayer?.playWhenReady = true
    }

    private fun initializePlayer() {
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        val mediaModel = intent.getSerializableExtra("data") as MediaModel
        exoPlayer = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
            .also {
                styledPlayerView.player = it
                val mediaItem: MediaItem =
                    MediaItem.fromUri(mediaModel.mediaPath)
                it.addListener(this)
                it.playWhenReady = playWhenReady
                it.shuffleModeEnabled = isShuffleOn
                it.setMediaItem(mediaItem)
                it.seekTo(currentMediaIndex, playbackPosition)
                it.repeatMode = repeatMode
                it.setPlaybackSpeed(playbackSpeed)
                it.prepare()
            }
    }

    private fun releasePlayer() {
        exoPlayer?.let {
            it.removeListener(this)
            playbackPosition = it.currentPosition
            currentMediaIndex = it.currentMediaItemIndex
            playWhenReady = it.playWhenReady
            isShuffleOn = it.shuffleModeEnabled
            it.release()
        }
        exoPlayer = null
    }

    private fun setNextMedia() {
        val ivNextVideo: AppCompatImageView = findViewById(R.id.ivNextVideo)
        ivNextVideo.setOnClickListener {
            exoPlayer?.let {
                if (it.hasNextMediaItem()) {
                    it.seekToNextMediaItem()
                }
            }
        }
    }

    private fun setPrevMedia() {
        val ivPrevVideo: AppCompatImageView = findViewById(R.id.ivPrevVideo)
        ivPrevVideo.setOnClickListener {
            exoPlayer?.let {
                if (it.hasPreviousMediaItem()) {
                    it.seekToPreviousMediaItem()
                }
            }
        }
    }

    private fun setPlayAndPause() {
        val ivPlayPauseVideo: AppCompatImageView = findViewById(R.id.ivPlayPauseVideo)
        setPlayOrPauseIcon(ivPlayPauseVideo)
        ivPlayPauseVideo.setOnClickListener {
            exoPlayer?.let {
                setPlayOrPauseIcon(ivPlayPauseVideo)
                if (it.isPlaying)
                    it.pause()
                else
                    it.play()
            }
        }
    }

    private fun setPlayOrPauseIcon(ivPlayPauseVideo: AppCompatImageView) {
        exoPlayer?.let {
            ivPlayPauseVideo.setImageResource(
                if (it.isPlaying)
                    R.drawable.ic_playlist_pause_media
                else
                    R.drawable.ic_playlist_play_media
            )
        }
    }

    private fun setSeekForward() {
        val ivSeekForward15Seconds: AppCompatImageView = findViewById(R.id.ivSeekForward15Seconds)
        ivSeekForward15Seconds.setOnClickListener {
            exoPlayer?.let {
                it.seekTo(it.currentPosition + SEEK_VALUE_MS)
                updateTime(it.currentPosition)
                updateSeekBar()
            }
        }
    }

    private fun setSeekBack() {
        val ivSeekBack15Seconds: AppCompatImageView = findViewById(R.id.ivSeekBack15Seconds)
        ivSeekBack15Seconds.setOnClickListener {
            exoPlayer?.let {
                it.seekTo(it.currentPosition - SEEK_VALUE_MS)
                updateTime(it.currentPosition)
                updateSeekBar()
            }
        }
    }

    private fun setSeekBarListener() {
        videoSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                exoPlayer?.let {
                    updateTime(it.currentPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserTrackingTouch = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isUserTrackingTouch = false
                exoPlayer?.let {
                    val percentage = seekBar.progress.toFloat() / 100f
                    it.seekTo((duration * percentage).toLong())
                    updateTime(it.currentPosition)
                }
            }
        })
    }

    private fun setCurrentPlayerPosition() {
        exoPlayer?.let {
            if (it.isPlaying) {
                updateTime(it.currentPosition)
                styledPlayerView.postDelayed(
                    this::setCurrentPlayerPosition,
                    updatePositionDelayMs
                )
            }
            if (it.isPlaying && !isUserTrackingTouch)
                updateSeekBar()
        }
    }

    private fun updateSeekBar() {
        exoPlayer?.let {
            videoSeekBar.progress =
                ((it.currentPosition.toFloat() / duration.toFloat()) * 100).toInt()
        }
    }

    private fun updateTime(currentPosition: Long) {
        tvVideoTimeElapsed.text = getFormattedTime(currentPosition, false)
        tvVideoTimeRemaining.text = getFormattedTime(duration - currentPosition, true)
    }

    private fun getFormattedTime(time: Long, isNegative: Boolean): String {
        val totalSeconds = time / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        val outSeconds = if (seconds < 10) "0$seconds" else "$seconds"
        val outMinutes = if (minutes < 10) "0${minutes}" else "$minutes"
        val outHours = if (hours == 0L) "" else if (hours < 10) "0$hours:" else "$hours:"
        return "${(if (isNegative) "-" else "")}$outHours$outMinutes:$outSeconds"
    }

    companion object {
        private const val SEEK_VALUE_MS = 15000
    }
}