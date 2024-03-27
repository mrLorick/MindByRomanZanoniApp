package com.lorick.chatterbox.view.activity.landscapeVideoPlay

import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.databinding.ActivityLandscapeVideoPlayBinding
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.finishActivityVideoZoom
import com.lorick.chatterbox.utils.gone
import com.lorick.chatterbox.utils.visible
import com.lorick.chatterbox.videoOrAudioControls.MediaCache
import com.lorick.chatterbox.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class LandscapeVideoPlayActivity : BaseActivity<ActivityLandscapeVideoPlayBinding>() {
    private val viewModal: HomeViewModel by viewModels()
    private var player: ExoPlayer? = null

    override fun getLayoutRes(): Int {
        return R.layout.activity_landscape_video_play
    }

    override fun initView() {
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        getIntentData()
        setOnClickListener()
    }

    private fun setOnClickListener() {
       binding.apply {
           ivPlayOrPause.setOnClickListener {
               if (player?.isPlaying == true) {
                   handlingPlayIcon(false)
               } else if (player?.isPlaying == false) {
                   handlingPlayIcon(true)
               }
           }

           videoView.setOnClickListener {
               if (player?.isPlaying == true) {
                   handlingPlayIcon(false)
               } else if (player?.isPlaying == false) {
                   handlingPlayIcon(true)
               }
           }
       }
    }

    override fun viewModel() {

    }

    private fun getIntentData() {
        val intent = intent.extras
        if (intent != null) {
            viewModal.videoUrl.set(intent.getString(AppConstants.VIDEO_URL).toString())
            initPlayer(viewModal.videoUrl.get() ?: "")
        }
    }



    private fun initPlayer(mediaPath: String) {
        player = ExoPlayer.Builder(applicationContext)
            .build()
            .apply { addListener(playerListener) }
        setUri(mediaPath)
        binding.videoView.player = player
    }

    @OptIn(UnstableApi::class) private fun setUri(mediaPath: String) {
        val metaData = MediaMetadata.Builder()
            .setTitle("Title")
            .setAlbumTitle("Album")
            .build()

        val mediaMetaData = MediaItem.Builder()
            .setUri(mediaPath.toUri())
            .setMediaMetadata(metaData)
            .build()
        val mediaItem = mediaPath.let { MediaItem.fromUri(it) }
        val mediaSourceFactory =
            ProgressiveMediaSource.Factory(MediaCache.getInstance(context = applicationContext).cacheFactory)
        val mediaSource = mediaItem.let { mediaSourceFactory.createMediaSource(it) }
        mediaSource.let { player?.setMediaSource(it, true) }
        player?.repeatMode = Player.REPEAT_MODE_ALL
        player?.setMediaItem(mediaMetaData)
        player?.prepare()
    }

    private fun releasePlayer() {
        player?.apply {
            playWhenReady = false
            release()
        }
        player = null
    }

    private fun pause() {
        player?.playWhenReady = false
    }

    private fun play() {
        player?.playWhenReady = true
    }

    private fun restartPlayer() {
        player?.seekTo(0)
        player?.playWhenReady = true
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_ENDED -> {
                    handlingPlayIcon(false)
                }

                Player.STATE_READY -> {
                    handlingPlayIcon(true)
                }
                Player.STATE_BUFFERING -> {
                    handlingPlayIcon(playStatus = false, isBuffering = true)
                }

                Player.STATE_IDLE -> {

                }
            }
        }
    }


    private fun handlingPlayIcon(playStatus: Boolean, isBuffering: Boolean = false) {
        binding.apply {
            if (isBuffering) {
                ivPlayOrPause.setBackgroundResource(R.drawable.ic_simple_icon)
                ivPlayOrPause.visible()
                videoProgress.visible()
            } else {
                if (playStatus) {
                    play()
                    ivPlayOrPause.setBackgroundResource(R.drawable.ic_pause_icon)
                    ivPlayOrPause.visible()
                    videoProgress.gone()
                    RunInScope.mainThread {
                        delay(500)
                        ivPlayOrPause.gone()
                    }
                } else {
                    pause()
                    ivPlayOrPause.setBackgroundResource(R.drawable.ic_play_icon)
                    ivPlayOrPause.visible()
                    videoProgress.gone()
                }
            }
        }
    }
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishActivityVideoZoom()
        }
    }

    override fun onStop() {
        super.onStop()
        player?.stop()
    }
}