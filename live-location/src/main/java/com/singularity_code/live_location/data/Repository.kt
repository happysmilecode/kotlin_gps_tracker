package com.singularity_code.live_location.data

import com.singularity_code.live_location.util.createRetrofitService
import com.singularity_code.live_location.util.defaultOkhttp
import com.singularity_code.live_location.util.websocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface Repository {
    val url: String
    val headers: HashMap<String, String>

    fun openConnection()

    fun sendData(
        data: String
    )

    fun closeConnection()
}

class WebSocketRepository(
    override val url: String,
    override val headers: HashMap<String, String>,
) : Repository {

    private val webSocket by lazy {
        websocket(
            apiURL = url,
            headers = headers
        )
    }

    override fun openConnection() {
        // nothing to do
    }

    override fun sendData(data: String) {
        webSocket.send(data)
    }

    override fun closeConnection() {
        webSocket.close(200, "normal closure")
    }

}

class RestfulRepository(
    override val url: String,
    override val headers: HashMap<String, String>
) : Repository {

    interface WebApi {
        @POST("{endpoint}")
        fun sendData(
            @Path("endpoint") endpointURL: String,
            @Body body: RequestBody
        )
    }

    private val retrofit by lazy {
        createRetrofitService(
            WebApi::class.java,
            defaultOkhttp(),
            url
        )
    }

    private val coroutineScope by lazy {
        CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    override fun openConnection() {
        // nothing to do
    }

    override fun sendData(data: String) {
        coroutineScope.launch {
            retrofit.sendData(
                body = data.toRequestBody(contentType = "text/plain".toMediaTypeOrNull()),
                endpointURL = url
            )
        }
    }

    override fun closeConnection() {
        // nothing to do
    }

}