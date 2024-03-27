package com.lorick.chatterbox.videoOrAudioControls

import android.content.Context
import android.net.Uri
import androidx.media3.session.MediaController
import kotlinx.coroutines.flow.StateFlow

interface MediaControllerInterface {

    /**
     * Initializing media player
     * @param context
     * @param addMediaData callback for add media item
     * @param controller callback of mediaController for player
     * */
    fun initializeMediaController(
        context: Context,
        controller: (mediaController: MediaController?) -> Unit,
        addMediaData: () -> Unit
    )

    /**
     * Adding data for media uri
     * @param mediaUri
     * @param title
     * @param artist
     * */
    fun addMediaData(
        mediaUri: Uri,
        title: String?,
        artist: String?,
        image : String?,
        playWhenReady: Boolean
    )

    /**
     * Play or pause music player
     * */
    fun playOrPauseMusic()


    /**
     * Change player progress
     * @param progress
     * */
    fun changeMusicProgress(
        progress: Long
    )

    /**
     * Release media controller
     * */
    fun releaseMediaController()

    /**
     * Returns stateflow of type boolean is music playing
     * */
    fun musicPLayingState(): StateFlow<Boolean>

    /**
     * Returns stateflow of Long music progress state flow
     * */
    fun musicProgress(): StateFlow<MediaControllerImpl.MediaProgress>

    /**
     * Returns music end time state flow
     * */
    fun musicEndTime(): StateFlow<MediaControllerImpl.MediaProgress>

    /**
     * Returns buffering state flow
     * */
    fun musicBuffering(): StateFlow<Boolean>

    /**
     * MediaController volume
     * @param volume
     * */
    fun changeVolume(volume: Int)

    /**
     * IsRepeat enabled
     * */
    fun musicRepeat()

    /**
     * Returns isRepeat mode enabled state flow
     * */
    fun musicRepeated(): StateFlow<Boolean>

    /**
     * Returns Fast Forwarded state flow
     * */
    fun fastSpeed(speed: Float)
}