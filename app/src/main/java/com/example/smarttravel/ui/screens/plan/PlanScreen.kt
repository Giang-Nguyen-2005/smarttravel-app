package com.example.smarttravel.ui.screens.schedule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.smarttravel.R
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.screens.home.HomeTopBar
import com.example.smarttravel.ui.theme.SmarttravelTheme
import com.example.smarttravel.ui.viewmodel.PlanListViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun PlanScreen(
    navController: NavController,
    viewModel: PlanListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showAllPlans by remember { mutableStateOf(false) }
    
    // Filter plans theo ngày được chọn hoặc hiển thị tất cả
    val filteredPlans = remember(uiState.plans, selectedDate, showAllPlans) {
        if (showAllPlans) {
            uiState.plans
        } else {
            uiState.plans.filter { plan ->
                if (plan.startDate == null || plan.endDate == null) return@filter false
                
                val start = plan.startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val end = plan.endDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                
                // Kiểm tra xem selectedDate có nằm trong khoảng [start, end] không
                !selectedDate.isBefore(start) && !selectedDate.isAfter(end)
            }
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
                        text = "Kế hoạch",
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
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {

            // Lịch dạng tuần (horizontal)
            item {
                WeeklyCalendarSection(
                    selectedDate = selectedDate,
                    onDateSelected = { 
                        selectedDate = it
                        showAllPlans = false // Tự động chuyển về chế độ filter theo ngày khi chọn ngày mới
                    }
                )
            }

            item {
                PlanListHeader(
                    filteredCount = filteredPlans.size,
                    totalCount = uiState.plans.size,
                    selectedDate = selectedDate,
                    showAllPlans = showAllPlans,
                    onViewAllClick = { showAllPlans = !showAllPlans }
                )
            }

            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                uiState.error != null -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Lỗi: ${uiState.error}",
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.loadPlans() }) {
                                    Text("Thử lại")
                                }
                            }
                        }
                    }
                }
                filteredPlans.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (uiState.plans.isEmpty()) {
                                        "Chưa có kế hoạch nào"
                                    } else {
                                        "Không có kế hoạch cho ngày ${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
                else -> {
                    items(filteredPlans) { plan ->
                        PlanItemCard(
                            plan = plan,
                            onClick = {
                                navController.navigate(Screen.PlanDetail.createRoute(plan.id))
                            },
                            onDelete = {
                                viewModel.deletePlan(
                                    planId = plan.id,
                                    onSuccess = {
                                        // Plan sẽ tự động được xóa khỏi danh sách nhờ Flow
                                    },
                                    onError = { error ->
                                        // Có thể hiển thị Toast hoặc Snackbar
                                        android.util.Log.e("PlanScreen", "Error deleting plan: $error")
                                    }
                                )
                            },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyCalendarSection(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    // Lấy ngày đầu tuần (Thứ Hai) của tuần chứa ngày được chọn
    val startOfWeek = selectedDate.with(DayOfWeek.MONDAY)
    val daysOfWeek = remember(startOfWeek) { // Chỉ tính lại khi startOfWeek thay đổi
        (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }
    // Lấy tháng và năm của ngày đầu tuần để hiển thị
    val displayMonthYear = startOfWeek.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi")))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("vi")) else it.toString() } // Viết hoa chữ cái đầu

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp) // Giảm padding dọc
            .clip(RoundedCornerShape(16.dp)) // Bo góc mềm mại hơn
            .background(Color(0xFFF1F7F8)) // Nền xám nhạt cho cả khu vực lịch
            .padding(16.dp)

    ) {
        // Header (Tháng Năm và nút điều hướng)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayMonthYear, // Hiển thị tháng/năm của tuần hiện tại
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold // Giảm độ đậm
            )
            Row {
                // Nút tuần trước
                IconButton(
                    onClick = { onDateSelected(selectedDate.minusWeeks(1)) }, // Lùi lại 1 tuần
                    modifier = Modifier.size(36.dp) // Giảm kích thước nút
                ) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Tuần trước", modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(4.dp)) // Khoảng cách giữa 2 nút
                // Nút tuần sau
                IconButton(
                    onClick = { onDateSelected(selectedDate.plusWeeks(1)) }, // Tiến tới 1 tuần
                    modifier = Modifier.size(36.dp) // Giảm kích thước nút
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Tuần sau", modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Tăng khoảng cách

        // Hàng chứa các ngày trong tuần
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween // Dàn đều các ngày
        ) {
            daysOfWeek.forEach { date ->
                val isSelected = date == selectedDate
                val isToday = date == today

                // Composable cho một ngày
                DateItem(
                    date = date,
                    isSelected = isSelected,
                    isToday = isToday,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

// Composable riêng cho một item ngày, giúp dễ tùy chỉnh hơn
@Composable
fun DateItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary // Màu xanh khi chọn
        else -> Color.Transparent // Nền trong suốt
    }
    val contentColor = when {
        isSelected -> Color.White // Chữ trắng khi chọn
        isToday -> MaterialTheme.colorScheme.primary // Chữ xanh cho ngày hôm nay
        else -> Color.Black // Chữ đen cho ngày thường
    }
    val dayOfWeekColor = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(width = 45.dp, height = 60.dp) // Kích thước cố định cho mỗi item
            .clip(RoundedCornerShape(12.dp)) // Bo góc mềm mại
            .background(backgroundColor)
            .border( // Thêm viền cho ngày hôm nay (nếu không được chọn)
                width = if (isToday && !isSelected) 1.dp else 0.dp,
                color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 4.dp) // Giảm padding dọc bên trong item
    ) {
        // Thứ (T2, T3...)
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("vi"))
                .replaceFirstChar { it.uppercase() },
            fontSize = 12.sp,
            color = dayOfWeekColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Ngày (18, 19...)
        Text(
            text = date.dayOfMonth.toString(),
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp,
            color = contentColor
        )
    }
}

@Composable
private fun PlanListHeader(
    filteredCount: Int,
    totalCount: Int,
    selectedDate: LocalDate,
    showAllPlans: Boolean,
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = if (showAllPlans) {
                    "Tất cả kế hoạch"
                } else {
                    "Kế hoạch cho ngày ${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (filteredCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$filteredCount kế hoạch",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        if (showAllPlans) {
            Text(
                text = "Lọc theo ngày",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        } else if (totalCount > filteredCount) {
            Text(
                text = "Xem tất cả ($totalCount)",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }
    }
}

@Composable
fun PlanItemCard(
    plan: TravelPlan, 
    onClick: () -> Unit,
    onDelete: () -> Unit,
    viewModel: PlanListViewModel
) {
    // Format dates
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val dateRange = if (plan.startDate != null && plan.endDate != null) {
        val start = plan.startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val end = plan.endDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        "${start.format(dateFormatter)} - ${end.format(dateFormatter)}"
    } else {
        "Chưa có ngày"
    }
    
    // Extract location from title or use destinationId
    val location = plan.title.substringAfter("đến ").substringBefore(",").ifEmpty { "Địa điểm" }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hiển thị ảnh từ coverImageUrl hoặc fallback
            if (plan.coverImageUrl.isNotEmpty()) {
                val isNetworkImage = plan.coverImageUrl.startsWith("http") || plan.coverImageUrl.startsWith("https://")
                if (isNetworkImage) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(plan.coverImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = plan.title,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val context = LocalContext.current
                    val resId = context.resources.getIdentifier(plan.coverImageUrl, "drawable", context.packageName)
                    val painter = if (resId != 0)
                        painterResource(id = resId)
                    else painterResource(id = R.drawable.ic_launcher_foreground)
                    Image(
                        painter = painter,
                        contentDescription = plan.title,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(plan.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(dateRange, color = Color.Gray, fontSize = 14.sp)
                if (plan.planDetail.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Có gợi ý AI",
                            color = Color(0xFFFFC107),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            // Nút xóa
            var showDeleteDialog by remember { mutableStateOf(false) }
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa kế hoạch",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Xóa kế hoạch") },
                    text = { Text("Bạn có chắc chắn muốn xóa kế hoạch này? Hành động này không thể hoàn tác.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                onDelete()
                            }
                        ) {
                            Text("Xóa", color = Color(0xFFE53935))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlanScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        PlanScreen(navController)
    }
}
