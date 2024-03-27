package com.lorick.chatterbox.videoOrAudioControls

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.RequestMetadata
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.lorick.chatterbox.utils.convertMilliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MediaControllerImpl : MediaControllerInterface {

    private var listenableFutureMediaController: ListenableFuture<MediaController>? = null
    private var sessionToken: SessionToken? = null
    private val mediaController: MediaController?
        get() = if (listenableFutureMediaController?.isDone == true) listenableFutureMediaController?.get() else null

    private var runnable: Runnable? = null
    private var handler: Handler? = null
    private var job: Job? = null
    private var playAndPauseJob: Job? = null
    private var bufferingJob: Job? = null

    private val mutableIsMusicPlaying by lazy {
        MutableStateFlow(false)
    }
    private val mutableMusicBuffering by lazy {
        MutableStateFlow(false)
    }

    private val mutableProgress by lazy {
        MutableStateFlow(
            value = MediaProgress(
                seekBarProgress = 0,
                musicProgress = "00:00"
            )
        )
    }

    private val mutableMusicEndTime by lazy {
        MutableStateFlow(
            value = MediaProgress(
                seekBarProgress = 0,
                musicProgress = "00:00"
            )
        )
    }
    private val mutableMusicRepeatMode by lazy {
        MutableStateFlow(
            value = false
        )
    }

    /**
     * Initializing media player
     * @param context
     * @param controller callback of mediaController for player
     * */
    override fun initializeMediaController(
        context: Context,
        controller: (mediaController: MediaController?) -> Unit,
        addMediaData: () -> Unit
    ) {
        synchronized(this) {
            /** Getting the session token of the media session */
            sessionToken = SessionToken(context, ComponentName(context, MediaService::class.java))

            /** Connecting the mediaController with media Session */
            sessionToken?.let { token ->
                listenableFutureMediaController = MediaController.Builder(
                    context, token
                ).buildAsync()
            }

            /** Connecting mediaController with media3 player */
            listenableFutureMediaController?.addListener({
                controller.invoke(mediaController)
                mediaControllerListener()
                addMediaData.invoke()
            }, MoreExecutors.directExecutor())
        }
    }

    /**
     * Adding data for media uri
     * @param mediaUri
     * @param title
     * @param artist
     * */
    override fun addMediaData(
        mediaUri: Uri,
        title: String?,
        artist: String?,
        image: String?,
        playWhenReady: Boolean
    ) {
        synchronized(this) {
            addMediaItemToMediaPlayer(
                title = title,
                artist = artist,
                mediaUri = mediaUri,
                image = image,
                playWhenReady = playWhenReady
            )
        }
    }

    /**
     * MediaController listener
     * */
    private fun mediaControllerListener() {
        mediaController?.addListener(object : Player.Listener {

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                if (playAndPauseJob != null) playAndPauseJob?.cancel()

                playAndPauseJob = CoroutineScope(Dispatchers.Main).launch {
                    mutableIsMusicPlaying.emit(
                        value = isPlaying
                    )
                }

                launchInMain {
                    getMusicProgress()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                val isBuffering = when (playbackState) {
                    Player.STATE_BUFFERING -> true
                    Player.STATE_READY -> false
                    else -> false
                }

                if (bufferingJob != null) bufferingJob?.cancel()
                bufferingJob = CoroutineScope(Dispatchers.Main).launch {
                    mutableMusicBuffering.emit(
                        value = isBuffering
                    )
                }

                launchInMain {
                    getMusicProgress()
                }
            }
        })
    }

    /**
     * Play or pause music player
     * and Adding media item if not added at first
     * */
    override fun playOrPauseMusic() {
        synchronized(this) {
            if (mediaController?.isPlaying == true) mediaController?.pause() else mediaController?.play()
        }
    }

    /**
     * Getting music progress
     * */
    private suspend fun getMusicProgress() {
        val maxDuration = mediaController?.duration
        var endTime = "00:00"
        convertMilliseconds(
            timeInMilli = maxDuration ?: 0L,
            convertedTime = { hour, minute, second ->
                endTime = "${
                    if (hour > 0) "${String.format("%02d", hour)}:" else ""
                }${String.format("%02d", minute)}:${String.format("%02d", second)}"
            }
        )

        mutableMusicEndTime.emit(
            value = MediaProgress(
                seekBarProgress = maxDuration ?: 0L,
                musicProgress = if (endTime == "-12:-55") "00:00" else endTime
            )
        )

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {

            /** Setting music dynamic progress */
            CoroutineScope(Dispatchers.Main).launch {

                val currentProgress = mediaController?.currentPosition
                var progress = "00:00"
                convertMilliseconds(
                    timeInMilli = mediaController?.currentPosition ?: 0L,
                    convertedTime = { hour, minute, second ->
                        progress = "${
                            if (hour > 0) "${String.format("%02d", hour)}:" else ""
                        }${String.format("%02d", minute)}:${String.format("%02d", second)}"
                    }
                )
                mutableProgress.emit(
                    value = MediaProgress(
                        seekBarProgress = currentProgress ?: 0L,
                        musicProgress = progress
                    )
                )
            }

            /** Running runnable if music is playing else remove runnable from handler */
            runnable?.let { run ->
                if (mediaController?.isPlaying == true){
                    handler?.postDelayed(run, 300)
                } else handler?.removeCallbacks(run)
            }
        }
        runnable?.run()
    }

    /**
     * Change player progress
     *   * @param progress
     * */
    override fun changeMusicProgress(progress: Long) {
        synchronized(this) {
            mediaController?.seekTo(progress)
        }
    }

    /**
     * Release media controller
     * */
    override fun releaseMediaController() {
        synchronized(this) {
            mediaController?.stop()
            mediaController?.playWhenReady = false
            mediaController?.release()
        }
    }

    /**
     * Returns stateflow of type boolean is music playing
     * */
    override fun musicPLayingState(): StateFlow<Boolean> {
        return mutableIsMusicPlaying.asStateFlow()
    }

    /**
     * Returns stateflow of Long music progress
     * */
    override fun musicProgress(): StateFlow<MediaProgress> {
        return mutableProgress.asStateFlow()
    }

    /**
     * Returns music end time
     * */
    override fun musicEndTime(): StateFlow<MediaProgress> {
        return mutableMusicEndTime.asStateFlow()
    }

    /**
     * Returns buffering state flow
     * */
    override fun musicBuffering(): StateFlow<Boolean> {
        return mutableMusicBuffering.asStateFlow()
    }

    /**
     * MediaController volume
     * @param volume
     * */
    override fun changeVolume(volume: Int) {
        synchronized(this) {
            val musicVolume = if (volume > 0) (volume.toDouble() / 100.toDouble()).toFloat() else 0f
            mediaController?.volume = musicVolume
        }
    }

    /**
     * IsRepeat enabled
     * */
    override fun musicRepeat() {
        synchronized(this) {
            val repeatMode =
                if (mediaController?.repeatMode == Player.REPEAT_MODE_ONE) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE
            mediaController?.repeatMode = repeatMode
            changeRepeatMode()
        }
    }

    /**
     * Returns isRepeat mode enabled state flow
     * */
    override fun musicRepeated(): StateFlow<Boolean> {
        return mutableMusicRepeatMode.asStateFlow()
    }

    override fun fastSpeed(speed: Float){
        synchronized(this) {
            mediaController?.setPlaybackSpeed(speed)
        }
    }

    private fun changeRepeatMode() {
        launchInMain {
            mutableMusicRepeatMode.emit(
                value = mediaController?.repeatMode == Player.REPEAT_MODE_ONE
            )
        }
    }

    /**
     * Add mediaItem to media player
     * */
    private fun addMediaItemToMediaPlayer(
        title: String? = "",
        artist: String? = "",
        mediaUri: Uri,
        image: String? = "",
        playWhenReady: Boolean
    ) {
        val mediaMetaData = MediaMetadata.Builder()
            .setTitle(title ?: "")
            .setArtist(artist ?: "")
            .setArtworkUri(image.orEmpty().toUri())
            .build()
        val requestMetaData = RequestMetadata.Builder()
            .setMediaUri(mediaUri)
            .build()
        val mediaItem = MediaItem.Builder()
            .setUri(mediaUri)
            .setMediaMetadata(mediaMetaData)
            .setRequestMetadata(requestMetaData)
            .build()
        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()

        if (playWhenReady) {
            mediaController?.playWhenReady = true
        }

        launchInMain {
            getMusicProgress()
        }
    }

    /**
     * Launching coroutines in main thread
     * */
    private fun launchInMain(body: suspend () -> Unit) {
        if (job != null) job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            ensureActive()
            body.invoke()
        }
    }

    data class MediaProgress(
        val seekBarProgress: Long,
        val musicProgress: String,
    )
}