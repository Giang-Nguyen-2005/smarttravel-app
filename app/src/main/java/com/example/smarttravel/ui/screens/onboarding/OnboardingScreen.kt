package com.example.smarttravel.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.smarttravel.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class) // Cần cho HorizontalPager
@Composable
fun OnboardingScreen(navController: NavController) {
    val colorScheme = MaterialTheme.colorScheme

    // Pager state cho 3 trang
    val pagerState = rememberPagerState(initialPage = 0) {
        3
    }
    val scope = rememberCoroutineScope()

    // Hàm chung để điều hướng đến Login (khi nhấn Skip hoặc nút cuối)
    val navigateToLogin = {
        navController.navigate(Screen.Login.route) {
            // Xóa Onboarding khỏi back stack
            popUpTo(Screen.Onboarding.route) {
                inclusive = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        HorizontalPager(state = pagerState) { page ->
        when (page) {
            // Trang 1
            0 -> OnboardingScreen01(
                onSkip = navigateToLogin,
                onStartClick = {
                    // Chuyển sang trang 2
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            )
            // Trang 2
            1 -> OnboardingScreen02(
                onSkip = navigateToLogin,
                onStartClick = {
                    // Chuyển sang trang 3
                    scope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                }
            )
            // Trang 3
            2 -> OnboardingScreen03(
                onSkip = navigateToLogin,
                onStartClick = navigateToLogin // Nút cuối cùng, chuyển đến Login
            )
        }
    }
    }
}