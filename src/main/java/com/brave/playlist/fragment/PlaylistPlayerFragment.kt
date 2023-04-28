package com.brave.playlist.fragment


import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.*
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.mediarouter.app.MediaRouteButton
import androidx.recyclerview.widget.RecyclerView
import com.brave.playlist.PlaylistVideoService
import com.brave.playlist.PlaylistViewModel
import com.brave.playlist.R
import com.brave.playlist.adapter.PlaylistItemAdapter
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.extension.dpToPx
import com.brave.playlist.listener.PlaylistItemClickListener
import com.brave.playlist.listener.PlaylistItemOptionsListener
import com.brave.playlist.local_database.PlaylistRepository
import com.brave.playlist.model.*
import com.brave.playlist.slidingpanel.BottomPanelLayout
import com.brave.playlist.util.ConnectionUtils
import com.brave.playlist.util.ConstantUtils
import com.brave.playlist.util.ConstantUtils.CAST_ACTION
import com.brave.playlist.util.ConstantUtils.DEFAULT_PLAYLIST
import com.brave.playlist.util.ConstantUtils.PLAYER_ITEMS
import com.brave.playlist.util.ConstantUtils.PLAYLIST_MODEL
import com.brave.playlist.util.ConstantUtils.PLAYLIST_NAME
import com.brave.playlist.util.ConstantUtils.SELECTED_PLAYLIST_ITEM_ID
import com.brave.playlist.util.ConstantUtils.SHOULD_SHOW_CONTROLS
import com.brave.playlist.util.MediaUtils
import com.brave.playlist.util.MenuUtils
import com.brave.playlist.util.PlaylistPreferenceUtils
import com.brave.playlist.util.PlaylistPreferenceUtils.rememberFilePlaybackPosition
import com.brave.playlist.util.PlaylistUtils
import com.brave.playlist.view.PlaylistToolbar
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException


