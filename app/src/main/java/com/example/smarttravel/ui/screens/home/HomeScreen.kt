package com.example.smarttravel.ui.screens.home

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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.smarttravel.R
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.model.Category
import com.example.smarttravel.model.Destination
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.components.AppBottomBar
import com.example.smarttravel.ui.components.DestinationCard
import com.example.smarttravel.ui.theme.SmarttravelTheme
import com.example.smarttravel.ui.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    // Lắng nghe các trạng thái từ ViewModel
    val destinationState by homeViewModel.destinationUiState.collectAsState()
    val categoryState by homeViewModel.categoryUiState.collectAsState()
    val selectedCategory by homeViewModel.selectedCategory.collectAsState()
    val userProfile by homeViewModel.userProfile.collectAsState()

    val colorScheme = MaterialTheme.colorScheme
    
    // Sử dụng Scaffold để chứa AppBottomBar
    Scaffold(
        containerColor = colorScheme.background,
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = Screen.Home.route)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(paddingValues), // Tránh nội dung bị BottomBar che khuất
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1. Top Bar
            item {
                HomeTopBar(
                    userName = userProfile?.displayName ?: "Khách",
                    avatarUrl = userProfile?.avatarUrl ?: "",
                    onNotificationClick = {}
                )
            }

            // 2. Danh Mục (Filter)
            item {
                if (!categoryState.isLoading && categoryState.error == null) {
                    CategorySection(
                        categories = categoryState.categories,
                        selectedCategory = selectedCategory,
                        onCategoryClick = { categoryId ->
                            homeViewModel.onCategorySelected(categoryId)
                        }
                    )
                }
            }

            // 3. Khối "Gợi ý AI" nổi bật (MỚI)
            item {
                val aiSuggestionsState by homeViewModel.aiSuggestionsState.collectAsState()
                val topDestination = aiSuggestionsState.destinations.firstOrNull()
                
                // Load AI suggestions lần đầu nếu chưa có cache cho hôm nay
                LaunchedEffect(Unit) {
                    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    if (aiSuggestionsState.cachedDate != today || aiSuggestionsState.destinations.isEmpty()) {
                        homeViewModel.loadAiSuggestions()
                    }
                }
                
                AiSuggestionCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    topDestination = topDestination,
                    isLoading = aiSuggestionsState.isLoading,
                    onClick = { 
                        // Điều hướng đến màn hình gợi ý AI
                        navController.navigate(Screen.AiSuggestions.route)
                    }
                )
            }

            // 4. Tiêu đề "Khám phá"
            item {
                Text(
                    text = "Khám phá",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            // 5. Danh sách địa điểm (LazyRow - đã được lọc bởi ViewModel)
            item {
                if (destinationState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (destinationState.error != null) {
                    Text(
                        "Lỗi: ${destinationState.error}", 
                        modifier = Modifier.padding(16.dp), 
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (destinationState.destinations.isEmpty()) {
                    Text(
                        "Không tìm thấy địa điểm phù hợp.", 
                        modifier = Modifier.padding(16.dp), 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(destinationState.destinations) { destination ->
                            DestinationCard(
                                modifier = Modifier.clickable {
                                    navController.navigate(Screen.Detail.createRoute(destination.id))
                                },
                                imageUrl = destination.images.firstOrNull() ?: "ha_long",
                                title = destination.name,
                                location = destination.location_name,
                                rating = destination.rating
                            )
                        }
                    }
                }
            }

            // 6. Khối "Kế hoạch gần đây" (MỚI)
            item {
                val recentPlanState by homeViewModel.recentPlanState.collectAsState()
                RecentPlanSection(
                    recentPlan = recentPlanState.recentPlan,
                    isLoading = recentPlanState.isLoading,
                    onClick = { navController.navigate(Screen.Calendar.route) },
                    onPlanClick = { planId ->
                        navController.navigate(Screen.PlanDetail.createRoute(planId))
                    }
                )
            }

            // 7. Tiêu đề "Đang thịnh hành"
            item {
                Text(
                    text = "Đang thịnh hành",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 16.dp)
                )
            }

            // 8. Danh sách thịnh hành (Card Lớn)
            item {
                val trendingState by homeViewModel.trendingDestinationsState.collectAsState()
                
                if (trendingState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (trendingState.error != null) {
                    Text(
                        "Lỗi: ${trendingState.error}",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (trendingState.destinations.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(trendingState.destinations) { destination ->
                            val savedIds by homeViewModel.savedDestinationIds.collectAsState()
                            val isBookmarked = savedIds.contains(destination.id)
                            
                            LargeDestinationCard(
                                modifier = Modifier,
                                name = destination.name,
                                rating = destination.rating,
                                imageUrl = destination.images.firstOrNull() ?: "ha_long",
                                destinationId = destination.id,
                                isBookmarked = isBookmarked,
                                onBookmarkClick = {
                                    homeViewModel.toggleBookmark(destination.id)
                                },
                                onCardClick = {
                                    navController.navigate(Screen.Detail.createRoute(destination.id))
                                }
                            )
                        }
                    }
                } else {
                    // Không có địa điểm thịnh hành
                    Text(
                        "Chưa có địa điểm thịnh hành.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ======================= CÁC COMPONENT CON =======================

@Composable
fun HomeTopBar(
    userName: String,
    avatarUrl: String,
    onNotificationClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (avatarUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(32.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.avatar), // Ảnh mặc định
                    contentDescription = "Avatar",
                    modifier = Modifier.size(32.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = userName,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorScheme.surfaceVariant.copy(alpha = 0.8f))
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Thông báo",
                tint = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun getIconForCategory(categoryId: String): Int {
    return when (categoryId) {
        "all" -> R.drawable.icon_all
        "nui" -> R.drawable.icon_mountain
        "bien" -> R.drawable.icon_beach
        "ho" -> R.drawable.icon_lake
        "rung" -> R.drawable.icon_forest
        else -> R.drawable.ic_launcher_foreground
    }
}

@Composable
fun CategorySection(
    categories: List<Category>,
    selectedCategory: String,
    onCategoryClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = "Danh Mục",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { category ->
                CategoryChip(
                    categoryName = category.name,
                    categoryIconRes = getIconForCategory(category.id),
                    isSelected = category.id == selectedCategory,
                    onClick = { onCategoryClick(category.id) }
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    categoryName: String,
    categoryIconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = if (isSelected) colorScheme.surface else colorScheme.surfaceVariant
    val contentColor = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) colorScheme.primary else colorScheme.outline.copy(alpha = 0.5f)
    Surface(
        modifier = Modifier
            .height(50.dp)
            .border(2.dp, borderColor, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(50),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) contentColor else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = categoryIconRes),
                    contentDescription = categoryName,
                    tint = if (isSelected) colorScheme.onPrimary else contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = categoryName,
                color = if (isSelected) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun AiSuggestionCard(
    modifier: Modifier = Modifier,
    topDestination: com.example.smarttravel.model.Destination?,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val aiLoadingComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.robot))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // Để hiển thị Box bên trong
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Thêm bóng nhẹ cho nổi bật trên nền trắng
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD), // Xanh dương rất nhạt (Material Blue 50) - Điểm bắt đầu
                            Color(0xFFF1F8FF), // Trung gian
                            Color(0xFFFFFFFF)  // Trắng tinh - Điểm kết thúc
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp), // Tăng padding một chút cho thoáng
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(IntrinsicSize.Min)
                ) {
                    // --- TIÊU ĐỀ: Đổi sang màu Xanh Đậm ---
                    Text(
                        text = "Gợi ý hôm nay ✨",
                        color = Color(0xFF0D47A1), // Blue 900 (Xanh Navy đậm)
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoading) {
                        LottieAnimation(
                            composition = aiLoadingComposition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(48.dp)
                        )
                    } else if (topDestination != null) {
                        // --- NỘI DUNG CHÍNH: Màu đen hoặc xám đậm ---
                        Text(
                            text = "Dựa trên sở thích của bạn: ${topDestination.name}!",
                            color = Color(0xFF1E1E1E), // Màu gần đen để dễ đọc nhất
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = topDestination.location_name,
                            color = Color(0xFF546E7A), // Blue Grey 600 (Xám xanh)
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Cập nhật sở thích để nhận gợi ý cá nhân hóa",
                            color = Color(0xFF1E1E1E),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // --- NÚT ACTION: Màu Primary ---
                    Text(
                        text = "Xem chi tiết →",
                        color = Color(0xFF1976D2), // Blue 700 (Màu xanh thương hiệu)
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                // --- ICON BÊN PHẢI ---
                // Vì nền sáng, ta làm nền icon đậm lên để tương phản
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color(0xFFE1F5FE), // Nền icon xanh nhạt
                            shape = CircleShape
                        )
                        .border(1.dp, Color(0xFFB3E5FC), CircleShape), // Viền nhẹ
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_chat),
                        contentDescription = "AI Suggestion",
                        tint = Color(0xFF0288D1), // Icon màu xanh đậm
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentPlanSection(
    recentPlan: com.example.smarttravel.data.model.TravelPlan?,
    isLoading: Boolean,
    onClick: () -> Unit,
    onPlanClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tiếp tục kế hoạch",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            Text(
                text = "Xem tất cả",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onClick() }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        // Hiển thị kế hoạch thật hoặc placeholder
        if (isLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Đang tải...",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else if (recentPlan != null) {
            // Format dates
            val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dateRange = if (recentPlan.startDate != null && recentPlan.endDate != null) {
                val start = recentPlan.startDate.toDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                val end = recentPlan.endDate.toDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                "${start.format(dateFormatter)} - ${end.format(dateFormatter)}"
            } else {
                "Chưa có ngày"
            }
            
            // Xác định trạng thái
            val status = if (recentPlan.startDate != null && recentPlan.endDate != null) {
                val today = java.time.LocalDate.now()
                val start = recentPlan.startDate.toDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                val end = recentPlan.endDate.toDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                
                when {
                    today.isBefore(start) -> "Sắp diễn ra"
                    today.isAfter(end) -> "Đã kết thúc"
                    else -> "Đang diễn ra"
                }
            } else {
                "Chưa xác định"
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onPlanClick(recentPlan.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recentPlan.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$dateRange • $status",
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            // Không có kế hoạch nào
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Chưa có kế hoạch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tạo kế hoạch mới để bắt đầu",
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LargeDestinationCard(
    name: String,
    rating: Double,
    imageUrl: String,
    destinationId: String,
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(250.dp)
            .height(320.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onCardClick() }
        ) {
            val isNetworkImage = imageUrl.startsWith("http") || imageUrl.startsWith("https://")
            if (isNetworkImage) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl).crossfade(true).build(),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val context = LocalContext.current
                val resId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
                val painter = if (resId != 0) painterResource(id = resId) else painterResource(id = R.drawable.ic_launcher_foreground)
                Image(
                    painter = painter,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 500f
                        )
                    )
            )
            IconButton(
                onClick = onBookmarkClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        if (isBookmarked) Color(0xFFFFC107).copy(alpha = 0.9f)
                        else Color.Black.copy(alpha = 0.3f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (isBookmarked) "Bỏ lưu" else "Lưu",
                    tint = Color.White
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$rating",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SmarttravelTheme {
        // Preview cần mock ViewModel hoặc chạy trên máy ảo/thiết bị thật
    }
}