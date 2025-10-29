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
@Composable
fun GoWithScreen(navController: NavController) {
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp)
                ) {
                    AppTopBar(onBackClick = { navController.popBackStack() })

                    Spacer(modifier = Modifier.width(12.dp))

                    LinearProgressIndicator(
                        progress = 0.25f,
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
                    text = "Ai sẽ đi cùng bạn? 🎒",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Text(
                    text = "Hãy bắt đầu bằng cách chọn bạn sẽ đi cùng ai.",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }

            // Các lựa chọn người đồng hành
            item {
                CompanionOption(
                    title = "Chỉ mình tôi",
                    emoji = "🕺",
                    description = "Du lịch một mình, chỉ có bạn.",
                    onClick = { /* TODO */ }
                )
            }
            item {
                CompanionOption(
                    title = "Cặp đôi",
                    emoji = "💕",
                    description = "Kỳ nghỉ lãng mạn cho hai người.",
                    onClick = { /* TODO */ }
                )
            }
            item {
                CompanionOption(
                    title = "Gia đình",
                    emoji = "👨‍👩‍👧",
                    description = "Thời gian chất lượng cùng người thân yêu.",
                    onClick = { /* TODO */ }
                )
            }
            item {
                CompanionOption(
                    title = "Bạn bè",
                    emoji = "✨",
                    description = "Phiêu lưu cùng những người bạn thân thiết.",
                    onClick = { /* TODO */ }
                )
            }
            item {
                CompanionOption(
                    title = "Công việc",
                    emoji = "💼",
                    description = "Du lịch công tác hoặc doanh nghiệp.",
                    onClick = { /* TODO */ }
                )
            }
            item {
                PrimaryButton(
                    text = "Tiếp tục",
                    onClick = { /* TODO: Navigate to next screen */ },
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}


@Composable
fun CompanionOption(
    title: String,
    emoji: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
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
            color = Color.Gray
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GoWithScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        GoWithScreen(navController = navController)
    }
}

