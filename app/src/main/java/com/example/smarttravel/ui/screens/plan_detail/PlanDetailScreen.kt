package com.example.smarttravel.ui.screens.plan_detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// Đã xóa import automirrored gây lỗi
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.smarttravel.R
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.components.MapDestination
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.components.TravelPlanMapView
import com.example.smarttravel.ui.viewmodel.PlanDetailViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// --- COLOR PALETTE ---
val AppPrimary = Color(0xFF037CAC)
val BackgroundLight = Color(0xFFF5F7FA)
val TextDark = Color(0xFF1A1A1A)
val TextGray = Color(0xFF757575)
val CardBorder = Color(0xFFEEEEEE)

@Composable
fun PlanDetailScreen(
    navController: NavController,
    planId: String,
    viewModel: PlanDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            if (uiState.plan != null) {
                PlanBottomBar(
                    onShareClick = {
                        val planDays = parsePlanDetail(uiState.plan!!.planDetail)
                        sharePlan(context, uiState.plan!!, uiState.destination, planDays)
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppPrimary)
                    }
                }
                uiState.error != null -> {
                    ErrorView(error = uiState.error!!, onRetry = { viewModel.loadPlan() })
                }
                uiState.plan != null -> {
                    PlanDetailContent(
                        navController = navController,
                        plan = uiState.plan!!,
                        destination = uiState.destination,
                        viewModel = viewModel
                    )

                    PlanTopControls(
                        onBackClick = { navController.popBackStack() },
                        onDeleteClick = { showDeleteDialog = true }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.deletePlan(
                    onSuccess = { navController.popBackStack() },
                    onError = { /* Handle error */ }
                )
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun PlanDetailContent(
    navController: NavController,
    plan: TravelPlan,
    destination: com.example.smarttravel.model.Destination?,
    viewModel: PlanDetailViewModel
) {
    val planDays = remember(plan.planDetail) { parsePlanDetail(plan.planDetail) }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    val displayedDays = remember(planDays, selectedDayIndex) {
        if (selectedDayIndex != null && selectedDayIndex!! in planDays.indices) {
            listOf(planDays[selectedDayIndex!!])
        } else {
            planDays
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            HeroImageSection(imageUrl = plan.coverImageUrl, title = plan.title)
        }

        item {
            InfoGridSection(plan)
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                val (_, priceText) = remember(planDays) { calculateTotalPrice(planDays) }
                if (priceText.isNotEmpty()) {
                    CompactPriceCard(priceText)
                }

                Spacer(modifier = Modifier.height(24.dp))

                val mapDestinations = remember(planDays, destination) { extractMapDestinations(planDays, destination) }
                if (mapDestinations.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, null, tint = AppPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bản đồ lịch trình", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TravelPlanMapView(
                        destinations = mapDestinations,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                        showRoute = true
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (planDays.size > 1) {
            item {
                DaySelectorBar(
                    planDays = planDays,
                    selectedDayIndex = selectedDayIndex,
                    onDaySelected = { index ->
                        selectedDayIndex = if (index == -1 || selectedDayIndex == index) null else index
                    }
                )
            }
        }

        if (displayedDays.isNotEmpty()) {
            itemsIndexed(displayedDays) { index, day ->
                val originalIndex = planDays.indexOfFirst { it.day == day.day && it.date == day.date }
                val realIndex = if (originalIndex >= 0) originalIndex else index

                TimelineDaySection(
                    day = day,
                    dayIndex = realIndex,
                    viewModel = viewModel
                )
            }
        } else {
            item { EmptyStateView() }
        }
    }
}

@Composable
fun HeroImageSection(imageUrl: String, title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        NetworkOrLocalImage(
            url = imageUrl,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 400f
                    )
                )
        )

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 16.dp, bottom = 40.dp)
        )
    }
}

