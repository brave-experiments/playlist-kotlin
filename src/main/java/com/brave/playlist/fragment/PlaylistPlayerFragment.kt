package com.brave.playlist.fragment

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.brave.playlist.R
import com.brave.playlist.model.MediaModel
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.util.ConstantUtils.PLAYLIST
import com.brave.playlist.util.ConstantUtils.SELECTED_PLAYLIST_ITEM
import com.brave.playlist.view.PlaylistToolbar
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView

class PlaylistPlayerFragment : Fragment(R.layout.fragment_playlist_player), Player.Listener {
    //    private lateinit var viewModel: PlaylistViewModel
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

    private lateinit var playlistToolbar: PlaylistToolbar
    private lateinit var styledPlayerView: StyledPlayerView
    private lateinit var videoSeekBar: SeekBar
    private lateinit var tvVideoTitle: AppCompatTextView
    private lateinit var tvVideoSource: AppCompatTextView
    private lateinit var tvVideoTimeElapsed: AppCompatTextView
    private lateinit var tvVideoTimeRemaining: AppCompatTextView
    private lateinit var ivPlaylistMediaSpeed: AppCompatImageView
    private lateinit var ivPlaylistRepeat: AppCompatImageView
    private lateinit var ivPlaylistShuffle: AppCompatImageView
    private lateinit var ivNextVideo: AppCompatImageView
    private lateinit var ivPrevVideo: AppCompatImageView
    private lateinit var ivPlayPauseVideo: AppCompatImageView
    private lateinit var ivSeekForward15Seconds: AppCompatImageView
    private lateinit var ivSeekBack15Seconds: AppCompatImageView

    private var playlistModel: PlaylistModel? = null
    private var selectedPlaylistItem: MediaModel? = null
    private var playlistItems: ArrayList<MediaModel>? = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistModel = it.getParcelable(PLAYLIST)
            selectedPlaylistItem = it.getParcelable(SELECTED_PLAYLIST_ITEM)
        }

        selectedPlaylistItem?.let { playlistItems?.add(it) }
        playlistModel?.items?.forEach {
            if (it.id != selectedPlaylistItem?.id) {
                playlistItems?.add(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel = activity?.let {
//            ViewModelProvider(
//                it, ViewModelProvider.NewInstanceFactory()
//            )
//        }!![PlaylistViewModel::class.java]
        playlistToolbar = view.findViewById(R.id.playlistToolbar)
        tvVideoTitle = view.findViewById(R.id.tvVideoTitle)
        tvVideoSource = view.findViewById(R.id.tvVideoSource)
        styledPlayerView = view.findViewById(R.id.styledPlayerView)
        videoSeekBar = view.findViewById(R.id.videoSeekBar)
        tvVideoTimeElapsed = view.findViewById(R.id.tvVideoTimeElapsed)
        tvVideoTimeRemaining = view.findViewById(R.id.tvVideoTimeRemaining)
        ivPlaylistMediaSpeed = view.findViewById(R.id.ivPlaylistMediaSpeed)
        ivPlaylistRepeat = view.findViewById(R.id.ivPlaylistRepeat)
        ivPlaylistShuffle = view.findViewById(R.id.ivPlaylistShuffle)
        ivNextVideo = view.findViewById(R.id.ivNextVideo)
        ivPrevVideo = view.findViewById(R.id.ivPrevVideo)
        ivPlayPauseVideo = view.findViewById(R.id.ivPlayPauseVideo)
        ivSeekForward15Seconds = view.findViewById(R.id.ivSeekForward15Seconds)
        ivSeekBack15Seconds = view.findViewById(R.id.ivSeekBack15Seconds)

        setToolbar()
        setNextMedia()
        setPrevMedia()
        setSeekForward()
        setSeekBack()
        setSeekBarListener()
        setPlaylistShuffle()
        setPlaylistRepeatMode()
        setPlaybackSpeed()

        initializePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        exoPlayer?.let {
            it.playWhenReady = true
            tvVideoTitle.text = playlistItems?.get(it.currentPeriodIndex)?.name
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == ExoPlayer.STATE_READY) {
            exoPlayer?.let {
                duration = it.duration
                updateTime(it.currentPosition)
                tvVideoTitle.text = playlistItems?.get(it.currentPeriodIndex)?.name
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying && !isUserTrackingTouch)
            styledPlayerView.postDelayed(this::setCurrentPlayerPosition, updatePositionDelayMs)
        setPlayAndPause()
    }

    private fun setToolbar() {
        playlistToolbar.setOptionsButtonClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun setPlaybackSpeed() {
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

    private fun initializePlayer() {
        val trackSelector = view?.let {
            DefaultTrackSelector(it.context).apply {
                setParameters(buildUponParameters().setMaxVideoSizeSd())
            }
        }

        exoPlayer = trackSelector?.let { tempTrackSelector ->
            ExoPlayer.Builder(requireContext())
                .setTrackSelector(tempTrackSelector)
                .build()
                .also { tempExoPlayer ->
                    styledPlayerView.player = tempExoPlayer
                    tempExoPlayer.addListener(this)
                    tempExoPlayer.shuffleModeEnabled = isShuffleOn
    //                        mediaItem?.let { it1 -> it.addMediaItem(it1) }
                    tempExoPlayer.seekTo(currentMediaIndex, playbackPosition)
                    tempExoPlayer.repeatMode = repeatMode
                    tempExoPlayer.setPlaybackSpeed(playbackSpeed)
                }
        }
        exoPlayer?.playWhenReady = playWhenReady
        playlistItems?.forEach {mediaModel ->
//            exoPlayer?.addMediaItem(MediaItem.fromUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
            exoPlayer?.addMediaItem(MediaItem.fromUri(mediaModel.mediaPath))
        }
        exoPlayer?.prepare()
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
        ivNextVideo.setOnClickListener {
            exoPlayer?.let {
                if (it.hasNextMediaItem()) {
                    it.seekToNextMediaItem()
                }
            }
        }
    }

    private fun setPrevMedia() {
        ivPrevVideo.setOnClickListener {
            exoPlayer?.let {
                if (it.hasPreviousMediaItem()) {
                    it.seekToPreviousMediaItem()
                }
            }
        }
    }

    private fun setPlayAndPause() {
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
                if (!it.isPlaying)
                    R.drawable.ic_playlist_pause_media
                else
                    R.drawable.ic_playlist_play_media
            )
        }
    }

    private fun setSeekForward() {
        ivSeekForward15Seconds.setOnClickListener {
            exoPlayer?.let {
                it.seekTo(it.currentPosition + SEEK_VALUE_MS)
                updateTime(it.currentPosition)
                updateSeekBar()
            }
        }
    }

    private fun setSeekBack() {
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
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
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

        @JvmStatic
        fun newInstance(playlistModel: PlaylistModel, selectedPlaylistItem: MediaModel) =
            PlaylistPlayerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PLAYLIST, playlistModel)
                    putParcelable(SELECTED_PLAYLIST_ITEM, selectedPlaylistItem)
                }
            }
    }
}