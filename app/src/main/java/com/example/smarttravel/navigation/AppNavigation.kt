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
import com.example.smarttravel.ui.screens.chat.ChatScreen // Import này có vẻ chưa được dùng?
import com.example.smarttravel.ui.screens.search.SearchScreen
import com.example.smarttravel.ui.screens.planregister.RegisterScreen as PlanSummaryScreen
import com.example.smarttravel.ui.viewmodel.PlanViewModel
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation // Đổi tên import 'navigation'
import com.example.smarttravel.ui.screens.profile.ProfileScreen
import com.example.smarttravel.ui.screens.profile.UserProfileDetailScreen // <<< IMPORT MÀN HÌNH BỊ THIẾU
import com.example.smarttravel.ui.screens.profile.SavedDestinationsScreen
import com.example.smarttravel.ui.screens.previous_trips.PreviousTripsScreen
import com.example.smarttravel.ui.screens.editprofile.EditProfileScreen
import com.example.smarttravel.ui.screens.planregister.EconomyScreen
import com.example.smarttravel.ui.screens.planregister.GoWithScreen
import com.example.smarttravel.ui.screens.planregister.PeriodScreen
import com.example.smarttravel.ui.screens.planregister.PurposeScreen
import com.example.smarttravel.ui.screens.schedule.PlanScreen
import com.example.smarttravel.ui.screens.plan_detail.PlanDetailScreen

@Composable
fun AppNavigation(navController: NavHostController) {

    // --- THAY ĐỔI Ở ĐÂY ---
    // Bắt đầu từ Splash Screen
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        // ... (Các màn hình Splash, Onboarding, Login, Register, ResetPassword, Home giữ nguyên) ...
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


        // ... (composable cho DetailScreen giữ nguyên) ...
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("destinationId") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400)
                )
            },
            exitTransition = { null },
            popEnterTransition = { null },
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


        // Màn hình Calendar/Plan (Kế hoạch)
        composable(Screen.Calendar.route) {
            PlanScreen(navController = navController)
        }
        
        // Màn hình Plan Detail
        composable(
            route = Screen.PlanDetail.route,
            arguments = listOf(navArgument("planId") { type = NavType.StringType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId")
            if (planId != null) {
                PlanDetailScreen(navController = navController, planId = planId)
            }
        }
        
        // Màn hình Chat
        composable(Screen.Chat.route) { 
            ChatScreen(navController = navController) 
        }

        // Màn hình Profile
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        // <<< THÊM COMPOSABLE BỊ THIẾU ĐỂ KHẮC PHỤC LỖI CRASH >>>
        composable(Screen.UserProfileDetail.route) {
            UserProfileDetailScreen(navController = navController)
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }

        composable(Screen.SavedDestinations.route) {
            SavedDestinationsScreen(navController = navController)
        }
        
        // Màn hình Các chuyến đi trước
        composable(Screen.PreviousTrips.route) {
            PreviousTripsScreen(navController = navController)
        }

        // Màn hình Search
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }

        // Màn hình AI Suggestions
        composable(Screen.AiSuggestions.route) {
            com.example.smarttravel.ui.screens.ai_suggestions.AiSuggestionsScreen(navController = navController)
        }

        //Plan Register
        planRegisterGraph(navController)
    }
}

// ... (Hàm planRegisterGraph giữ nguyên) ...
private fun NavGraphBuilder.planRegisterGraph(navController: NavHostController) {
    // ... (Code của bạn giữ nguyên) ...
    navigation(
        route = Screen.PlanRegisterFlow.route,
        startDestination = Screen.GoWith.route,
        arguments = listOf(
            navArgument("destinationId") { type = NavType.StringType },
            navArgument("destinationName") { type = NavType.StringType }
        )
    ) {
        @Composable
        fun getSharedViewModel(backStackEntry: NavBackStackEntry): PlanViewModel {
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.PlanRegisterFlow.route)
            }
            return hiltViewModel(parentEntry)
        }
        composable(Screen.GoWith.route) {
            val viewModel = getSharedViewModel(it)
            GoWithScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Period.route) {
            val viewModel = getSharedViewModel(it)
            PeriodScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Economy.route) {
            val viewModel = getSharedViewModel(it)
            EconomyScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Purpose.route) {
            val viewModel = getSharedViewModel(it)
            PurposeScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.PlanSummary.route) {
            val viewModel = getSharedViewModel(it)
            PlanSummaryScreen(navController = navController, viewModel = viewModel)
        }
    }
}