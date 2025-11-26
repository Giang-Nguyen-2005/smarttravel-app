package com.example.smarttravel.ui.screens.ai_suggestions

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AutoAwesome
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.smarttravel.R
import com.example.smarttravel.model.Destination
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// --- MÀU SẮC ĐỒNG BỘ VỚI APP ---
val AppPrimaryColor = Color(0xFF037CAC) // Xanh chủ đạo của app
val AppSecondaryColor = Color(0xFF00C6FF) // Xanh sáng để tạo Gradient
val TextDark = Color(0xFF1A1A1A)
val TextLight = Color(0xFF757575)

@Composable
fun AiSuggestionsScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val aiSuggestionsState by homeViewModel.aiSuggestionsState.collectAsState()
    val userProfile by homeViewModel.userProfile.collectAsState()

    // Load data
    LaunchedEffect(Unit) {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        if (aiSuggestionsState.cachedDate != today || aiSuggestionsState.destinations.isEmpty()) {
            homeViewModel.loadAiSuggestions()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA) // Nền sáng nhẹ
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background Gradient Xanh (Mềm mại hơn)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                AppPrimaryColor.copy(alpha = 0.15f), // Xanh nhạt
                                Color.Transparent
                            )
                        )
                    )
            )

            // 2. Nội dung chính
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header
                CustomHeader(navController)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // --- LỜI CHÀO & INSIGHT ---
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                text = "Gợi ý thông minh",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Dành riêng cho chuyến đi tiếp theo của bạn",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextLight
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Insight Card Xanh
                            BlueInsightCard(
                                interests = userProfile?.interests ?: emptyList()
                            )
                        }
                    }

                    // --- STATES: LOADING (Đưa lên ưu tiên để check trước) ---
                    if (aiSuggestionsState.isLoading) {
                        item {
                            // Gọi LoadingView ở đây
                            LoadingView()
                        }
                    } else if (aiSuggestionsState.error != null) {
                        item { SimpleStateView(icon = Icons.Default.ErrorOutline, message = "Lỗi: ${aiSuggestionsState.error}") }
                    } else if (aiSuggestionsState.destinations.isEmpty()) {
                        item { SimpleStateView(icon = Icons.Default.TravelExplore, message = "Chưa tìm thấy gợi ý phù hợp.") }
                    } else {
                        // --- NẾU CÓ DỮ LIỆU MỚI HIỆN CÁC PHẦN DƯỚI ---

                        // --- TOP PICK (HERO CARD) ---
                        if (aiSuggestionsState.destinations.isNotEmpty()) {
                            val topPick = aiSuggestionsState.destinations.first()
                            item {
                                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    SectionTitle(title = "Phù hợp nhất", icon = Icons.Rounded.AutoAwesome)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HeroDestinationCard(
                                        destination = topPick,
                                        onClick = { navController.navigate(Screen.Detail.createRoute(topPick.id)) }
                                    )
                                }
                            }
                        }

                        // --- DANH SÁCH PHỤ ---
                        if (aiSuggestionsState.destinations.size > 1) {
                            val otherPicks = aiSuggestionsState.destinations.drop(1)
                            item {
                                Column {
                                    PaddingBox {
                                        SectionTitle(title = "Khám phá thêm", icon = Icons.Default.Explore)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(otherPicks) { destination ->
                                            VerticalDestinationCard(
                                                destination = destination,
                                                onClick = { navController.navigate(Screen.Detail.createRoute(destination.id)) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= COMPOSABLES (BLUE THEME) =================

@Composable
fun CustomHeader(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .size(44.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, Color(0xFFEEEEEE), CircleShape)
                // Đổ bóng nhẹ
                .shadow(2.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextDark,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PaddingBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
        content()
    }
}

@Composable
fun SectionTitle(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppPrimaryColor, // Dùng màu xanh App
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
    }
}

@Composable
fun BlueInsightCard(interests: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            // Viền xanh nhạt tinh tế
            .border(1.dp, AppPrimaryColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Icon AI Xanh
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppPrimaryColor.copy(alpha = 0.1f)), // Nền xanh nhạt
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = AppPrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "AI Phân tích sở thích",
                    style = MaterialTheme.typography.labelLarge,
                    color = AppPrimaryColor, // Chữ xanh
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (interests.isNotEmpty()) {
                    Text(
                        text = "Gợi ý dựa trên: ${interests.take(3).joinToString(", ")}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLight,
                        lineHeight = 20.sp
                    )
                } else {
                    Text(
                        text = "Đang tìm hiểu sở thích du lịch của bạn...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLight
                    )
                }
            }
        }
    }
}

@Composable
fun HeroDestinationCard(destination: Destination, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            // Đổ bóng xanh nhẹ
            .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = AppPrimaryColor.copy(alpha = 0.2f), spotColor = AppPrimaryColor.copy(alpha = 0.2f))
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageUrl = destination.images.firstOrNull() ?: "ha_long"
            NetworkOrLocalImage(
                url = imageUrl,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient đen phía dưới
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

            // Badge % Match (Màu Xanh Đậm nổi bật)
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = AppPrimaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "98% Match",
                        color = AppPrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Nội dung text
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFFDDDDDD), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = destination.location_name,
                        color = Color(0xFFDDDDDD),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    // Rating Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFC107))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, null, tint = Color.Black, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${destination.rating}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    // Nút màu xanh chuẩn của app
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryColor),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Xem lịch trình", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VerticalDestinationCard(destination: Destination, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(260.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.weight(1.8f)) {
                val imageUrl = destination.images.firstOrNull() ?: "ha_long"
                NetworkOrLocalImage(
                    url = imageUrl,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("${destination.rating}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = TextLight, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = destination.location_name,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun NetworkOrLocalImage(url: String, modifier: Modifier = Modifier) {
    if (url.startsWith("http")) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url).crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        val context = LocalContext.current
        val resId = remember(url) {
            context.resources.getIdentifier(url, "drawable", context.packageName)
        }
        if (resId != 0) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        } else {
            Box(modifier = modifier.background(Color.LightGray))
        }
    }
}

@Composable
fun LoadingView() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.robot))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp), // Chiếm chiều cao lớn để đẩy xuống giữa [cite: 195]
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(280.dp) // To hơn (gấp đôi cũ) [cite: 195]
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "AI đang suy nghĩ...",
            style = MaterialTheme.typography.titleMedium,
            color = AppPrimaryColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Đang tìm kiếm địa điểm tốt nhất cho bạn",
            style = MaterialTheme.typography.bodyMedium,
            color = TextLight,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun SimpleStateView(icon: ImageVector, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = TextLight, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = TextLight, style = MaterialTheme.typography.bodyMedium)
    }
}