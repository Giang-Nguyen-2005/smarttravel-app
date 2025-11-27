package com.example.smarttravel.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File

/**
 * Component hiển thị bản đồ OpenStreetMap với một địa điểm
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OpenStreetMapView(
    latitude: Double,
    longitude: Double,
    locationName: String = "",
    modifier: Modifier = Modifier,
    showOpenExternalButton: Boolean = true
) {
    val context = LocalContext.current
    
    // Cấu hình Osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
        
        // Cấu hình thư mục cache
        val osmdroidBasePath = File(context.cacheDir, "osmdroid")
        osmdroidBasePath.mkdirs()
        Configuration.getInstance().osmdroidBasePath = osmdroidBasePath
    }
    
    // Yêu cầu quyền truy cập vị trí
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
    
    if (permissionsState.allPermissionsGranted) {
        Box(modifier = modifier) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        minZoomLevel = 3.0
                        maxZoomLevel = 19.0
                        
                        // Đặt vị trí và zoom
                        controller.setZoom(15.0)
                        controller.setCenter(GeoPoint(latitude, longitude))
                        
                        // Thêm marker
                        val marker = Marker(this).apply {
                            position = GeoPoint(latitude, longitude)
                            title = locationName.ifEmpty { "Địa điểm" }
                            snippet = "Lat: $latitude, Lng: $longitude"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        overlays.add(marker)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
            
            // Nút mở ứng dụng bản đồ bên ngoài
            if (showOpenExternalButton) {
                FloatingActionButton(
                    onClick = {
                        openInExternalMap(context, latitude, longitude, locationName)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Mở trong ứng dụng bản đồ",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cần cấp quyền để hiển thị bản đồ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                    Text("Cấp quyền")
                }
            }
        }
    }
}

/**
 * Component hiển thị bản đồ với nhiều điểm trong lịch trình du lịch
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TravelPlanMapView(
    destinations: List<MapDestination>,
    modifier: Modifier = Modifier,
    showRoute: Boolean = true
) {
    val context = LocalContext.current
    
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
    
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
    
    if (permissionsState.allPermissionsGranted && destinations.isNotEmpty()) {
        // Lấy màu primary từ MaterialTheme trong context composable
        val primaryColor = MaterialTheme.colorScheme.primary
        
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    minZoomLevel = 3.0
                    maxZoomLevel = 19.0
                    
                    // Tính toán bounds để fit tất cả điểm
                    val points = destinations.map { 
                        GeoPoint(it.latitude, it.longitude) 
                    }
                    
                    // Thêm marker cho mỗi điểm
                    destinations.forEachIndexed { index, dest ->
                        val marker = Marker(this).apply {
                            position = GeoPoint(dest.latitude, dest.longitude)
                            title = "${index + 1}. ${dest.name}"
                            snippet = dest.location
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        overlays.add(marker)
                    }
                    
                    // Vẽ route nếu có nhiều hơn 1 điểm
                    if (showRoute && destinations.size > 1) {
                        val routePoints = destinations.map { 
                            GeoPoint(it.latitude, it.longitude) 
                        }
                        val polyline = Polyline().apply {
                            setPoints(routePoints)
                            color = primaryColor.hashCode()
                            width = 8f
                        }
                        overlays.add(polyline)
                    }
                    
                    // Zoom để hiển thị tất cả điểm
                    controller.setZoom(12.0)
                    if (points.isNotEmpty()) {
                        val centerLat = points.map { it.latitude }.average()
                        val centerLng = points.map { it.longitude }.average()
                        controller.setCenter(GeoPoint(centerLat, centerLng))
                    }
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .height(400.dp)
        )
    } else if (destinations.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(400.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Không có địa điểm để hiển thị",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(400.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cần cấp quyền để hiển thị bản đồ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                    Text("Cấp quyền")
                }
            }
        }
    }
}

/**
 * Data class cho địa điểm trên bản đồ
 */
data class MapDestination(
    val name: String,
    val location: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Mở địa điểm trong ứng dụng bản đồ bên ngoài (Google Maps, Waze, etc.)
 */
private fun openInExternalMap(
    context: Context,
    latitude: Double,
    longitude: Double,
    label: String
) {
    try {
        // Chỉ dùng tọa độ trong query, không kèm label/tên
        // Format 1: Google Maps với geo URI (chuẩn nhất) - chỉ dùng tọa độ
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        
        // Thử mở Google Maps app trước
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
            return
        }
        
        // Format 2: Google Maps URL (fallback nếu app không có) - chỉ dùng tọa độ
        val googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
        val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUrl)).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        if (urlIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(urlIntent)
            return
        }
        
        // Format 3: Generic geo URI (mở bất kỳ app bản đồ nào hỗ trợ geo URI)
        val fallbackIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        if (fallbackIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(fallbackIntent)
            return
        }
        
        // Format 4: Mở trong trình duyệt web (fallback cuối cùng)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
        
    } catch (e: Exception) {
        android.util.Log.e("MapView", "Error opening map: ${e.message}", e)
        // Có thể thêm Toast hoặc Snackbar để thông báo lỗi cho user
    }
}

