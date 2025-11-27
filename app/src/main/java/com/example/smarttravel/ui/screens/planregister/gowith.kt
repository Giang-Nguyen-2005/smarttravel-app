package com.example.smarttravel.ui.screens.planregister

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
fun GoWithScreen(
    navController: NavController,
    viewModel: PlanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedOption = uiState.companion

    // Logic kiểm tra nút Tiếp tục: nút chỉ BẬT nếu có ít nhất một lựa chọn (companion không rỗng)
    val isButtonEnabled = !selectedOption.isNullOrBlank()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // --- HEADER CỐ ĐỊNH (STICKY HEADER) - CĂN GIỮA TUYỆT ĐỐI THANH TIẾN ĐỘ ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface)
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
                        progress = { 0.25f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = colorScheme.surfaceVariant,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                    )
                }
            }
            // --- KẾT THÚC HEADER CỐ ĐỊNH ---

            // --- NỘI DUNG CUỘN ---
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
                        text = "Bạn sẽ đi với ai? 👨‍👩‍👧‍👦",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                item {
                    Text(
                        text = "Chúng tôi sẽ điều chỉnh kế hoạch dựa trên đối tượng đồng hành của bạn.",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
                val companionOptions = listOf(
                    CompanionData("Chỉ một mình", "👤", "Chuyến đi một mình, khám phá tự do."),
                    CompanionData("Gia đình", "👨‍👩‍👧‍👦", "Chuyến đi ưu tiên các hoạt động phù hợp cho trẻ em."),
                    CompanionData("Cặp đôi", "❤️", "Chuyến đi lãng mạn, ưu tiên không gian riêng tư."),
                    CompanionData("Bạn bè", "👥", "Chuyến đi nhóm, ưu tiên các hoạt động vui vẻ, náo nhiệt."),
                    CompanionData("Đồng nghiệp", "💼", "Chuyến đi công tác hoặc nghỉ dưỡng kết hợp công việc.")
                )

                items(companionOptions) { option ->
                    CompanionOption(
                        title = option.title,
                        emoji = option.emoji,
                        description = option.description,
                        isSelected = selectedOption == option.title,
                        onClick = { viewModel.setCompanion(option.title) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // --- PHẦN NÚT BẤM (Cố định ở đáy) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface) // Nền cho nút bấm cố định
                    .padding(16.dp)
            ) {
                PrimaryButton(
                    text = "Tiếp tục",
                    onClick = {
                        navController.navigate(Screen.Period.route)
                    },
                    enabled = isButtonEnabled, // Áp dụng logic kiểm tra ở đây
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

data class CompanionData(
    val title: String,
    val emoji: String,
    val description: String
)

@Composable
fun CompanionOption(
    title: String,
    emoji: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val borderColor = if (isSelected) colorScheme.primary else Color.Transparent
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant)
            .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = "$title $emoji",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}