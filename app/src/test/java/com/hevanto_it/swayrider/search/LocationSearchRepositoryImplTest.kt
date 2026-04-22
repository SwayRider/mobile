package com.hevanto_it.swayrider.search

import com.hevanto_it.swayrider.core.network.NetworkResult
import com.hevanto_it.swayrider.data.search.LocationSearchRepositoryImpl
import com.hevanto_it.swayrider.data.search.dto.SearchServiceRequest
import com.hevanto_it.swayrider.data.search.dto.SearchServiceResponse
import com.hevanto_it.swayrider.data.search.dto.SearchResult
import com.hevanto_it.swayrider.data.search.remote.SearchServiceApi
import com.hevanto_it.swayrider.domain.search.BoundingBox
import com.hevanto_it.swayrider.domain.search.Coordinate
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.util.Locale

class FakeSearchServiceApi(
    private val response: SearchServiceResponse = SearchServiceResponse(emptyList()),
    private val throwOnCall: Boolean = false
) : SearchServiceApi {
    var lastRequest: SearchServiceRequest? = null

    override suspend fun search(request: SearchServiceRequest): SearchServiceResponse {
        lastRequest = request
        if (throwOnCall) throw IOException("network error")
        return response
    }
}

class LocationSearchRepositoryImplTest {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val dispatcher = Dispatchers.Unconfined
    private val viewport = BoundingBox(minLat = 37.8, minLon = -1.2, maxLat = 38.2, maxLon = -0.8)

    private fun makeResult(label: String, layer: String = "venue", lat: Double = 38.0, lon: Double = -1.0) =
        SearchResult(
            label = label,
            locality = "Murcia",
            region = "Region of Murcia",
            country = "Spain",
            confidence = 0.9,
            layer = layer,
            lat = lat,
            lon = lon
        )

    @Test
    fun `results are mapped correctly from SearchServiceResponse`() = runBlocking {
        val api = FakeSearchServiceApi(
            SearchServiceResponse(listOf(makeResult("Plaza Sandoval")))
        )
        val repo = LocationSearchRepositoryImpl(api, dispatcher, moshi)

        val result = repo.search("plaza sandoval", viewport)

        assertTrue(result is NetworkResult.Success)
        val items = (result as NetworkResult.Success).data
        assertEquals(1, items.size)
        assertEquals("Plaza Sandoval", items[0].label)
        assertEquals("Murcia", items[0].locality)
        assertEquals("Region of Murcia", items[0].region)
        assertEquals("Spain", items[0].country)
        assertEquals(0.9, items[0].confidence, 0.001)
        assertEquals("venue", items[0].layer)
        assertEquals(38.0, items[0].lat, 0.001)
        assertEquals(-1.0, items[0].lon, 0.001)
    }

    @Test
    fun `focusPoint is forwarded in the request when provided`() = runBlocking {
        val api = FakeSearchServiceApi()
        val repo = LocationSearchRepositoryImpl(api, dispatcher, moshi)

        repo.search("test", viewport, focusPoint = Coordinate(lat = 38.1, lon = -1.05))

        val req = api.lastRequest
        assertNotNull(req)
        assertNotNull(req!!.focusPoint)
        assertEquals(38.1, req.focusPoint!!.lat, 0.001)
        assertEquals(-1.05, req.focusPoint.lon, 0.001)
    }

    @Test
    fun `focusPoint is null in the request when not provided`() = runBlocking {
        val api = FakeSearchServiceApi()
        val repo = LocationSearchRepositoryImpl(api, dispatcher, moshi)

        repo.search("test", viewport)

        assertNull(api.lastRequest!!.focusPoint)
    }

    @Test
    fun `language is set from Locale`() = runBlocking {
        val api = FakeSearchServiceApi()
        val repo = LocationSearchRepositoryImpl(api, dispatcher, moshi)

        repo.search("test", viewport)

        assertEquals(Locale.getDefault().toLanguageTag(), api.lastRequest!!.language)
    }

    @Test
    fun `size is 5 in the request`() = runBlocking {
        val api = FakeSearchServiceApi()
        val repo = LocationSearchRepositoryImpl(api, dispatcher, moshi)

        repo.search("test", viewport)

        assertEquals(5, api.lastRequest!!.size)
    }

    @Test
    fun `network exception returns NetworkResult Exception`() = runBlocking {
        val api = FakeSearchServiceApi(throwOnCall = true)
        val repo = LocationSearchRepositoryImpl(api, dispatcher, moshi)

        val result = repo.search("test", viewport)

        assertTrue(result is NetworkResult.Exception)
    }

    @Test
    fun `empty results returns NetworkResult Success with empty list`() = runBlocking {
        val api = FakeSearchServiceApi(SearchServiceResponse(emptyList()))
        val repo = LocationSearchRepositoryImpl(api, dispatcher, moshi)

        val result = repo.search("test", viewport)

        assertTrue(result is NetworkResult.Success)
        assertTrue((result as NetworkResult.Success).data.isEmpty())
    }
}
