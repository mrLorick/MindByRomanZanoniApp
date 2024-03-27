package com.lorick.chatterbox.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lorick.chatterbox.R
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import com.lorick.chatterbox.view.activity.dashboard.DashboardActivity
import com.lorick.chatterbox.view.activity.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var sharedPrefs: SharedPrefs

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
            if (remoteMessage.data.isNotEmpty()) {
                sendNotification(remoteMessage)
                /** Get Automatically Intent In app */
                Log.d("PUSH NOTIFICATION","getID "+remoteMessage.data)

                if (sharedPrefs.getUserLogin()){
                    CoroutineScope(Dispatchers.IO).launch {
                        val intent = Intent(this@MyFirebaseMessagingService, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)

                        Log.d("PUSH NOTIFICATION","getID "+remoteMessage.data["body"])
                    }
                }
            Log.d("PUSH NOTIFICATION","without notification"+remoteMessage.data.toString())
        }

        remoteMessage.notification?.let {
             sendNotification(remoteMessage)
            Log.d("PUSH NOTIFICATION","notification "+remoteMessage.data.toString())
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"]
        val message = remoteMessage.data["message"]
        Log.d("PUSH NOTIFICATION", "NOTIFICATION $title")

        val intent: Intent?
        if (sharedPrefs.getUserLogin()){
            intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }else{
            intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.splash_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,"Channel human readable title",NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0,notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}









