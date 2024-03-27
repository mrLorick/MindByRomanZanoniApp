package com.lorick.chatterbox.videoOrAudioControls

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.lorick.chatterbox.R
import com.lorick.chatterbox.view.activity.nowPlaying.NowPlayingActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaService : MediaLibraryService() {

    @Inject
    lateinit var player: Player
    private var mediaLibrarySession: MediaLibrarySession? = null

    private val mediaLibrarySessionListener by lazy {
        object : MediaLibrarySession.Callback {

            /** Called when media controller connect to media session*/
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                val connectionResult = super.onConnect(session, controller)
                val sessionCommands =
                    connectionResult.availableSessionCommands
                        .buildUpon()
                        .add(SessionCommand("", Bundle()))
                        .build()
                return MediaSession.ConnectionResult.accept(
                    sessionCommands, connectionResult.availablePlayerCommands
                )
            }

            /** Called when media item update in media session via media controller */
            override fun onAddMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: MutableList<MediaItem>
            ): ListenableFuture<MutableList<MediaItem>> {
                val updateMediaItems = mediaItems.map { mediaItem ->
                    mediaItem.buildUpon().setUri(mediaItem.requestMetadata.mediaUri).build()
                }.toMutableList()
                return Futures.immediateFuture(updateMediaItems)
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onCreate() {
        super.onCreate()
        initializeMediaSession()
        setListener(MediaServiceListener())
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onDestroy() {
        super.onDestroy()
        player.release()
        mediaLibrarySession?.release()
        clearListener()
    }

    private fun initializeMediaSession() {
        val pendingIntent = PendingIntent.getActivity(this, 0,
            Intent(this, NowPlayingActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        mediaLibrarySession = MediaLibrarySession.Builder(this, player, mediaLibrarySessionListener)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    /**
     * Called when foreGroundService didn't start or get exception
     * */
    @UnstableApi
    inner class MediaServiceListener : Listener {

        private var channelId: String? = null

        @SuppressLint("NotificationPermission")
        @RequiresApi(31)
        override fun onForegroundServiceStartNotAllowedException() {
            super.onForegroundServiceStartNotAllowedException()

            channelId = getString(R.string.app_name)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = notificationChannel()
            if (notificationChannel != null) {
                notificationManager.createNotificationChannel(notificationChannel)
            }

            val pendingIntent = PendingIntent.getActivity(this@MediaService, 0,
                Intent(this@MediaService, NowPlayingActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = Notification.Builder(this@MediaService, channelId)
                .setContentTitle("Playback cannot be resumed")
                .setContentText("")
                .setSmallIcon(R.drawable.splash_logo)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            notificationManager.notify(1221, notification)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun notificationChannel(): NotificationChannel {
            return NotificationChannel(
                channelId, "MediaService", NotificationManager.IMPORTANCE_DEFAULT
            )
        }
    }
}