package com.example.smarttravel.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.os.Build
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.smarttravel.R
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.components.AppBottomBar
import com.example.smarttravel.ui.theme.SmarttravelTheme
import com.example.smarttravel.ui.viewmodel.AuthViewModel
import com.example.smarttravel.ui.viewmodel.ProfileViewModel

// --- DỮ LIỆU MENU ---
data class ProfileMenuItem(
    val title: String,
    val icon: ImageVector,
    val subtitle: String? = null
)

val menuItems = listOf(
    ProfileMenuItem("Hồ Sơ", Icons.Default.PersonOutline),
    ProfileMenuItem("Đã Lưu", Icons.Default.BookmarkBorder),
    ProfileMenuItem("Các chuyến đi trước", Icons.Default.TravelExplore),
    ProfileMenuItem("Cài đặt", Icons.Default.Settings),
    ProfileMenuItem("Version", Icons.Default.Info),
    ProfileMenuItem("Đăng xuất", Icons.Default.ExitToApp)
)

// --- NÚT BACK & EDIT ---
@Composable
fun RoundIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(46.dp)
            .background(colorScheme.surfaceVariant, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

// --- PROFILE SCREEN ---
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()

    // Lấy version từ PackageManager
    val appVersion = remember {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
            "${packageInfo.versionName}"
        } catch (e: Exception) {
            "1.0"
        }
    }

    // Cập nhật menu items với version
    val menuItemsWithVersion = remember(appVersion) {
        menuItems.map { item ->
            if (item.title == "Version") {
                item.copy(subtitle = appVersion)
            } else {
                item
            }
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = Screen.Profile.route)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            // Top Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Nút back (căn trái)
                    RoundIconButton(
                        onClick = { navController.popBackStack() },
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    // Title căn giữa
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Nút edit (căn phải)
                    RoundIconButton(
                        onClick = { navController.navigate(Screen.EditProfile.route) },
                        icon = Icons.Default.Edit,
                        contentDescription = "Chỉnh sửa",
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }

            // Avatar
            item {
                userProfile?.let { profile ->
                    val safeName = profile.displayName ?: ""
                    val safeEmail = profile.email ?: ""
                    val displayName = safeName.ifEmpty { safeEmail.substringBefore("@") }

                    AvatarSection(
                        userName = displayName,
                        userEmail = safeEmail,
                        avatarUrl = profile.avatarUrl
                    )
                } ?: AvatarSection(
                    userName = "Đang tải...",
                    userEmail = "",
                    avatarUrl = null
                )
            }

            // Menu
            item {
                MenuSection(
                    items = menuItemsWithVersion,
                    onMenuItemClick = { item ->
                        when (item.title) {
                            "Hồ Sơ" -> navController.navigate(Screen.UserProfileDetail.route)
                            "Đã Lưu" -> navController.navigate(Screen.SavedDestinations.route)
                            "Các chuyến đi trước" -> navController.navigate(Screen.PreviousTrips.route)
                            "Cài đặt" -> navController.navigate(Screen.Settings.route)
                            "Version" -> { /* Không có hành động, chỉ hiển thị thông tin */ }
                            "Đăng xuất" -> {
                                authViewModel.logout()
                                // Điều hướng về màn hình Login và xóa toàn bộ stack
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

// --- USER PROFILE DETAIL SCREEN (KHÔNG MÀU NỀN + BỎ ICON >) ---
@Composable
fun UserProfileDetailScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val profile = userProfile

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .statusBarsPadding()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundIconButton(
                        onClick = { navController.popBackStack() },
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại"
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Thông tin hồ sơ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                val displayName = (profile?.displayName ?: "")
                    .ifEmpty { profile?.email?.substringBefore("@") ?: "N/A" }

                val detailItems = listOf(
                    ProfileMenuItem("Tên hiển thị", Icons.Default.Person, displayName),
                    ProfileMenuItem("Email", Icons.Default.Email, profile?.email ?: "N/A"),
                    ProfileMenuItem("Số điện thoại", Icons.Default.Phone, profile?.phoneNumber?.takeIf { it.isNotBlank() } ?: "Chưa cập nhật"),
                    ProfileMenuItem("Địa chỉ", Icons.Default.LocationOn, profile?.location?.takeIf { it.isNotBlank() } ?: "Chưa cập nhật"),
                    ProfileMenuItem("Sở thích", Icons.Default.Favorite, profile?.interests?.joinToString(", ")?.takeIf { it.isNotBlank() } ?: "Chưa cập nhật")
                )

                // DÙNG DetailMenuSection → KHÔNG CÓ ICON >
                DetailMenuSection(items = detailItems)
            }
        }
    }
}

// --- AVATAR SECTION ---
@Composable
fun AvatarSection(userName: String, userEmail: String, avatarUrl: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!avatarUrl.isNullOrEmpty()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(R.drawable.avatar),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = userEmail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// --- MENU SECTION (CÓ ICON >) ---
@Composable
fun MenuSection(
    items: List<ProfileMenuItem>,
    onMenuItemClick: (ProfileMenuItem) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.surface,
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
            items.forEachIndexed { index, item ->
                ProfileMenuItem(item = item, onClick = { onMenuItemClick(item) })
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(item: ProfileMenuItem, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
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
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
            item.subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

// --- DETAIL MENU SECTION (KHÔNG CÓ ICON >) ---
@Composable
fun DetailMenuSection(items: List<ProfileMenuItem>) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            items.forEachIndexed { index, item ->
                DetailMenuItem(item = item)
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun DetailMenuItem(item: ProfileMenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.subtitle ?: "Chưa cập nhật",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3
            )
        }
    }
}

// --- VERSION DIALOG ---
@Composable
fun VersionDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Thông tin phiên bản",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VersionInfoRow(label = "Tên ứng dụng", value = "Smart Travel")
                VersionInfoRow(label = "Phiên bản", value = "1.0")
                VersionInfoRow(label = "Version Code", value = "1")
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "© 2024 Smart Travel. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", fontWeight = FontWeight.Medium)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun VersionInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
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

@Preview(showBackground = true)
@Composable
fun UserProfileDetailScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        UserProfileDetailScreen(navController = navController)
    }
}