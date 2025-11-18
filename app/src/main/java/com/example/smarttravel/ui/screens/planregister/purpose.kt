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
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.viewmodel.PlanViewModel

@Composable
fun PurposeScreen(
    navController: NavController,
    viewModel: PlanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPurposes = uiState.purposes // Giả định đây là Set<String> hoặc List<String>
    val selectedCount = selectedPurposes.size
    val maxSelection = 3 // Giới hạn tối đa 3 lựa chọn

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
        TravelPurpose("Cuộc sống về đêm (Nightlife)", "💃"),
        TravelPurpose("Thể thao (Trượt tuyết, lặn...)", "⛷️"),
        TravelPurpose("Sự kiện & Âm nhạc", "🎶"),
        TravelPurpose("Phượt xe (Road Trip)", "🏍️"),
        TravelPurpose("Tình nguyện", "🤝")
    )

    Scaffold{ paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // --- HEADER CỐ ĐỊNH (STICKY HEADER) - CĂN GIỮA TUYỆT ĐỐI THANH TIẾN ĐỘ ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp)
                    .padding(top = 60.dp, bottom = 16.dp)
            ) {
                // Nút Back (Căn trái)
                Box(modifier = Modifier.align(Alignment.CenterStart)) {
                    AppTopBar(onBackClick = { navController.popBackStack() })
                }

                // Thanh tiến trình (Căn giữa tuyệt đối trong Box)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f) // Giới hạn chiều rộng của thanh progress
                        .align(Alignment.Center)
                ) {
                    LinearProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color(0xFFE0E0E0),
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                    )
                }
            }
            // --- KẾT THÚC HEADER CỐ ĐỊNH ---

            // --- PHẦN NỘI DUNG CUỘN (LazyColumn) ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Spacer để bù đắp khoảng trống dưới sticky header
                item { Spacer(modifier = Modifier.height(8.dp)) }

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

                item {
                    Text(
                        text = "$selectedCount/$maxSelection",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedCount > 0) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }

                items(items = purposes, key = { it.title }) { purpose ->
                    val isSelected = selectedPurposes.contains(purpose.title)
                    val isSelectable = isSelected || selectedCount < maxSelection
                    PurposeCard(
                        title = purpose.title,
                        emoji = purpose.emoji,
                        isSelected = isSelected,
                        isSelectable = isSelectable,
                        onClick = {
                            if (isSelectable) {
                                viewModel.togglePurpose(purpose.title)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // --- PHẦN NÚT BẤM (CỐ ĐỊNH Ở DƯỚI) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // Thêm nền trắng cho nút bấm cố định
                    .padding(16.dp)
            ) {
                PrimaryButton(
                    text = "Tiếp tục",
                    onClick = {
                        navController.navigate(Screen.PlanSummary.route)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    // Nút chỉ được BẬT khi có ít nhất 1 lựa chọn
                    enabled = selectedCount > 0
                )
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
    isSelectable: Boolean, // Thêm tham số mới
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF037CAC) else Color.Transparent

    // Giảm độ mờ (opacity) nếu không được chọn và không thể chọn (đã đạt max limit)
    val contentAlpha = if (isSelectable) 1f else 0.5f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5).copy(alpha = contentAlpha)) // Áp dụng alpha cho nền
            .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            // Chỉ cho phép click nếu nó đang selected (để bỏ chọn) hoặc còn slot để chọn
            .clickable(enabled = isSelectable) { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = "$title ${emoji}",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = contentAlpha) // Áp dụng alpha cho chữ
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}