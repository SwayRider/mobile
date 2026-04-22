package com.hevanto_it.swayrider.domain.search

data class LocationSearchResult(
    val label: String,
    val locality: String?,
    val region: String?,
    val country: String?,
    val confidence: Double,
    val layer: String,
    val lat: Double,
    val lon: Double
)
