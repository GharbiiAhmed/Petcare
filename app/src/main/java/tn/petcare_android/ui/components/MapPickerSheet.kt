package tn.petcare_android.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import tn.petcare_android.ui.theme.*
import java.util.Locale

/**
 * Map Picker Sheet for selecting location
 * iOS Reference: MapPickerSheet in JoinPetSitterView.swift and JoinVetView.swift
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerSheet(
    initialCoordinate: Pair<Double, Double> = Pair(10.1815, 36.8065), // Default: Tunis
    onConfirm: (Pair<Double, Double>, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedCoordinate by remember { mutableStateOf(initialCoordinate) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isGeocoding by remember { mutableStateOf(false) }
    var addressText by remember { mutableStateOf<String?>(null) }
    
    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation(context) { location ->
                location?.let {
                    selectedCoordinate = Pair(it.longitude, it.latitude)
                    mapView?.mapboxMap?.setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(it.longitude, it.latitude))
                            .zoom(15.0)
                            .build()
                    )
                    reverseGeocode(context, it.latitude, it.longitude) { address ->
                        addressText = address
                    }
                }
            }
        }
    }
    
    // Reverse geocode when coordinate changes
    LaunchedEffect(selectedCoordinate) {
        reverseGeocode(context, selectedCoordinate.second, selectedCoordinate.first) { address ->
            addressText = address
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f),
        containerColor = PageBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Location",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimary
                    )
                }
            }
            
            // Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).also { mv ->
                            mapView = mv
                            mv.mapboxMap.setCamera(
                                CameraOptions.Builder()
                                    .center(Point.fromLngLat(selectedCoordinate.first, selectedCoordinate.second))
                                    .zoom(15.0)
                                    .build()
                            )
                            
                            mv.mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
                                val annotationManager = mv.annotations.createPointAnnotationManager()
                                
                                // Add marker at selected location
                                annotationManager.create(
                                    PointAnnotationOptions()
                                        .withPoint(Point.fromLngLat(selectedCoordinate.first, selectedCoordinate.second))
                                        .withIconImage(android.graphics.Bitmap.createBitmap(48, 48, android.graphics.Bitmap.Config.ARGB_8888).apply {
                                            val canvas = android.graphics.Canvas(this)
                                            val paint = android.graphics.Paint().apply {
                                                color = android.graphics.Color.parseColor("#FF6B35")
                                                isAntiAlias = true
                                            }
                                            canvas.drawCircle(24f, 24f, 20f, paint)
                                        })
                                )
                                
                                // Update marker on map drag
                                mv.mapboxMap.addOnMapIdleListener {
                                    val center = mv.mapboxMap.cameraState.center
                                    selectedCoordinate = Pair(center.longitude(), center.latitude())
                                    
                                    // Update marker position
                                    annotationManager.deleteAll()
                                    annotationManager.create(
                                        PointAnnotationOptions()
                                            .withPoint(Point.fromLngLat(center.longitude(), center.latitude()))
                                            .withIconImage(android.graphics.Bitmap.createBitmap(48, 48, android.graphics.Bitmap.Config.ARGB_8888).apply {
                                                val canvas = android.graphics.Canvas(this)
                                                val paint = android.graphics.Paint().apply {
                                                    color = android.graphics.Color.parseColor("#FF6B35")
                                                    isAntiAlias = true
                                                }
                                                canvas.drawCircle(24f, 24f, 20f, paint)
                                            })
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Center pin indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = VetCanyon,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            // Address display
            if (addressText != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = VetCanyon,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = addressText ?: "Loading address...",
                            fontSize = 14.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Use Current Location button
                OutlinedButton(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        
                        if (hasPermission) {
                            getCurrentLocation(context) { location ->
                                location?.let {
                                    selectedCoordinate = Pair(it.longitude, it.latitude)
                                    mapView?.mapboxMap?.setCamera(
                                        CameraOptions.Builder()
                                            .center(Point.fromLngLat(it.longitude, it.latitude))
                                            .zoom(15.0)
                                            .build()
                                    )
                                    reverseGeocode(context, it.latitude, it.longitude) { address ->
                                        addressText = address
                                    }
                                }
                            }
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = VetCanyon
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use Current", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                
                // Confirm button
                Button(
                    onClick = {
                        onConfirm(selectedCoordinate, addressText)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = VetCanyon)
                ) {
                    Text("Use This Location", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Get current location using LocationManager
 */
private fun getCurrentLocation(
    context: Context,
    onLocationResult: (android.location.Location?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            if (locationManager != null && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                
                withContext(Dispatchers.Main) {
                    onLocationResult(location)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onLocationResult(null)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onLocationResult(null)
            }
        }
    }
}

/**
 * Reverse geocode coordinates to address
 */
private fun reverseGeocode(
    context: Context,
    latitude: Double,
    longitude: Double,
    onResult: (String?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            
            withContext(Dispatchers.Main) {
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressParts = mutableListOf<String>()
                    
                    address.getAddressLine(0)?.let { addressParts.add(it) }
                    address.locality?.let { addressParts.add(it) }
                    address.adminArea?.let { addressParts.add(it) }
                    address.countryName?.let { addressParts.add(it) }
                    
                    onResult(addressParts.joinToString(", "))
                } else {
                    onResult("${latitude}, ${longitude}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult("${latitude}, ${longitude}")
            }
        }
    }
}
