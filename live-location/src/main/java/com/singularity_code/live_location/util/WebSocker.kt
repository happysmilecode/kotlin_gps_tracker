package com.singularity_code.live_location.util

import android.content.Context
import android.util.Log
import okhttp3.*

fun websocket(
    context: Context,
    apiURL: String,
    headers: HashMap<String,String>
): WebSocket {
    val client = OkHttpClient.Builder()
        .addInterceptor(chuckerInterceptor(context))
        .build()

    val request = Request.Builder()
        .apply {
            url(apiURL)
            headers.forEach{
                addHeader(
                    it.key, it.value
                )
            }
        }
        .build()



    return client.newWebSocket(
        request,
        object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("TAG", "WebSocket: Opened")
                super.onOpen(webSocket, response)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.d("TAG", "WebSocket: Failure ${t.message ?: t.cause?.message}")
                super.onFailure(webSocket, t, response)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("TAG", "WebSocket: Closed")
                super.onClosed(webSocket, code, reason)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("TAG", "WebSocket: Message $text")
                super.onMessage(webSocket, text)
            }
        }
    )
}