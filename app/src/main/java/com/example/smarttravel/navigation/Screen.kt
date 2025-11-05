package com.example.smarttravel.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Onboarding : Screen("onboarding_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")

    object ResetPassword : Screen("reset_password_screen")

    // 4 Màn hình chính trên thanh Nav
    object Home : Screen("home_screen")
    object Calendar : Screen("schedule_screen")
    object Chat : Screen("chat_screen")
    object Profile : Screen("profile_screen")

    object Search : Screen("search_screen")
}