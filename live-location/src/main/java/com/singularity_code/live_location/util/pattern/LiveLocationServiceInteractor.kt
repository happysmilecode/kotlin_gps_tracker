package com.singularity_code.live_location.util.pattern

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread

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
