package com.singularity_code.live_location.util.pattern

import androidx.annotation.DrawableRes


interface NotificationConfig {
    val foregroundServiceID: Int
    val notificationChannelID: String
    val notificationChannelName: String
    val notificationChannelDescription: String
    val notificationPriority: Int
    @get:DrawableRes
    val iconRes: Int?
}