package com.example.smarttravel.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button // <-- GIỮ NGUYÊN
import androidx.compose.material3.ButtonDefaults // <-- GIỮ NGUYÊN
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator // <-- THÊM MỚI
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // <-- THÊM MỚI
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.smarttravel.R
// TÁI SỬ DỤNG COMPONENT
import com.example.smarttravel.ui.components.DestinationCard
import com.example.smarttravel.ui.theme.SmarttravelTheme
// --- THÊM CÁC IMPORT NÀY ---
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smarttravel.model.Category // <-- THÊM MODEL MỚI
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.viewmodel.AuthViewModel
import com.example.smarttravel.ui.viewmodel.HomeViewModel // <-- THÊM VIEWMODEL MỚI

// --- TOÀN BỘ DATA GIẢ ĐÃ BỊ XÓA ---

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel() // <-- LẤY VIEWMODEL
) {
    // Lắng nghe trạng thái từ ViewModel
    val destinationState by homeViewModel.destinationUiState.collectAsState()
    val categoryState by homeViewModel.categoryUiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. Top Bar Tùy Chỉnh (Giữ nguyên)
        item {
            HomeTopBar(
                userName = "Nguyễn Văn A", // TODO: Lấy userName từ authViewModel
                onNotificationClick = {}
            )
        }

        // 2. Danh Mục
        item {
            // Xử lý trạng thái Loading/Error/Success cho Category
            when {
                categoryState.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                categoryState.error != null -> {
                    Text("Lỗi tải danh mục: ${categoryState.error}", modifier = Modifier.padding(16.dp), color = Color.Red)
                }
                else -> {
                    // Truyền dữ liệu thật vào
                    CategorySection(
                        categories = categoryState.categories
                    )
                }
            }
        }

        item {
            Text(
                text = "Điểm địa gợi ý",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        // 3. Hàng cuộn ngang (Gợi ý)
        item {
            // Xử lý trạng thái Loading/Error/Success cho Destination
            when {
                destinationState.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                destinationState.error != null -> {
                    Text("Lỗi tải địa điểm: ${destinationState.error}", modifier = Modifier.padding(16.dp), color = Color.Red)
                }
                else -> {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Dùng dữ liệu thật
                        items(destinationState.destinations) { destination ->
                            DestinationCard(
                                // Lấy ảnh đầu tiên trong mảng, nếu rỗng thì dùng fallback
                                imageUrl = destination.images.firstOrNull() ?: "ha_long",
                                title = destination.name,
                                location = destination.location_name,
                                // TODO: Thêm trường rating vào Firestore và Destination model
                                rating = 4.8
                            )
                        }
                    }
                }
            }
        }

        // 4. Tiêu đề "Điểm đến tốt nhất"
        item {
            Text(
                text = "Điểm đến tốt nhất",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        // 5. Hàng cuộn ngang (Card Lớn)
        item {
            // Cũng dùng destinationState, chỉ khác là dùng LargeDestinationCard
            if (!destinationState.isLoading && destinationState.error == null) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(destinationState.destinations) { destination ->
                        LargeDestinationCard(
                            name = destination.name,
                            // TODO: Thêm trường rating vào Firestore và Destination model
                            rating = 4.8,
                            imageUrl = destination.images.firstOrNull() ?: "ha_long"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- THÊM NÚT LOGOUT (TEST) ---
        item {
            Button(
                onClick = {
                    authViewModel.logout()
                    // Quay về Splash, nó sẽ tự điều hướng về Login
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("ĐĂNG XUẤT (TEST)")
            }
        }
        // --- KẾT THÚC: THÊM NÚT LOGOUT (TEST) ---
    }
}

// (HomeTopBar giữ nguyên)
@Composable
fun HomeTopBar(
    userName: String,
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
                .background(Color(0xFFD9D9D9).copy(alpha = 0.5f)) // Màu hồng nhạt
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.avatar), // Thay avatar
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = userName,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        // Nút chuông
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFEEEEEE).copy(alpha = 0.8f)) // Nền xám nhạt
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Thông báo",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Hàm helper để map category ID (ví dụ: "nui") sang icon (ví dụ: R.drawable.icon_mountain)
 */
@Composable
fun getIconForCategory(categoryId: String): Int {
    return when (categoryId) {
        "nui" -> R.drawable.icon_mountain
        "bien" -> R.drawable.icon_beach
        "ho" -> R.drawable.icon_lake
        "rung" -> R.drawable.icon_forest
        // Icon mặc định nếu không khớp
        else -> R.drawable.ic_launcher_foreground
    }
}

@Composable
fun CategorySection(
    categories: List<Category> // <-- SỬ DỤNG MODEL MỚI
) {
    // Lấy id của category đầu tiên làm mục được chọn mặc định
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }

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
                // DÙNG COMPONENT MỚI CHO CATEGORY
                CategoryChip(
                    categoryName = category.name, // <-- Dùng tên thật
                    categoryIconRes = getIconForCategory(category.id), // <-- Dùng icon đã map
                    isSelected = category.id == selectedCategory,
                    onClick = { selectedCategory = category.id }
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    categoryName: String, // <-- Sửa tham số
    categoryIconRes: Int, // <-- Sửa tham số
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
            // Icon
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) contentColor else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = categoryIconRes), // <-- Dùng icon đã map
                    contentDescription = categoryName,
                    tint = if (isSelected) Color.White else contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Text
            Text(
                text = categoryName, // <-- Dùng tên thật
                color = Color.Black, // Text luôn là màu đen
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun LargeDestinationCard(
    // Sửa lại tham số để nhận dữ liệu thật
    name: String,
    rating: Double,
    imageUrl: String
) {
    // Card lớn cho "Điểm đến tốt nhất" (Vịnh Hạ Long)
    Card(
        modifier = Modifier
            .width(250.dp)
            .height(320.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Logic tải ảnh
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
            // Lớp phủ Gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 1f)),
                            startY = 600f
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
            // Text nội dung
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$rating",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        HomeScreen(navController = navController)
    }
}