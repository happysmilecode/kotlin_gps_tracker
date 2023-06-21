package com.singularity_code.live_location.util

import android.app.Service
import android.content.Context
import android.location.LocationManager

// Properties
val Context.locationManager
    get() = getSystemService(Service.LOCATION_SERVICE) as LocationManager

val Context.isGPSEnabled: Boolean
    get() = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false