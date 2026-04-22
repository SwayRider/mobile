package com.hevanto_it.swayrider.ui.screens

import android.Manifest
import android.graphics.Bitmap
import com.hevanto_it.swayrider.domain.search.Coordinate
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.hevanto_it.swayrider.BuildConfig
import com.hevanto_it.swayrider.domain.auth.AuthState
import com.hevanto_it.swayrider.domain.search.BoundingBox
import com.hevanto_it.swayrider.ui.components.LocationSearchBar
import com.hevanto_it.swayrider.ui.components.ProfileMenu
import com.hevanto_it.swayrider.viewmodel.AuthViewModel
import com.hevanto_it.swayrider.viewmodel.LocationSearchState
import com.hevanto_it.swayrider.viewmodel.LocationSearchViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

private const val SEARCH_MARKER_ICON = "search_result_marker"

private fun createSearchMarkerBitmap(): Bitmap {
    val size = 48
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    paint.color = android.graphics.Color.RED
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)
    return bitmap
}

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    locationSearchViewModel: LocationSearchViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapStyleName = if (isSystemInDarkTheme()) "dark" else "light"
    val styleUrl = "${BuildConfig.TILES_SERVICE_HOST}:${BuildConfig.TILES_SERVICE_PORT}${BuildConfig.TILES_SERVICE_PREFIX}/v1/tiles/styles/$mapStyleName"

    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var coordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleRef by remember { mutableStateOf<Style?>(null) }
    var centeredOnLocation by remember { mutableStateOf(false) }
    var symbolManager by remember { mutableStateOf<SymbolManager?>(null) }
    var searchSymbol by remember { mutableStateOf<Symbol?>(null) }

    val searchState by locationSearchViewModel.searchState.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // Show toast on network error from search
    LaunchedEffect(searchState) {
        if (searchState is LocationSearchState.Error) {
            Toast.makeText(context, (searchState as LocationSearchState.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!locationGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Enable the blue dot once the style is loaded and permission is granted
    LaunchedEffect(locationGranted, mapRef, styleRef) {
        val map = mapRef ?: return@LaunchedEffect
        val style = styleRef ?: return@LaunchedEffect
        if (!locationGranted) return@LaunchedEffect

        val locationComponent = map.locationComponent
        if (!locationComponent.isLocationComponentActivated) {
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(context, style)
                    .useDefaultLocationEngine(true)
                    .build()
            )
        }
        locationComponent.isLocationComponentEnabled = true
        locationComponent.cameraMode = CameraMode.NONE
        locationComponent.renderMode = RenderMode.NORMAL
    }

    // Animate camera to first real location once both map and coordinates are ready
    LaunchedEffect(coordinates, mapRef) {
        val map = mapRef ?: return@LaunchedEffect
        val (lat, lon) = coordinates ?: return@LaunchedEffect
        if (!centeredOnLocation) {
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(LatLng(lat, lon))
                        .zoom(9.0)
                        .build()
                )
            )
            centeredOnLocation = true
        }
    }

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply {
            val mv = this
            getMapAsync { map ->
                mapRef = map
                map.setStyle(styleUrl) { style ->
                    styleRef = style
                    style.addImage(SEARCH_MARKER_ICON, createSearchMarkerBitmap())
                    symbolManager = SymbolManager(mv, map, style)
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationListener = LocationListener { location: Location ->
            coordinates = Pair(location.latitude, location.longitude)
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    if (ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        try {
                            locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, 1000L, 1f, locationListener
                            )
                            locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER, 1000L, 1f, locationListener
                            )
                            val initial =
                                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                            if (initial != null) coordinates = Pair(initial.latitude, initial.longitude)
                        } catch (e: Exception) { /* permission revoked */ }
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapView.onPause()
                    locationManager.removeUpdates(locationListener)
                }
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            locationManager.removeUpdates(locationListener)
            mapView.onDestroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    LocationSearchBar(
                        state = searchState,
                        onSearch = { query ->
                            val region = mapRef?.projection?.visibleRegion
                            if (region != null) {
                                val corners = listOfNotNull(
                                    region.nearLeft, region.nearRight,
                                    region.farLeft, region.farRight
                                )
                                if (corners.size == 4) {
                                    val lats = corners.map { it.latitude }
                                    val lons = corners.map { it.longitude }
                                    locationSearchViewModel.search(
                                        query,
                                        BoundingBox(
                                            minLat = lats.min(),
                                            minLon = lons.min(),
                                            maxLat = lats.max(),
                                            maxLon = lons.max()
                                        ),
                                        focusPoint = coordinates?.let { Coordinate(it.first, it.second) }
                                    )
                                }
                            }
                        },
                        onDismiss = { locationSearchViewModel.dismiss() }
                    )
                },
                onDismiss = { locationSearchViewModel.dismiss() },
                onResultSelected = { result ->
                    val map = mapRef ?: return@LocationSearchBar
                    // Animate camera first, so it always runs regardless of marker state
                    val zoom = when (result.layer) {
                        "venue", "address", "street" -> 16.0
                        "neighbourhood", "localadmin", "locality" -> 14.0
                        "county", "macrocounty" -> 11.0
                        "region", "macroregion" -> 8.0
                        else -> 5.0 // country, continent
                    }
                    map.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(LatLng(result.lat, result.lon))
                                .zoom(zoom)
                                .build()
                        )
                    )
                    // Remove existing marker and place new one
                    searchSymbol?.let { symbolManager?.delete(it) }
                    searchSymbol = symbolManager?.create(
                        SymbolOptions()
                            .withLatLng(LatLng(result.lat, result.lon))
                            .withIconImage(SEARCH_MARKER_ICON)
                    )
                },
                actions = {
                    if (authState is AuthState.Authenticated) {
                        ProfileMenu(authViewModel)
                    }
                }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { mapView }
            )

            // Results dropdown overlaid on top of the map
            val currentResults = searchState as? LocationSearchState.Results
            if (currentResults != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 4.dp
                    ) {
                        LazyColumn {
                            items(currentResults.items) { result ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val map = mapRef ?: return@clickable
                                            searchSymbol?.let { symbolManager?.delete(it) }
                                            searchSymbol = symbolManager?.create(
                                                SymbolOptions().withLatLng(LatLng(result.lat, result.lon))
                                            )
                                            val zoom = when (result.layer) {
                                                "venue", "address", "street" -> 16.0
                                                "neighbourhood", "localadmin", "locality" -> 14.0
                                                "county", "macrocounty" -> 11.0
                                                "region", "macroregion" -> 8.0
                                                else -> 5.0
                                            }
                                            map.animateCamera(
                                                CameraUpdateFactory.newCameraPosition(
                                                    CameraPosition.Builder()
                                                        .target(LatLng(result.lat, result.lon))
                                                        .zoom(zoom)
                                                        .build()
                                                )
                                            )
                                            locationSearchViewModel.dismiss()
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = result.label,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    val secondary = listOfNotNull(
                                        result.locality ?: result.region,
                                        result.country
                                    ).joinToString(", ")
                                    if (secondary.isNotEmpty()) {
                                        Text(
                                            text = secondary,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            // Transparent backdrop: captures outside taps to dismiss the dropdown
            if (currentResults != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { locationSearchViewModel.dismiss() }
                )
            }

            coordinates?.let { (lat, lon) ->
                val latDir = if (lat >= 0) "N" else "S"
                val lonDir = if (lon >= 0) "E" else "W"
                Text(
                    text = "%.5f° %s, %.5f° %s".format(Math.abs(lat), latDir, Math.abs(lon), lonDir),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
