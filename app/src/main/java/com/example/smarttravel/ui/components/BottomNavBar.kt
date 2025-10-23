package com.example.smarttravel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smarttravel.R
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.theme.SmarttravelTheme

// -------------------- Dữ liệu cho từng mục --------------------
sealed class AppBottomBarItem(val route: String, val iconRes: Int, val title: String) {
    object Home : AppBottomBarItem(Screen.Home.route, R.drawable.icon_home, "Trang chủ")
    object Calendar : AppBottomBarItem(Screen.Calendar.route, R.drawable.icon_calendar, "Kế hoạch")
    object Search : AppBottomBarItem(Screen.Search.route, R.drawable.icon_search, "Tìm kiếm")
    object Chat : AppBottomBarItem(Screen.Chat.route, R.drawable.icon_chat, "ChatBot")
    object Profile : AppBottomBarItem(Screen.Profile.route, R.drawable.icon_profile, "Hồ Sơ")
}

// -------------------- Thanh điều hướng dưới --------------------
@Composable
fun AppBottomBar(
    navController: NavController,
    currentRoute: String?
) {
    val items = listOf(
        AppBottomBarItem.Home,
        AppBottomBarItem.Calendar,
        AppBottomBarItem.Search,
        AppBottomBarItem.Chat,
        AppBottomBarItem.Profile
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                if (item == AppBottomBarItem.Search) {
                    // 🔵 Nút giữa tròn xanh
                    Box(
                        modifier = Modifier
                            .size(65.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(55.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1976D2))
                                .clickable {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.title,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                } else {
                    // ⚪ Các nút còn lại
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.title,
                            tint = if (isSelected) Color(0xFF1976D2) else Color.Gray,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = item.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF1976D2) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// -------------------- Preview --------------------
@Preview(showBackground = true)
@Composable
fun AppBottomBarPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        AppBottomBar(navController = navController, currentRoute = Screen.Calendar.route)
    }
}
