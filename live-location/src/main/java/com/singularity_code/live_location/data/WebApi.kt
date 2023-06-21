package com.singularity_code.live_location.data

import com.google.gson.JsonElement
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PUT

interface WebApi {
    @FormUrlEncoded
    @PUT("endpoint/somewhere")
    suspend fun pushLiveLock(
        @FieldMap field: HashMap<String, String>
    ): JsonElement
}