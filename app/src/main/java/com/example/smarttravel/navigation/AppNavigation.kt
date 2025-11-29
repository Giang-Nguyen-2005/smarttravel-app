package com.example.smarttravel.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smarttravel.ui.screens.detail.DetailScreen
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

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
import com.example.smarttravel.ui.screens.ai_generating.AiGeneratingScreen
import com.example.smarttravel.ui.screens.settings.SettingsScreen
import com.example.smarttravel.ui.screens.add_destination.AddDestinationScreen
import com.example.smarttravel.ui.screens.manage_destinations.ManageDestinationsScreen

// Helper function cho animation transitions hiện đại
fun <T> AnimatedContentTransitionScope<T>.modernSlideIn() = 
    slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(350, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(350))

fun <T> AnimatedContentTransitionScope<T>.modernSlideOut() = 
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(350, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(350))

fun <T> AnimatedContentTransitionScope<T>.modernPopSlideIn() = 
    slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(350, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(350))

fun <T> AnimatedContentTransitionScope<T>.modernPopSlideOut() = 
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(350, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(350))

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
        composable(
            route = Screen.Onboarding.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            OnboardingScreen(navController = navController)
        }

        // Màn hình Login
        composable(
            route = Screen.Login.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            LoginScreen(navController = navController)
        }

        // Màn hình Register
        composable(
            route = Screen.Register.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            RegisterScreen(navController = navController)
        }
        // Màn hình restpassword
        composable(
            route = Screen.ResetPassword.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            ResetPasswordScreen(navController = navController)
        }

        // Màn hình Home (sau khi đăng nhập)
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            HomeScreen(navController = navController)
        }


        // ... (composable cho DetailScreen giữ nguyên) ...
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("destinationId") { type = NavType.StringType }),
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = { null },
            popEnterTransition = { null },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(400))
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
        composable(
            route = Screen.Calendar.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            PlanScreen(navController = navController)
        }
        
        // Màn hình Plan Detail
        composable(
            route = Screen.PlanDetail.route,
            arguments = listOf(navArgument("planId") { type = NavType.StringType }),
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId")
            if (planId != null) {
                PlanDetailScreen(navController = navController, planId = planId)
            }
        }
        
        // Màn hình Chat
        composable(
            route = Screen.Chat.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) { 
            ChatScreen(navController = navController) 
        }

        // Màn hình Profile
        composable(
            route = Screen.Profile.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            ProfileScreen(navController = navController)
        }

        // <<< THÊM COMPOSABLE BỊ THIẾU ĐỂ KHẮC PHỤC LỖI CRASH >>>
        composable(
            route = Screen.UserProfileDetail.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            UserProfileDetailScreen(navController = navController)
        }

        composable(
            route = Screen.EditProfile.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            EditProfileScreen(navController = navController)
        }

        composable(
            route = Screen.SavedDestinations.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            SavedDestinationsScreen(navController = navController)
        }
        
        // Màn hình Các chuyến đi trước
        composable(
            route = Screen.PreviousTrips.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            PreviousTripsScreen(navController = navController)
        }

        // Màn hình Cài đặt
        composable(
            route = Screen.Settings.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            SettingsScreen(navController = navController)
        }
        
        // Màn hình Thêm địa điểm (Admin)
        composable(
            route = Screen.AddDestination.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            AddDestinationScreen(navController = navController)
        }
        
        composable(
            route = Screen.ManageDestinations.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            ManageDestinationsScreen(navController = navController)
        }
        
        composable(
            route = Screen.EditDestination.route,
            arguments = listOf(navArgument("destinationId") { type = NavType.StringType }),
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) { backStackEntry ->
            val destinationId = backStackEntry.arguments?.getString("destinationId") ?: ""
            AddDestinationScreen(
                navController = navController,
                destinationId = destinationId
            )
        }

        // Màn hình Search
        composable(
            route = Screen.Search.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            SearchScreen(navController = navController)
        }

        // Màn hình AI Suggestions
        composable(
            route = Screen.AiSuggestions.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            com.example.smarttravel.ui.screens.ai_suggestions.AiSuggestionsScreen(navController = navController)
        }
        
        // Màn hình thêm kế hoạch mới - Chọn ngày
        composable(
            route = Screen.AddPlanDateSelection.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            com.example.smarttravel.ui.screens.add_plan.DateSelectionScreen(navController = navController)
        }
        
        // Navigation graph con để chia sẻ ViewModel giữa AddPlanActivities và AddActivityForm
        navigation(
            startDestination = Screen.AddPlanActivities.route,
            route = "add_plan_flow/{startDate}/{endDate}",
            arguments = listOf(
                navArgument("startDate") { type = NavType.StringType },
                navArgument("endDate") { type = NavType.StringType }
            )
        ) {
            composable(
                route = Screen.AddPlanActivities.route,
                arguments = listOf(
                    navArgument("startDate") { type = NavType.StringType },
                    navArgument("endDate") { type = NavType.StringType }
                ),
                enterTransition = { modernSlideIn() },
                exitTransition = { modernSlideOut() },
                popEnterTransition = { modernPopSlideIn() },
                popExitTransition = { modernPopSlideOut() }
            ) { backStackEntry ->
                val startDate = backStackEntry.arguments?.getString("startDate")
                val endDate = backStackEntry.arguments?.getString("endDate")
                @Composable
                fun getSharedViewModel(entry: NavBackStackEntry): com.example.smarttravel.ui.viewmodel.ManualPlanViewModel {
                    val parentRoute = "add_plan_flow/$startDate/$endDate"
                    val parentEntry = remember(entry) {
                        try {
                            navController.getBackStackEntry(parentRoute)
                        } catch (e: Exception) {
                            entry
                        }
                    }
                    return hiltViewModel(parentEntry)
                }
                val viewModel = getSharedViewModel(backStackEntry)
                com.example.smarttravel.ui.screens.add_plan.AddPlanActivitiesScreen(
                    navController = navController,
                    startDate = startDate,
                    endDate = endDate,
                    viewModel = viewModel
                )
            }
            
            composable(
                route = Screen.AddActivityForm.route,
                arguments = listOf(
                    navArgument("dayIndex") { type = NavType.IntType },
                    navArgument("startDate") { type = NavType.StringType },
                    navArgument("endDate") { type = NavType.StringType }
                ),
                enterTransition = { modernSlideIn() },
                exitTransition = { modernSlideOut() },
                popEnterTransition = { modernPopSlideIn() },
                popExitTransition = { modernPopSlideOut() }
            ) { backStackEntry ->
                val dayIndex = backStackEntry.arguments?.getInt("dayIndex") ?: 0
                val startDate = backStackEntry.arguments?.getString("startDate")
                val endDate = backStackEntry.arguments?.getString("endDate")
                @Composable
                fun getSharedViewModel(entry: NavBackStackEntry): com.example.smarttravel.ui.viewmodel.ManualPlanViewModel {
                    val parentRouteStr = "add_plan_flow/$startDate/$endDate"
                    val parentEntry = remember(entry) {
                        try {
                            navController.getBackStackEntry(parentRouteStr)
                        } catch (e: Exception) {
                            entry
                        }
                    }
                    return hiltViewModel(parentEntry)
                }
                val viewModel = getSharedViewModel(backStackEntry)
                com.example.smarttravel.ui.screens.add_plan.AddActivityFormScreen(
                    navController = navController,
                    dayIndex = dayIndex,
                    viewModel = viewModel,
                    startDate = startDate,
                    endDate = endDate
                )
            }
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
        composable(
            route = Screen.GoWith.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            val viewModel = getSharedViewModel(it)
            GoWithScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            route = Screen.Period.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            val viewModel = getSharedViewModel(it)
            PeriodScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            route = Screen.Economy.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            val viewModel = getSharedViewModel(it)
            EconomyScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            route = Screen.Purpose.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            val viewModel = getSharedViewModel(it)
            PurposeScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            route = Screen.PlanSummary.route,
            enterTransition = { modernSlideIn() },
            exitTransition = { modernSlideOut() },
            popEnterTransition = { modernPopSlideIn() },
            popExitTransition = { modernPopSlideOut() }
        ) {
            val viewModel = getSharedViewModel(it)
            PlanSummaryScreen(navController = navController, viewModel = viewModel)
        }
        
        // Màn hình AI Generating (nằm trong plan register flow để share ViewModel)
        // Chỉ dùng fade, không có slide
        composable(
            route = Screen.AiGenerating.route,
            arguments = listOf(navArgument("planId") { type = NavType.StringType }),
            enterTransition = { fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) },
            exitTransition = { fadeOut(animationSpec = tween(400, easing = FastOutSlowInEasing)) },
            popEnterTransition = { fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) },
            popExitTransition = { fadeOut(animationSpec = tween(400, easing = FastOutSlowInEasing)) }
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId")
            val viewModel = getSharedViewModel(backStackEntry)
            AiGeneratingScreen(
                navController = navController,
                planId = planId,
                viewModel = viewModel
            )
        }
    }
}