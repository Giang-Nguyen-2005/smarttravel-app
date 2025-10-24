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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

data class Category(val name: String, val iconRes: Int)
data class Destination(val id: Int, val name: String, val location: String, val rating: Double, val price: Int, val imageUrl: String)
data class LargeDestination(val id: Int, val name: String, val rating: Double, val imageUrl: String)

val dummyCategories = listOf(
    Category("Hồ", R.drawable.icon_lake),
    Category("Biển", R.drawable.icon_beach),
    Category("Núi", R.drawable.icon_mountain),
    Category("Rừng", R.drawable.icon_forest)
)

val dummyDestinations = listOf(
    Destination(1, "Hồ Lắk", "Idaho", 4.5, 40, "ha_long"), // Tên file ảnh trong drawable
    Destination(2, "Putang inomo", "Canada", 4.5, 40, "ha_long"),
    Destination(3, "Ba Vì", "Canada", 4.7, 50, "ha_long")
)

val dummyLargeDestinations = listOf(
    LargeDestination(1, "Vịnh Hạ Long", 4.8, "avatar"), // Tên file ảnh trong drawable
    LargeDestination(2, "Ba Vi", 4.6, "ha_long")
)
// --- Kết thúc Dữ liệu giả ---


@Composable
fun HomeScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Nền trắng cho toàn màn hình
    ) {
        // 1. Top Bar Tùy Chỉnh (Nguyễn Văn A & Chuông)
        item {
            HomeTopBar(
                userName = "Nguyễn Văn A",
                onNotificationClick = {}
            )
        }

        // 2. Danh Mục (Hồ, Biển, Núi...)
        item {
            CategorySection(
                categories = dummyCategories
            )
        }

        // 3. Hàng cuộn ngang (Sử dụng DestinationCard của bạn)
        // (Không có tiêu đề "Gợi ý" trong ảnh, nên tôi bỏ qua)
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(dummyDestinations) { destination ->
                    DestinationCard(
                        imageUrl = destination.imageUrl,
                        title = destination.name,
                        location = destination.location,
                        rating = destination.rating
                    )
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

        // 5. Hàng cuộn ngang cho Card Lớn
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(dummyLargeDestinations) { destination ->

                    LargeDestinationCard(destination = destination)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

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
                modifier = Modifier.size(32.dp).clip(CircleShape)
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

@Composable
fun CategorySection(
    categories: List<Category>
) {
    var selectedCategory by remember { mutableStateOf(categories.first().name) }

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
                    category = category,
                    isSelected = category.name == selectedCategory,
                    onClick = { selectedCategory = category.name }
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: Category,
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
                    painter = painterResource(id = category.iconRes),
                    contentDescription = category.name,
                    tint = if (isSelected) Color.White else contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Text
            Text(
                text = category.name,
                color = Color.Black, // Text luôn là màu đen
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun LargeDestinationCard(
    destination: LargeDestination
) {
    // Card lớn cho "Điểm đến tốt nhất" (Vịnh Hạ Long)
    Card(
        modifier = Modifier
            .width(250.dp)
            .height(320.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Logic tải ảnh giống hệt DestinationCard.kt [cite: 42-52]
            val imageUrl = destination.imageUrl
            val isNetworkImage = imageUrl.startsWith("http") || imageUrl.startsWith("https://")

            if (isNetworkImage) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl).crossfade(true).build(),
                    contentDescription = destination.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val context = LocalContext.current
                val resId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
                val painter = if (resId != 0) painterResource(id = resId) else painterResource(id = R.drawable.ic_launcher_foreground)

                Image(
                    painter = painter,
                    contentDescription = destination.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Lớp phủ Gradient mờ ở dưới
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 600f
                        )
                    )
            )

            IconButton(
                onClick = { /*TODO*/ },
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

            // Text nội dung ở dưới
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = destination.name,
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
                        text = "${destination.rating}",
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