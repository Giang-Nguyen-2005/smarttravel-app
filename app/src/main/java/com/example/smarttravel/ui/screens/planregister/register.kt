package com.example.smarttravel.ui.screens.planregister


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smarttravel.ui.components.AppBottomBar
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.theme.SmarttravelTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.components.AppTopBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import com.example.smarttravel.ui.screens.auth.RegisterScreen
import com.example.smarttravel.ui.viewmodel.PlanViewModel
import com.example.smarttravel.ui.viewmodel.SaveState

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: PlanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is SaveState.Success -> {
                Toast.makeText(context, "Đã tạo kế hoạch!", Toast.LENGTH_SHORT).show()
                viewModel.resetSaveState()
                // Quay về Home (hoặc màn Kế hoạch) và xóa luồng này khỏi back stack
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.PlanRegisterFlow.route) { inclusive = true }
                }
            }
            is SaveState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }
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
            // Giảm khoảng cách chung do đã có divider
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp)
                ) {
                    AppTopBar(onBackClick = { navController.popBackStack() })

                    Spacer(modifier = Modifier.width(50.dp))

                    Text(
                        text = "Tóm tắt đánh giá",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // --- Cụm 1: Địa điểm ---
            item {
                Spacer(modifier = Modifier.height(8.dp)) // Thêm khoảng cách ở trên
                SummaryItem(
                    label = "📍Địa điểm",
                    value = uiState.destinationName.ifEmpty { "Chưa chọn" }
                )
                Spacer(modifier = Modifier.height(8.dp)) // Thêm khoảng cách ở dưới
            }

            // --- Divider ---
            item {
                CenteredDivider()
            }

            // --- Cụm 2: Người đồng hành ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SummaryItem(
                    label = "👥Người đồng hành",
                    value = uiState.companion
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- Divider ---
            item {
                CenteredDivider()
            }

            // --- Cụm 3: Thời gian ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // TODO: Cập nhật PeriodScreen để set ngày cho ViewModel
                val dateText = if (uiState.startDate != null && uiState.endDate != null) {
                    "${uiState.startDate} - ${uiState.endDate}"
                } else "Chưa chọn ngày"
                SummaryItem(
                    label = "🗓️Thời gian",
                    value = dateText
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- Divider ---
            item {
                CenteredDivider()
            }

            // --- Cụm 4: Sở thích ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SummaryItem(
                    label = "❤️Sở thích",
                    value = uiState.purposes.joinToString(", ").ifEmpty { "Chưa chọn" }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- Divider ---
            item {
                CenteredDivider()
            }

            // --- Cụm 5: Ngân sách ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SummaryItem(
                    label = "💰Ngân sách",
                    value = uiState.budget
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- Nút xác nhận ---
            item {
                Spacer(modifier = Modifier.height(16.dp)) // Khoảng cách lớn hơn trước nút
                PrimaryButton(
                    text = if (saveState is SaveState.Loading) "Đang tạo..." else "Xác nhận hành trình",
                    onClick = {
                        if (saveState !is SaveState.Loading) {
                            viewModel.savePlan() // Bắt đầu lưu
                        }
                    },
                    enabled = saveState !is SaveState.Loading, // Vô hiệu hóa khi đang lưu
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Composable mới để vẽ đường kẻ căn giữa
 */
@Composable
private fun CenteredDivider() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center // Căn giữa nội dung bên trong
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(0.7f), // Chiếm 70%
            color = Color.Gray.copy(alpha = 0.3f), // Màu xám mờ
            thickness = 1.dp
        )
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
