package com.hevanto_it.swayrider.data.search.remote

import com.hevanto_it.swayrider.core.network.AuthRequired
import com.hevanto_it.swayrider.data.search.dto.SearchServiceRequest
import com.hevanto_it.swayrider.data.search.dto.SearchServiceResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SearchServiceApi {
    @AuthRequired
    @POST("search")
    suspend fun search(@Body request: SearchServiceRequest): SearchServiceResponse
}
