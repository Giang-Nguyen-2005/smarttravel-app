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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    // Sử dụng Scaffold để chứa AppBottomBar
    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = Screen.Home.route)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
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
                    Text("Lỗi: ${destinationState.error}", modifier = Modifier.padding(16.dp), color = Color.Red)
                } else if (destinationState.destinations.isEmpty()) {
                    Text("Không tìm thấy địa điểm phù hợp.", modifier = Modifier.padding(16.dp), color = Color.Gray)
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
                // Tạm thời hiển thị top 5 địa điểm đầu tiên
                if (!destinationState.isLoading && destinationState.destinations.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(destinationState.destinations.take(5)) { destination ->
                            LargeDestinationCard(
                                modifier = Modifier.clickable {
                                    navController.navigate(Screen.Detail.createRoute(destination.id))
                                },
                                name = destination.name,
                                rating = destination.rating,
                                imageUrl = destination.images.firstOrNull() ?: "ha_long"
                            )
                        }
                    }
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
                .background(Color(0xFFD9D9D9).copy(alpha = 0.5f))
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
                .background(Color(0xFFEEEEEE).copy(alpha = 0.8f))
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Thông báo",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
    val backgroundColor = if (isSelected) Color.White else Color(0xFFF5F5F5)
    val contentColor = if (isSelected) Color(0xFF037CAC) else Color(0xFF00838F)
    val borderColor = if (isSelected) Color(0xFF037CAC) else Color(0xFFEEEEEE)
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
                    tint = if (isSelected) Color.White else contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = categoryName,
                color = Color.Black,
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        // Sử dụng màu gradient hoặc màu nổi bật cho AI
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF7FF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Gợi ý hôm nay ✨",
                    color = Color(0xFF037CAC),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF037CAC)
                    )
                } else if (topDestination != null) {
                    Text(
                        text = "Dựa trên sở thích của bạn: ${topDestination.name}!",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = topDestination.location_name,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Cập nhật sở thích để nhận gợi ý cá nhân hóa",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Xem chi tiết →",
                    color = Color(0xFF037CAC),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Ảnh minh họa cho AI (có thể thay bằng robot hoặc icon phù hợp)
            Icon(
                painter = painterResource(id = R.drawable.icon_chat), // Tạm dùng icon chat
                contentDescription = "AI Suggestion",
                tint = Color(0xFF037CAC).copy(alpha = 0.8f),
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, CircleShape)
                    .padding(16.dp)
            )
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
                fontWeight = FontWeight.Bold
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
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
                        color = Color.Gray,
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recentPlan.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$dateRange • $status",
                            color = Color.Gray,
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Chưa có kế hoạch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tạo kế hoạch mới để bắt đầu",
                            color = Color.Gray,
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(250.dp)
            .height(320.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                onClick = { /* TODO */ },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
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