
package com.example.smarttravel.ui.screens.editprofile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smarttravel.R
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.components.AppTextField
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.components.PhoneNumberField
import com.example.smarttravel.ui.theme.SmarttravelTheme
import com.example.smarttravel.ui.viewmodel.ProfileViewModel

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val updateMessage by viewModel.updateMessage.collectAsState()
    val context = LocalContext.current

    // State cho các trường trong Form
    var displayName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    // 💡 TRƯỜNG SỞ THÍCH MỚI 💡
    var interestsString by remember { mutableStateOf("") } // Dùng String để dễ nhập

    // Tự động điền dữ liệu vào form khi userProfile tải xong
    LaunchedEffect(userProfile) {
        userProfile?.let {
            displayName = it.displayName
            location = it.location
            phoneNumber = it.phoneNumber
            // 💡 ĐIỀN DỮ LIỆU SỞ THÍCH 💡
            // Chuyển List<String> thành chuỗi phân cách bởi dấu phẩy
            interestsString = it.interests.joinToString(", ")
        }
    }

    // Lắng nghe thông báo cập nhật
    LaunchedEffect(updateMessage) {
        if (updateMessage != null) {
            Toast.makeText(context, updateMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage() // Xóa thông báo
            if (updateMessage == "Cập nhật thành công!") {
                navController.popBackStack() // Quay về màn hình trước
            }
        }
    }

    Scaffold(
        // Không cần bottom bar ở màn hình Edit
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Bar
            item {
                EditProfileTopBar(
                    onBackClick = { navController.popBackStack() },
                    onUpdateClick = {
                        // Gọi ViewModel để cập nhật
                        // 💡 TRUYỀN DỮ LIỆU SỞ THÍCH MỚI 💡
                        viewModel.updateUserProfile(
                            displayName,
                            phoneNumber,
                            location,
                            interestsString // Truyền chuỗi sở thích
                        )
                    },
                    isLoading = isLoading
                )
            }

            // Avatar (Lấy từ userProfile)
            item {
                EditAvatarSection(
                    userName = userProfile?.displayName ?: "...",
                    avatarUrl = userProfile?.avatarUrl ?: ""
                )
            }

            // Form nhập liệu
            item {
                ProfileFormSection(
                    displayName = displayName,
                    onDisplayNameChange = { displayName = it },
                    location = location,
                    onLocationChange = { location = it },
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = { phoneNumber = it },
                    // 💡 THÊM THAM SỐ SỞ THÍCH VÀO FORM 💡
                    interestsString = interestsString,
                    onInterestsStringChange = { interestsString = it }
                )
            }

            // Spacer cuối
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ... Các Composable khác (EditProfileTopBar, EditAvatarSection) giữ nguyên ...

@Composable
fun EditProfileTopBar(
    onBackClick: () -> Unit,
    onUpdateClick: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppTopBar(onBackClick = onBackClick)

        Text(
            text = "Chỉnh sửa hồ sơ",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // Nút Cập nhật
        Text(
            text = if (isLoading) "Đang..." else "Cập nhật",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier
                .background(color = Color(0xFFE0E0E0), shape = CircleShape)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clickable(enabled = !isLoading) {
                    onUpdateClick()
                }
        )
    }
}

@Composable
fun EditAvatarSection(userName: String, avatarUrl: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dùng AsyncImage như màn hình ProfileScreen
        if (avatarUrl.isNotEmpty()) {
            coil.compose.AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.avatar),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun ProfileFormSection(
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    // 💡 THAM SỐ SỞ THÍCH MỚI 💡
    interestsString: String,
    onInterestsStringChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {

        // Tên hiển thị
        Text(
            text = "Tên hiển thị",
            fontSize = 20.sp,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        AppTextField(
            value = displayName,
            onValueChange = onDisplayNameChange,
            placeholder = "Nhập tên hiển thị"
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Địa chỉ
        Text(
            text = "Địa chỉ",
            fontSize = 20.sp,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        AppTextField(
            value = location,
            onValueChange = onLocationChange,
            placeholder = "Nhập địa chỉ"
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Số điện thoại
        Text(
            text = "Số điện thoại",
            fontSize = 20.sp,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        PhoneNumberField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 💡 TRƯỜNG NHẬP SỞ THÍCH MỚI 💡
        Text(
            text = "Sở thích",
            fontSize = 20.sp,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        AppTextField(
            value = interestsString,
            onValueChange = onInterestsStringChange,
            placeholder = "Ví dụ: Leo núi, Đạp xe, Ẩm thực "
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        EditProfileScreen(navController = navController)
    }
}