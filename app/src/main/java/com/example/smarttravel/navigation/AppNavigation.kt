package com.example.smarttravel.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// --- THÊM IMPORT 2 MÀN HÌNH MỚI ---
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

        // TODO: Thêm các màn hình khác của bạn sau
        // composable(Screen.Calendar.route) { CalendarScreen(navController = navController) }
        // composable(Screen.Chat.route) { ChatScreen(navController = navController) }
        // composable(Screen.Profile.route) { ProfileScreen(navController = navController) }
        // composable(Screen.Search.route) { SearchScreen(navController = navController) }
    }
}