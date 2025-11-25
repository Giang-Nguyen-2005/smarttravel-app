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
import android.content.Intent
import android.net.Uri

@Composable
fun PlanDetailScreen(
    navController: NavController,
    planId: String,
    viewModel: PlanDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                    destination = uiState.destination,
                    onDeleteClick = { showDeleteDialog = true },
                    viewModel = viewModel
                )
            }
        }
        
        // Dialog xác nhận xóa
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xóa kế hoạch") },
                text = { Text("Bạn có chắc chắn muốn xóa kế hoạch này? Hành động này không thể hoàn tác.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deletePlan(
                                onSuccess = {
                                    navController.popBackStack()
                                },
                                onError = { error ->
                                    android.util.Log.e("PlanDetailScreen", "Error deleting plan: $error")
                                }
                            )
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
    }
}

@Composable
fun PlanDetailContent(
    navController: NavController,
    plan: TravelPlan,
    destination: com.example.smarttravel.model.Destination? = null,
    onDeleteClick: () -> Unit = {},
    viewModel: PlanDetailViewModel? = null
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
            
            // 3.5. Tổng tiền ước tính
            item {
                val (title, priceText) = remember(planDays) {
                    calculateTotalPrice(planDays)
                }
                if (priceText.isNotEmpty()) {
                    TotalPriceSection(title = title, totalPrice = priceText)
                }
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
                    val dayIndex = if (originalIndex >= 0) originalIndex else displayedIndex
                    PlanDayItem(
                        day = day, 
                        dayNumber = dayNumber,
                        dayIndex = dayIndex,
                        viewModel = viewModel
                    )
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
                val context = LocalContext.current
                PrimaryButton(
                    text = "Chia sẻ Kế hoạch",
                    onClick = { 
                        sharePlan(
                            context = context,
                            plan = plan,
                            destination = destination,
                            planDays = planDays
                        )
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // 6. Nút Back và Xóa
        PlanTopControls(
            onBackClick = { navController.popBackStack() },
            onDeleteClick = onDeleteClick
        )
    }
}

// Parse giá từ string (có thể là range hoặc số cụ thể)
private fun parsePrice(priceString: String): Pair<Long, Long>? {
    if (priceString.isEmpty() || priceString.contains("Miễn phí", ignoreCase = true)) {
        return null
    }
    
    // Loại bỏ các ký tự không cần thiết
    var cleanPrice = priceString
        .replace("VNĐ", "", ignoreCase = true)
        .replace("/người", "", ignoreCase = true)
        .replace("/đêm", "", ignoreCase = true)
        .replace("đ", "", ignoreCase = true)
        .trim()
    
    // Xử lý range (ví dụ: "50.000 - 150.000")
    if (cleanPrice.contains("-")) {
        val parts = cleanPrice.split("-").map { it.trim() }
        if (parts.size == 2) {
            val min = extractNumber(parts[0])
            val max = extractNumber(parts[1])
            if (min != null && max != null) {
                return Pair(min, max)
            }
        }
    }
    
    // Xử lý số cụ thể
    val number = extractNumber(cleanPrice)
    return number?.let { Pair(it, it) }
}

// Trích xuất số từ string (loại bỏ dấu chấm, phẩy)
private fun extractNumber(text: String): Long? {
    val cleaned = text.replace(".", "").replace(",", "").trim()
    return cleaned.toLongOrNull()
}

// Tính tổng tiền ước tính từ tất cả activities và hotels
private fun calculateTotalPrice(planDays: List<PlanDayData>): Pair<String, String> {
    var minTotal: Long = 0
    var maxTotal: Long = 0
    var hasPrice = false
    
    // Thu thập và tính tổng giá từ hotels
    planDays.forEach { day ->
        day.hotel?.price?.let { priceStr ->
            parsePrice(priceStr)?.let { (min, max) ->
                minTotal += min
                maxTotal += max
                hasPrice = true
            }
        }
    }
    
    // Thu thập và tính tổng giá từ activities
    planDays.forEach { day ->
        day.activities.forEach { activity ->
            activity.price?.let { priceStr ->
                parsePrice(priceStr)?.let { (min, max) ->
                    minTotal += min
                    maxTotal += max
                    hasPrice = true
                }
            }
        }
    }
    
    if (!hasPrice) {
        return Pair("", "")
    }
    
    // Format tổng giá
    val formattedMin = formatPrice(minTotal)
    val formattedMax = formatPrice(maxTotal)
    
    val totalPriceText = if (minTotal == maxTotal) {
        formattedMin
    } else {
        "$formattedMin - $formattedMax"
    }
    
    return Pair("Tổng chi phí ước tính", totalPriceText)
}

// Format giá tiền
private fun formatPrice(amount: Long): String {
    return String.format("%,d VNĐ", amount).replace(",", ".")
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
                val price = activityMap["price"] as? String
                
                ActivityInfo(
                    time = time,
                    type = type,
                    name = name,
                    location = location,
                    description = description,
                    recommendedDishes = recommendedDishes,
                    tips = tips,
                    price = price
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
 * Thêm destination chính và các địa điểm từ activities/hotels nếu có location
 */
private fun extractMapDestinations(
    planDays: List<PlanDayData>,
    destination: com.example.smarttravel.model.Destination?
): List<MapDestination> {
    val destinations = mutableListOf<MapDestination>()
    val addedLocations = mutableSetOf<String>() // Để tránh trùng lặp
    
    // Thêm destination chính nếu có tọa độ
    destination?.let { dest ->
        if (dest.latitude != 0.0 && dest.longitude != 0.0) {
            val key = "${dest.latitude},${dest.longitude}"
            if (key !in addedLocations) {
                destinations.add(
                    MapDestination(
                        name = dest.name,
                        location = dest.location_name,
                        latitude = dest.latitude,
                        longitude = dest.longitude
                    )
                )
                addedLocations.add(key)
            }
        }
    }
    
    // Thêm các địa điểm từ hotels và activities
    // Lưu ý: Vì không có tọa độ từ activities/hotels, chúng ta sẽ chỉ thêm vào danh sách
    // để hiển thị trên bản đồ nếu có tọa độ. Hiện tại chỉ thêm destination chính.
    // Có thể mở rộng sau bằng cách sử dụng Geocoding API.
    
    return destinations
}

/**
 * Mở địa điểm trong Google Maps bằng location string
 */
private fun openLocationInGoogleMaps(context: android.content.Context, location: String, name: String) {
    try {
        // Encode location và name để tránh lỗi với ký tự đặc biệt
        val encodedLocation = Uri.encode(location)
        val encodedName = Uri.encode(name)
        
        // Tạo URI cho Google Maps với location string
        val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedLocation($encodedName)")
        
        // Thử mở Google Maps app trước
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        
        // Nếu Google Maps không có, mở bằng trình duyệt
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback: mở trong trình duyệt
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedLocation")
            )
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        android.util.Log.e("PlanDetailScreen", "Error opening Google Maps: ${e.message}", e)
    }
}

/**
 * Format kế hoạch thành text đẹp để chia sẻ
 */
private fun formatPlanForSharing(
    plan: TravelPlan,
    destination: com.example.smarttravel.model.Destination?,
    planDays: List<PlanDayData>
): String {
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
    
    val destinationName = destination?.name ?: plan.title.replace("Chuyến đi đến ", "")
    
    val sb = StringBuilder()
    sb.appendLine("🗺️ ${plan.title}")
    sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    sb.appendLine()
    sb.appendLine("📅 Thời gian: $dateRange ($duration)")
    sb.appendLine("👥 Người đồng hành: ${plan.companion}")
    sb.appendLine("💰 Ngân sách: ${plan.budget}")
    if (plan.purposes.isNotEmpty()) {
        sb.appendLine("🎯 Mục đích: ${plan.purposes.joinToString(", ")}")
    }
    sb.appendLine()
    sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    sb.appendLine()
    
    if (planDays.isEmpty()) {
        sb.appendLine("📝 Lịch trình đang được tạo...")
    } else {
        sb.appendLine("📋 LỊCH TRÌNH CHI TIẾT:")
        sb.appendLine()
        
        planDays.forEachIndexed { index, day ->
            sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            sb.appendLine("📌 ${day.title}")
            sb.appendLine("   Ngày: ${day.date}")
            sb.appendLine()
            
            // Thông tin khách sạn
            day.hotel?.let { hotel ->
                sb.appendLine("🏨 Khách sạn:")
                sb.appendLine("   • Tên: ${hotel.name}")
                sb.appendLine("   • Địa chỉ: ${hotel.location}")
                sb.appendLine("   • Giá: ${hotel.price}")
                sb.appendLine("   • Xếp hạng: ${hotel.rating}")
                if (hotel.description.isNotEmpty()) {
                    sb.appendLine("   • Mô tả: ${hotel.description}")
                }
                sb.appendLine()
            }
            
            // Các hoạt động
            if (day.activities.isNotEmpty()) {
                sb.appendLine("🎯 Hoạt động trong ngày:")
                day.activities.forEach { activity ->
                    val activityType = when (activity.type) {
                        "breakfast" -> "🍳 Bữa sáng"
                        "lunch" -> "🍽️ Bữa trưa"
                        "dinner" -> "🍴 Bữa tối"
                        "attraction" -> "🏛️ Tham quan"
                        "activity" -> "🎮 Hoạt động"
                        "entertainment" -> "🎪 Giải trí"
                        "rest" -> "😴 Nghỉ ngơi"
                        else -> "📍 Hoạt động"
                    }
                    sb.appendLine("   $activityType - ${activity.time}")
                    sb.appendLine("   • ${activity.name}")
                    if (activity.location.isNotEmpty()) {
                        sb.appendLine("   • Địa chỉ: ${activity.location}")
                    }
                    if (activity.description.isNotEmpty()) {
                        sb.appendLine("   • Mô tả: ${activity.description}")
                    }
                    if (activity.price != null && activity.price.isNotEmpty()) {
                        sb.appendLine("   • Giá: ${activity.price}")
                    }
                    if (activity.recommendedDishes.isNotEmpty()) {
                        sb.appendLine("   • Món đề xuất: ${activity.recommendedDishes.joinToString(", ")}")
                    }
                    if (activity.tips != null && activity.tips.isNotEmpty()) {
                        sb.appendLine("   • Mẹo: ${activity.tips}")
                    }
                    sb.appendLine()
                }
            }
            
            if (index < planDays.size - 1) {
                sb.appendLine()
            }
        }
    }
    
    sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    sb.appendLine()
    sb.appendLine("📱 Được tạo bởi SmartTravel App")
    
    return sb.toString()
}

/**
 * Chia sẻ kế hoạch qua Android Share Intent
 */
private fun sharePlan(
    context: android.content.Context,
    plan: TravelPlan,
    destination: com.example.smarttravel.model.Destination?,
    planDays: List<PlanDayData>
) {
    try {
        val shareText = formatPlanForSharing(plan, destination, planDays)
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, plan.title)
        }
        
        val chooserIntent = Intent.createChooser(shareIntent, "Chia sẻ kế hoạch du lịch")
        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        android.util.Log.e("PlanDetailScreen", "Error sharing plan: ${e.message}", e)
    }
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
    val tips: String? = null,
    val price: String? = null
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
fun PlanTopControls(
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
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
        
        // Nút xóa
        if (onDeleteClick != {}) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Xóa kế hoạch",
                    tint = Color.White
                )
            }
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
fun TotalPriceSection(title: String, totalPrice: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            // Hiển thị giá tiền bên dưới
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = totalPrice,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 32.dp) // Căn lề với icon và text phía trên
            )
        }
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
fun PlanDayItem(
    day: PlanDayData, 
    dayNumber: Int,
    dayIndex: Int,
    viewModel: PlanDetailViewModel? = null
) {
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
                    val context = LocalContext.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                        // Nút mở Google Maps
                        TextButton(
                            onClick = {
                                openLocationInGoogleMaps(context, hotel.location, hotel.name)
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Mở trong Google Maps",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bản đồ",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
                    
                    // Nút "Gợi ý khác"
                    if (viewModel != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val uiState by viewModel.uiState.collectAsState()
                        val context = LocalContext.current
                        val isGenerating = uiState.generatingAlternative?.let { 
                            it.first == dayIndex && it.second == "hotel" 
                        } ?: false
                        
                        TextButton(
                            onClick = {
                                viewModel.requestAlternativeSuggestion(
                                    dayIndex = dayIndex,
                                    itemType = "hotel",
                                    onSuccess = { /* Plan sẽ tự động update qua Flow */ },
                                    onError = { error ->
                                        android.widget.Toast.makeText(
                                            context,
                                            error,
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            },
                            enabled = !isGenerating,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đang tạo gợi ý...", fontSize = 12.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gợi ý khác", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Danh sách hoạt động
        Column {
            day.activities.forEachIndexed { activityIndex, activity ->
                ActivityItem(
                    activity = activity,
                    dayIndex = dayIndex,
                    activityIndex = activityIndex,
                    viewModel = viewModel
                )
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
fun ActivityItem(
    activity: ActivityInfo,
    dayIndex: Int,
    activityIndex: Int,
    viewModel: PlanDetailViewModel? = null
) {
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
                val context = LocalContext.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                    // Nút mở Google Maps
                    TextButton(
                        onClick = {
                            openLocationInGoogleMaps(context, activity.location, activity.name)
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Mở trong Google Maps",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Bản đồ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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
            
            // Price
            if (activity.price != null && activity.price.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Giá: ${activity.price}",
                        fontSize = 13.sp,
                        color = Color(0xFF4CAF50),
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
            
            // Nút "Gợi ý khác"
            if (viewModel != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current
                val isGenerating = uiState.generatingAlternative?.let { 
                    it.first == dayIndex && it.second == "activity"
                } ?: false
                
                TextButton(
                    onClick = {
                        viewModel.requestAlternativeSuggestion(
                            dayIndex = dayIndex,
                            itemType = "activity",
                            activityIndex = activityIndex,
                            onSuccess = { /* Plan sẽ tự động update qua Flow */ },
                            onError = { error ->
                                android.widget.Toast.makeText(
                                    context,
                                    error,
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    enabled = !isGenerating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đang tạo gợi ý...", fontSize = 12.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gợi ý khác", fontSize = 12.sp)
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
