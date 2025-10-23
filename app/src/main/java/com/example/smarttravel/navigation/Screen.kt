package com.example.smarttravel.navigation

sealed class Screen(val route: String) {
    // mấy cái này test trước thôi
    // Các màn hình không có thanh Nav
    object Splash : Screen("splash_screen")
    object Onboarding : Screen("onboarding_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")


    // 4 Màn hình chính trên thanh Nav
    object Home : Screen("home_screen")
    object Calendar : Screen("schedule_screen")
    object Chat : Screen("chat_screen")
    object Profile : Screen("profile_screen")


    object Search : Screen("search_screen")
}