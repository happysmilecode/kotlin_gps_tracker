package com.singularity_code.live_location.util

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.singularity_code.live_location.LiveLocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File


fun Activity.getLiveLocationServiceBinder(
    channelID: String,
    channelName: String,
    channelDescription: String,
    lifecycleOwner: LifecycleOwner,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    autoStart: Boolean = true
): Lazy<Flow<LiveLocationService.LocalBinder?>> {
    return lazy {
        val binder = MutableStateFlow<LiveLocationService.LocalBinder?>(null)

        fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Create or update the notification channel
                val notificationChannel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                notificationChannel.description = channelDescription
                notificationChannel.setShowBadge(true)
                /*notificationChannel.lockscreenVisibility = NotificationChannel.VISIBILITY_PUBLIC*/

                // Set the custom icon for the notification channel
                notificationChannel.setSound(
                    Uri.fromFile(File("//assets/no_sound_short.mp3")), null)
                notificationChannel.lightColor = Color.BLUE
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                notificationChannel.vibrationPattern = LongArray(0)
                notificationChannel.enableVibration(false)

                // Set the desired icon for the notification channel
                /*notificationChannel.ico(Icon.createWithResource(context, R.drawable.custom_notification_icon))*/

                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        fun removeNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // The id of the channel.
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.deleteNotificationChannel(channelID)
            }
        }

        val serviceConnection: ServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                coroutineScope.launch {
                    binder.emit(p1 as? LiveLocationService.LocalBinder)
                }
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                coroutineScope.launch {
                    binder.emit(null)
                }
            }
        }

        fun createIntent() = Intent(this, LiveLocationService::class.java)
            .apply {
                bindService(
                    this,
                    serviceConnection,
                    Context.BIND_AUTO_CREATE
                )
            }

        lifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    super.onCreate(owner)
                    createNotificationChannel()
                }

                override fun onResume(owner: LifecycleOwner) {
                    super.onResume(owner)
                    createIntent()
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    removeNotificationChannel()
                    super.onDestroy(owner)
                }
            }
        )

        binder
    }
}