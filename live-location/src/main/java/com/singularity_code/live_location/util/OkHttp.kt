package com.singularity_code.live_location.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.http2.ConnectionShutdownException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun defaultOkhttp(
    context: Context,
    /*authPreference: Preference*/
) = OkHttpClient.Builder()
    .apply {
        addInterceptor(
            chuckerInterceptor(
                context
            )
        )
        /**
         * handle common IO error
         */
        addInterceptor { chain ->
            val newRequest = chain.request()
                .newBuilder()
                .addHeader(
                    "Authorization",
                    "Bearer knaJWHWUkx_4vfSQ"
                )
                .build()

            val response = kotlin.runCatching {
                chain.proceed(
                    newRequest
                )
            }.getOrElse {
                val message = when (it) {
                    is SocketTimeoutException -> {
                        "Timeout - Please check your internet connection"
                    }

                    is UnknownHostException -> {
                        "Unable to make a connection. Please check your internet"
                    }

                    is ConnectionShutdownException -> {
                        "Connection shutdown. Please check your internet"
                    }

                    is IOException -> {
                        "Server is unreachable, please try again later."
                    }

                    else -> {
                        "${it.message}"
                    }
                }
                Response.Builder()
                    .request(newRequest)
                    .protocol(
                        Protocol.HTTP_1_0
                    )
                    .body(
                        "{}".toResponseBody("application/json".toMediaType())
                    )
                    .message(message)
                    .code(1)
                    .build()
            }

            response
        }

        /**
         * Add extra interceptor to handle authentication error.
         * Since some endpoint return 400 for authentication error.
         */
        addInterceptor { chain ->
            val response = chain.proceed(chain.request())

            val isAuthorized = when (response.code) {
                401 -> false
                400 -> {
                    val errorMessage = runCatching {
                        val bodyJson = response.body
                            ?.string()
                            ?.let {
                                Gson().fromJson(
                                    it,
                                    JsonObject::class.java
                                )
                            }
                            ?: JsonObject()

                        @Suppress("CAST_NEVER_SUCCEEDS")
                        bodyJson["message"].toString()
                    }.getOrElse {
                        ""
                    }

                    !errorMessage.contains("Token tidak valid")
                }

                else -> true
            }

            /*if (!isAuthorized) {
                authPreference.logout()
                ProcessPhoenix.triggerRebirth(context)
            }*/

            response
        }

    }
    .build()
