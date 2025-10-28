package com.example.smarttravel.ui.screens.schedule

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smarttravel.R
import com.example.smarttravel.ui.screens.home.HomeTopBar
import com.example.smarttravel.ui.theme.SmarttravelTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

data class SavedPlan(
    val id: Int,
    val title: String,
    val dateRange: String,
    val location: String,
    val imageUrl: String,
)

val dummyPlans = listOf(
    SavedPlan(1, "Khám phá Vịnh Hạ Long", "22/11 - 24/11/2025", "Quảng Ninh", "ha_long"),
    SavedPlan(2, "Thăm phố sương mù Đà Lạt", "05/12 - 08/12/2025", "Lâm Đồng", "avatar"),
    SavedPlan(3, "Nghỉ dưỡng Phú Quốc", "10/01 - 15/01/2026", "Kiên Giang", "ha_long")
)

@Composable
fun PlanScreen(navController: NavController) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                HomeTopBar(
                    userName = "Nguyễn Văn A",
                    onNotificationClick = {}
                )
            }

            // Lịch dạng tuần (horizontal)
            item {
                WeeklyCalendarSection(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )
            }

            item {
                PlanListHeader(onViewAllClick = { /*TODO*/ })
            }

            items(dummyPlans) { plan ->
                PlanItemCard(plan = plan, onClick = { /*TODO*/ })
            }
        }


        /*FloatingActionButton(

            onClick = { /* TODO: Navigate to create plan */ },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .height(56.dp)
                .fillMaxWidth(0.8f),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tạo kế hoạch",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tạo kế hoạch mới",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        */
    }
}

@Composable
fun WeeklyCalendarSection(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    // Lấy ngày đầu tuần (Thứ Hai) của tuần chứa ngày được chọn
    val startOfWeek = selectedDate.with(DayOfWeek.MONDAY)
    val daysOfWeek = remember(startOfWeek) { // Chỉ tính lại khi startOfWeek thay đổi
        (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }
    // Lấy tháng và năm của ngày đầu tuần để hiển thị
    val displayMonthYear = startOfWeek.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi")))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("vi")) else it.toString() } // Viết hoa chữ cái đầu

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp) // Giảm padding dọc
            .clip(RoundedCornerShape(16.dp)) // Bo góc mềm mại hơn
            .background(Color(0xFFF1F7F8)) // Nền xám nhạt cho cả khu vực lịch
            .padding(16.dp)

    ) {
        // Header (Tháng Năm và nút điều hướng)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayMonthYear, // Hiển thị tháng/năm của tuần hiện tại
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold // Giảm độ đậm
            )
            Row {
                // Nút tuần trước
                IconButton(
                    onClick = { onDateSelected(selectedDate.minusWeeks(1)) }, // Lùi lại 1 tuần
                    modifier = Modifier.size(36.dp) // Giảm kích thước nút
                ) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Tuần trước", modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(4.dp)) // Khoảng cách giữa 2 nút
                // Nút tuần sau
                IconButton(
                    onClick = { onDateSelected(selectedDate.plusWeeks(1)) }, // Tiến tới 1 tuần
                    modifier = Modifier.size(36.dp) // Giảm kích thước nút
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Tuần sau", modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Tăng khoảng cách

        // Hàng chứa các ngày trong tuần
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween // Dàn đều các ngày
        ) {
            daysOfWeek.forEach { date ->
                val isSelected = date == selectedDate
                val isToday = date == today

                // Composable cho một ngày
                DateItem(
                    date = date,
                    isSelected = isSelected,
                    isToday = isToday,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

// Composable riêng cho một item ngày, giúp dễ tùy chỉnh hơn
@Composable
fun DateItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary // Màu xanh khi chọn
        else -> Color.Transparent // Nền trong suốt
    }
    val contentColor = when {
        isSelected -> Color.White // Chữ trắng khi chọn
        isToday -> MaterialTheme.colorScheme.primary // Chữ xanh cho ngày hôm nay
        else -> Color.Black // Chữ đen cho ngày thường
    }
    val dayOfWeekColor = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(width = 45.dp, height = 60.dp) // Kích thước cố định cho mỗi item
            .clip(RoundedCornerShape(12.dp)) // Bo góc mềm mại
            .background(backgroundColor)
            .border( // Thêm viền cho ngày hôm nay (nếu không được chọn)
                width = if (isToday && !isSelected) 1.dp else 0.dp,
                color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 4.dp) // Giảm padding dọc bên trong item
    ) {
        // Thứ (T2, T3...)
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("vi"))
                .replaceFirstChar { it.uppercase() },
            fontSize = 12.sp,
            color = dayOfWeekColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Ngày (18, 19...)
        Text(
            text = date.dayOfMonth.toString(),
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp,
            color = contentColor
        )
    }
}

@Composable
private fun PlanListHeader(onViewAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Kế hoạch đã lưu",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Xem tất cả",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onViewAllClick() }
        )
    }
}

@Composable
fun PlanItemCard(plan: SavedPlan, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val resId = context.resources.getIdentifier(plan.imageUrl, "drawable", context.packageName)
            val painter = if (resId != 0)
                painterResource(id = resId)
            else painterResource(id = R.drawable.ic_launcher_foreground)

            Image(
                painter = painter,
                contentDescription = plan.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(plan.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(plan.dateRange, color = Color.Gray, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(plan.location, color = Color.Gray, fontSize = 14.sp)
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlanScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        PlanScreen(navController)
    }
}
