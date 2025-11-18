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
fun EconomyScreen(navController: NavController, viewModel: PlanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedOption = uiState.budget

    // Logic kiểm tra nút Tiếp tục: nút chỉ BẬT nếu có ít nhất một lựa chọn (budget không rỗng)
    val isButtonEnabled = !selectedOption.isNullOrBlank()

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
                        progress = { 0.75f },
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
                        text = "Thiết lập ngân sách của bạn",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                item {
                    Text(
                        text = "Chúng tôi sẽ giúp bạn tạo kế hoạch phù hợp với ngân sách của bạn.",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
                // Các lựa chọn ngân sách
                val options = listOf(
                    BudgetOption("Tiết kiệm", "💰", "Chuyến đi ưu tiên chi phí thấp."),
                    BudgetOption("Cân bằng", "⚖️", "Chi tiêu hợp lý để cân bằng giữa trải nghiệm và chi phí."),
                    BudgetOption("Cao cấp", "👑", "Trải nghiệm cao cấp, sang chảnh với chi phí cao hơn."),
                    BudgetOption("Linh hoạt", "🔓", "Chi tiêu linh hoạt, thoải mái, không giới hạn.")
                )
                items(options.size) { index ->
                    val option = options[index]
                    BudgetCard(
                        title = option.title,
                        emoji = option.emoji,
                        description = option.description,
                        isSelected = selectedOption == option.title,
                        onClick = { viewModel.setBudget(option.title) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // --- NÚT BẤM (CỐ ĐỊNH Ở DƯỚI) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // Thêm nền trắng cho nút bấm cố định
                    .padding(16.dp)
            ) {
                PrimaryButton(
                    text = "Tiếp tục",
                    onClick = {
                        navController.navigate(Screen.Purpose.route)
                    },
                    enabled = isButtonEnabled, // Áp dụng logic kiểm tra ở đây
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

data class BudgetOption(
    val title: String,
    val emoji: String,
    val description: String
)

@Composable
fun BudgetCard(
    title: String,
    emoji: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF037CAC) else Color.Transparent
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
        Text(
            text = description,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}