package com.singularity_code.live_location.data.payload

import com.google.android.gms.maps.model.LatLng

data class PushLiveLocPLD(
    val livelocToken: String,
    val latlng: LatLng
) {
    fun getFields(): HashMap<String, String> = hashMapOf(
        "liveLocationToken" to livelocToken,
        "latitude" to latlng.latitude.toString(),
        "longitude" to latlng.longitude.toString()
    )
}