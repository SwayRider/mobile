package com.hevanto_it.swayrider.data.search.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchServiceResponse(val results: List<SearchResult> = emptyList())

@JsonClass(generateAdapter = true)
data class SearchResult(
    val label: String,
    val locality: String?,
    val region: String?,
    val country: String?,
    val confidence: Double,
    val layer: String,
    val lat: Double,
    val lon: Double
)
