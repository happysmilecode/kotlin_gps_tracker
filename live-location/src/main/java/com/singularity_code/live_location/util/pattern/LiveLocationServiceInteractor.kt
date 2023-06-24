package com.singularity_code.live_location.util.pattern

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread

interface NotificationConfig {
    val foregroundServiceID: Int
    val notificationChannelID: String
    val notificationChannelName: String
    val notificationChannelDescription: String
    val notificationPriority: Int
    @get:DrawableRes
    val iconRes: Int?
}

interface GPSConfig {
    val samplingRate: Long
}

interface LiveLocationServiceInteractor {
    enum class ServiceStatus {
        RUNNING, DEAD
    }

    val context: Context

    val gpsConfig: GPSConfig

    val notificationConfig: NotificationConfig

    val networkConfiguration: LiveLocationNetworkConfiguration

    fun startService(
        notificationTitle: String,
        notificationMessage: String,
    )

    fun stopService()

    @MainThread
    fun onServiceStatusChanged(
        serviceStatus: ServiceStatus
    ) {}

    @MainThread
    fun onError(
        message: String?
    ) {}

    @MainThread
    fun onReceiveUpdate(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        updateTime: Long
    ) {}
}
