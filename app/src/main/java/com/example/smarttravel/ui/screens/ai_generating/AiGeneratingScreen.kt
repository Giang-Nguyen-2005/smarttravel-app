package com.example.smarttravel.ui.screens.ai_generating

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
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
import kotlin.random.Random

// --- CẤU HÌNH THỜI GIAN ---
private const val MIN_DISPLAY_TIME = 5000L // 5 giây

@Composable
fun AiGeneratingScreen(
    navController: NavController,
    planId: String?,
    viewModel: PlanViewModel
) {
    val saveState by viewModel.saveState.collectAsState()
    val startTime = remember { System.currentTimeMillis() }

    // Hiển thị UI
    AiGeneratingScreenUI()

    // Logic điều hướng
    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is SaveState.Success -> {
                val elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime < MIN_DISPLAY_TIME) {
                    delay(MIN_DISPLAY_TIME - elapsedTime)
                }
                navController.navigate(Screen.PlanDetail.createRoute(state.planId)) {
                    popUpTo(Screen.PlanRegisterFlow.route) { inclusive = true }
                }
                viewModel.resetSaveState()
            }
            is SaveState.Error -> {
                val elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime < 3000L) delay(3000L - elapsedTime)
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

    // 1. Dynamic Loading Text (Thay đổi text liên tục)
    val loadingMessages = listOf(
        "Đang phân tích sở thích của bạn...",
        "Kết nối dữ liệu du lịch...",
        "Tìm kiếm khách sạn tốt nhất...",
        "Tối ưu hóa lịch trình di chuyển...",
        "Đang hoàn thiện kế hoạch..."
    )
    var messageIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500) // Đổi text mỗi 1.5 giây
            messageIndex = (messageIndex + 1) % loadingMessages.size
        }
    }

    // 2. Travel Tip Ngẫu nhiên
    val tips = listOf(
        "Mang theo sạc dự phòng để không bỏ lỡ khoảnh khắc nào.",
        "Thử các món ăn đường phố để hiểu rõ văn hóa địa phương.",
        "Luôn mang theo một ít tiền mặt bên người.",
        "Dậy sớm để ngắm bình minh tại điểm đến.",
        "Kiểm tra dự báo thời tiết trước khi khởi hành."
    )
    val randomTip = remember { tips[Random.nextInt(tips.size)] }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF037CAC),
                        Color(0xFF005F9E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // --- BACKGROUND PULSE EFFECT (3 Lớp) ---
        PulsingCircle(delay = 0, maxScale = 1.5f)
        PulsingCircle(delay = 500, maxScale = 1.3f)
        PulsingCircle(delay = 1000, maxScale = 1.1f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 48.dp, horizontal = 32.dp)
        ) {
            // Spacer trên cùng để đẩy nội dung xuống giữa
            Spacer(modifier = Modifier.weight(1f))

            // --- CENTER CONTENT: BOT + STATUS ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(2f)
            ) {
                // Bot Animation
                Box(contentAlignment = Alignment.Center) {
                    // Ánh sáng sau lưng Bot
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                                )
                            )
                    )
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(220.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Status Card (Glassmorphism)
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "AI Đang Xử Lý",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Animated Text
                        AnimatedContent(
                            targetState = loadingMessages[messageIndex],
                            transitionSpec = {
                                (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                            },
                            label = "status"
                        ) { targetText ->
                            Text(
                                text = targetText,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                minLines = 2 // Giữ chiều cao cố định
                            )
                        }
                        // Đã xóa thanh Progress Bar ở đây
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // --- BOTTOM: TRAVEL TIP ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFFFE082), // Màu vàng
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mẹo: $randomTip",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Component vòng tròn lan tỏa
@Composable
fun PulsingCircle(delay: Int, maxScale: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_$delay")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(300.dp)
            .scale(scale)
            .alpha(alpha)
            .background(Color.White.copy(alpha = 0.1f), CircleShape)
    )
}