package com.example.smarttravel.ui.components

import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.util.Locale

@Composable
fun LocationPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, Double, String) -> Unit, // lat, lng, address
    initialLat: Double? = null,
    initialLng: Double? = null,
    initialAddress: String = ""
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedLat by remember { mutableStateOf<Double?>(initialLat) }
    var selectedLng by remember { mutableStateOf<Double?>(initialLng) }
    var selectedAddress by remember { mutableStateOf(initialAddress) }
    var addressInput by remember { mutableStateOf(initialAddress) }
    var isLoadingAddress by remember { mutableStateOf(false) }
    var isGeocoding by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    
    // Cấu hình Osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
        
        val osmdroidBasePath = File(context.cacheDir, "osmdroid")
        osmdroidBasePath.mkdirs()
        Configuration.getInstance().osmdroidBasePath = osmdroidBasePath
    }
    
    // Hàm geocoding để lấy tọa độ từ địa chỉ
    fun getLocationFromAddress(address: String) {
        if (address.isBlank()) return
        
        isGeocoding = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(address, 1)
                withContext(Dispatchers.Main) {
                    if (addresses != null && addresses.isNotEmpty()) {
                        val addressObj = addresses[0]
                        selectedLat = addressObj.latitude
                        selectedLng = addressObj.longitude
                        selectedAddress = address
                        
                        // Cập nhật bản đồ
                        mapView?.let { view ->
                            val geoPoint = GeoPoint(addressObj.latitude, addressObj.longitude)
                            view.controller.setCenter(geoPoint)
                            view.controller.setZoom(15.0)
                            
                            // Xóa marker cũ và thêm marker mới
                            val existingMarker = view.overlays.find { it is Marker } as? Marker
                            existingMarker?.let { view.overlays.remove(it) }
                            
                            val marker = Marker(view).apply {
                                position = geoPoint
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            view.overlays.add(marker)
                            view.invalidate()
                        }
                    } else {
                        android.util.Log.e("LocationPicker", "No location found for address: $address")
                    }
                    isGeocoding = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.util.Log.e("LocationPicker", "Error geocoding address: ${e.message}", e)
                    isGeocoding = false
                }
            }
        }
    }
    
    // Hàm reverse geocoding để lấy địa chỉ từ lat/lng
    fun getAddressFromLocation(lat: Double, lng: Double) {
        isLoadingAddress = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                withContext(Dispatchers.Main) {
                    if (addresses != null && addresses.isNotEmpty()) {
                        val addressObj = addresses[0]
                        val addressLines = mutableListOf<String>()
                        
                        for (i in 0..addressObj.maxAddressLineIndex) {
                            addressObj.getAddressLine(i)?.let { addressLines.add(it) }
                        }
                        
                        selectedAddress = if (addressLines.isNotEmpty()) {
                            addressLines.joinToString(", ")
                        } else {
                            val parts = listOfNotNull(
                                addressObj.featureName,
                                addressObj.thoroughfare,
                                addressObj.subLocality,
                                addressObj.locality,
                                addressObj.adminArea,
                                addressObj.countryName
                            )
                            parts.joinToString(", ")
                        }
                    } else {
                        selectedAddress = "$lat, $lng"
                    }
                    isLoadingAddress = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    selectedAddress = "$lat, $lng"
                    isLoadingAddress = false
                    android.util.Log.e("LocationPicker", "Error getting address: ${e.message}")
                }
            }
        }
    }
    
    // Load địa chỉ ban đầu nếu có tọa độ
    LaunchedEffect(selectedLat, selectedLng) {
        if (selectedLat != null && selectedLng != null && selectedAddress.isEmpty()) {
            getAddressFromLocation(selectedLat!!, selectedLng!!)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chọn vị trí",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Text field để nhập địa chỉ
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập địa chỉ (ví dụ: Đà Lạt, Lâm Đồng)") },
                    singleLine = true,
                    trailingIcon = {
                        if (isGeocoding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(
                                onClick = { 
                                    if (addressInput.isNotBlank()) {
                                        getLocationFromAddress(addressInput)
                                    }
                                },
                                enabled = addressInput.isNotBlank() && !isGeocoding
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Search,
                                    contentDescription = "Tìm kiếm"
                                )
                            }
                        }
                    }
                )
                
                var currentMarker by remember { mutableStateOf<Marker?>(null) }
                
                val onLocationSelected: (GeoPoint) -> Unit = { geoPoint ->
                    selectedLat = geoPoint.latitude
                    selectedLng = geoPoint.longitude
                    getAddressFromLocation(geoPoint.latitude, geoPoint.longitude)
                }
                
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            minZoomLevel = 3.0
                            maxZoomLevel = 19.0
                            
                            // Đặt vị trí ban đầu
                            if (initialLat != null && initialLng != null) {
                                controller.setZoom(15.0)
                                controller.setCenter(GeoPoint(initialLat, initialLng))
                                // Thêm marker ban đầu
                                val marker = Marker(this).apply {
                                    position = GeoPoint(initialLat, initialLng)
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                overlays.add(marker)
                                currentMarker = marker
                            } else {
                                controller.setZoom(15.0)
                                controller.setCenter(GeoPoint(10.762622, 106.660172)) // Hồ Chí Minh
                            }
                            
                            mapView = this
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    update = { view ->
                        val existingOverlay = view.overlays.find { it is MapEventsOverlay }
                        existingOverlay?.let { view.overlays.remove(it) }
                        
                        val mapEventsReceiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                p?.let { geoPoint ->
                                    currentMarker?.let { marker ->
                                        view.overlays.remove(marker)
                                    }
                                    
                                    val marker = Marker(view).apply {
                                        position = geoPoint
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    }
                                    view.overlays.add(marker)
                                    currentMarker = marker
                                    
                                    onLocationSelected(geoPoint)
                                    view.invalidate()
                                }
                                return true
                            }
                            
                            override fun longPressHelper(p: GeoPoint?): Boolean {
                                return false
                            }
                        }
                        
                        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
                        view.overlays.add(0, mapEventsOverlay)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Hiển thị địa chỉ đã chọn
                if (isLoadingAddress) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else if (selectedAddress.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Địa chỉ:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedAddress,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (selectedLat != null && selectedLng != null) {
                                Text(
                                    text = "Tọa độ: ${String.format("%.6f", selectedLat)}, ${String.format("%.6f", selectedLng)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedLat != null && selectedLng != null) {
                        onConfirm(selectedLat!!, selectedLng!!, selectedAddress)
                    }
                },
                enabled = selectedLat != null && selectedLng != null && !isLoadingAddress
            ) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

