package com.singularity_code.live_location.util.pattern

import android.content.Context
import androidx.annotation.MainThread
import com.google.android.gms.location.LocationResult

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

    @Deprecated("soon will be removed")
    @MainThread
    fun onReceiveUpdate(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        updateTime: Long
    ) {}

    @MainThread
    fun onReceiveUpdate(
        location: LocationResult
    ) {}
}
