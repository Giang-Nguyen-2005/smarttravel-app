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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp)
                ) {
                    AppTopBar(onBackClick = { navController.popBackStack() })

                    Spacer(modifier = Modifier.width(40.dp))

                    Text(
                        text = "Tóm tắt đánh giá",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            item {
                SummaryItem(
                    label = "📍Địa điểm",
                    value = uiState.destinationName.ifEmpty { "Chưa chọn" }
                )
            }
            item {
                SummaryItem(
                    label = "👥Người đồng hành",
                    value = uiState.companion
                )
            }
            item {
                // TODO: Cập nhật PeriodScreen để set ngày cho ViewModel
                val dateText = if (uiState.startDate != null && uiState.endDate != null) {
                    "${uiState.startDate} - ${uiState.endDate}"
                } else "Chưa chọn ngày"
                SummaryItem(
                    label = "🗓️Thời gian",
                    value = dateText
                )
            }
            item {
                SummaryItem(
                    label = "❤️Sở thích",
                    value = uiState.purposes.joinToString(", ").ifEmpty { "Chưa chọn" }
                )
            }
            item {
                SummaryItem(
                    label = "💰Ngân sách",
                    value = uiState.budget
                )
            }

            item {
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



@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        RegisterScreen(navController = navController)
    }
}
