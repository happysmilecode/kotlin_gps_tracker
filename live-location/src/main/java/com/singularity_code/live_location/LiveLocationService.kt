package com.singularity_code.live_location

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.singularity_code.live_location.data.Repository
import com.singularity_code.live_location.util.ERROR_MISSING_LOCATION_PERMISSION
import com.singularity_code.live_location.util.ErrorMessage
import com.singularity_code.live_location.util.isGPSEnabled
import com.singularity_code.live_location.util.websocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.WebSocket

class LiveLocationService : Service() {

    private val liveLocationError = MutableStateFlow<ErrorMessage?>(null)
    private val currentLocation = MutableStateFlow<LatLng?>(null)
    private val liveLocationRunning = MutableStateFlow(false)

    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }

    private val locationRequest: LocationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(5000)
        .setFastestInterval(5000)

    private val locationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this.applicationContext)
    }

    private val locationCallBack = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            p0.runCatching {
                coroutineScope.launch {
                    currentLocation.emit(
                        LatLng(lastLocation.latitude, lastLocation.longitude)
                    )

                    webSocket.send("Live Location LatLng: ${lastLocation.latitude}, ${lastLocation.longitude}")
                    liveLocationError.emit(null)
                }

                Log.d("TAG", "onLocationResult: ${p0.lastLocation}")
            }.onFailure {

                coroutineScope.launch {
                    liveLocationError.emit(
                        it.message ?: it.cause?.message ?: "Unknown Error"
                    )
                }
                Log.d("TAG", "onLocationResult: $it")
            }
        }
    }

    private var ServiceID: Int? = null
    private lateinit var webSocket: WebSocket

    private fun startLocationService(
        serviceID: Int,
        channelID: String,
        serviceTitle: String,
        notificationMessage: String,
        notificationPriority: Int,
    ): Boolean {

        /** permission check **/
        run {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                coroutineScope.launch {
                    liveLocationRunning.emit(false)
                    liveLocationError.emit(ERROR_MISSING_LOCATION_PERMISSION)
                }
                return false
            }
        }

        /** notification **/
        run {
            /** store service id to cancel notification**/
            ServiceID = serviceID

            val builder = NotificationCompat.Builder(this@LiveLocationService, channelID)
            val notification = builder
                .setContentTitle(serviceTitle)
                .setContentText(notificationMessage)
                .setSmallIcon(androidx.appcompat.R.drawable.abc_ic_clear_material)
                .setPriority(notificationPriority)
                .setSound(null)
                .build()

            startForeground(
                serviceID,
                notification
            )
        }

        /** prepare websocket connection **/
        run {
            webSocket = websocket()
        }

        /** start location watcher **/
        run {
            locationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallBack,
                Looper.getMainLooper()
            )
        }

        coroutineScope.launch {
            liveLocationRunning.emit(true)
            liveLocationError.emit(null)
        }

        return true
    }

    private fun stopLocationService(): Boolean {

        /** destroy notification **/
        run {
            if (ServiceID != null) {
                stopForeground(ServiceID!!)
                ServiceID = null
            }
        }

        /** stop location watcher **/
        runCatching {
            locationProviderClient.removeLocationUpdates(locationCallBack)
        }

        /** close web socker **/
        run {
            webSocket.close(1000, "service stop")
        }

        coroutineScope.launch {
            liveLocationRunning.emit(false)
        }

        return true
    }

    /**
     * Binding service
     */
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        val liveLocationError: Flow<ErrorMessage?> = this@LiveLocationService.liveLocationError
        val currentLocation: Flow<LatLng?> = this@LiveLocationService.currentLocation
        val liveLocationRunning = this@LiveLocationService.liveLocationRunning
        val isGPSEnabled: Boolean get() = this@LiveLocationService.isGPSEnabled

        fun startService(
            serviceID: Int = 1030,
            channelID: String,
            serviceTitle: String,
            notificationMessage: String,
            notificationPriority: Int = NotificationCompat.PRIORITY_HIGH
        ): Boolean {
            return startLocationService(
                serviceID,
                channelID,
                serviceTitle,
                notificationMessage,
                notificationPriority
            )
        }

        fun stopService() = stopLocationService()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        runCatching{
            stopLocationService()
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching{
            stopLocationService()
        }
    }
}