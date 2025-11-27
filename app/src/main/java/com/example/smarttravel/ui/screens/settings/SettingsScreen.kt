package com.example.smarttravel.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.viewmodel.AuthViewModel
import com.example.smarttravel.ui.viewmodel.SettingsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

// --- COLOR PALETTE ---
// val AppPrimary = Color(0xFF037CAC)
// val BackgroundLight = Color(0xFFF5F7FA)
// val TextDark = Color(0xFF1A1A1A)
// val TextGray = Color(0xFF757575)
// val CardBorder = Color(0xFFEEEEEE)
// val DangerRed = Color(0xFFD32F2F)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val permissionsState by settingsViewModel.permissionsState.collectAsState()
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val currentUser = authViewModel.getCurrentUser()
    
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    
    // Permissions
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    val storagePermissionsState = rememberMultiplePermissionsState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            emptyList() // Android 13+ không cần
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    )
    
    val notificationPermissionState = rememberMultiplePermissionsState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyList()
        }
    )

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            AppTopBar(
                onBackClick = { navController.popBackStack() },
                title = "Cài đặt",
                containerColor = colorScheme.surfaceVariant
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Tài khoản & Bảo mật
            item {
                SectionHeader("Tài khoản & Bảo mật")
            }
            
            // Đổi mật khẩu
            item {
                if (currentUser?.email != null && currentUser.providerData.any { 
                    it.providerId == "password" 
                }) {
                    SettingsItem(
                        title = "Đổi mật khẩu",
                        icon = Icons.Default.Lock,
                        onClick = { showChangePasswordDialog = true }
                    )
                }
            }
            
            // Xóa tài khoản
            item {
                SettingsItem(
                    title = "Xóa tài khoản",
                    icon = Icons.Default.DeleteForever,
                    iconTint = Color.Red, // Changed from DangerRed
                    onClick = { showDeleteAccountDialog = true }
                )
            }
            
            // Quản lý quyền truy cập
            item {
                SettingsItem(
                    title = "Quản lý quyền truy cập",
                    icon = Icons.Default.Security,
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                )
            }
            
            // Chi tiết quyền
            item {
                PermissionDetailsSection(
                    permissionsState = permissionsState,
                    locationPermissionsState = locationPermissionsState,
                    storagePermissionsState = storagePermissionsState,
                    notificationPermissionState = notificationPermissionState,
                    onPermissionRequest = {
                        when (it) {
                            "location" -> locationPermissionsState.launchMultiplePermissionRequest()
                            "storage" -> if (storagePermissionsState.permissions.isNotEmpty()) {
                                storagePermissionsState.launchMultiplePermissionRequest()
                            }
                            "notification" -> if (notificationPermissionState.permissions.isNotEmpty()) {
                                notificationPermissionState.launchMultiplePermissionRequest()
                            }
                        }
                        settingsViewModel.checkPermissions()
                    }
                )
            }
            
            // Section: Giao diện
            item {
                SectionHeader("Giao diện")
            }
            
            // Chế độ sáng/tối
            item {
                SettingsItem(
                    title = "Chế độ sáng/tối",
                    icon = Icons.Default.Palette,
                    subtitle = when (themeMode) {
                        com.example.smarttravel.ui.theme.ThemeMode.LIGHT -> "Sáng"
                        com.example.smarttravel.ui.theme.ThemeMode.DARK -> "Tối"
                        com.example.smarttravel.ui.theme.ThemeMode.SYSTEM -> "Theo hệ thống"
                    },
                    onClick = { showThemeDialog = true }
                )
            }
            
            // Section: Về ứng dụng
            item {
                SectionHeader("Về ứng dụng")
            }
            
            // Phiên bản ứng dụng
            item {
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
                        "${packageInfo.versionName} (Build $versionCode)"
                    } catch (e: Exception) {
                        "1.0"
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Phiên bản ứng dụng",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = appVersion,
                                fontSize = 14.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Điều khoản sử dụng
            item {
                SettingsItem(
                    title = "Điều khoản sử dụng",
                    icon = Icons.Default.Description,
                    onClick = { showTermsDialog = true }
                )
            }
            
            // Chính sách bảo mật
            item {
                SettingsItem(
                    title = "Chính sách bảo mật",
                    icon = Icons.Default.PrivacyTip,
                    onClick = { showPrivacyDialog = true }
                )
            }
        }
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { 
                showChangePasswordDialog = false
                authViewModel.resetAuthState()
            },
            onConfirm = { currentPassword, newPassword ->
                authViewModel.changePassword(currentPassword, newPassword)
            },
            authState = authState,
            onDismissRequest = { 
                showChangePasswordDialog = false
                authViewModel.resetAuthState()
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onDismiss = { 
                showDeleteAccountDialog = false
                authViewModel.resetAuthState()
            },
            onConfirm = { password ->
                authViewModel.deleteAccount(password)
            },
            authState = authState,
            onDismissRequest = { 
                showDeleteAccountDialog = false
                authViewModel.resetAuthState()
            },
            hasPassword = currentUser?.providerData?.any { it.providerId == "password" } == true,
            onDeleteSuccess = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                    launchSingleTop = true
                }
                showDeleteAccountDialog = false
                authViewModel.resetAuthState()
            }
        )
    }
    
    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onThemeSelected = { theme ->
                settingsViewModel.setThemeMode(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
    
    // Terms of Service Dialog
    if (showTermsDialog) {
        TermsOfServiceDialog(
            onDismiss = { showTermsDialog = false }
        )
    }
    
    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
fun SettingsItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground // Changed from TextDark
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant // Changed from TextGray
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDetailsSection(
    permissionsState: Map<String, Boolean>,
    locationPermissionsState: com.google.accompanist.permissions.MultiplePermissionsState,
    storagePermissionsState: com.google.accompanist.permissions.MultiplePermissionsState,
    notificationPermissionState: com.google.accompanist.permissions.MultiplePermissionsState,
    onPermissionRequest: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Changed from Color.White
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Chi tiết quyền truy cập",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground // Changed from TextDark
            )
            
            PermissionItem(
                name = "Vị trí",
                description = "Để tìm địa điểm và chỉ đường",
                isGranted = permissionsState["location"] == true,
                onRequest = { onPermissionRequest("location") }
            )
            
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                PermissionItem(
                    name = "Lưu trữ",
                    description = "Để lưu và chia sẻ ảnh",
                    isGranted = permissionsState["storage"] == true,
                    onRequest = { onPermissionRequest("storage") }
                )
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionItem(
                    name = "Thông báo",
                    description = "Để nhận thông báo về chuyến đi",
                    isGranted = permissionsState["notification"] == true,
                    onRequest = { onPermissionRequest("notification") }
                )
            }
        }
    }
}

