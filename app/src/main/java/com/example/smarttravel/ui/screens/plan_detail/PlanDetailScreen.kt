package com.example.smarttravel.ui.screens.plan_detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.smarttravel.R
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.components.TravelPlanMapView
import com.example.smarttravel.ui.components.MapDestination
import com.example.smarttravel.ui.theme.SmarttravelTheme
import com.example.smarttravel.ui.viewmodel.PlanDetailViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PlanDetailScreen(
    navController: NavController,
    planId: String,
    viewModel: PlanDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Lỗi: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadPlan() }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            uiState.plan != null -> {
                PlanDetailContent(
                    navController = navController,
                    plan = uiState.plan!!,
                    destination = uiState.destination
                )
            }
        }
    }
}

@Composable
fun PlanDetailContent(
    navController: NavController,
    plan: TravelPlan,
    destination: com.example.smarttravel.model.Destination? = null
) {
    // Parse planDetail từ Firestore
    val planDays = remember(plan.planDetail) {
        parsePlanDetail(plan.planDetail)
    }
    
    // State để lưu ngày được chọn (null = hiển thị tất cả)
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    
    // Filter planDays theo ngày được chọn
    val displayedDays = remember(planDays, selectedDayIndex) {
        if (selectedDayIndex != null && selectedDayIndex!! in planDays.indices) {
            listOf(planDays[selectedDayIndex!!])
        } else {
            planDays
        }
    }
    
    // Format dates
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val dateRange = if (plan.startDate != null && plan.endDate != null) {
        val start = plan.startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val end = plan.endDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        "${start.format(dateFormatter)} - ${end.format(dateFormatter)}"
    } else {
        "Chưa có ngày"
    }
    
    val duration = if (plan.startDate != null && plan.endDate != null) {
        val start = plan.startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val end = plan.endDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1
        "$days ngày ${days - 1} đêm"
    } else {
        "Chưa xác định"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // 1. Ảnh bìa
            item {
                ImageHeader(
                    imageUrl = plan.coverImageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }

            // 2. Header thông tin
            item {
                PlanInfoHeader(
                    title = plan.title,
                    dateRange = dateRange
                )
            }

            // 3. Thông tin tổng quan
            item {
                OverviewSection(
                    duration = duration,
                    participants = plan.companion,
                    budget = plan.budget
                )
            }

            // 3.5. Bản đồ lịch trình
            item {
                val mapDestinations = remember(planDays, destination) {
                    extractMapDestinations(planDays, destination)
                }
                if (mapDestinations.isNotEmpty()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        Text(
                            text = "Bản đồ lịch trình",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        TravelPlanMapView(
                            destinations = mapDestinations,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            showRoute = true
                        )
                    }
                }
            }

            // 3.6. Thanh chọn ngày
            if (planDays.isNotEmpty() && planDays.size > 1) {
                item {
                    DaySelectorBar(
                        planDays = planDays,
                        selectedDayIndex = selectedDayIndex,
                        onDaySelected = { index ->
                            selectedDayIndex = when {
                                index == -1 -> null // Reset về "Tất cả"
                                selectedDayIndex == index -> null // Click lại để bỏ chọn
                                else -> index
                            }
                        }
                    )
                }
            }

            // 4. Lịch trình chi tiết theo ngày
            if (displayedDays.isNotEmpty()) {
                itemsIndexed(displayedDays) { displayedIndex, day ->
                    // Tìm index gốc của day trong planDays
                    val originalIndex = planDays.indexOfFirst { 
                        it.day == day.day && it.date == day.date 
                    }
                    val dayNumber = if (originalIndex >= 0) originalIndex + 1 else displayedIndex + 1
                    PlanDayItem(day = day, dayNumber = dayNumber)
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Đang tạo gợi ý AI...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // 5. Nút Chia sẻ
            item {
                PrimaryButton(
                    text = "Chia sẻ Kế hoạch",
                    onClick = { /*TODO: Share plan*/ },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // 6. Nút Back
        PlanTopControls(
            onBackClick = { navController.popBackStack() }
        )
    }
}

// Parse planDetail từ Firestore
@Suppress("UNCHECKED_CAST")
private fun parsePlanDetail(planDetail: List<Map<String, Any>>): List<PlanDayData> {
    return planDetail.mapNotNull { dayMap ->
        try {
            val day = (dayMap["day"] as? Number)?.toInt() ?: return@mapNotNull null
            val date = dayMap["date"] as? String ?: ""
            val title = dayMap["title"] as? String ?: ""
            val hotelMap = dayMap["hotel"] as? Map<String, Any>
            val activitiesList = dayMap["activities"] as? List<Map<String, Any>> ?: emptyList()
            
            val hotel = hotelMap?.let {
                HotelInfo(
                    name = it["name"] as? String ?: "",
                    location = it["location"] as? String ?: "",
                    price = it["price"] as? String ?: "",
                    rating = it["rating"] as? String ?: "",
                    description = it["description"] as? String ?: ""
                )
            }
            
            val activities = activitiesList.mapNotNull { activityMap ->
                val time = activityMap["time"] as? String ?: ""
                val type = activityMap["type"] as? String ?: ""
                val name = activityMap["name"] as? String ?: ""
                val location = activityMap["location"] as? String ?: ""
                val description = activityMap["description"] as? String ?: ""
                val recommendedDishes = (activityMap["recommendedDishes"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                val tips = activityMap["tips"] as? String
                
                ActivityInfo(
                    time = time,
                    type = type,
                    name = name,
                    location = location,
                    description = description,
                    recommendedDishes = recommendedDishes,
                    tips = tips
                )
            }
            
            PlanDayData(
                day = day,
                date = date,
                title = title,
                hotel = hotel,
                activities = activities
            )
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Trích xuất các địa điểm từ plan để hiển thị trên bản đồ
 * Lưu ý: Vì ActivityInfo chỉ có location là string, hàm này sẽ chỉ hiển thị
 * destination chính nếu có tọa độ. Để hiển thị đầy đủ, cần tích hợp Geocoding API.
 */
private fun extractMapDestinations(
    planDays: List<PlanDayData>,
    destination: com.example.smarttravel.model.Destination?
): List<MapDestination> {
    val destinations = mutableListOf<MapDestination>()
    
    // Thêm destination chính nếu có tọa độ
    destination?.let { dest ->
        if (dest.latitude != 0.0 && dest.longitude != 0.0) {
            destinations.add(
                MapDestination(
                    name = dest.name,
                    location = dest.location_name,
                    latitude = dest.latitude,
                    longitude = dest.longitude
                )
            )
        }
    }
    
    // TODO: Thêm các điểm từ activities và hotel
    // Hiện tại không có tọa độ từ activities, cần:
    // 1. Lưu tọa độ trong ActivityInfo khi tạo plan
    // 2. Sử dụng Geocoding API để chuyển đổi location string thành tọa độ
    
    return destinations
}

// Data classes
data class PlanDayData(
    val day: Int,
    val date: String,
    val title: String,
    val hotel: HotelInfo?,
    val activities: List<ActivityInfo>
)

data class HotelInfo(
    val name: String,
    val location: String,
    val price: String,
    val rating: String,
    val description: String
)

data class ActivityInfo(
    val time: String,
    val type: String,
    val name: String,
    val location: String,
    val description: String,
    val recommendedDishes: List<String> = emptyList(),
    val tips: String? = null
)

// --- CÁC COMPONENT CON ---

@Composable
fun ImageHeader(imageUrl: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        if (imageUrl.isNotEmpty()) {
            val isNetworkImage = imageUrl.startsWith("http") || imageUrl.startsWith("https://")
            if (isNetworkImage) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Cover Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val context = LocalContext.current
                val resId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
                val painter = if (resId != 0) 
                    painterResource(id = resId) 
                else 
                    painterResource(id = R.drawable.ic_launcher_foreground)
                Image(
                    painter = painter,
                    contentDescription = "Cover Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
            }
        }
        // Lớp phủ Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                        startY = 400f
                    )
                )
        )
    }
}

@Composable
fun PlanTopControls(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@Composable
fun PlanInfoHeader(title: String, dateRange: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dateRange,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun OverviewSection(duration: String, participants: String, budget: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        OverviewItem(icon = Icons.Default.CalendarMonth, text = duration)
        OverviewItem(icon = Icons.Default.Group, text = participants)
        OverviewItem(icon = Icons.Default.MonetizationOn, text = budget)
    }
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
        color = Color.LightGray.copy(alpha = 0.3f)
    )
}

@Composable
fun OverviewItem(icon: ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun DaySelectorBar(
    planDays: List<PlanDayData>,
    selectedDayIndex: Int?,
    onDaySelected: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nút "Tất cả"
            DaySelectorButton(
                label = "Tất cả",
                isSelected = selectedDayIndex == null,
                onClick = { onDaySelected(-1) } // -1 để reset về null
            )
            
            // Các nút chọn ngày
            planDays.forEachIndexed { index, day ->
                // Format date từ "2025-11-19" thành "19/11"
                val formattedDate = try {
                    val date = LocalDate.parse(day.date)
                    date.format(DateTimeFormatter.ofPattern("dd/MM"))
                } catch (e: Exception) {
                    day.date
                }
                
                DaySelectorButton(
                    label = "Ngày ${index + 1}",
                    subtitle = formattedDate,
                    isSelected = selectedDayIndex == index,
                    onClick = { onDaySelected(index) }
                )
            }
        }
    }
}

@Composable
fun DaySelectorButton(
    label: String,
    subtitle: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color(0xFFF5F5F5)
    }
    val contentColor = if (isSelected) {
        Color.White
    } else {
        Color.Black
    }
    
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp,
            color = contentColor
        )
        if (subtitle != null && subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun PlanDayItem(day: PlanDayData, dayNumber: Int) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        // Tiêu đề ngày
        Text(
            text = day.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = day.date,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Thông tin khách sạn
        day.hotel?.let { hotel ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hotel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Khách sạn",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = hotel.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = hotel.location,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    if (hotel.rating.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = hotel.rating,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            if (hotel.price.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• ${hotel.price}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    if (hotel.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hotel.description,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Danh sách hoạt động
        Column {
            day.activities.forEach { activity ->
                ActivityItem(activity = activity)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp),
            color = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun ActivityItem(activity: ActivityInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon theo type
                val icon = when (activity.type) {
                    "breakfast", "lunch", "dinner" -> Icons.Default.Restaurant
                    "attraction" -> Icons.Default.Explore
                    "activity", "entertainment" -> Icons.Default.SportsSoccer
                    "hotel" -> Icons.Default.Hotel
                    else -> Icons.Default.Place
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = activity.time,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = activity.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
            
            if (activity.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = activity.location,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
            
            if (activity.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activity.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            // Món ăn được đề xuất
            if (activity.recommendedDishes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Món đề xuất: ${activity.recommendedDishes.joinToString(", ")}",
                        fontSize = 13.sp,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Tips
            activity.tips?.let { tips ->
                if (tips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tips,
                            fontSize = 13.sp,
                            color = Color(0xFFFFC107),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlanDetailScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        // Preview sẽ không hoạt động vì cần planId, nhưng giữ lại để không lỗi compile
    }
}
