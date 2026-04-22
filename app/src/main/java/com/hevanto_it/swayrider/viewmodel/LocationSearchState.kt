package com.hevanto_it.swayrider.viewmodel

import com.hevanto_it.swayrider.domain.search.LocationSearchResult

sealed class LocationSearchState {
    object Idle : LocationSearchState()
    object Loading : LocationSearchState()
    data class Results(val items: List<LocationSearchResult>) : LocationSearchState()
    data class Error(val message: String) : LocationSearchState()
}
