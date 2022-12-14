package com.brave.playlist.fragment


import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.*
import android.util.Rational
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.registerReceiver
import androidx.fragment.app.Fragment
import androidx.mediarouter.app.MediaRouteButton
import androidx.recyclerview.widget.RecyclerView
import com.brave.playlist.PlaylistVideoService
import com.brave.playlist.R
import com.brave.playlist.adapter.PlaylistItemAdapter
import com.brave.playlist.listener.OnPlaylistItemClickListener
import com.brave.playlist.model.MediaModel
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.slidingpanel.BottomPanelLayout
import com.brave.playlist.util.ConstantUtils.PLAYER_ITEMS
import com.brave.playlist.util.ConstantUtils.PLAYLIST_MODEL
import com.brave.playlist.util.ConstantUtils.PLAYLIST_NAME
import com.brave.playlist.util.ConstantUtils.SELECTED_PLAYLIST_ITEM
import com.brave.playlist.view.PlaylistToolbar
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.material.card.MaterialCardView


class PlaylistPlayerFragment : Fragment(R.layout.fragment_playlist_player), Player.Listener,
    OnPlaylistItemClickListener {
    private var isPIPModeEnabled: Boolean = true
    private var isCastInProgress : Boolean = false
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
    private lateinit var hoverControlsLayout: LinearLayoutCompat
    private lateinit var fullscreenImg: AppCompatImageView
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
    private lateinit var layoutVideoControls: ConstraintLayout
    private lateinit var layoutBottom: MaterialCardView
    private lateinit var layoutPlayer : FrameLayout

    private var playlistModel: PlaylistModel? = null
    private var selectedPlaylistItem: MediaModel? = null
    private var playlistItems = mutableListOf<MediaModel>()

    private lateinit var rvPlaylist: RecyclerView
    private lateinit var playlistItemAdapter: PlaylistItemAdapter
    private lateinit var mainLayout: BottomPanelLayout

    private var playlistVideoService: PlaylistVideoService? = null

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Checks the orientation of the screen
        val density = requireContext().resources.displayMetrics.density
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(requireContext(), "landscape", Toast.LENGTH_SHORT).show()
            mainLayout.panelHeight = 0
            styledPlayerView.useController = true
            styledPlayerView.controllerHideOnTouch = false
            styledPlayerView.showController()
            layoutVideoControls.visibility = View.GONE
            playlistToolbar.visibility = View.GONE
            fullscreenImg.setImageResource(R.drawable.ic_close_fullscreen)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(requireContext(), "portrait", Toast.LENGTH_SHORT).show()
            mainLayout.panelHeight = (BottomPanelLayout.DEFAULT_PANEL_HEIGHT * density + 0.5f).toInt()
            if (!isCastInProgress) {
                styledPlayerView.useController = false
                layoutVideoControls.visibility = View.VISIBLE
            }
            playlistToolbar.visibility = View.VISIBLE
            fullscreenImg.setImageResource(R.drawable.ic_fullscreen)
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlaylistVideoService.PlaylistVideoServiceBinder) {
                playlistVideoService = service.getServiceInstance()

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
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistModel = it.getParcelable(PLAYLIST_MODEL)
            selectedPlaylistItem = it.getParcelable(SELECTED_PLAYLIST_ITEM)
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction("action")
        activity?.registerReceiver(broadcastReceiver, intentFilter)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistToolbar = view.findViewById(R.id.playlistToolbar)
        tvVideoTitle = view.findViewById(R.id.tvVideoTitle)
        tvVideoSource = view.findViewById(R.id.tvVideoSource)
        styledPlayerView = view.findViewById(R.id.styledPlayerView)
        styledPlayerView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    showHoveringControls()
                }
            }

            v?.onTouchEvent(event) ?: true
        }
        hoverControlsLayout = view.findViewById(R.id.hover_controls_layout)
        fullscreenImg = view.findViewById(R.id.fullscreen_img)
        fullscreenImg.setOnClickListener {
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
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

        mainLayout = view.findViewById(R.id.sliding_layout)
        layoutBottom = view.findViewById(R.id.bottom_layout)
        layoutPlayer = view.findViewById(R.id.player_layout)

        val mediaRouteButton: MediaRouteButton =
            view.findViewById(R.id.media_route_button)
        CastButtonFactory.setUpMediaRouteButton(requireContext(), mediaRouteButton)

        selectedPlaylistItem?.let { playlistItems.add(it) }
        layoutVideoControls = view.findViewById(R.id.layoutVideoControls)

        playlistModel?.items?.forEach {
            if (it.id != selectedPlaylistItem?.id) {
                playlistItems.add(it)
            }
        }

        val intent = Intent(requireContext(), PlaylistVideoService::class.java)
            .apply {
                putExtra(
                    PLAYLIST_NAME,
                    if (playlistModel?.id == "default") getString(R.string.watch_later) else playlistModel?.name
                )
                putParcelableArrayListExtra(PLAYER_ITEMS, ArrayList(playlistItems))
            }

        // Playlist Video service
        activity?.startService(intent)
        activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        // Bottom Layout set up
        rvPlaylist = view.findViewById(R.id.rvPlaylists)
        playlistItemAdapter = PlaylistItemAdapter(playlistItems, this)
        playlistItemAdapter.setBottomLayout()
        rvPlaylist.adapter = playlistItemAdapter

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
//                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
//                        && requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
//                        && isPIPModeEnabled) {
//                        enterPIPMode()
//                    } else {
//                        requireActivity().onBackPressed()
//                    }
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        this.remove()
                        requireActivity().onBackPressed()
                    }
                }
            }
            )
    }

    private fun showHoveringControls() {
        hoverControlsLayout.visibility = View.VISIBLE
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                hoverControlsLayout.visibility = View.GONE
            }, 5000)
        }
    }

    override fun onDestroyView() {
        releasePlayer()
        activity?.unregisterReceiver(broadcastReceiver);
        super.onDestroyView()
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        playlistVideoService?.getCurrentPlayer()?.let {
            it.playWhenReady = true
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == ExoPlayer.STATE_READY) {
            playlistVideoService?.getCurrentPlayer()?.let {
                duration = it.duration
                updateTime(it.currentPosition)
                tvVideoTitle.text = playlistItems[it.currentPeriodIndex].name
                ivNextVideo.isEnabled = it.hasNextMediaItem()
                ivNextVideo.alpha = if (it.hasNextMediaItem()) 1.0f else 0.4f
                ivPrevVideo.isEnabled = it.hasPreviousMediaItem()
                ivPrevVideo.alpha = if (it.hasPreviousMediaItem()) 1.0f else 0.4f
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
            playlistVideoService?.getCurrentPlayer()?.setPlaybackSpeed(playbackSpeed)
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
            playlistVideoService?.getCurrentPlayer()?.repeatMode = repeatMode
        }
    }

    private fun setPlaylistShuffle() {
        ivPlaylistShuffle.setOnClickListener {
            isShuffleOn = !isShuffleOn
            playlistVideoService?.getCurrentPlayer()?.shuffleModeEnabled = isShuffleOn
            ivPlaylistShuffle.setImageResource(
                if (isShuffleOn)
                    R.drawable.ic_playlist_shuffle_on
                else
                    R.drawable.ic_playlist_shuffle_off
            )
        }
    }

    private fun initializePlayer() {
        playlistVideoService?.setPlayerView(styledPlayerView)
        styledPlayerView.player = playlistVideoService?.getCurrentPlayer()
        playlistVideoService?.getCurrentPlayer()?.addListener(this)
        playlistVideoService?.getCurrentPlayer()?.shuffleModeEnabled = isShuffleOn
        playlistVideoService?.getCurrentPlayer()?.seekTo(currentMediaIndex, playbackPosition)
        playlistVideoService?.getCurrentPlayer()?.repeatMode = repeatMode
        playlistVideoService?.getCurrentPlayer()?.setPlaybackSpeed(playbackSpeed)
        playlistVideoService?.getCurrentPlayer()?.playWhenReady = playWhenReady
//        playlistItems.forEach { mediaModel ->
//            playlistVideoService?.addItem(
//                MediaItem.Builder()
//                    .setUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
////                    .setUri(mediaModel.mediaPath)
//                    .setMimeType(MimeTypes.VIDEO_MP4).build()
//            )
////            exoPlayer?.addMediaItem(MediaItem.fromUri(mediaModel.mediaPath))
//        }
    }

    private fun releasePlayer() {
        playlistVideoService?.getCurrentPlayer()?.let {
            it.removeListener(this)
            playbackPosition = it.currentPosition
            currentMediaIndex = it.currentMediaItemIndex
            playWhenReady = it.playWhenReady
            isShuffleOn = it.shuffleModeEnabled
        }
        activity?.unbindService(connection)
    }

    private fun disableNextPreviousControls() {
        ivNextVideo.isEnabled = false
        ivNextVideo.alpha = 0.4f
        ivPrevVideo.isEnabled = false
        ivPrevVideo.alpha = 0.4f
    }


    private fun setNextMedia() {
        ivNextVideo.setOnClickListener {
            playlistVideoService?.getCurrentPlayer()?.let {
                if (it.hasNextMediaItem()) {
                    it.seekToNextMediaItem()
                    disableNextPreviousControls()
                }
            }
        }
    }

    private fun setPrevMedia() {
        ivPrevVideo.setOnClickListener {
            playlistVideoService?.getCurrentPlayer()?.let {
                if (it.hasPreviousMediaItem()) {
                    it.seekToPreviousMediaItem()
                    disableNextPreviousControls()
                }
            }
        }
    }

    private fun setPlayAndPause() {
        setPlayOrPauseIcon(ivPlayPauseVideo)
        ivPlayPauseVideo.setOnClickListener {
            playlistVideoService?.getCurrentPlayer()?.let {
                setPlayOrPauseIcon(ivPlayPauseVideo)
                if (it.isPlaying)
                    it.pause()
                else
                    it.play()
            }
        }
    }

    private fun setPlayOrPauseIcon(ivPlayPauseVideo: AppCompatImageView) {
        playlistVideoService?.getCurrentPlayer()?.let {
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
            playlistVideoService?.getCurrentPlayer()?.let {
                it.seekTo(it.currentPosition + SEEK_VALUE_MS)
                updateTime(it.currentPosition)
                updateSeekBar()
            }
        }
    }

    private fun setSeekBack() {
        ivSeekBack15Seconds.setOnClickListener {
            playlistVideoService?.getCurrentPlayer()?.let {
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
                playlistVideoService?.getCurrentPlayer()?.let {
                    updateTime(it.currentPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserTrackingTouch = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isUserTrackingTouch = false
                playlistVideoService?.getCurrentPlayer()?.let {
                    val percentage = seekBar.progress.toFloat() / 100f
                    it.seekTo((duration * percentage).toLong())
                    updateTime(it.currentPosition)
                }
            }
        })
    }

    private fun setCurrentPlayerPosition() {
        playlistVideoService?.getCurrentPlayer()?.let {
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
        playlistVideoService?.getCurrentPlayer()?.let {
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

    override fun onPlaylistItemClick(count: Int) {
//        exoPlayer?.seekTo(count,0)
//        exoPlayer?.playWhenReady = true
        playlistVideoService?.setCurrentItem(count)
        mainLayout.smoothToBottom()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        if (isInPictureInPictureMode) {
            styledPlayerView.useController = true
            layoutVideoControls.visibility = View.GONE
            playlistToolbar.visibility = View.GONE
//            layoutBottom.visibility = View.GONE
//            fullscreenImg.setImageResource(R.drawable.ic_close_fullscreen)
        } else {
            styledPlayerView.useController = false
            layoutVideoControls.visibility = View.VISIBLE
            playlistToolbar.visibility = View.VISIBLE
//            layoutBottom.visibility = View.VISIBLE
//            fullscreenImg.setImageResource(R.drawable.ic_fullscreen)
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
    }

    @Suppress("DEPRECATION")
    fun enterPIPMode(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder().setAspectRatio(Rational(styledPlayerView.width, styledPlayerView.height))
                activity?.enterPictureInPictureMode(params.build())
            } else {
                activity?.enterPictureInPictureMode()
            }
            /* We need to check this because the system permission check is publically hidden for integers for non-manufacturer-built apps
               https://github.com/aosp-mirror/platform_frameworks_base/blob/studio-3.1.2/core/java/android/app/AppOpsManager.java#L1640
               ********* If we didn't have that problem *********
                val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                if(appOpsManager.checkOpNoThrow(AppOpManager.OP_PICTURE_IN_PICTURE, packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).uid, packageName) == AppOpsManager.MODE_ALLOWED)
                30MS window in even a restricted memory device (756mb+) is more than enough time to check, but also not have the system complain about holding an action hostage.
             */
//            Handler().postDelayed({checkPIPPermission()}, 30)
        }
    }

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action.equals("action")) {
                val shouldShowControls = intent.getBooleanExtra("should_show_controls", true)
                isCastInProgress = !shouldShowControls
                layoutVideoControls.visibility = if (shouldShowControls) View.VISIBLE else View.GONE
            } else {

            }

//            val datapassed = intent.getIntExtra("DATAPASSED", 0
//            })
//            val s = intent.action.toString()
//            val s1 = intent.getStringExtra("DATAPASSED")
//            Toast.makeText(
//                context,
//                """
//                Triggered by Service!
//                Data passed: ${s1.toString()}
//                """.trimIndent(),
//                Toast.LENGTH_LONG
//            ).show()
//            layoutVideoControls.visibility = if (layoutVideoControls.isShown) View.GONE else View.VISIBLE
        }
    }

    companion object {
        private const val SEEK_VALUE_MS = 15000

        @JvmStatic
        fun newInstance(playlistModel: PlaylistModel, selectedPlaylistItem: MediaModel) =
            PlaylistPlayerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PLAYLIST_MODEL, playlistModel)
                    putParcelable(SELECTED_PLAYLIST_ITEM, selectedPlaylistItem)
                }
            }
    }
}