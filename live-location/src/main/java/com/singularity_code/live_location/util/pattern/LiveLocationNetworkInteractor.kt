package com.singularity_code.live_location.util.pattern

interface LiveLocationNetworkInteractor {
    enum class NetworkMethod {
        HTTP, WEBSOCKET
    }

    val url: String
    val method: NetworkMethod
    val headers: HashMap<String, String>
}