@Composable
fun PermissionItem(
    name: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground // Changed from TextDark
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Changed from TextGray
            )
        }
        if (!isGranted) {
            TextButton(onClick = onRequest) {
                Text("Cấp quyền", fontSize = 12.sp)
            }
        } else {
            Text(
                text = "Đã cấp",
                fontSize = 12.sp,
                color = Color(0xFF388E3C),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    authState: AuthViewModel.AuthState,
    onDismissRequest: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState) {
        val currentState = authState
        when (currentState) {
            is AuthViewModel.AuthState.Success -> {
                onDismiss()
            }
            is AuthViewModel.AuthState.Error -> {
                errorMessage = currentState.message
            }
            else -> {}
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Đổi mật khẩu", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { 
                        currentPassword = it
                        errorMessage = null
                    },
                    label = { Text("Mật khẩu hiện tại") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPassword) VisualTransformation.None 
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                null
                            )
                        }
                    },
                    isError = errorMessage != null && errorMessage!!.contains("Mật khẩu hiện tại"),
                    supportingText = if (errorMessage != null && errorMessage!!.contains("Mật khẩu hiện tại")) {
                        {
                            Text(
                                text = errorMessage!!,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    } else null
                )
                
                // Hiển thị lỗi chung (không phải lỗi của mật khẩu hiện tại hoặc xác nhận)
                if (errorMessage != null && 
                    !errorMessage!!.contains("Mật khẩu hiện tại") && 
                    !errorMessage!!.contains("không khớp") && 
                    !errorMessage!!.contains("xác nhận") &&
                    !errorMessage!!.contains("Vui lòng nhập")) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        errorMessage = null
                    },
                    label = { Text("Mật khẩu mới") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showNewPassword) androidx.compose.ui.text.input.VisualTransformation.None 
                        else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                null
                            )
                        }
                    }
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Xác nhận mật khẩu mới") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None 
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                null
                            )
                        }
                    },
                    isError = errorMessage != null && (errorMessage!!.contains("không khớp") || errorMessage!!.contains("xác nhận")),
                    supportingText = if (errorMessage != null && (errorMessage!!.contains("không khớp") || errorMessage!!.contains("xác nhận"))) {
                        {
                            Text(
                                text = errorMessage!!,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    } else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Xóa lỗi cũ
                    errorMessage = null
                    
                    // Validation
                    if (currentPassword.isEmpty()) {
                        errorMessage = "Vui lòng nhập mật khẩu hiện tại"
                    } else if (newPassword.isEmpty()) {
                        errorMessage = "Vui lòng nhập mật khẩu mới"
                    } else if (confirmPassword.isEmpty()) {
                        errorMessage = "Vui lòng xác nhận mật khẩu mới"
                    } else if (newPassword != confirmPassword) {
                        errorMessage = "Mật khẩu xác nhận không khớp"
                    } else if (newPassword.length < 6) {
                        errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
                    } else {
                        onConfirm(currentPassword, newPassword)
                    }
                },
                enabled = authState !is AuthViewModel.AuthState.Loading
            ) {
                if (authState is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Xác nhận")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
    authState: AuthViewModel.AuthState,
    onDismissRequest: () -> Unit,
    hasPassword: Boolean,
    onDeleteSuccess: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState) {
        val currentState = authState
        when (currentState) {
            is AuthViewModel.AuthState.Success -> {
                onDeleteSuccess()
            }
            is AuthViewModel.AuthState.Error -> {
                errorMessage = currentState.message
            }
            else -> {}
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Xóa tài khoản", fontWeight = FontWeight.Bold, color = Color.Red) // Changed from DangerRed
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Bạn có chắc chắn muốn xóa tài khoản? Hành động này không thể hoàn tác và tất cả dữ liệu của bạn sẽ bị xóa vĩnh viễn.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                if (hasPassword) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Nhập mật khẩu để xác nhận") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showPassword) VisualTransformation.None 
                            else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null
                                )
                            }
                        },
                        isError = errorMessage != null && errorMessage!!.contains("Mật khẩu"),
                        supportingText = if (errorMessage != null && errorMessage!!.contains("Mật khẩu")) {
                            {
                                Text(
                                    text = errorMessage!!,
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        } else null
                    )

                    if (errorMessage != null && !errorMessage!!.contains("Mật khẩu")) {
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errorMessage = null

                    if (hasPassword && password.isEmpty()) {
                        errorMessage = "Vui lòng nhập mật khẩu"
                    } else {
                        onConfirm(if (hasPassword) password else null)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                enabled = authState !is AuthViewModel.AuthState.Loading
            ) {
                if (authState is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("Xóa tài khoản")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: com.example.smarttravel.ui.theme.ThemeMode,
    onThemeSelected: (com.example.smarttravel.ui.theme.ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chọn chế độ sáng/tối",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground // Changed from TextDark
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOption(
                    title = "Sáng",
                    isSelected = currentTheme == com.example.smarttravel.ui.theme.ThemeMode.LIGHT,
                    onClick = { onThemeSelected(com.example.smarttravel.ui.theme.ThemeMode.LIGHT) }
                )
                ThemeOption(
                    title = "Tối",
                    isSelected = currentTheme == com.example.smarttravel.ui.theme.ThemeMode.DARK,
                    onClick = { onThemeSelected(com.example.smarttravel.ui.theme.ThemeMode.DARK) }
                )
                ThemeOption(
                    title = "Theo hệ thống",
                    isSelected = currentTheme == com.example.smarttravel.ui.theme.ThemeMode.SYSTEM,
                    onClick = { onThemeSelected(com.example.smarttravel.ui.theme.ThemeMode.SYSTEM) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun ThemeOption(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TermsOfServiceDialog(onDismiss: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Điều khoản sử dụng",
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Cập nhật lần cuối: ${java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "1. Chấp nhận điều khoản\n\n" +
                            "Bằng việc sử dụng ứng dụng Smart Travel, bạn đồng ý tuân thủ các điều khoản và điều kiện được nêu trong tài liệu này. Nếu bạn không đồng ý với bất kỳ điều khoản nào, vui lòng không sử dụng ứng dụng.\n\n" +
                            "2. Mô tả dịch vụ\n\n" +
                            "Smart Travel là ứng dụng hỗ trợ lập kế hoạch du lịch thông minh, cung cấp các tính năng:\n" +
                            "• Tư vấn và gợi ý địa điểm du lịch\n" +
                            "• Tạo lịch trình tự động bằng AI\n" +
                            "• Quản lý kế hoạch du lịch cá nhân\n" +
                            "• Tìm kiếm và lưu địa điểm yêu thích\n" +
                            "• Chia sẻ kế hoạch du lịch\n\n" +
                            "3. Tài khoản người dùng\n\n" +
                            "• Bạn chịu trách nhiệm bảo mật thông tin tài khoản của mình\n" +
                            "• Bạn không được chia sẻ thông tin đăng nhập với bất kỳ ai\n" +
                            "• Bạn chịu trách nhiệm cho mọi hoạt động diễn ra trên tài khoản của bạn\n\n" +
                            "4. Sử dụng dịch vụ\n\n" +
                            "• Bạn cam kết sử dụng ứng dụng một cách hợp pháp và phù hợp với mục đích\n" +
                            "• Không được sử dụng ứng dụng để thực hiện các hành vi vi phạm pháp luật\n" +
                            "• Không được can thiệp, phá hoại hoặc làm gián đoạn hoạt động của ứng dụng\n\n" +
                            "5. Quyền sở hữu trí tuệ\n\n" +
                            "• Tất cả nội dung trong ứng dụng, bao gồm logo, hình ảnh, văn bản đều thuộc quyền sở hữu của Smart Travel\n" +
                            "• Bạn không được sao chép, phân phối hoặc sử dụng nội dung mà không có sự cho phép\n\n" +
                            "6. Miễn trừ trách nhiệm\n\n" +
                            "• Ứng dụng cung cấp thông tin tham khảo, không đảm bảo tính chính xác tuyệt đối\n" +
                            "• Smart Travel không chịu trách nhiệm về bất kỳ thiệt hại nào phát sinh từ việc sử dụng ứng dụng\n" +
                            "• Người dùng tự chịu trách nhiệm về quyết định du lịch của mình\n\n" +
                            "7. Thay đổi điều khoản\n\n" +
                            "Chúng tôi có quyền thay đổi các điều khoản này bất cứ lúc nào. Thay đổi sẽ có hiệu lực ngay sau khi được đăng tải trên ứng dụng.\n\n" +
                            "8. Liên hệ\n\n" +
                            "Nếu bạn có câu hỏi về các điều khoản này, vui lòng liên hệ với chúng tôi qua email: support@smarttravel.com",
                    fontSize = 14.sp,
                    color = colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = colorScheme.primary)
            }
        },
        containerColor = colorScheme.surface
    )
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chính sách bảo mật",
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Cập nhật lần cuối: ${java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "1. Thông tin chúng tôi thu thập\n\n" +
                            "Smart Travel thu thập các thông tin sau:\n" +
                            "• Thông tin tài khoản: Tên, email, ảnh đại diện\n" +
                            "• Thông tin du lịch: Kế hoạch, địa điểm đã lưu, sở thích\n" +
                            "• Thông tin thiết bị: Vị trí (khi được cấp quyền), loại thiết bị\n" +
                            "• Dữ liệu sử dụng: Lịch sử tìm kiếm, tương tác với ứng dụng\n\n" +
                            "2. Mục đích sử dụng thông tin\n\n" +
                            "Chúng tôi sử dụng thông tin để:\n" +
                            "• Cung cấp và cải thiện dịch vụ\n" +
                            "• Cá nhân hóa trải nghiệm người dùng\n" +
                            "• Gửi thông báo về các tính năng mới\n" +
                            "• Phân tích và cải thiện hiệu suất ứng dụng\n\n" +
                            "3. Bảo mật thông tin\n\n" +
                            "• Chúng tôi sử dụng các biện pháp bảo mật tiên tiến để bảo vệ thông tin của bạn\n" +
                            "• Dữ liệu được mã hóa trong quá trình truyền tải\n" +
                            "• Chỉ nhân viên được ủy quyền mới có thể truy cập thông tin\n" +
                            "• Chúng tôi không bán hoặc chia sẻ thông tin cá nhân cho bên thứ ba\n\n" +
                            "4. Quyền truy cập và kiểm soát\n\n" +
                            "Bạn có quyền:\n" +
                            "• Xem và chỉnh sửa thông tin cá nhân\n" +
                            "• Xóa tài khoản và dữ liệu bất cứ lúc nào\n" +
                            "• Từ chối cấp quyền truy cập vị trí\n" +
                            "• Yêu cầu xuất dữ liệu cá nhân\n\n" +
                            "5. Cookie và công nghệ theo dõi\n\n" +
                            "• Ứng dụng sử dụng cookie để cải thiện trải nghiệm\n" +
                            "• Bạn có thể quản lý cookie trong cài đặt thiết bị\n\n" +
                            "6. Quyền truy cập của bên thứ ba\n\n" +
                            "Chúng tôi có thể chia sẻ thông tin với:\n" +
                            "• Nhà cung cấp dịch vụ đám mây (Firebase, Google)\n" +
                            "• Các đối tác phân tích để cải thiện dịch vụ\n" +
                            "• Chỉ khi có yêu cầu pháp lý\n\n" +
                            "7. Bảo vệ trẻ em\n\n" +
                            "Ứng dụng không dành cho trẻ em dưới 13 tuổi. Chúng tôi không cố ý thu thập thông tin từ trẻ em.\n\n" +
                            "8. Thay đổi chính sách\n\n" +
                            "Chúng tôi có thể cập nhật chính sách này. Thay đổi sẽ được thông báo qua ứng dụng.\n\n" +
                            "9. Liên hệ\n\n" +
                            "Nếu có câu hỏi về chính sách bảo mật, vui lòng liên hệ:\n" +
                            "Email: privacy@smarttravel.com\n" +
                            "Địa chỉ: Việt Nam",
                    fontSize = 14.sp,
                    color = colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = colorScheme.primary)
            }
        },
        containerColor = colorScheme.surface
    )
}
