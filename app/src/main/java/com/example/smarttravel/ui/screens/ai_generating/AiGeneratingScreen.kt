package com.example.smarttravel.ui.screens.ai_generating

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.smarttravel.R
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.viewmodel.PlanViewModel
import com.example.smarttravel.ui.viewmodel.SaveState
import kotlinx.coroutines.delay

@Composable
fun AiGeneratingScreen(
    navController: NavController,
    planId: String?, // Tham số này có thể không cần dùng nếu lấy từ SaveState
    viewModel: PlanViewModel
) {
    val saveState by viewModel.saveState.collectAsState()

    // Track thời gian bắt đầu
    val startTime = remember { System.currentTimeMillis() }

    // Hiển thị UI
    AiGeneratingScreenUI()

    // Logic điều hướng (Giữ nguyên logic 5s của bạn)
    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is SaveState.Success -> {
                val elapsedTime = System.currentTimeMillis() - startTime
                val minDisplayTime = 4500L // Giảm xuống chút để khớp với animation text

                if (elapsedTime < minDisplayTime) {
                    delay(minDisplayTime - elapsedTime)
                }

                navController.navigate(Screen.PlanDetail.createRoute(state.planId)) {
                    popUpTo(Screen.PlanRegisterFlow.route) { inclusive = true }
                }
                viewModel.resetSaveState()
            }
            is SaveState.Error -> {
                val elapsedTime = System.currentTimeMillis() - startTime
                val minDisplayTime = 3000L

                if (elapsedTime < minDisplayTime) {
                    delay(minDisplayTime - elapsedTime)
                }
                navController.popBackStack()
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }
}

@Composable
fun AiGeneratingScreenUI() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bot))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    // --- 1. Cải tiến: Dynamic Loading Text ---
    val loadingMessages = listOf(
        "Đang phân tích sở thích của bạn...",
        "Đang tìm kiếm các điểm đến phù hợp...",
        "Đang lựa chọn khách sạn tốt nhất...",
        "Đang tối ưu hóa lộ trình di chuyển...",
        "Đang hoàn thiện kế hoạch..."
    )
    var messageIndex by remember { mutableIntStateOf(0) }

    // Tự động đổi text mỗi 1.5 giây
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            messageIndex = (messageIndex + 1) % loadingMessages.size
        }
    }

    // --- 2. Cải tiến: Pulse Animation (Hiệu ứng nhịp đập phía sau) ---
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF037CAC),
                        Color(0xFF00C6FF) // Gradient sáng hơn chút ở dưới
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Hiệu ứng vòng tròn lan tỏa phía sau Bot
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(scale)
                .alpha(alpha)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Lottie Animation
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "AI Đang Làm Việc",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Text thay đổi nội dung mượt mà
            AnimatedContent(
                targetState = loadingMessages[messageIndex],
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + slideInVertically { it / 2 } togetherWith
                            fadeOut(animationSpec = tween(300)) + slideOutVertically { -it / 2 }
                },
                label = "loadingText"
            ) { targetText ->
                Text(
                    text = targetText,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.height(50.dp) // Giữ chiều cao cố định để không bị nhảy layout
                )
            }
        }
    }
}