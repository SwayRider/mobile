package com.hevanto_it.swayrider.search

import com.hevanto_it.swayrider.core.network.NetworkResult
import com.hevanto_it.swayrider.domain.search.BoundingBox
import com.hevanto_it.swayrider.domain.search.Coordinate
import com.hevanto_it.swayrider.domain.search.LocationSearchRepository
import com.hevanto_it.swayrider.domain.search.LocationSearchResult
import com.hevanto_it.swayrider.viewmodel.LocationSearchState
import com.hevanto_it.swayrider.viewmodel.LocationSearchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class LocationSearchViewModelTest {

    private val viewport = BoundingBox(50.0, 0.0, 51.0, 1.0)
    // Unconfined dispatcher runs coroutines synchronously — no need for kotlinx-coroutines-test
    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    private fun makeResult(label: String) = LocationSearchResult(
        label = label, locality = null, region = null, country = null,
        confidence = 0.9, layer = "venue", lat = 48.8, lon = 2.3
    )

    private fun viewModel(repo: LocationSearchRepository) =
        LocationSearchViewModel(repo, externalScope = testScope)

    @Test
    fun `initial state is Idle`() {
        val vm = viewModel(object : LocationSearchRepository {
            override suspend fun search(query: String, viewportBounds: BoundingBox, focusPoint: Coordinate?) =
                NetworkResult.Success<List<LocationSearchResult>>(emptyList())
        })
        assertTrue(vm.searchState.value is LocationSearchState.Idle)
    }

    @Test
    fun `search transitions to Results on non-empty response`() = runBlocking {
        val vm = viewModel(object : LocationSearchRepository {
            override suspend fun search(query: String, viewportBounds: BoundingBox, focusPoint: Coordinate?) =
                NetworkResult.Success(listOf(makeResult("Eiffel Tower")))
        })
        vm.search("Eiffel Tower", viewport)

        val state = vm.searchState.value
        assertTrue(state is LocationSearchState.Results)
        assertEquals("Eiffel Tower", (state as LocationSearchState.Results).items.first().label)
    }

    @Test
    fun `search transitions to Idle on empty response`() = runBlocking {
        val vm = viewModel(object : LocationSearchRepository {
            override suspend fun search(query: String, viewportBounds: BoundingBox, focusPoint: Coordinate?) =
                NetworkResult.Success<List<LocationSearchResult>>(emptyList())
        })
        vm.search("nothing", viewport)

        assertTrue(vm.searchState.value is LocationSearchState.Idle)
    }

    @Test
    fun `search transitions to Error on network exception`() = runBlocking {
        val vm = viewModel(object : LocationSearchRepository {
            override suspend fun search(query: String, viewportBounds: BoundingBox, focusPoint: Coordinate?) =
                NetworkResult.Exception(IOException("no network"))
        })
        vm.search("test", viewport)

        val state = vm.searchState.value
        assertTrue(state is LocationSearchState.Error)
        assertEquals("no network", (state as LocationSearchState.Error).message)
    }

    @Test
    fun `blank query is ignored and state remains Idle`() = runBlocking {
        var searchCalled = false
        val vm = viewModel(object : LocationSearchRepository {
            override suspend fun search(query: String, viewportBounds: BoundingBox, focusPoint: Coordinate?): NetworkResult<List<LocationSearchResult>> {
                searchCalled = true
                return NetworkResult.Success(emptyList())
            }
        })
        vm.search("   ", viewport)

        assertFalse(searchCalled)
        assertTrue(vm.searchState.value is LocationSearchState.Idle)
    }
}
