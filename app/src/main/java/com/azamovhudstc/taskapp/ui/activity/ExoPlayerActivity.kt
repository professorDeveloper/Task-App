package com.azamovhudstc.taskapp.ui.activity

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.*
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.azamovhudstc.taskapp.R
import com.azamovhudstc.taskapp.cache.ExoPlayerCache
import com.azamovhudstc.taskapp.data.remote.response.Lesson
import com.azamovhudstc.taskapp.databinding.ActivityExoPlayerBinding
import com.azamovhudstc.taskapp.utils.hideSystemBars
import com.azamovhudstc.taskapp.utils.loadData
import com.bumptech.glide.Glide
import com.github.vkay94.dtpv.DoubleTapPlayerView
import com.github.vkay94.dtpv.youtube.YouTubeOverlay
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import kotlinx.coroutines.*
import java.lang.Runnable

class ExoPlayerActivity : AppCompatActivity() {
    private var isInitialized: Boolean = false
    private lateinit var animeTitle: String
    private lateinit var animeKind: String
    private lateinit var videoLinks: String
    private lateinit var videos: List<String>
    private var videoTotal: Int = 0
    private var videoNumber: Int = 0

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var binding: ActivityExoPlayerBinding



    // Top buttons
    private lateinit var exoAnimeTitle: TextView
    private lateinit var backButton: ImageButton
    private lateinit var exoQuality: AutoCompleteTextView
    private lateinit var exoSpeed: ImageButton
    private lateinit var exoLock: ImageButton

    // Middle buttons
    private lateinit var exoPrevEp: ImageButton
    private lateinit var cvPlay: CardView
    private lateinit var exoPlay: ImageButton
    private lateinit var exoProgressBar: ProgressBar
    private lateinit var exoNextEp: ImageButton

    // Bottom buttons
    private lateinit var exoPip: ImageButton
    private lateinit var exoSkip: ImageButton
    private lateinit var exoScreen: ImageButton

    private lateinit var exoTopControllers: LinearLayout
    private lateinit var exoMiddleControllers: LinearLayout
    private lateinit var exoBottomControllers: LinearLayout
    private lateinit var trackSelector: DefaultTrackSelector

    private lateinit var dataSourceFactory: DataSource.Factory

