package com.example.smarttravel.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smarttravel.R
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.theme.SmarttravelTheme

// --- Dữ liệu giả ---
data class ProfileMenuItem(
    val title: String,
    val icon: ImageVector
)

val menuItems = listOf(
    ProfileMenuItem("Hồ Sơ", Icons.Default.PersonOutline),
    ProfileMenuItem("Đã Lưu", Icons.Default.BookmarkBorder),
    ProfileMenuItem("Các chuyến đi trước", Icons.Default.TravelExplore),
    ProfileMenuItem("Cài đặt", Icons.Default.Settings),
    ProfileMenuItem("Version", Icons.Default.Info)
)
// --- Kết thúc dữ liệu giả ---

@Composable
fun ProfileScreen(navController: NavController) {
    // Dùng LazyColumn để toàn bộ màn hình có thể cuộn (nếu cần)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            // Nền xám nhạt cho toàn màn hình
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        // 1. Top Bar Tùy Chỉnh
        item {
            ProfileTopBar(
                onBackClick = { navController.popBackStack() },
                onEditClick = { /*TODO: Navigate to Edit Profile*/ }
            )
        }

        // 2. Thông tin Avatar
        item {
            AvatarSection(
                userName = "Nguyễn Văn A",
                userEmail = "nguyenvanA@example.com"
            )
        }

        // 3. Menu
        item {
            MenuSection(
                items = menuItems,
                onMenuItemClick = { /*TODO: Navigate to item screen*/ }
            )
        }
    }
}

// --- CÁC COMPONENT CON ---

@Composable
fun ProfileTopBar(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tái sử dụng AppTopBar của bạn cho nút Back
        AppTopBar(onBackClick = onBackClick)

        // Tiêu đề "Profile"
        Text(
            text = "Profile",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // Nút Edit (Thiết kế tương tự AppTopBar)
        IconButton(
            onClick = onEditClick,
            modifier = Modifier
                .size(46.dp)
                .background(
                    color = Color(0xFFE0E0E0), // Nền xám nhạt [cite: 8]
                    shape = CircleShape
                ),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = Color(0xFF1A1A1A) // Icon đen [cite: 8]
            )
        ) {
            Icon(
                imageVector = Icons.Default.Edit, // Thay bằng icon của bạn
                contentDescription = "Edit Profile",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AvatarSection(userName: String, userEmail: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.avatar), // Thay avatar của bạn
            contentDescription = "Avatar",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = userEmail,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun MenuSection(
    items: List<ProfileMenuItem>,
    onMenuItemClick: (ProfileMenuItem) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = Color.White,
        // Bo góc chỉ ở trên
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            items.forEachIndexed { index, item ->
                ProfileMenuItem(
                    item = item,
                    onClick = { onMenuItemClick(item) }
                )
                // Thêm đường kẻ, trừ item cuối cùng
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.LightGray.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    item: ProfileMenuItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null, // Không cần mô tả
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        ProfileScreen(navController = navController)
    }
}