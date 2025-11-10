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
import androidx.compose.foundation.border
import com.example.smarttravel.ui.viewmodel.PlanViewModel

@Composable
fun EconomyScreen(navController: NavController,
                  viewModel: PlanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedOption = uiState.budget
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
                        progress = 0.75f,
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
                    isSelected = selectedOption == option.title, // Đọc state
                    onClick = { viewModel.setBudget(option.title) } // Ghi state
                )
            }

            item {
                PrimaryButton(
                    text = "Tiếp tục",
                    onClick = {
                        // Chuyển sang màn hình tiếp theo
                        navController.navigate(Screen.Purpose.route)
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

@Preview(showBackground = true)
@Composable
fun EconomyScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        EconomyScreen(
            navController = navController,
            viewModel = TODO()
        )
    }
}

