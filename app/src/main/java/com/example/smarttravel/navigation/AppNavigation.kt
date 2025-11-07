package com.example.smarttravel.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smarttravel.ui.screens.detail.DetailScreen
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween

import com.example.smarttravel.ui.screens.onboarding.OnboardingScreen
import com.example.smarttravel.ui.screens.splash.SplashScreen

// Import các màn hình cũ
import com.example.smarttravel.ui.screens.auth.LoginScreen
import com.example.smarttravel.ui.screens.auth.RegisterScreen
import com.example.smarttravel.ui.screens.home.HomeScreen
import com.example.smarttravel.ui.screens.auth.ResetPasswordScreen

@Composable
fun AppNavigation(navController: NavHostController) {

    // --- THAY ĐỔI Ở ĐÂY ---
    // Bắt đầu từ Splash Screen
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        // Màn hình Splash
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        // Màn hình Onboarding (chứa 3 trang)
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        // Màn hình Login
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // Màn hình Register
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        // Màn hình restpassword
        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(navController = navController)
        }

        // Màn hình Home (sau khi đăng nhập)
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("destinationId") { type = NavType.StringType }),
            // 1. Hiệu ứng khi MỞ màn hình: Trượt từ dưới lên
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400) // Thời gian 400ms
                )
            },
            // 2. Hiệu ứng khi ĐÓNG màn hình (hoặc mở màn hình khác đè lên): Giữ nguyên hoặc mờ đi chút
            exitTransition = {
                null // Hoặc fadeOut() nếu muốn
            },
            // 3. Hiệu ứng khi QUAY LẠI màn hình này (ít dùng cho detail):
            popEnterTransition = {
                null
            },
            // 4. Hiệu ứng khi BACK khỏi màn hình này: Trượt xuống dưới
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400)
                )
            }
        ) { backStackEntry ->
            val destinationId = backStackEntry.arguments?.getString("destinationId")
            if (destinationId != null) {
                DetailScreen(
                    navController = navController,
                    destinationId = destinationId
                )
            }
        }

        // TODO: Thêm các màn hình khác của bạn sau
        // composable(Screen.Calendar.route) { CalendarScreen(navController = navController) }
        // composable(Screen.Chat.route) { ChatScreen(navController = navController) }
        // composable(Screen.Profile.route) { ProfileScreen(navController = navController) }
        // composable(Screen.Search.route) { SearchScreen(navController = navController) }
    }
}