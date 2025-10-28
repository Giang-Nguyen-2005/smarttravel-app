package com.example.smarttravel.ui.screens.plan_detail
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smarttravel.R // <-- Import R
import com.example.smarttravel.ui.components.PrimaryButton // <-- TÁI SỬ DỤNG
import com.example.smarttravel.ui.theme.SmarttravelTheme

// --- Dữ liệu giả ---
data class PlanDayActivity(
    val time: String,
    val description: String
)
data class PlanDay(
    val dayTitle: String,
    val date: String,
    val imageUrl: String, // Ảnh đại diện cho ngày
    val activities: List<PlanDayActivity>
)
data class PlanDetail(
    val id: Int,
    val title: String,
    val dateRange: String,
    val coverImageUrl: String,
    val rating: Double,
    val duration: String,
    val participants: String,
    val estimatedCost: String,
    val days: List<PlanDay>
)

val dummyPlanDetail = PlanDetail(
    id = 1,
    title = "Khám phá Vịnh Hạ Long",
    dateRange = "22/11 - 24/11/2025",
    coverImageUrl = "ha_long",
    rating = 4.8,
    duration = "3 ngày 2 đêm",
    participants = "2 người",
    estimatedCost = "2.500.000đ",
    days = listOf(
        PlanDay(
            dayTitle = "Ngày 1: Tham quan Hang động",
            date = "Thứ Sáu, 22/11",
            imageUrl = "avatar", // Ảnh hang Sửng Sốt
            activities = listOf(
                PlanDayActivity("9:00", "Tham quan hang Sửng Sốt"),
                PlanDayActivity("14:00", "Chèo kayak khám phá vịnh")
            )
        ),
        PlanDay(
            dayTitle = "Ngày 2: Đảo Titop & Làng chài",
            date = "Thứ Bảy, 23/11",
            imageUrl = "ha_long", // Ảnh đảo Titop
            activities = listOf(
                PlanDayActivity("8:00", "Tắm biển, leo núi tại đảo Titop"),
                PlanDayActivity("15:00", "Thăm làng chài Cửa Vạn")
            )
        )
    )
)
// --- Kết thúc dữ liệu giả ---

@Composable
fun PlanDetailScreen(
    navController: NavController,
    plan: PlanDetail // Truyền dữ liệu thật vào đây
) {
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
                        .height(300.dp) // Chiều cao ảnh bìa
                )
            }

            // 2. Header thông tin (tên, ngày, sao)
            item {
                PlanInfoHeader(plan = plan)
            }

            // 3. Thông tin tổng quan (ngày, người, tiền)
            item {
                OverviewSection(plan = plan)
            }

            // 4. Lịch trình chi tiết theo ngày
            itemsIndexed(plan.days) { index, day ->
                PlanDayItem(day = day, dayNumber = index + 1)
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

        // 6. Nút Back & Bookmark (Nổi lên trên)
        PlanTopControls(
            onBackClick = { navController.popBackStack() },
            onBookmarkClick = { /*TODO*/ }
        )
    }
}

// --- CÁC COMPONENT CON ---

@Composable
fun ImageHeader(imageUrl: String, modifier: Modifier = Modifier) {
    // Logic tải ảnh từ drawable
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
    val painter = if (resId != 0) painterResource(id = resId) else painterResource(id = R.drawable.ic_launcher_foreground)

    Box(modifier = modifier) {
        Image(
            painter = painter,
            contentDescription = "Cover Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
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
fun PlanTopControls(onBackClick: () -> Unit, onBookmarkClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút Back tròn, nền mờ, icon trắng (Giống DetailScreen)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }
}


@Composable
fun PlanInfoHeader(plan: PlanDetail) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = plan.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = plan.dateRange,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun OverviewSection(plan: PlanDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        OverviewItem(icon = Icons.Default.CalendarMonth, text = plan.duration)
        OverviewItem(icon = Icons.Default.Group, text = plan.participants)
        OverviewItem(icon = Icons.Default.MonetizationOn, text = plan.estimatedCost)
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
}

@Composable
fun OverviewItem(icon: ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun PlanDayItem(day: PlanDay, dayNumber: Int) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        // Tiêu đề ngày
        Text(
            text = "Ngày $dayNumber: ${day.dayTitle}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = day.date,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Ảnh và hoạt động
        Row(modifier = Modifier.fillMaxWidth()) {
            // Ảnh nhỏ
            val context = LocalContext.current
            val resId = context.resources.getIdentifier(day.imageUrl, "drawable", context.packageName)
            val painter = if (resId != 0) painterResource(id = resId) else painterResource(id = R.drawable.ic_launcher_foreground)
            Image(
                painter = painter,
                contentDescription = day.dayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Danh sách hoạt động
            Column {
                day.activities.forEach { activity ->
                    Row {
                        Text(
                            text = "${activity.time}: ",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = activity.description,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun PlanDetailScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        PlanDetailScreen(navController = navController, plan = dummyPlanDetail)
    }
}