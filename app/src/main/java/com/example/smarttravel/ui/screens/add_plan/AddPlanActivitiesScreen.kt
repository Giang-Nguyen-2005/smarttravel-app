package com.example.smarttravel.ui.screens.add_plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.viewmodel.ManualPlanViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DayActivities(
    val day: Int,
    val date: LocalDate,
    val activities: MutableList<ActivityData> = mutableListOf()
)

data class ActivityData(
    val time: String = "",
    val name: String = "",
    val category: String = "",
    val address: String = "",
    val note: String = ""
)

@Composable
fun AddPlanActivitiesScreen(
    navController: NavController,
    startDate: String? = null,
    endDate: String? = null,
    viewModel: ManualPlanViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("vi"))
    
    val start = remember(startDate) {
        startDate?.let { LocalDate.parse(it, dateFormatter) } ?: LocalDate.now()
    }
    val end = remember(endDate) {
        endDate?.let { LocalDate.parse(it, dateFormatter) } ?: start
    }
    
    // Khởi tạo plan khi có startDate và endDate
    LaunchedEffect(startDate, endDate) {
        if (startDate != null && endDate != null) {
            // Chỉ khởi tạo nếu days list rỗng hoặc chưa được khởi tạo
            if (uiState.days.isEmpty() || uiState.startDate != start || uiState.endDate != end) {
                viewModel.initializePlan(start, end)
            }
        }
    }
    
    var planTitle by remember { mutableStateOf("") }
    
    // Tạo default title
    LaunchedEffect(start, end, uiState.title) {
        if (planTitle.isEmpty() && uiState.title.isEmpty()) {
            planTitle = if (start == end) {
                "Kế hoạch cho ngày ${start.format(displayFormatter)}"
            } else {
                "Kế hoạch cho ngày ${start.format(displayFormatter)} -> ${end.format(displayFormatter)}"
            }
            viewModel.setTitle(planTitle)
        } else if (uiState.title.isNotEmpty()) {
            planTitle = uiState.title
        }
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
                            .background(colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Thêm kế hoạch",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorScheme.background)
        ) {
            // Input tên kế hoạch
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tên kế hoạch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = planTitle,
                        onValueChange = { 
                            planTitle = it
                            viewModel.setTitle(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập tên kế hoạch") },
                        singleLine = true
                    )
                }
            }
            
            // Danh sách ngày
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(uiState.days) { index, dayData ->
                    // Log để debug
                    LaunchedEffect(dayData.activities.size) {
                        android.util.Log.d("AddPlanActivities", "Day $index has ${dayData.activities.size} activities")
                    }
                    DayActivitiesCard(
                        dayData = dayData,
                        displayFormatter = displayFormatter,
                        onAddActivity = {
                            val startDateStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val endDateStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            navController.navigate(
                                Screen.AddActivityForm.createRoute(index, startDateStr, endDateStr)
                            )
                        },
                        onActivityClick = { activityIndex ->
                            val startDateStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val endDateStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            navController.navigate(
                                Screen.AddActivityForm.createRoute(index, startDateStr, endDateStr)
                            )
                        },
                        onDeleteActivity = { activityIndex ->
                            viewModel.deleteActivity(index, activityIndex)
                        }
                    )
                }
            }
            
            // Hiển thị lỗi nếu có
            if (uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Nút lưu
            Button(
                onClick = {
                    viewModel.savePlan(
                        onSuccess = { planId ->
                            // Navigate đến PlanDetailScreen để xem chi tiết kế hoạch vừa tạo
                            navController.navigate(Screen.PlanDetail.createRoute(planId)) {
                                // Xóa toàn bộ back stack từ Calendar trở đi
                                popUpTo(Screen.Calendar.route) { inclusive = false }
                            }
                        },
                        onError = { error ->
                            // Lỗi đã được hiển thị trong uiState.error
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (uiState.isLoading) "Đang lưu..." else "Lưu kế hoạch",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun DayActivitiesCard(
    dayData: DayActivities,
    displayFormatter: DateTimeFormatter,
    onAddActivity: () -> Unit,
    onActivityClick: (Int) -> Unit,
    onDeleteActivity: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ngày thứ ${dayData.day}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dayData.date.format(displayFormatter),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (dayData.activities.isEmpty()) {
                Text(
                    text = "Chưa có hoạt động nào",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                dayData.activities.forEachIndexed { index, activity ->
                    ActivityItem(
                        activity = activity,
                        onClick = { onActivityClick(index) },
                        onDelete = { onDeleteActivity(index) }
                    )
                    if (index < dayData.activities.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Nút thêm hoạt động
            OutlinedButton(
                onClick = onAddActivity,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm hoạt động")
            }
        }
    }
}

@Composable
fun ActivityItem(
    activity: ActivityData,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (activity.time.isNotEmpty()) {
                    Text(
                        text = activity.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = activity.name.ifEmpty { "Hoạt động chưa có tên" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface
                )
                if (activity.address.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activity.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = { onDelete() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