    private var isBuffering = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseExtra()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        // show video inside notch if API >= 28 and orientation is landscape
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        ) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        binding = ActivityExoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // hiding 3 bottom buttons by default and showing when user swipes
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onBackPressedDispatcher.addCallback(this) {
            finishAndRemoveTask()
        }
        createPlayer()
    }

    public override fun onResume() {
        super.onResume()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        binding.playerView.useController = true
        exoPlayer.prepare()
    }

    public override fun onPause() {
        super.onPause()

        if (pipStatus) pauseVideo()
    }

    override fun onDestroy() {
        super.onDestroy()

        pipStatus = false
        exoPlayer.release()
    }


    private fun createPlayer() {
        setupTextViewValues()
        setupMiddleControllers()
//        setupQuality()
        setupOnClickListeners()
        val mediaDataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)

        val simpleCache = ExoPlayerCache.getInstance(this)
        dataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache!!)
            .setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory().setUserAgent("user-agent")
            )
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(videoLinks))
        val mediaSourceFactory = DefaultMediaSourceFactory(mediaDataSourceFactory)

        trackSelector = DefaultTrackSelector(this)

        exoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        exoPlayer.setMediaItem(MediaItem.fromUri(videoLinks))

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                        exoProgressBar.isVisible = true
                        cvPlay.isVisible = false
                    }
                    Player.STATE_READY -> {
                        isBuffering = false
                        exoProgressBar.isVisible = false
                        cvPlay.isVisible = true
                    }
                    Player.STATE_IDLE -> {
                        isBuffering = false
                        exoProgressBar.isVisible = true
                        cvPlay.isVisible = false
                    }
                    Player.STATE_ENDED -> {
                        finish()
                    }
                    else -> {}
                }
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                if (isLoading) {
                    isBuffering = true
                    if (exoPlayer.isCurrentMediaItemSeekable
                        && !exoPlayer.isPlaying
                        && exoPlayer.bufferedPosition == 0L
                    ) {
                        exoProgressBar.isVisible = true
                    }
                } else {
                    isBuffering = false
                    exoProgressBar.isVisible = false
                }
            }
        })

        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (connectivityManager.activeNetwork == network) {
                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.Main) {
                            exoPlayer.prepare()
                        }
                    }
                }
            }

            override fun onLost(network: Network) {
//                Toast.makeText(this@ExoPlayerActivity, "Соединение потеряно", Toast.LENGTH_SHORT)
//                    .show()
            }
        }
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        doubleTabEnabled()
        exoPlayer.prepare()
        playVideo()
    }

    private fun doubleTabEnabled() {
        binding.playerView.player = exoPlayer
        binding.youtubeOverlay.performListener(object : YouTubeOverlay.PerformListener {
            override fun onAnimationEnd() {
                binding.youtubeOverlay.visibility = View.GONE
            }

            override fun onAnimationStart() {
                binding.youtubeOverlay.visibility = View.VISIBLE
            }

            override fun shouldForward(
                player: Player,
                playerView: DoubleTapPlayerView,
                posX: Float,
            ): Boolean? {
                if (player.currentPosition > 500 && posX < playerView.width * 0.35 && !isLocked)
                    return false

                if (player.currentPosition < player.duration && posX > playerView.width * 0.65
                    && !isLocked
                )
                    return true

                return null
            }
        })
        binding.youtubeOverlay.player(exoPlayer)
    }

    private fun setupTextViewValues() {
        exoAnimeTitle.text = animeTitle
    }

    private fun setupOnClickListeners() {
        backButton.setOnClickListener {
            exoPlayer.release()
            finish()
        }

        exoPrevEp.setOnClickListener {
            if (videoNumber != 0) {
                videoNumber -= 1
                if (videoNumber != -0) exoPrevEp.alpha = 1f
                else exoPrevEp.alpha = 0.7f
                exoNextEp.alpha = 1f
                exoNextEp.isEnabled = true
                videoLinks = videos.get(videoNumber)
                exoPlayer.setMediaItem(MediaItem.fromUri(videoLinks))
                val data = loadData<List<Lesson>>("unLockList") ?: emptyList()
                exoAnimeTitle.text = data.get(videoNumber).name
                animeTitle = data.get(videoNumber).name
            } else {
                exoPrevEp.alpha = 0.7f
            }
        }

        exoNextEp.setOnClickListener {
            if (videoNumber != videos.size - 1) {
                videoNumber += 1
                if (videoNumber != videos.size - 1) exoNextEp.alpha = 1f
                else exoNextEp.alpha = 0.7f
                exoPrevEp.alpha = 1f
                exoPrevEp.isEnabled = true
                videoLinks = videos.get(videoNumber)
                val data = loadData<List<Lesson>>("unLockList") ?: emptyList()
                exoAnimeTitle.text = data.get(videoNumber).name
                animeTitle = data.get(videoNumber).name
                exoPlayer.setMediaItem(MediaItem.fromUri(videoLinks))
            } else {
                exoNextEp.alpha = 0.7f
            }
        }

        exoPlay.setOnClickListener {
            if (exoPlayer.isPlaying) pauseVideo()
            else playVideo()
        }

        exoScreen.setOnClickListener {
            if (isFullscreen) {
                isFullscreen = false
                playInFullscreen(enable = false)
            } else {
                isFullscreen = true
                playInFullscreen(enable = true)
            }
        }

        exoSpeed.setOnClickListener {
            val builder =
                AlertDialog.Builder(this, R.style.SpeedDialog)
            builder.setTitle("Speed")
            val speed = arrayOf("0.25", "0.5", "Normal", "1.5", "2")
            builder.setItems(speed) { _, which ->
                val param = when (which) {
                    0 -> PlaybackParameters(0.25f)
                    1 -> PlaybackParameters(0.5f)
                    2 -> PlaybackParameters(1f)
                    3 -> PlaybackParameters(1.5f)
                    else -> PlaybackParameters(2f)
                }
                exoPlayer.playbackParameters = param
            }
            val dialog = builder.create()
            // hide status bar
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            hideSystemBars()
            dialog.show()
        }

        exoLock.setOnClickListener {
            if (!isLocked) {
                exoLock.setImageResource(R.drawable.ic_lock_24)
            } else {
                exoLock.setImageResource(R.drawable.ic_lock_open_24)
            }
            isLocked = !isLocked
            lockScreen(isLocked)
        }

        exoPip.setOnClickListener {

            // permission check
            val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE, android.os.Process.myUid(), packageName
                ) == AppOpsManager.MODE_ALLOWED
            } else false
            // API >= 26 check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (status) {
                    this.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                    binding.playerView.useController = false
                    pipStatus = false
                } else {
                    val intent = Intent(
                        "android.settings.PICTURE_IN_PICTURE_SETTINGS",
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
            } else {
                Toast.makeText(
                    this,
                    "Feature not supported on this device",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        exoSkip.setOnClickListener {
            val currentPositionMs: Long = exoPlayer.currentPosition
            val newPositionMs = currentPositionMs + 85000
            exoPlayer.seekTo(newPositionMs)
        }
    }

    private fun setupMiddleControllers() {
        when (videoNumber) {
            0 -> {
                exoPrevEp.alpha = 0.7f
                exoPrevEp.isEnabled = false
                if (videoTotal == 1) {
                    exoNextEp.alpha = 0.7f
                    exoNextEp.isEnabled = false
                } else {
                    exoNextEp.alpha = 1f
                    exoNextEp.isEnabled = true
                }
            }
            videoTotal -> {
                exoNextEp.alpha = 0.7f
                exoPrevEp.isEnabled = true
                exoNextEp.isEnabled = false
            }
            else -> {
                exoPrevEp.isEnabled = true
                exoNextEp.isEnabled = true
            }
        }
    }

    private fun playVideo() {
        Glide.with(this).load(R.drawable.anim_play_to_pause).into(exoPlay)
        exoPlayer.play()
    }

    private fun pauseVideo() {
        Glide.with(this).load(R.drawable.anim_pause_to_play).into(exoPlay)
        exoPlayer.pause()
    }

    private fun playInFullscreen(enable: Boolean) {
        if (enable) {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            exoScreen.setImageResource(R.drawable.ic_fullscreen_exit_28)
        } else {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            exoScreen.setImageResource(R.drawable.ic_fullscreen_28)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        configuration: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, configuration)
        if (pipStatus) {
            lifecycleScope.launch {
                finish()
                val data = loadData<List<Lesson>>("unLockList") ?: emptyList()
                val videos = ArrayList<String>()
                data!!.onEach {
                    videos.add(it.video_url)
                }
                val intent = newIntent(
                    this@ExoPlayerActivity,
                    episodeLinks = videoLinks,
                    videoNumber,
                    videos.size,
                    videos,
                    animeTitle,
                    animeKind
                )
                startActivity(intent)
            }
        }
    }

    private fun lockScreen(locked: Boolean) {
        if (locked) {
            exoTopControllers.visibility = View.INVISIBLE
            exoMiddleControllers.visibility = View.INVISIBLE
            exoBottomControllers.visibility = View.INVISIBLE
        } else {
            exoTopControllers.visibility = View.VISIBLE
            exoMiddleControllers.visibility = View.VISIBLE
            exoBottomControllers.visibility = View.VISIBLE
        }
    }

    private fun setupViews() {
        exoTopControllers = findViewById(R.id.exo_top_controllers)
        exoMiddleControllers = findViewById(R.id.exo_middle_controllers)
        exoBottomControllers = findViewById(R.id.exo_bottom_controllers)

        // Top buttons
        backButton = findViewById(R.id.exo_back)
        exoAnimeTitle = findViewById(R.id.exo_title)
        exoQuality = findViewById(R.id.exo_quality)
        exoSpeed = findViewById(R.id.exo_speed)
        exoLock = findViewById(R.id.exo_lock)

        // Middle buttons
        exoPrevEp = findViewById(R.id.exo_prev_ep)
        cvPlay = findViewById(R.id.cv_exo_play_pause)
        exoPlay = findViewById(R.id.ib_exo_play_pause)
        exoProgressBar = findViewById(R.id.exo_progress_bar)
        exoNextEp = findViewById(R.id.exo_next_ep)

        // Bottom buttons
        exoPip = findViewById(R.id.exo_pip)
        exoSkip = findViewById(R.id.exo_skip)
        exoScreen = findViewById(R.id.exo_screen)
    }

    private fun parseExtra() {
        if (!intent.hasExtra(EXTRA_EPISODE_LINKS) || !intent.hasExtra(EXTRA_EPISODE_NUMBER) ||
            !intent.hasExtra(EXTRA_EPISODES_TOTAL) || !intent.hasExtra(EXTRA_ANIME_TITLE) ||
            !intent.hasExtra(EXTRA_ANIME_TITLE) || !intent.hasExtra(EXTRA_ANIME_KIND)
        ) {
            finish()
            return
        }
        videoLinks = intent.getSerializableExtra(EXTRA_EPISODE_LINKS) as String
        videoNumber = intent.getIntExtra(EXTRA_EPISODE_NUMBER, EMPTY_EPISODE_NUMBER_VALUE)
        videoTotal = intent.getIntExtra(EXTRA_EPISODES_TOTAL, EMPTY_EPISODE_NUMBER_VALUE)
        videos = intent.getSerializableExtra(EXTRA_EPISODES) as List<String>
        animeTitle = intent.getStringExtra(EXTRA_ANIME_TITLE) ?: EMPTY_STRING_VALUE
        animeKind = intent.getStringExtra(EXTRA_ANIME_KIND) ?: EMPTY_STRING_VALUE
    }

    companion object {
        private var isFullscreen: Boolean = false
        private var isLocked: Boolean = false
        var link: String = ""
        var pipStatus: Boolean = false

        private const val EXTRA_EPISODE_LINKS = "episodeLinks"
        private const val EXTRA_EPISODE_NUMBER = "episodeNumber"
        private const val EXTRA_EPISODES_TOTAL = "episodesTotal"
        private const val EXTRA_EPISODES = "episodes"
        private const val EXTRA_ANIME_TITLE = "animeTitle"
        private const val EXTRA_ANIME_KIND = "animeKind"

        private const val EMPTY_STRING_VALUE = "~"
        private const val EMPTY_EPISODE_NUMBER_VALUE = 0

        fun newIntent(
            context: Context,
            episodeLinks: String,
            episodeNumber: Int,
            episodesTotal: Int,
            episodes: List<String>,
            animeTitle: String,
            animeKind: String,
        ): Intent {
            val intent = Intent(context, ExoPlayerActivity::class.java)
            intent.putExtra(EXTRA_EPISODE_LINKS, episodeLinks as java.io.Serializable)
            intent.putExtra(EXTRA_EPISODE_NUMBER, episodeNumber)
            intent.putExtra(EXTRA_EPISODES_TOTAL, episodesTotal)
            intent.putExtra(EXTRA_EPISODES, episodes as java.io.Serializable)
            intent.putExtra(EXTRA_ANIME_TITLE, animeTitle)
            intent.putExtra(EXTRA_ANIME_KIND, animeKind)
            return intent
        }
    }


    @SuppressLint("ViewConstructor")
    class ExtendedTimeBar(
        context: Context,
        attrs: AttributeSet?
    ) : DefaultTimeBar(context, attrs) {
        private var enabled = false
        private var forceDisabled = false
        override fun setEnabled(enabled: Boolean) {
            this.enabled = enabled
            super.setEnabled(!forceDisabled && this.enabled)
        }

        fun setForceDisabled(forceDisabled: Boolean) {
            this.forceDisabled = forceDisabled
            isEnabled = enabled
        }
    }

}