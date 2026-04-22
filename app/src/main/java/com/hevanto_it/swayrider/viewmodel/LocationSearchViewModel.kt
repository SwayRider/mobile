package com.hevanto_it.swayrider.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hevanto_it.swayrider.core.network.NetworkResult
import com.hevanto_it.swayrider.domain.search.BoundingBox
import com.hevanto_it.swayrider.domain.search.Coordinate
import com.hevanto_it.swayrider.domain.search.LocationSearchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationSearchViewModel(
    private val locationSearchRepository: LocationSearchRepository,
    // Allows injecting a test scope without needing kotlinx-coroutines-test
    private val externalScope: CoroutineScope? = null
) : ViewModel() {

    private val scope get() = externalScope ?: viewModelScope

    private val _searchState = MutableStateFlow<LocationSearchState>(LocationSearchState.Idle)
    val searchState: StateFlow<LocationSearchState> = _searchState

    fun dismiss() {
        _searchState.value = LocationSearchState.Idle
    }

    fun search(query: String, viewportBounds: BoundingBox, focusPoint: Coordinate? = null) {
        if (query.isBlank()) return
        scope.launch {
            _searchState.value = LocationSearchState.Loading
            _searchState.value = when (val result = locationSearchRepository.search(query, viewportBounds, focusPoint)) {
                is NetworkResult.Success -> {
                    if (result.data.isEmpty()) LocationSearchState.Idle
                    else LocationSearchState.Results(result.data)
                }
                is NetworkResult.Exception ->
                    LocationSearchState.Error(result.throwable.message ?: "Network error")
                is NetworkResult.Error ->
                    LocationSearchState.Error(result.error.message)
            }
        }
    }
}

class LocationSearchViewModelFactory(
    private val locationSearchRepository: LocationSearchRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationSearchViewModel(locationSearchRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
