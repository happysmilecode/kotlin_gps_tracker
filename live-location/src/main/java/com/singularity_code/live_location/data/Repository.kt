package com.singularity_code.live_location.data

import android.content.Context
import arrow.core.Either
import com.singularity_code.live_location.util.ErrorMessage
import com.singularity_code.live_location.util.defaultOkhttp
import com.singularity_code.live_location.util.websocket
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody


interface Repository {
    val url: String
    val headers: HashMap<String, String>
    val context: Context

    fun openConnection()

    fun closeConnection()

    suspend fun sendData(
        data: String
    ): Either<ErrorMessage, String>
}

class WebSocketRepository(
    override val url: String,
    override val headers: HashMap<String, String>,
    override val context: Context
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

    override suspend fun sendData(data: String): Either<ErrorMessage, String> {
        return kotlin.runCatching {
            val result = webSocket.send(data)
            Either.Right(result.toString())
        }.getOrElse {
            Either.Left(it.message ?: it.cause?.message ?: "unknown error")
        }
    }

    override fun closeConnection() {
        webSocket.close(200, "normal closure")
    }

}

class RestfulRepository(
    override val url: String,
    override val headers: HashMap<String, String>,
    override val context: Context
) : Repository {

    private val okHttpClient by lazy {
        defaultOkhttp(context)
    }

    override fun openConnection() {
        // nothing to do
    }

    override suspend fun sendData(
        data: String
    ): Either<ErrorMessage, String> {
        val requestBody: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), data)

        val request: Request = Request.Builder()
            .apply {
                url(url)
                post(requestBody)
                headers.forEach {
                    addHeader(it.key, it.value)
                }
            }
            .build()

        return runCatching {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Either.Right(response.body?.string() ?: "nothing to show")
            }else {
                Either.Left(response.message)
            }
        }.getOrElse {
            Either.Left(it.message ?: it.cause?.message ?: "unknown error")
        }
    }

    override fun closeConnection() {
        // nothing to do
    }

}