@Composable
fun PlanTopControls(onBackClick: () -> Unit, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // --- SỬA LỖI: Dùng Icons.Default.ArrowBack thay vì AutoMirrored ---
        AppTopBar(
            onBackClick = onBackClick,
            containerColor = Color.White.copy(alpha = 0.9f),
            iconTint = TextDark
        )

        Box(
            modifier = Modifier
                .size(46.dp)
                .background(color = Color.White.copy(alpha = 0.9f), shape = CircleShape)
                .clickable { onDeleteClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete",
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun InfoGridSection(plan: TravelPlan) {
    val durationString = remember(plan.startDate, plan.endDate) {
        if (plan.startDate != null && plan.endDate != null) {
            val start = plan.startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val end = plan.endDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1
            "$days ngày ${days - 1} đêm"
        } else "Chưa xác định"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-32).dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 20.dp, horizontal = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoColumn(Icons.Outlined.CalendarMonth, "Thời gian", durationString)
            VerticalDivider()
            InfoColumn(Icons.Outlined.Groups, "Đồng hành", plan.companion)
            VerticalDivider()
            InfoColumn(Icons.Outlined.MonetizationOn, "Ngân sách", plan.budget)
        }
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(40.dp)
            .width(1.dp)
            .background(Color(0xFFEEEEEE))
    )
}

@Composable
fun InfoColumn(icon: ImageVector, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 80.dp, max = 110.dp)
    ) {
        Icon(icon, null, tint = AppPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, color = TextGray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
fun CompactPriceCard(priceText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, AppPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(AppPrimary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Wallet, null, tint = AppPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("Tổng chi phí ước tính", fontSize = 12.sp, color = TextGray)
            Text(priceText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppPrimary)
        }
    }
}

@Composable
fun DaySelectorBar(planDays: List<PlanDayData>, selectedDayIndex: Int?, onDaySelected: (Int) -> Unit) {
    Surface(color = BackgroundLight, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.width(16.dp))

            FilterChip(
                selected = selectedDayIndex == null,
                onClick = { onDaySelected(-1) },
                label = { Text("Tất cả") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White
                ),
                border = null,
                elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp)
            )

            planDays.forEachIndexed { index, day ->
                val shortDate = try {
                    LocalDate.parse(day.date).format(DateTimeFormatter.ofPattern("dd/MM"))
                } catch (e: Exception) { day.date }

                FilterChip(
                    selected = selectedDayIndex == index,
                    onClick = { onDaySelected(index) },
                    label = { Text("Ngày ${index + 1} • $shortDate") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White
                    ),
                    border = null,
                    elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
fun TimelineDaySection(day: PlanDayData, dayIndex: Int, viewModel: PlanDetailViewModel) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 16.dp)) {
            Box(modifier = Modifier.size(12.dp).background(AppPrimary, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = day.title.uppercase(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextDark)
                Text(text = day.date, style = MaterialTheme.typography.bodySmall, color = TextGray)
            }
        }
        day.hotel?.let { hotel ->
            TimelineItem(time = "Nghỉ ngơi", isHotel = true, content = { HotelCard(hotel = hotel, dayIndex = dayIndex, viewModel = viewModel) })
        }
        day.activities.forEachIndexed { index, activity ->
            TimelineItem(
                time = activity.time,
                isLast = index == day.activities.lastIndex && day.hotel == null,
                content = { ActivityCard(activity = activity, dayIndex = dayIndex, activityIndex = index, viewModel = viewModel) }
            )
        }
    }
}

@Composable
fun TimelineItem(time: String, isHotel: Boolean = false, isLast: Boolean = false, content: @Composable () -> Unit) {
    IntrinsicHeightRow {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
            Text(text = time, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isHotel) AppPrimary else TextDark, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.size(if (isHotel) 14.dp else 10.dp).border(2.dp, if (isHotel) AppPrimary else Color(0xFFBDBDBD), CircleShape).background(Color.White, CircleShape))
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color(0xFFE0E0E0)))
            }
        }
        Box(modifier = Modifier.padding(bottom = 24.dp, start = 8.dp).weight(1f)) {
            content()
        }
    }
}

@Composable
fun IntrinsicHeightRow(content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) { content() }
}

