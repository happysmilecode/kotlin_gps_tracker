package com.singularity_code.live_location.data

import android.content.Context
import arrow.core.Either
import com.google.gson.JsonElement
import com.singularity_code.live_location.data.payload.PushLiveLocPLD
import com.singularity_code.live_location.util.ErrorMessage
import com.singularity_code.live_location.util.createRetrofitService
import com.singularity_code.live_location.util.defaultOkhttp

class Repository(
    private val context: Context
) {
    private val api = createRetrofitService(
        WebApi::class.java,
        defaultOkhttp(context),
        "http://somewhere/api/"
    )

    suspend fun pushLiveLock(
        pld: PushLiveLocPLD
    ): Either<ErrorMessage, JsonElement> {
        return kotlin.runCatching {
            val result = api.pushLiveLock(
                pld.getFields()
            )

            Either.Right(result)
        }.getOrElse {
            Either.Left(
                it.message ?: it.cause?.message ?: "unknown error"
            )
        }
    }
}