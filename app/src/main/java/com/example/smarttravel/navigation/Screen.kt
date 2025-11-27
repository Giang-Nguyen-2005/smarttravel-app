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

    object Detail : Screen("detail_screen/{destinationId}") {
        fun createRoute(destinationId: String) = "detail_screen/$destinationId"
    }
    object PlanRegisterFlow : Screen("plan_register_flow/{destinationId}/{destinationName}") {
        fun createRoute(destinationId: String, destinationName: String) =
            "plan_register_flow/$destinationId/$destinationName"
    }

    object GoWith : Screen("go_with_screen")
    object Period : Screen("period_screen")
    object Economy : Screen("economy_screen")
    object Purpose : Screen("purpose_screen")
    object PlanSummary : Screen("plan_summary_screen")

    object EditProfile : Screen("edit_profile_screen")

    object UserProfileDetail : Screen("user_profile_detail_screen")
    
    object SavedDestinations : Screen("saved_destinations_screen")
    
    object PlanDetail : Screen("plan_detail_screen/{planId}") {
        fun createRoute(planId: String) = "plan_detail_screen/$planId"
    }
    
    object PreviousTrips : Screen("previous_trips_screen")
    
    object AiSuggestions : Screen("ai_suggestions_screen")
    
    object AiGenerating : Screen("ai_generating_screen/{planId}") {
        fun createRoute(planId: String) = "ai_generating_screen/$planId"
    }
    
    object AddPlanDateSelection : Screen("add_plan_date_selection_screen")
    object AddPlanActivities : Screen("add_plan_activities_screen/{startDate}/{endDate}") {
        fun createRoute(startDate: String, endDate: String) = "add_plan_activities_screen/$startDate/$endDate"
    }
    object AddActivityForm : Screen("add_activity_form_screen/{dayIndex}/{startDate}/{endDate}") {
        fun createRoute(dayIndex: Int, startDate: String, endDate: String) = "add_activity_form_screen/$dayIndex/$startDate/$endDate"
    }

    object Settings : Screen("settings_screen")

}