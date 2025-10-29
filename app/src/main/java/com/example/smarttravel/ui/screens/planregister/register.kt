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

@Composable
fun RegisterScreen(navController: NavController) {
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
                SummaryItem(label = "\uD83D\uDCCDĐịa điểm", value = "Vịnh Hạ Long, Quảng Ninh, Việt Nam")
            }

            item {
                SummaryItem(label = "\uD83D\uDC65Người đồng hành", value = "Cặp đôi 💕")
            }

            item {
                SummaryItem(label = "\uD83D\uDDD3Thời gian", value = "12–14/10/2025")
            }

            item {
                SummaryItem(label = "❤\uFE0FSở thích", value = "Nghỉ dưỡng & Thư giãn 🐱, Du lịch Biển 🏄‍♂️")
            }

            item {
                SummaryItem(label = "\uD83D\uDCB0Ngân sách", value = "Cân bằng 📸")
            }

            item {
                PrimaryButton(
                    text = "Đăng kí hành trình",
                    onClick = { /* TODO: Submit or navigate */ },
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
