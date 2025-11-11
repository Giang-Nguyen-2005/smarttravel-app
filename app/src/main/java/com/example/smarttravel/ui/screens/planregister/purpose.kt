package com.example.smarttravel.ui.screens.planregister

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smarttravel.ui.components.AppBottomBar
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.viewmodel.PlanViewModel

@Composable
fun PurposeScreen(
    navController: NavController,
    viewModel: PlanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPurposes = uiState.purposes // Đọc từ uiState

    val purposes = listOf(
        TravelPurpose("Nghỉ dưỡng & Thư giãn", "🐱"),
        TravelPurpose("Khám phá Thiên nhiên", "🌳"),
        TravelPurpose("Du lịch Biển", "🏖️"),
        TravelPurpose("Văn hóa & Lịch sử", "🏛️"),
        TravelPurpose("Khám phá Ẩm thực", "🍜"),
        TravelPurpose("Thành phố - Sôi động", "🎉"),
        TravelPurpose("Dành cho Gia đình", "👨‍👩‍👧‍👦"),
        TravelPurpose("Phiêu lưu", "🧗"),
        TravelPurpose("Mua sắm", "🛍️"),
        TravelPurpose("Sức khỏe & Yoga", "🧘"),
        TravelPurpose("Du lịch Bụi (Backpacking)", "🎒"),
        TravelPurpose("Nhiếp ảnh", "📸"),
        TravelPurpose("Tâm linh & Chùa chiền", "🙏"),
        TravelPurpose("Nightlife", "💃"),
        TravelPurpose("Thể thao", "⛷️"),
        TravelPurpose("Sự kiện & Âm nhạc", "🎶"),
    )

    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = Screen.Profile.route)
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
                //  🔙  Nút quay lại + tiến trình
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp)
                ) {
                    AppTopBar(onBackClick = { navController.popBackStack() })
                    Spacer(modifier = Modifier.width(12.dp))
                    LinearProgressIndicator(
                        progress = { 1f },
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
                    text = "Chuyến đi này mang màu sắc của bạn ✨",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            item {
                Text(
                    text = "Hãy chọn những loại hình du lịch bạn yêu thích để AI có thể gợi ý lịch trình phù hợp nhất.",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            items(items = purposes, key = { it.title }) { purpose ->
                val isSelected = selectedPurposes.contains(purpose.title)
                PurposeCard(
                    title = purpose.title,
                    emoji = purpose.emoji,
                    isSelected = isSelected,
                    onClick = {
                        viewModel.togglePurpose(purpose.title)
                    }
                )
            }

            item {
                PrimaryButton(
                    text = "Tiếp tục",
                    onClick = {
                        // Chuyển sang màn hình cuối
                        navController.navigate(Screen.PlanSummary.route)
                    },
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

data class TravelPurpose(
    val title: String,
    val emoji: String,
)

@Composable
fun PurposeCard(
    title: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF037CAC) else Color.Transparent
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = "$title ${emoji}",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}
