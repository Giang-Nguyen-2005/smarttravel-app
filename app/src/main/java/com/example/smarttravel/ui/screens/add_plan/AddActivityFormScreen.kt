package com.example.smarttravel.ui.screens.add_plan

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.smarttravel.ui.viewmodel.ManualPlanViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.events.MapEvent
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ActivityCategory(
    val id: String,
    val name: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityFormScreen(
    navController: NavController,
    dayIndex: Int,
    viewModel: ManualPlanViewModel,
    startDate: String? = null,
    endDate: String? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    // Khởi tạo plan nếu chưa được khởi tạo
    LaunchedEffect(startDate, endDate, uiState.days.isEmpty()) {
        if (startDate != null && endDate != null && uiState.days.isEmpty()) {
            val start = LocalDate.parse(startDate, dateFormatter)
            val end = LocalDate.parse(endDate, dateFormatter)
            viewModel.initializePlan(start, end)
            android.util.Log.d("AddActivityForm", "Initialized plan with startDate=$startDate, endDate=$endDate")
        }
    }
    
    var time by remember { mutableStateOf("") }
    var activityName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    
    // Parse time từ string để khởi tạo time picker
    val initialTime = remember(time) {
        if (time.isNotEmpty()) {
            try {
                val parts = time.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toIntOrNull() ?: 12
                    val minute = parts[1].toIntOrNull() ?: 0
                    Pair(hour, minute)
                } else {
                    Pair(12, 0)
                }
            } catch (e: Exception) {
                Pair(12, 0)
            }
        } else {
            Pair(12, 0)
        }
    }
    
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.first,
        initialMinute = initialTime.second,
        is24Hour = true
    )
    
    val categories = remember {
        listOf(
            ActivityCategory("activity", "Hoạt động", Icons.Default.Explore),
            ActivityCategory("hotel", "Khách sạn", Icons.Default.Bed),
            ActivityCategory("food", "Ăn Uống", Icons.Default.Restaurant),
            ActivityCategory("walking", "Đi dạo", Icons.Default.DirectionsWalk),
            ActivityCategory("photo", "Chụp hình", Icons.Default.CameraAlt)
        )
    }
    
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .statusBarsPadding()
                        .padding(top = 8.dp)
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(colorScheme.surfaceVariant, CircleShape)
                            .align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Thêm hoạt động",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Thời gian
            item {
                Column {
                    Text(
                        text = "Thời gian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val isTimeSelected = time.isNotEmpty()
                        
                        OutlinedTextField(
                            value = time.ifEmpty { "" },
                            onValueChange = { },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Chọn thời gian") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Schedule, 
                                    contentDescription = null,
                                    tint = if (isTimeSelected) colorScheme.onSurface else Color.Unspecified
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.AccessTime, 
                                    contentDescription = "Chọn thời gian",
                                    tint = if (isTimeSelected) colorScheme.onSurface else Color.Unspecified
                                )
                            },
                            readOnly = true,
                            enabled = true,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                                focusedBorderColor = colorScheme.outline,
                                unfocusedBorderColor = colorScheme.outline,
                                focusedTextColor = if (isTimeSelected) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                                unfocusedTextColor = if (isTimeSelected) colorScheme.onSurface else colorScheme.onSurfaceVariant
                            )
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showTimePicker = true }
                        )
                    }
                    Text(
                        text = "Nhấn vào ô để chọn thời gian",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Tên hoạt động
            item {
                Column {
                    Text(
                        text = "Tên hoạt động",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = activityName,
                        onValueChange = { activityName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập tên hoạt động") },
                        singleLine = true
                    )
                }
            }
            
            // Danh mục
            item {
                Column {
                    Text(
                        text = "Danh mục",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            CategoryChip(
                                category = category,
                                isSelected = selectedCategory == category.id,
                                onClick = { selectedCategory = category.id }
                            )
                        }
                    }
                }
            }
            
            // Địa chỉ
            item {
                Column {
                    Text(
                        text = "Địa chỉ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Nhập địa chỉ") },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null)
                            },
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                // Hiển thị dialog map để chọn vị trí
                                showLocationPicker = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = colorScheme.onPrimary
                            )
                        }
                    }
                    Text(
                        text = "Nhấn nút bản đồ để mở Google Maps và chọn vị trí",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Ghi chú
            item {
                Column {
                    Text(
                        text = "Ghi chú (tùy chọn)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Nhập ghi chú (nếu có)") },
                        maxLines = 5
                    )
                }
            }
            
            // Nút Hủy và Lưu
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hủy")
                    }
                    Button(
                        onClick = {
                            if (activityName.isNotEmpty() && selectedCategory != null) {
                                val activity = ActivityData(
                                    time = time,
                                    name = activityName,
                                    category = selectedCategory!!,
                                    address = address,
                                    note = note
                                )
                                android.util.Log.d("AddActivityForm", "Saving activity: name=${activity.name}, dayIndex=$dayIndex")
                                
                                // Kiểm tra state trước khi thêm
                                val currentState = viewModel.uiState.value
                                android.util.Log.d("AddActivityForm", "Current state: days.size=${currentState.days.size}, dayIndex=$dayIndex")
                                
                                if (currentState.days.isEmpty()) {
                                    android.util.Log.e("AddActivityForm", "Days list is empty! Cannot add activity.")
                                } else if (dayIndex >= currentState.days.size) {
                                    android.util.Log.e("AddActivityForm", "Invalid dayIndex: $dayIndex >= ${currentState.days.size}")
                                } else {
                                    viewModel.addActivity(dayIndex, activity)
                                    navController.popBackStack()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = activityName.isNotEmpty() && selectedCategory != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Lưu")
                    }
                }
            }
        }
        
        // Time Picker Dialog - hiển thị bên ngoài LazyColumn
        if (showTimePicker) {
            TimePickerDialog(
                onDismiss = { showTimePicker = false },
                onConfirm = {
                    val selectedHour = timePickerState.hour
                    val selectedMinute = timePickerState.minute
                    time = String.format("%02d:%02d", selectedHour, selectedMinute)
                    showTimePicker = false
                },
                timePickerState = timePickerState
            )
        }
        
        // Location Picker Dialog
        if (showLocationPicker) {
            LocationPickerDialog(
                onDismiss = { showLocationPicker = false },
                onConfirm = { selectedAddress ->
                    address = selectedAddress
                    showLocationPicker = false
                }
            )
        }
    }
}