@Composable
fun HotelCard(hotel: HotelInfo, dayIndex: Int, viewModel: PlanDetailViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isGenerating = uiState.generatingAlternative?.let { it.first == dayIndex && it.second == "hotel" } ?: false

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppPrimary.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Hotel, null, tint = AppPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("KHÁCH SẠN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppPrimary)
                Spacer(modifier = Modifier.weight(1f))
                if (hotel.rating.isNotEmpty()) {
                    Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Text(hotel.rating, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(hotel.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)

            if (hotel.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(hotel.location, fontSize = 13.sp, color = TextGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            if (hotel.price.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(hotel.price, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppPrimary)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SuggestionButton(isGenerating = isGenerating, onClick = { viewModel.requestAlternativeSuggestion(dayIndex, "hotel", onSuccess = {}, onError = {}) })
                SmallMapButton(onClick = { openLocationInGoogleMaps(context, hotel.location, hotel.name) })
            }
        }
    }
}

@Composable
fun ActivityCard(activity: ActivityInfo, dayIndex: Int, activityIndex: Int, viewModel: PlanDetailViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isGenerating = uiState.generatingAlternative?.let { it.first == dayIndex && it.second == "activity" } ?: false

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(activity.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)

            if (activity.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(activity.description, fontSize = 13.sp, color = TextGray, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }

            if (activity.recommendedDishes.isNotEmpty() || activity.price != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    activity.price?.let {
                        InfoChip(icon = Icons.Default.AttachMoney, text = it, color = Color(0xFF388E3C))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    activity.recommendedDishes.forEach { dish ->
                        InfoChip(icon = Icons.Default.RestaurantMenu, text = dish, color = Color(0xFFF57C00))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            activity.tips?.let {
                if (it.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFFFA000), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(it, fontSize = 12.sp, color = Color(0xFFFFA000), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                SuggestionButton(isGenerating = isGenerating, onClick = { viewModel.requestAlternativeSuggestion(dayIndex, "activity", activityIndex, {}, {}) })
                SmallMapButton(onClick = { openLocationInGoogleMaps(context, activity.location, activity.name) })
            }
        }
    }
}

@Composable
fun InfoChip(icon: ImageVector, text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SmallMapButton(onClick: () -> Unit) {
    Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onClick() }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Chỉ đường", fontSize = 12.sp, color = AppPrimary, fontWeight = FontWeight.Bold)
        // --- SỬA LỖI: Dùng Icons.Default.ArrowForward ---
        Icon(Icons.Default.ArrowForward, null, tint = AppPrimary, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun SuggestionButton(isGenerating: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(enabled = !isGenerating) { onClick() }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (isGenerating) {
            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = TextGray)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Đang tìm...", fontSize = 12.sp, color = TextGray)
        } else {
            Icon(Icons.Default.Refresh, null, tint = TextGray, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Gợi ý khác", fontSize = 12.sp, color = TextGray)
        }
    }
}

@Composable
fun PlanBottomBar(onShareClick: () -> Unit) {
    Surface(shadowElevation = 16.dp, color = Color.White) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            PrimaryButton(text = "Chia sẻ kế hoạch", onClick = onShareClick, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xóa kế hoạch?", fontWeight = FontWeight.Bold) },
        text = { Text("Bạn có chắc chắn muốn xóa kế hoạch này? Hành động này không thể hoàn tác.") },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Xóa") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } },
        containerColor = Color.White
    )
}

@Composable
fun ErrorView(error: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Đã xảy ra lỗi", fontWeight = FontWeight.Bold)
        Text(error, color = TextGray, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        Button(onClick = onRetry) { Text("Thử lại") }
    }
}

@Composable
fun EmptyStateView() {
    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.AutoAwesome, null, tint = AppPrimary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Đang tạo lịch trình AI...", color = TextGray)
    }
}

@Composable
fun NetworkOrLocalImage(url: String, modifier: Modifier = Modifier) {
    if (url.startsWith("http")) {
        AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier)
    } else {
        val ctx = LocalContext.current
        val id = remember(url) { ctx.resources.getIdentifier(url, "drawable", ctx.packageName) }
        if (id != 0) Image(painterResource(id), null, contentScale = ContentScale.Crop, modifier = modifier) else Box(modifier.background(Color.LightGray))
    }
}

// ======================= LOGIC HELPERS =======================

data class PlanDayData(val day: Int, val date: String, val title: String, val hotel: HotelInfo?, val activities: List<ActivityInfo>)
data class HotelInfo(val name: String, val location: String, val price: String, val rating: String, val description: String)
data class ActivityInfo(val time: String, val type: String, val name: String, val location: String, val description: String, val recommendedDishes: List<String> = emptyList(), val tips: String? = null, val price: String? = null)

@Suppress("UNCHECKED_CAST")
private fun parsePlanDetail(planDetail: List<Map<String, Any>>): List<PlanDayData> {
    return planDetail.mapNotNull { dayMap ->
        try {
            val day = (dayMap["day"] as? Number)?.toInt() ?: return@mapNotNull null
            val date = dayMap["date"] as? String ?: ""
            val title = dayMap["title"] as? String ?: ""
            val hotelMap = dayMap["hotel"] as? Map<String, Any>
            val activitiesList = dayMap["activities"] as? List<Map<String, Any>> ?: emptyList()

            // Chỉ tạo hotel nếu hotelMap không null và không rỗng
            val hotel = hotelMap?.let {
                val name = it["name"] as? String ?: ""
                val location = it["location"] as? String ?: ""
                // Chỉ tạo hotel nếu có ít nhất một thông tin (name hoặc location)
                if (name.isNotEmpty() || location.isNotEmpty()) {
                    HotelInfo(
                        name = name,
                        location = location,
                        price = it["price"] as? String ?: "",
                        rating = it["rating"] as? String ?: "",
                        description = it["description"] as? String ?: ""
                    )
                } else {
                    null
                }
            }

            val activities = activitiesList.mapNotNull { activityMap ->
                ActivityInfo(
                    time = activityMap["time"] as? String ?: "",
                    type = activityMap["type"] as? String ?: "",
                    name = activityMap["name"] as? String ?: "",
                    location = activityMap["location"] as? String ?: "",
                    description = activityMap["description"] as? String ?: "",
                    recommendedDishes = (activityMap["recommendedDishes"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    tips = activityMap["tips"] as? String,
                    price = activityMap["price"] as? String
                )
            }
            PlanDayData(day, date, title, hotel, activities)
        } catch (e: Exception) { null }
    }
}

private fun calculateTotalPrice(planDays: List<PlanDayData>): Pair<String, String> {
    var minTotal: Long = 0
    var maxTotal: Long = 0
    var hasPrice = false

    fun parse(price: String?) {
        price?.let { p ->
            if (p.isNotEmpty() && !p.contains("Miễn phí", true)) {
                val clean = p.replace(Regex("[^0-9-]"), "")
                if (clean.contains("-")) {
                    val parts = clean.split("-")
                    if (parts.size == 2) {
                        minTotal += parts[0].toLongOrNull() ?: 0
                        maxTotal += parts[1].toLongOrNull() ?: 0
                        hasPrice = true
                    }
                } else {
                    val v = clean.toLongOrNull() ?: 0
                    minTotal += v
                    maxTotal += v
                    hasPrice = true
                }
            }
        }
    }

    planDays.forEach { day ->
        parse(day.hotel?.price)
        day.activities.forEach { parse(it.price) }
    }

    if (!hasPrice) return Pair("", "")
    val minStr = String.format("%,d VNĐ", minTotal).replace(",", ".")
    val maxStr = String.format("%,d VNĐ", maxTotal).replace(",", ".")
    return Pair("Tổng chi phí", if (minTotal == maxTotal) minStr else "$minStr - $maxStr")
}

private fun extractMapDestinations(planDays: List<PlanDayData>, destination: com.example.smarttravel.model.Destination?): List<MapDestination> {
    val list = mutableListOf<MapDestination>()
    destination?.let {
        if (it.latitude != 0.0 && it.longitude != 0.0) {
            list.add(MapDestination(it.name, it.location_name, it.latitude, it.longitude))
        }
    }
    return list
}

private fun openLocationInGoogleMaps(context: android.content.Context, location: String, name: String) {
    try {
        val query = Uri.encode("$location($name)")
        val uri = Uri.parse("geo:0,0?q=$query")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    } catch (e: Exception) { }
}

private fun sharePlan(context: android.content.Context, plan: TravelPlan, destination: com.example.smarttravel.model.Destination?, planDays: List<PlanDayData>) {
    val text = StringBuilder().apply {
        appendLine("🗺️ ${plan.title}")
        appendLine("📅 ${plan.companion} • ${plan.budget}")
        appendLine("--- LỊCH TRÌNH ---")
        planDays.forEach { day ->
            appendLine("\n📌 ${day.title} (${day.date})")
            day.hotel?.let { appendLine("🏨 Khách sạn: ${it.name}") }
            day.activities.forEach { act -> appendLine("📍 ${act.time}: ${act.name}") }
        }
    }.toString()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Chia sẻ lịch trình"))
}