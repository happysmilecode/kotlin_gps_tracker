package com.singularity_code.live_location.util.pattern

import com.singularity_code.live_location.util.enums.NetworkMethod

interface LiveLocationNetworkConfiguration {
    val url: String
    val networkMethod: NetworkMethod
    val headers: HashMap<String, String>
    val messageDescriptor: String
}