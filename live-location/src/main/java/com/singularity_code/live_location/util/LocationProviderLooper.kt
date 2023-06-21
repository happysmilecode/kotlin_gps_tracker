package com.singularity_code.live_location.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Deprecated("will be deleted soon")
class LocationProviderLooper(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : HandlerThread("LocationProviderLooper") {

    private val _locationServiceError = MutableStateFlow<ErrorMessage?>(null)
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    private val _liveLocationRunning = MutableStateFlow(false)

    /** Interfaces **/
    val locationServiceError = _locationServiceError
    val currentLocation = _currentLocation
    val liveLocationRunning = _liveLocationRunning

    /**
     * Instances
     */
    private val locationCallBack = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            p0.runCatching {
                coroutineScope.launch {
                    _currentLocation.emit(
                        LatLng(lastLocation.latitude, lastLocation.longitude)
                    )

                    _locationServiceError.emit(null)
                }
            }.onFailure {

                coroutineScope.launch {
                    _locationServiceError.emit(
                        it.message ?: it.cause?.message ?: "Unknown Error"
                    )
                }
            }
        }
    }

    private lateinit var locationRequest: LocationRequest
    private val locationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context.applicationContext)
    }

    private var handler: Handler? = null

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(
        intervalMillis: Long,
        fastestIntervalMillis: Long
    ): Boolean {

        if (!context.hasLocationPermission) {
            coroutineScope.launch {
                _liveLocationRunning.emit(
                    false
                )
                _locationServiceError.emit(
                    ERROR_NO_GPS_PERMISSION
                )
            }

            return false
        }

        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(intervalMillis)
            .setFastestInterval(fastestIntervalMillis)

        if (looper == null) {
            return false
        }

        handler = Handler(looper)
        handler?.post {
            locationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallBack,
                looper
            )
        }

        coroutineScope.launch {
            _liveLocationRunning.emit(true)
        }
        return true
    }

    fun stopLocationUpdates() {
        handler?.post {
            locationProviderClient.removeLocationUpdates(locationCallBack)
        }

        coroutineScope.launch {
            _liveLocationRunning.emit(false)
        }
    }
}
