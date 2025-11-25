package com.example.smarttravel.ui.screens.add_plan

import android.content.Intent
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
import androidx.navigation.NavController
import com.example.smarttravel.ui.viewmodel.ManualPlanViewModel
import kotlinx.coroutines.delay
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .statusBarsPadding()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE0E0E0), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF1A1A1A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Thêm hoạt động",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Thời gian
            item {
                Column {
                    Text(
                        text = "Thời gian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = time.ifEmpty { "" },
                            onValueChange = { },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Chọn thời gian") },
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                            },
                            trailingIcon = {
                                Icon(Icons.Default.AccessTime, contentDescription = "Chọn thời gian")
                            },
                            readOnly = true,
                            enabled = false,
                            singleLine = true,
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
                        color = Color.Gray,
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
                        fontWeight = FontWeight.Bold
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
                        fontWeight = FontWeight.Bold
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
                        fontWeight = FontWeight.Bold
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
                                // Mở Google Maps để chọn địa chỉ
                                openGoogleMapsForLocation(context)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = "Nhấn nút bản đồ để mở Google Maps và chọn vị trí",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
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
                        fontWeight = FontWeight.Bold
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
    }
}

@Composable
fun CategoryChip(
    category: ActivityCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF5F5F5)
    val contentColor = if (isSelected) Color.White else Color.Black
    
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

private fun openGoogleMapsForLocation(context: android.content.Context) {
    try {
        // Mở Google Maps ở chế độ chọn vị trí
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Nếu không có Google Maps, mở trình duyệt
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps"))
            context.startActivity(browserIntent)
        }
    } catch (e: Exception) {
        android.util.Log.e("AddActivityForm", "Error opening Google Maps: ${e.message}")
    }
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
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.primary,
                        clockDialColor = MaterialTheme.colorScheme.primaryContainer,
                        selectorColor = MaterialTheme.colorScheme.primary,
                        periodSelectorBorderColor = MaterialTheme.colorScheme.primary
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

