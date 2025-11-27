package com.example.smarttravel.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.example.smarttravel.model.Destination
import com.example.smarttravel.navigation.Screen

// Màu chủ đạo & Màu nền
private val AppPrimaryColor = Color(0xFF037CAC)
private val BackgroundColor = Color(0xFFF8F9FA) // Xám rất nhạt cho nền tổng thể
private val TextPrimary = Color(0xFF1A1C1E)
private val TextSecondary = Color(0xFF6C757D)

@Composable
fun SavedDestinationsScreen(
    navController: NavController,
    viewModel: com.example.smarttravel.ui.viewmodel.SavedDestinationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            ModernTopBar(
                title = "Bộ sưu tập",
                count = uiState.destinations.size,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppPrimaryColor)
                    }
                }
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error ?: "Đã xảy ra lỗi không xác định",
                        onRetry = { viewModel.loadSavedDestinations() }
                    )
                }
                uiState.destinations.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp) // Khoảng cách thoáng hơn
                    ) {
                        items(
                            items = uiState.destinations,
                            key = { it.id }
                        ) { destination ->
                            SavedDestinationItem(
                                destination = destination,
                                onClick = {
                                    navController.navigate(Screen.Detail.createRoute(destination.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTS ---

@Composable
fun ModernTopBar(
    title: String,
    count: Int,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút Back tròn, nhẹ nhàng
        Surface(
            onClick = onBackClick,
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (count > 0) {
                Text(
                    text = "$count địa điểm",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun SavedDestinationItem(
    destination: Destination,
    onClick: () -> Unit
) {
    // Card thiết kế theo phong cách Minimalist
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp) // Chiều cao cố định
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.1f) // Bóng mềm màu đen nhạt
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Column {
            // Phần Ảnh (Chiếm 65% chiều cao)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
            ) {
                val imageUrl = destination.images.firstOrNull() ?: ""
                val isNetworkImage = imageUrl.startsWith("http")

                if (isNetworkImage) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = destination.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Xử lý ảnh local (fallback)
                    val context = LocalContext.current
                    val resId = try {
                        context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
                    } catch (e: Exception) { 0 }

                    Image(
                        painter = if (resId != 0) painterResource(id = resId) else painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Nút Bookmark (Góc trên phải) - Nhỏ gọn, tinh tế
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Đã lưu",
                        tint = AppPrimaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Rating Badge (Góc dưới trái của ảnh)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107), // Màu vàng của sao
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${destination.rating}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Phần Thông tin (Chiếm 35% chiều cao)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Tên địa điểm
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextPrimary
                )

                // Địa chỉ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = destination.location_name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Giá tiền hoặc CTA (Call to action)
                // Giả sử có estimated_cost, nếu không có thì hiện Text khác
                val priceText = java.text.NumberFormat.getIntegerInstance(java.util.Locale("vi", "VN"))
                    .format(destination.estimated_cost) + "đ"

                Text(
                    text = "Chỉ từ $priceText / người", // Hoặc "Xem chi tiết"
                    style = MaterialTheme.typography.labelLarge,
                    color = AppPrimaryColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hình minh họa Empty dạng vector hoặc icon lớn với vòng tròn nền
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)), // Màu xanh rất nhạt
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = AppPrimaryColor
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Chưa có gì ở đây cả",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Hãy thả tim những địa điểm bạn thích để dễ dàng tìm lại sau này nhé!",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFEF5350) // Đỏ nhẹ
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Úi, có lỗi rồi!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryColor),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
        ) {
            Text("Thử lại", fontWeight = FontWeight.Bold)
        }
    }
}