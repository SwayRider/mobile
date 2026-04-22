package com.hevanto_it.swayrider.data.search

import com.hevanto_it.swayrider.core.network.NetworkResult
import com.hevanto_it.swayrider.core.network.safeApiCall
import com.hevanto_it.swayrider.data.search.dto.FocusPoint
import com.hevanto_it.swayrider.data.search.dto.LatLon
import com.hevanto_it.swayrider.data.search.dto.SearchServiceRequest
import com.hevanto_it.swayrider.data.search.dto.ViewportBounds
import com.hevanto_it.swayrider.data.search.remote.SearchServiceApi
import com.hevanto_it.swayrider.domain.search.BoundingBox
import com.hevanto_it.swayrider.domain.search.Coordinate
import com.hevanto_it.swayrider.domain.search.LocationSearchRepository
import com.hevanto_it.swayrider.domain.search.LocationSearchResult
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import java.util.Locale

class LocationSearchRepositoryImpl(
    private val searchServiceApi: SearchServiceApi,
    private val dispatcher: CoroutineDispatcher,
    private val moshi: Moshi
) : LocationSearchRepository {

    override suspend fun search(
        query: String,
        viewportBounds: BoundingBox,
        focusPoint: Coordinate?
    ): NetworkResult<List<LocationSearchResult>> {
        val request = SearchServiceRequest(
            text = query,
            viewport = ViewportBounds(
                bottomLeft = LatLon(lat = viewportBounds.minLat, lon = viewportBounds.minLon),
                topRight = LatLon(lat = viewportBounds.maxLat, lon = viewportBounds.maxLon)
            ),
            focusPoint = focusPoint?.let { FocusPoint(lat = it.lat, lon = it.lon) },
            language = Locale.getDefault().toLanguageTag(),
            size = 5
        )

        val result = safeApiCall(dispatcher, moshi) {
            searchServiceApi.search(request)
        }

        return when (result) {
            is NetworkResult.Success -> {
                val items = result.data.results.map { r ->
                    LocationSearchResult(
                        label = r.label,
                        locality = r.locality,
                        region = r.region,
                        country = r.country,
                        confidence = r.confidence,
                        layer = r.layer,
                        lat = r.lat,
                        lon = r.lon
                    )
                }
                NetworkResult.Success(items)
            }
            is NetworkResult.Exception -> result
            is NetworkResult.Error -> result
        }
    }
}
