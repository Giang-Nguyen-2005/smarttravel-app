package com.example.smarttravel.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.example.smarttravel.R
import com.example.smarttravel.navigation.Screen
import kotlinx.coroutines.delay
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import androidx.compose.runtime.getValue
import com.airbnb.lottie.compose.LottieAnimation

@Composable
fun SplashScreen(navController: NavController) { // Thêm NavController

    // Tự động điều hướng sau 3 giây
    LaunchedEffect(key1 = true) {
        delay(3500L) // Chờ 3 giây
        navController.navigate(Screen.Onboarding.route) {
            // Xóa Splash khỏi back stack để không quay lại được
            popUpTo(Screen.Splash.route) {
                inclusive = true
            }
        }
    }

    // Giao diện (UI) của Splash
    SplashScreenUI()
}

@Composable
fun SplashScreenUI() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.travel_bus))
    val progress by animateLottieCompositionAsState(composition)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF037CAC)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Smart Travel",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 🎬 Animation
            LottieAnimation(
                composition,
                progress,
                modifier = Modifier.size(250.dp)
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() { // Đổi tên Preview
    SplashScreenUI() // Preview chỉ cần giao diện
}