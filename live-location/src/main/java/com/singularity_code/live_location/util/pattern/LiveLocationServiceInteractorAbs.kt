package com.singularity_code.live_location.util.pattern

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import com.singularity_code.live_location.LiveLocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class LiveLocationServiceInteractorAbs : LiveLocationServiceInteractor {

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
                            if (locationResult.lastLocation == null)
                                return@collect

                            // TODO: soon be removed
                            onReceiveUpdate(
                                latitude = locationResult.lastLocation!!.latitude,
                                longitude = locationResult.lastLocation!!.longitude,
                                accuracy = locationResult.lastLocation!!.accuracy,
                                updateTime = updateTime
                            )

                            onReceiveUpdate(locationResult)
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
        notificationTitle: String,
        notificationMessage: String,
    ) {

        context.bindService(
            LiveLocationService.createIntent(
                context = context,
                foregroundServiceID = notificationConfig.foregroundServiceID,
                notificationChannelID = notificationConfig.notificationChannelID,
                notificationChannelName = notificationConfig.notificationChannelName,
                notificationChannelDescription = notificationConfig.notificationChannelDescription,
                iconRes = notificationConfig.iconRes,
                notificationTitle = notificationTitle,
                notificationMessage = notificationMessage,
                gpsSamplingRate = gpsConfig.samplingRate,
                notificationPriority = notificationConfig.notificationPriority,
                networkConfiguration = this.networkConfiguration,
                messageDescriptor = networkConfiguration.messageDescriptor
            ),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun stopService() {
        runCatching {
            context.unbindService(
                serviceConnection
            )
        }
        onServiceStatusChanged(
            LiveLocationServiceInteractor.ServiceStatus.DEAD
        )
    }
}
