package com.example.smarttravel.ui.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.smarttravel.R
// TÁI SỬ DỤNG COMPONENT NÚT BẤM CỦA BẠN
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.theme.SmarttravelTheme

// --- Dữ liệu giả ---
data class DetailDestination(
    val id: Int,
    val name: String,
    val location: String,
    val subLocation: String,
    val rating: Double,
    val reviews: Int,
    val price: Int,
    val imageUrl: String,
    val galleryUrls: List<String>,
    val description: String
)

val dummyDetail = DetailDestination(
    id = 1,
    name = "Vịnh Hạ Long",
    location = "Quảng Ninh, Việt Nam",
    subLocation = "Quảng Ninh",
    rating = 4.7,
    reviews = 2498,
    price = 59,
    imageUrl = "ha_long", // Tên ảnh trong res/drawable
    galleryUrls = listOf("avatar", "avatar", "avatar", "avatar", "avatar"),
    description = "Vịnh Hạ Long được UNESCO công nhận là Di sản Thiên nhiên Thế giới với hàng nghìn hòn đảo đá vôi lớn nhỏ giữa biển xanh. Đây là điểm đến nổi tiếng với cảnh quan kỳ vĩ, thích hợp cho cả nghỉ dưỡng và khám phá."
)
// --- Kết thúc dữ liệu giả ---

@Composable
fun DetailScreen(
    navController: NavController,
    destination: DetailDestination // Truyền dữ liệu thật vào đây
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. Hình ảnh nền
        ImageHeader(
            imageUrl = destination.imageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp) // Chiều cao của ảnh
        )

        // 2. Nội dung cuộn (Tờ giấy trắng)
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Spacer trống để "đẩy" nội dung xuống
            // Chiều cao spacer = chiều cao ảnh - 60dp (để tờ giấy đè lên ảnh)
            item {
                Spacer(modifier = Modifier.height(340.dp))
            }

            // Đây là tờ giấy trắng
            item {
                ContentSheet(destination = destination)
            }
        }

        // 3. Các nút điều khiển (Back, Bookmark)
        // Đặt ở trên cùng, xếp chồng lên trên tất cả
        TopControls(
            onBackClick = { navController.popBackStack() },
            onBookmarkClick = { /*TODO*/ }
        )
    }
}

@Composable
fun ImageHeader(imageUrl: String, modifier: Modifier = Modifier) {
    // Logic tải ảnh từ drawable (giống HomeScreen)
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
    val painter = if (resId != 0) painterResource(id = resId) else painterResource(id = R.drawable.ic_launcher_foreground)

    Box(modifier = modifier) {
        Image(
            painter = painter,
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Lớp phủ mờ ở dưới ảnh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                        startY = 600f
                    )
                )
        )
    }
}

@Composable
fun TopControls(onBackClick: () -> Unit, onBookmarkClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 48.dp), // Đẩy xuống dưới thanh status
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút Back
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Nút Bookmark
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
                .clickable { onBookmarkClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = "Bookmark",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ContentSheet(destination: DetailDestination) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
        // Bo góc chỉ ở trên
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp)
        ) {
            // Thanh kéo (Drag Handle)
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tiêu đề
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = destination.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hàng thông tin (Location, Rating, Price)
            InfoRow(destination = destination)

            Spacer(modifier = Modifier.height(24.dp))

            // Hàng ảnh Gallery
            GalleryRow(galleryUrls = destination.galleryUrls)

            Spacer(modifier = Modifier.height(24.dp))

            // Mô tả
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Mô tả điểm đến",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${destination.description} ${destination.description}", // Lặp lại cho dài
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    lineHeight = 22.sp // Giãn dòng
                )
                Text(
                    text = "..Đọc thêm.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút bấm
            PrimaryButton( // Tái sử dụng component của bạn [cite: 61]
                text = "Xem Gợi Ý Hành Trình",
                onClick = { /*TODO*/ },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun InfoRow(destination: DetailDestination) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoItem(
            icon = Icons.Default.LocationOn,
            text = destination.subLocation
        )
        InfoItem(
            icon = Icons.Default.Star,
            text = "${destination.rating} (${destination.reviews})"
        )
        Text(
            text = "$${destination.price}/Người",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun GalleryRow(galleryUrls: List<String>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(galleryUrls) { imageUrl ->
            // Logic tải ảnh
            val context = LocalContext.current
            val resId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
            val painter = if (resId != 0) painterResource(id = resId) else painterResource(id = R.drawable.ic_launcher_foreground)

            Image(
                painter = painter,
                contentDescription = "Gallery Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        DetailScreen(navController = navController, destination = dummyDetail)
    }
}