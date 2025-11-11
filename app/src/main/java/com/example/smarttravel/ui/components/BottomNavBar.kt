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
// --- THÊM IMPORT NÀY ---
import androidx.compose.foundation.layout.navigationBarsPadding

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
    // --- Tách các item ra ---
    val homeItem = AppBottomBarItem.Home
    val calendarItem = AppBottomBarItem.Calendar
    val searchItem = AppBottomBarItem.Search
    val chatItem = AppBottomBarItem.Chat
    val profileItem = AppBottomBarItem.Profile

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Tự động thêm padding để né thanh điều hướng
            .shadow(10.dp, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                // --- Bỏ horizontalArrangement ---
                .height(IntrinsicSize.Min), // Đảm bảo các item con có chiều cao tối thiểu
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Thêm 5 item thủ công, mỗi item dùng Modifier.weight(1f) ---

            // 1. Home
            BottomBarItem(
                item = homeItem,
                isSelected = currentRoute == homeItem.route,
                navController = navController
            )

            // 2. Calendar
            BottomBarItem(
                item = calendarItem,
                isSelected = currentRoute == calendarItem.route,
                navController = navController
            )

            // 3. Search (Nút đặc biệt)
            SearchBottomBarItem(
                item = searchItem,
                isSelected = currentRoute == searchItem.route,
                navController = navController
            )

            // 4. Chat
            BottomBarItem(
                item = chatItem,
                isSelected = currentRoute == chatItem.route,
                navController = navController
            )

            // 5. Profile
            BottomBarItem(
                item = profileItem,
                isSelected = currentRoute == profileItem.route,
                navController = navController
            )
        }
    }
}

// --- Composable cho 4 item phụ (Home, Calendar, Chat, Profile) ---
@Composable
private fun RowScope.BottomBarItem(
    item: AppBottomBarItem,
    isSelected: Boolean,
    navController: NavController
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f) // <-- DÙNG WEIGHT 1F
            .fillMaxHeight() // Lấp đầy chiều cao của Row
            .clickable {
                if (!isSelected) {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
        verticalArrangement = Arrangement.Center // Căn giữa theo chiều dọc
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

// --- Composable cho item Search (Kính lúp) ---
@Composable
private fun RowScope.SearchBottomBarItem(
    item: AppBottomBarItem,
    isSelected: Boolean,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .weight(1f) // <-- DÙNG WEIGHT 1F
            .fillMaxHeight(), // Lấp đầy chiều cao của Row
        contentAlignment = Alignment.Center
    ) {
        //  🔵  Nút giữa tròn xanh
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
                        if (!isSelected) {
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