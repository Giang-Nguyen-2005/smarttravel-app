package com.example.smarttravel.ui.screens.planregister

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smarttravel.ui.components.AppTopBar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.smarttravel.ui.theme.SmarttravelTheme
import com.example.smarttravel.ui.components.AppBottomBar
import com.example.smarttravel.navigation.Screen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.smarttravel.ui.components.PrimaryButton
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextAlign

@Composable
fun PeriodScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = Screen.Calendar.route)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // 🔙 Nút quay lại + tiến trình
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp)
                ) {
                    AppTopBar(onBackClick = { navController.popBackStack() })

                    Spacer(modifier = Modifier.width(12.dp))

                    LinearProgressIndicator(
                        progress = 0.50f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color(0xFFE0E0E0),
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                    )
                }
            }

            item {
                Text(
                    text = "Khi nào chuyến đi của bạn sẽ bắt đầu và kết thúc?📅",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Text(
                    text = "Chọn ngày cho chuyến đi của bạn. Điều này giúp chúng tôi lập kế hoạch hành trình hoàn chỉnh cho khoảng thời gian du lịch của bạn.",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            // 📅 Khung lịch giả lập (có thể thay bằng calendar thực)
            item {
                Text(
                    text = "12 Tháng 10, 2025 - 14 Tháng 10, 2025 ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.width(40.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center
                ) {
                    items(mockCalendarOctober) { date ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (date.isSelected) Color(0xFF4CAF50) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.day.toString(),
                                color = if (date.isSelected) Color.White else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Nút Tiếp tục
            item {
                PrimaryButton(
                    text = "Tiếp tục",
                    onClick = { /* TODO: Điều hướng tiếp theo */ },
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
data class TravelDate(
    val day: Int,           // ví dụ: 12
    val date: String,       // "2025-10-12"
    val dayOfWeek: String,  // "Chủ nhật"
    val isSelected: Boolean = false
)

val mockCalendarOctober = listOf(
    TravelDate(1, "2025-10-01", "Thứ tư"),
    TravelDate(2, "2025-10-02", "Thứ năm"),
    TravelDate(3, "2025-10-03", "Thứ sáu"),
    TravelDate(4, "2025-10-04", "Thứ bảy"),
    TravelDate(5, "2025-10-05", "Chủ nhật"),
    TravelDate(6, "2025-10-06", "Thứ hai"),
    TravelDate(7, "2025-10-07", "Thứ ba"),
    TravelDate(8, "2025-10-08", "Thứ tư"),
    TravelDate(9, "2025-10-09", "Thứ năm"),
    TravelDate(10, "2025-10-10", "Thứ sáu"),
    TravelDate(11, "2025-10-11", "Thứ bảy"),
    TravelDate(12, "2025-10-12", "Chủ nhật", isSelected = true),
    TravelDate(13, "2025-10-13", "Thứ hai", isSelected = true),
    TravelDate(14, "2025-10-14", "Thứ ba", isSelected = true),
    TravelDate(15, "2025-10-15", "Thứ tư"),
    TravelDate(16, "2025-10-16", "Thứ năm"),
    TravelDate(17, "2025-10-17", "Thứ sáu"),
    TravelDate(18, "2025-10-18", "Thứ bảy"),
    TravelDate(19, "2025-10-19", "Chủ nhật"),
    TravelDate(20, "2025-10-20", "Thứ hai"),
    TravelDate(21, "2025-10-21", "Thứ ba"),
    TravelDate(22, "2025-10-22", "Thứ tư"),
    TravelDate(23, "2025-10-23", "Thứ năm"),
    TravelDate(24, "2025-10-24", "Thứ sáu"),
    TravelDate(25, "2025-10-25", "Thứ bảy"),
    TravelDate(26, "2025-10-26", "Chủ nhật"),
    TravelDate(27, "2025-10-27", "Thứ hai"),
    TravelDate(28, "2025-10-28", "Thứ ba"),
    TravelDate(29, "2025-10-29", "Thứ tư"),
    TravelDate(30, "2025-10-30", "Thứ năm"),
    TravelDate(31, "2025-10-31", "Thứ sáu")
)




@Preview(showBackground = true)
@Composable
fun PeriodScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        PeriodScreen(navController = navController)
    }
}