@Composable
fun CategoryChip(
    category: ActivityCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant
    val contentColor = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface
    
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            ),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = category.name,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun LocationPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedLat by remember { mutableStateOf<Double?>(null) }
    var selectedLng by remember { mutableStateOf<Double?>(null) }
    var selectedAddress by remember { mutableStateOf("") }
    var isLoadingAddress by remember { mutableStateOf(false) }
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
                        
                        // Lấy địa chỉ từ getAddressLine
                        for (i in 0..addressObj.maxAddressLineIndex) {
                            addressObj.getAddressLine(i)?.let { addressLines.add(it) }
                        }
                        
                        selectedAddress = if (addressLines.isNotEmpty()) {
                            addressLines.joinToString(", ")
                        } else {
                            // Fallback: ghép các thành phần địa chỉ
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Map View
                var currentMarker by remember { mutableStateOf<Marker?>(null) }
                
                // Callback để xử lý khi chọn vị trí
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
                            
                            // Đặt vị trí mặc định (Việt Nam)
                            controller.setZoom(15.0)
                            controller.setCenter(GeoPoint(10.762622, 106.660172)) // Tọa độ Hồ Chí Minh
                            
                            mapView = this
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    update = { view ->
                        // Xóa overlay cũ nếu có
                        val existingOverlay = view.overlays.find { it is MapEventsOverlay }
                        existingOverlay?.let { view.overlays.remove(it) }
                        
                        // Thêm MapEventsOverlay để xử lý click
                        val mapEventsReceiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                p?.let { geoPoint ->
                                    // Xóa marker cũ
                                    currentMarker?.let { marker ->
                                        view.overlays.remove(marker)
                                    }
                                    
                                    // Tạo marker mới
                                    val marker = Marker(view).apply {
                                        position = geoPoint
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    }
                                    view.overlays.add(marker)
                                    currentMarker = marker
                                    
                                    // Gọi callback để cập nhật state
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Đang lấy địa chỉ...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (selectedAddress.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Địa chỉ đã chọn:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedAddress,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Nhấn vào bản đồ để chọn vị trí",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedAddress.isNotEmpty()) {
                        onConfirm(selectedAddress)
                    }
                },
                enabled = selectedAddress.isNotEmpty() && !isLoadingAddress,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    timePickerState: androidx.compose.material3.TimePickerState
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chọn thời gian",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialSelectedContentColor = Color(0xFF000000), // Màu xanh cyan
                        clockDialColor = Color(0xFF9BDEFD), // Màu xanh cyan nhạt cho nền
                        selectorColor = Color(0xFFFFFFFF), // Màu xanh cyan cho selector
                        periodSelectorBorderColor = Color(0xFF000000), // Màu xanh cyan cho border
                        timeSelectorSelectedContainerColor = Color(0xFF9BDEFD), // Màu nền xanh cyan nhạt cho giờ/phút đã chọn
                        timeSelectorUnselectedContainerColor = Color(0xFFFFFFFF), // Màu nền xám nhạt cho giờ/phút chưa chọn
                        timeSelectorSelectedContentColor = Color(0xFF000000), // Màu text xanh cyan cho giờ/phút đã chọn
                        timeSelectorUnselectedContentColor = Color(0xFF000000) // Màu text xám cho giờ/phút chưa chọn
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
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

