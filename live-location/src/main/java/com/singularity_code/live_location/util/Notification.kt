package com.singularity_code.live_location.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat


fun Context.createNotificationApp(
    channelID: String,
    iconResource: Int? = null,
    title: String = "Title",
    contentText: String = "Message",
    pendingIntent: PendingIntent = PendingIntent.getActivity(
        this,
        0,
        Intent(),
        getPendingIntentFlag()
    ),
    autoCancel: Boolean = false,
    priority: Int = NotificationCompat.PRIORITY_MAX,
    behavior: Int = NotificationCompat.DEFAULT_ALL

): Notification {
    val notificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationBuilder = NotificationCompat.Builder(
        applicationContext,
        channelID
    )

    iconResource?.let {
        notificationBuilder.setSmallIcon(it)
    }
    notificationBuilder.setContentTitle(title)
    notificationBuilder.setDefaults(behavior)
    notificationBuilder.setContentText(contentText)
    notificationBuilder.setContentIntent(pendingIntent)
    notificationBuilder.setAutoCancel(autoCancel)
    notificationBuilder.priority = priority

    return notificationBuilder.build()
}

fun getPendingIntentFlag(): Int {
    var flag = PendingIntent.FLAG_UPDATE_CURRENT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    }
    return flag
}
