package com.singularity_code.live_location.util.pattern

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.MainThread
import com.singularity_code.live_location.LiveLocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class LiveLocationServiceInteractorAbs : LiveLocationServiceInteractor {

    abstract override val context: Context

    /** GPS Sampling rate in millisecond **/
    abstract override val samplingRate: Long

    abstract override val networkInteractor: LiveLocationNetworkInteractor?

    private var binder: LiveLocationService.LocalBinder? = null
    private val coroutineJobs = arrayListOf<Job>()

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            p0: ComponentName?,
            p1: IBinder?
        ) {
            binder = p1 as? LiveLocationService.LocalBinder

            val currentLocationCollectorJob =
                CoroutineScope(Dispatchers.IO).launch {
                    binder?.currentLocation?.collect {
                        it?.apply {
                            onReceiveUpdate(
                                latitude = locationResult.lastLocation.latitude,
                                longitude = locationResult.lastLocation.longitude,
                                accuracy = locationResult.lastLocation.accuracy,
                                updateTime = updateTime
                            )
                        }
                    }
                }

            val locationErrorCollectorJob =
                CoroutineScope(Dispatchers.IO).launch {
                    binder?.liveLocationError?.collect {
                        onError(it)
                    }
                }

            val liveLocationServiceRunningCollector =
                CoroutineScope(Dispatchers.IO).launch {
                    binder?.liveLocationRunning?.collect {
                        onServiceStatusChanged(
                            if (it) LiveLocationServiceInteractor.ServiceStatus.RUNNING
                            else LiveLocationServiceInteractor.ServiceStatus.DEAD
                        )
                    }
                }

            coroutineJobs.addAll(
                listOf(
                    currentLocationCollectorJob,
                    locationErrorCollectorJob,
                    liveLocationServiceRunningCollector
                )
            )
        }

        override fun onServiceDisconnected(
            p0: ComponentName?
        ) {
            coroutineJobs.forEach { it.cancel() }
            coroutineJobs.clear()
            binder = null
        }
    }

    override fun startService(
        foregroundServiceID: Int,
        notificationTitle: String,
        notificationMessage: String,
        notificationChannelID: String,
        notificationChannelName: String,
        notificationChannelDescription: String,
        notificationPriority: Int,
    ) {

        context.bindService(
            LiveLocationService.createIntent(
                context = context,
                foregroundServiceID = foregroundServiceID,
                notificationChannelID = notificationChannelID,
                notificationChannelName = notificationChannelName,
                notificationChannelDescription = notificationChannelDescription,
                notificationTitle = notificationTitle,
                notificationMessage = notificationMessage,
                notificationPriority = notificationPriority,
            ),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    @MainThread
    override fun stopService() {
        context.unbindService(
            serviceConnection
        )
    }
}
