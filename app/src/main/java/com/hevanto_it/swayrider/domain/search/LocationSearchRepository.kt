package com.hevanto_it.swayrider.domain.search

import com.hevanto_it.swayrider.core.network.NetworkResult

data class BoundingBox(
    val minLat: Double,
    val minLon: Double,
    val maxLat: Double,
    val maxLon: Double
)

data class Coordinate(val lat: Double, val lon: Double)

interface LocationSearchRepository {
    suspend fun search(
        query: String,
        viewportBounds: BoundingBox,
        focusPoint: Coordinate? = null
    ): NetworkResult<List<LocationSearchResult>>
}
