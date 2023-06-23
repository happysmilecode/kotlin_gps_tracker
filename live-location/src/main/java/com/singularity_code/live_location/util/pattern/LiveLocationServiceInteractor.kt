package com.singularity_code.live_location.util.pattern

import android.content.Context
import androidx.core.app.NotificationCompat

interface LiveLocationServiceInteractor {
    enum class ServiceStatus {
        RUNNING, DEAD
    }

    val context: Context

    /** GPS Sampling rate in millisecond **/
    val samplingRate: Long

    val networkConfiguration: LiveLocationNetworkConfiguration

    fun startService(
        foregroundServiceID: Int = 1003,
        notificationTitle: String,
        notificationMessage: String,
        notificationChannelID: String,
        notificationChannelName: String,
        notificationChannelDescription: String,
        notificationPriority: Int = NotificationCompat.PRIORITY_DEFAULT
    )

    fun stopService()

    fun onServiceStatusChanged(
        serviceStatus: ServiceStatus
    )

    fun onError(
        message: String?
    )

    fun onReceiveUpdate(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        updateTime: Long
    )
}
