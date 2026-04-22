package com.hevanto_it.swayrider.data.search.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchServiceRequest(
    val text: String,
    val viewport: ViewportBounds,
    @param:Json(name = "focus_point") val focusPoint: FocusPoint? = null,
    val language: String? = null,
    val size: Int? = null
)

@JsonClass(generateAdapter = true)
data class ViewportBounds(
    @param:Json(name = "bottom_left") val bottomLeft: LatLon,
    @param:Json(name = "top_right") val topRight: LatLon
)

@JsonClass(generateAdapter = true)
data class FocusPoint(val lat: Double, val lon: Double)

@JsonClass(generateAdapter = true)
data class LatLon(val lat: Double, val lon: Double)