class PlaylistPlayerFragment : Fragment(R.layout.fragment_playlist_player), Player.Listener,
    PlaylistItemClickListener, PlaylistItemOptionsListener, BottomPanelLayout.PanelSlideListener {
    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private lateinit var playlistViewModel: PlaylistViewModel
    private var isCastInProgress: Boolean = false
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
    private lateinit var emptyView: View
    private lateinit var fullscreenImg: AppCompatImageView
    private lateinit var backImg: AppCompatImageView
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
    private lateinit var layoutPlayer: LinearLayoutCompat
    private lateinit var ivVideoOptions: AppCompatImageView
    private lateinit var tvPlaylistName: AppCompatTextView
    private lateinit var progressBar: ProgressBar

    private lateinit var videoPlayerLoading: ProgressBar

    private var playlistModel: PlaylistModel? = null
    private var selectedPlaylistItemId: String = ""
    private var playlistItems = mutableListOf<PlaylistItemModel>()

    private lateinit var rvPlaylist: RecyclerView
    private lateinit var playlistItemAdapter: PlaylistItemAdapter
    private lateinit var mainLayout: BottomPanelLayout

    private var playlistVideoService: PlaylistVideoService? = null

    private val playlistRepository: PlaylistRepository by lazy {
        PlaylistRepository(requireContext())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            updateLandscapeView()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            updatePortraitView()
        }
    }

    private fun updatePortraitView() {
        val layoutParams: FrameLayout.LayoutParams =
            styledPlayerView.layoutParams as FrameLayout.LayoutParams
        layoutParams.height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            if (context?.resources?.getBoolean(R.bool.isTablet) == true) 650f else 300f,
            resources.displayMetrics
        ).toInt()
        styledPlayerView.layoutParams = layoutParams

        val hoverLayoutParams: FrameLayout.LayoutParams =
            hoverControlsLayout.layoutParams as FrameLayout.LayoutParams
        layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
        hoverControlsLayout.layoutParams = hoverLayoutParams

        mainLayout.mSlideState = BottomPanelLayout.PanelState.COLLAPSED
        mainLayout.panelHeight = if (context?.resources?.getBoolean(R.bool.isTablet) == true) 150.dpToPx.toInt() else 70.dpToPx.toInt()
        if (!isCastInProgress) {
            styledPlayerView.useController = false
            layoutVideoControls.visibility = View.VISIBLE
        }
        playlistToolbar.visibility = View.VISIBLE
        backImg.visibility = View.GONE
        emptyView.visibility = View.GONE
        fullscreenImg.setImageResource(R.drawable.ic_fullscreen)
    }

    private fun updateLandscapeView() {
        val layoutParams: FrameLayout.LayoutParams =
            styledPlayerView.layoutParams as FrameLayout.LayoutParams
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
        styledPlayerView.layoutParams = layoutParams

        val hoverLayoutParams: FrameLayout.LayoutParams =
            hoverControlsLayout.layoutParams as FrameLayout.LayoutParams
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        hoverControlsLayout.layoutParams = hoverLayoutParams

        mainLayout.mSlideState = BottomPanelLayout.PanelState.HIDDEN
        styledPlayerView.useController = true
        styledPlayerView.controllerHideOnTouch = false
        styledPlayerView.showController()
        layoutVideoControls.visibility = View.GONE
        playlistToolbar.visibility = View.GONE
        backImg.visibility = View.VISIBLE
        emptyView.visibility = View.VISIBLE
        fullscreenImg.setImageResource(R.drawable.ic_close_fullscreen)
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

        Log.e("NTP", "Player Oncreate")

        arguments?.let {
            playlistModel = it.getParcelable(PLAYLIST_MODEL)
            selectedPlaylistItemId = it.getString(SELECTED_PLAYLIST_ITEM_ID).toString()
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(CAST_ACTION)
        activity?.registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistViewModel = activity?.let {
            ViewModelProvider(
                it, ViewModelProvider.NewInstanceFactory()
            )
        }!![PlaylistViewModel::class.java]

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        playlistModel?.id?.let { playlistViewModel.fetchPlaylistData(it) }

        playlistToolbar = view.findViewById(R.id.playlistToolbar)
        tvVideoTitle = view.findViewById(R.id.tvVideoTitle)
        tvVideoSource = view.findViewById(R.id.tvVideoSource)
        tvPlaylistName = view.findViewById(R.id.tvPlaylistName)

        tvVideoSource.text =
            if (playlistModel?.id == DEFAULT_PLAYLIST) getString(R.string.playlist_play_later) else playlistModel?.name
        tvPlaylistName.text =
            if (playlistModel?.id == DEFAULT_PLAYLIST) getString(R.string.playlist_play_later) else playlistModel?.name

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
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        backImg = view.findViewById(R.id.back_img)
        backImg.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        emptyView = view.findViewById(R.id.empty_view)

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
        ivVideoOptions = view.findViewById(R.id.ivVideoOptions)
        ivVideoOptions.setOnClickListener {
            playlistModel?.let { model ->
                val currentPlaylistItem =
                    playlistItems[playlistVideoService?.getCurrentPlayer()?.currentPeriodIndex!!]
                MenuUtils.showPlaylistItemMenu(
                    view.context, parentFragmentManager,
                    currentPlaylistItem, playlistId = model.id, playlistItemOptionsListener = this,
                    shouldHideDeleteOption = true
                )
            }
        }
        videoPlayerLoading = view.findViewById(R.id.videoPlayerLoading)

        mainLayout = view.findViewById(R.id.sliding_layout)
        mainLayout.addPanelSlideListener(this)
        layoutBottom = view.findViewById(R.id.bottom_layout)
        layoutPlayer = view.findViewById(R.id.player_layout)
        layoutVideoControls = view.findViewById(R.id.layoutVideoControls)

        rvPlaylist = view.findViewById(R.id.rvPlaylists)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        rvPlaylist.visibility = View.GONE

        val mediaRouteButton: MediaRouteButton =
            view.findViewById(R.id.media_route_button)
        CastButtonFactory.setUpMediaRouteButton(requireContext(), mediaRouteButton)

        playlistViewModel.playlistData.observe(viewLifecycleOwner) { playlistData ->
            playlistItems = mutableListOf()
//            val playlistJsonObject = JSONObject(playlistData)
//            val jsonArray: JSONArray = playlistJsonObject.getJSONArray("items")
            playlistData.items.forEach {
                if (it.id != selectedPlaylistItemId) {
                    playlistItems.add(it)
                } else {
                    playlistItems.add(0, it)
                }
            }
//            for (i in 0 until jsonArray.length()) {
//                val jsonObject = jsonArray.getJSONObject(i)
//                val playlistItemModel = PlaylistItemModel(
//                    jsonObject.getString("id"),
//                    playlistJsonObject.getString("id"),
//                    jsonObject.getString("name"),
//                    jsonObject.getString("page_source"),
//                    jsonObject.getString("media_path"),
//                    jsonObject.getString("media_src"),
//                    jsonObject.getString("thumbnail_path"),
//                    jsonObject.getString("author"),
//                    jsonObject.getString("duration"),
//                    jsonObject.getInt("last_played_position"),
//                    jsonObject.getBoolean("cached")
//                )
//
//                if (playlistItemModel.id != selectedPlaylistItemId) {
//                    playlistItems.add(playlistItemModel)
//                } else {
//                    playlistItems.add(0, playlistItemModel)
//                }
//            }

            playlistModel = PlaylistModel(
                playlistData.id,
                playlistData.name,
                playlistItems
            )

            val intent = Intent(requireContext(), PlaylistVideoService::class.java)
                .apply {
                    putExtra(
                        PLAYLIST_NAME,
                        if (playlistModel?.id == DEFAULT_PLAYLIST) getString(R.string.playlist_play_later) else playlistModel?.name
                    )
                    putParcelableArrayListExtra(PLAYER_ITEMS, ArrayList(playlistItems))
                }

            // Playlist Video service
            Log.e("NTP", "Player startService")
            activity?.startService(intent)
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)

            scope.launch {
                playlistItems.forEach {
                    try {
                        if (it.isCached) {
                            val fileSize =
                                MediaUtils.getFileSizeFromUri(view.context, Uri.parse(it.mediaPath))
                            it.fileSize = fileSize
                        }
                    } catch (ex: IOException) {
                        Log.e(ConstantUtils.TAG, ex.message.toString())
                    }
                }

                activity?.runOnUiThread {
                    // Bottom Layout set up
                    playlistItemAdapter =
                        PlaylistItemAdapter(playlistItems, this@PlaylistPlayerFragment)
                    playlistItemAdapter.setBottomLayout()
                    rvPlaylist.adapter = playlistItemAdapter

                    progressBar.visibility = View.GONE
                    rvPlaylist.visibility = View.VISIBLE
                    playlistViewModel.downloadProgress.observe(viewLifecycleOwner) {
                        playlistItemAdapter.updatePlaylistItemDownloadProgress(it)
                    }

                    playlistViewModel.playlistEventUpdate.observe(viewLifecycleOwner) {
                        playlistItemAdapter.updatePlaylistItem(it)
                    }
                }
            }
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            updateLandscapeView()
        } else {
            updatePortraitView()
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    this.remove()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
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
        activity?.unregisterReceiver(broadcastReceiver)
        mainLayout.removePanelSlideListener(this)
        super.onDestroyView()
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        playlistVideoService?.getCurrentPlayer()?.let {
            updateSeekBar()
            duration = it.duration
            updateTime(it.currentPosition)
            tvVideoTitle.text = playlistItems[it.currentPeriodIndex].name
            ivNextVideo.isEnabled = it.hasNextMediaItem()
            ivNextVideo.alpha = if (it.hasNextMediaItem()) 1.0f else 0.4f
            ivPrevVideo.isEnabled = it.hasPreviousMediaItem()
            ivPrevVideo.alpha = if (it.hasPreviousMediaItem()) 1.0f else 0.4f
        }
        videoPlayerLoading.visibility = View.GONE
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
            videoPlayerLoading.visibility = View.GONE
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        Log.e("PlaylistPlayerFragment", "onIsPlayingChanged")
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
        playlistVideoService?.getCurrentPlayer()?.let { currentPlayer ->
            currentPlayer.addListener(this)
            currentPlayer.shuffleModeEnabled = isShuffleOn
            val currentPlaylistItemId = playlistItems[currentPlayer.currentMediaItemIndex].id
            scope.launch {
                playlistRepository.getPlaylistItemById(currentPlaylistItemId)?.lastPlayedPosition?.let { lastPosition ->
                    val handler = Handler(currentPlayer.applicationLooper)
                    val runnableCode = Runnable {
                        currentPlayer.seekTo(
                            currentMediaIndex,
                            if (PlaylistPreferenceUtils.defaultPrefs(requireContext()).rememberFilePlaybackPosition) lastPosition else 0
                        )
                    }
                    handler.post(runnableCode)
                }
            }
            currentPlayer.repeatMode = repeatMode
            currentPlayer.setPlaybackSpeed(playbackSpeed)
        }
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
                    if (!playlistItems[it.nextMediaItemIndex].isCached && !ConnectionUtils.isDeviceOnline(
                            requireContext()
                        )
                    ) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.playlist_offline_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        it.seekToNextMediaItem()
                        disableNextPreviousControls()
                    }
                }
            }
        }
    }

    private fun setPrevMedia() {
        ivPrevVideo.setOnClickListener {
            playlistVideoService?.getCurrentPlayer()?.let {
                if (it.hasPreviousMediaItem()) {
                    if (!playlistItems[it.previousMediaItemIndex].isCached && !ConnectionUtils.isDeviceOnline(
                            requireContext()
                        )
                    ) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.playlist_offline_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        it.seekToPreviousMediaItem()
                        disableNextPreviousControls()
                    }
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
        if (!playlistItems[count].isCached && !ConnectionUtils.isDeviceOnline(requireContext())) {
            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_offline_message),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        playlistVideoService?.setCurrentItem(count)
        mainLayout.smoothToBottom()
    }

    override fun onPlaylistItemMenuClick(view: View, playlistItemModel: PlaylistItemModel) {
        MenuUtils.showPlaylistItemMenu(
            view.context,
            parentFragmentManager,
            playlistItemModel = playlistItemModel,
            playlistId = playlistItemModel.playlistId,
            playlistItemOptionsListener = this,
            shouldHideDeleteOption = true
        )
    }

    override fun onOptionClicked(playlistItemOptionModel: PlaylistItemOptionModel) {
        if (playlistItemOptionModel.optionType == PlaylistOptions.SHARE_PLAYLIST_ITEM) {
            playlistItemOptionModel.playlistItemModel?.pageSource?.let {
                PlaylistUtils.showSharingDialog(
                    requireContext(),
                    it
                )
            }
        } else {
            if (playlistItemOptionModel.optionType == PlaylistOptions.DELETE_PLAYLIST_ITEM) {
                playlistVideoService?.getCurrentPlayer()?.stop()
                activity?.onBackPressedDispatcher?.onBackPressed()
            } else if (playlistItemOptionModel.optionType == PlaylistOptions.MOVE_PLAYLIST_ITEM || playlistItemOptionModel.optionType == PlaylistOptions.COPY_PLAYLIST_ITEM) {
                val moveOrCopyItems = ArrayList<PlaylistItemModel>()
                playlistItemOptionModel.playlistItemModel?.let { moveOrCopyItems.add(it) }
                PlaylistUtils.moveOrCopyModel =
                    MoveOrCopyModel(playlistItemOptionModel.optionType, "", moveOrCopyItems)
            }
            playlistViewModel.setPlaylistItemOption(playlistItemOptionModel)
        }
    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action.equals(CAST_ACTION)) {
                val shouldShowControls = intent.getBooleanExtra(SHOULD_SHOW_CONTROLS, true)
                Log.e("NTP", "should_show_controls : ")
                isCastInProgress = !shouldShowControls
                layoutVideoControls.visibility = if (shouldShowControls) View.VISIBLE else View.GONE
            }
        }
    }

    companion object {
        private const val SEEK_VALUE_MS = 15000

        @JvmStatic
        fun newInstance(selectedPlaylistItemId: String, playlistModel: PlaylistModel) =
            PlaylistPlayerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PLAYLIST_MODEL, playlistModel)
                    putString(SELECTED_PLAYLIST_ITEM_ID, selectedPlaylistItemId)
                }
            }
    }

    private fun enableControls(enable: Boolean, vg: ViewGroup) {
        for (i in 0 until vg.childCount) {
            val child = vg.getChildAt(i)
            child.isEnabled = enable
            if (child is ViewGroup) {
                enableControls(enable, child)
            }
        }
    }

    override fun onPanelSlide(panel: View?, slideOffset: Float) {
        // Do nothing here
    }

    override fun onPanelStateChanged(
        panel: View?,
        previousState: BottomPanelLayout.PanelState?,
        newState: BottomPanelLayout.PanelState?
    ) {
        val shouldEnableControls = newState != BottomPanelLayout.PanelState.EXPANDED
        enableControls(shouldEnableControls , layoutPlayer)
    }
}
