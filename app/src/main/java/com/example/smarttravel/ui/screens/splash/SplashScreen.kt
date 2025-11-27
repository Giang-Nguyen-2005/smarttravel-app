package com.example.smarttravel.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.example.smarttravel.R
import com.example.smarttravel.navigation.Screen
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import androidx.compose.runtime.getValue
import com.airbnb.lottie.compose.LottieAnimation


import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smarttravel.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
// --- KẾT THÚC THÊM IMPORT ---

@Composable
fun SplashScreen(navController: NavController) {

    // Lấy AuthViewModel
    val authViewModel: AuthViewModel = hiltViewModel()

    // Quan sát trạng thái authState
    val authState by authViewModel.authCheckState.collectAsState()
    

    // Biến để theo dõi xem 3.5 giây đã trôi qua chưa
    var isTimerFinished by remember { mutableStateOf(false) }

    // Chạy đồng hồ hẹn giờ
    LaunchedEffect(key1 = true) {
        delay(3500L) // Chờ 3.5 giây
        isTimerFinished = true
    }

    // 1. Luôn hiển thị UI (Lottie)
    SplashScreenUI()

    // Chỉ điều hướng khi CẢ HAI điều kiện đều đúng
    // (authState không còn Loading VÀ isTimerFinished là true)
    LaunchedEffect(authState, isTimerFinished) {
        // Chỉ hành động khi 3.5s đã qua VÀ auth đã được kiểm tra xong
        if (isTimerFinished) {
            when (authState) {
                is AuthViewModel.AuthCheckState.LoggedIn -> {
                    // ĐÃ ĐĂNG NHẬP -> TỚI HOME
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
                is AuthViewModel.AuthCheckState.LoggedOut -> {
                    // CHƯA ĐĂNG NHẬP -> TỚI ONBOARDING
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
                is AuthViewModel.AuthCheckState.Loading -> {
                }
            }
        }

    }
}

// CODE UI ---
@Composable
fun SplashScreenUI() {
    val colorScheme = MaterialTheme.colorScheme
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.travel_bus))
    val progress by animateLottieCompositionAsState(composition)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Smart Travel",
                color = colorScheme.onPrimary,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Animation
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
fun SplashScreenPreview() {
    SplashScreenUI() // Preview chỉ cần giao diện
}