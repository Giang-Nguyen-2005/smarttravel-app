package com.example.smarttravel.ui.screens.editprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smarttravel.R
import com.example.smarttravel.ui.components.AppTextField
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.theme.SmarttravelTheme
import com.example.smarttravel.ui.components.PhoneNumberField
import com.example.smarttravel.ui.components.AppBottomBar
import com.example.smarttravel.navigation.Screen
import androidx.compose.foundation.clickable

@Composable
fun ProfileScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = Screen.Profile.route)
        }
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
                ProfileTopBar(
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { /* TODO: Handle edit click */ }
                )
            }

            // Avatar
            item {
                AvatarSection(userName = "Nguyễn Văn A")
            }

            // Form nhập liệu
            item {
                ProfileFormSection()
            }

            // Spacer cuối
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}


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
        AppTopBar(onBackClick = onBackClick)

        Text(
            text = "Chỉnh sửa hồ sơ",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "Cập nhật",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier
                .background(color = Color(0xFFE0E0E0), shape = CircleShape)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clickable { onEditClick() }
        )

    }
}

@Composable
fun AvatarSection(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.avatar),
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
    }
}

@Composable
fun ProfileFormSection() {
    var firstName by remember { mutableStateOf("A") }
    var lastName by remember { mutableStateOf("Nguyễn") }
    var location by remember { mutableStateOf("Việt Nam") }
    var phoneNumber by remember { mutableStateOf("+84 01758-000666") }

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        // Họ
        Text(
            text = "First Name",
            fontSize = 20.sp,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        AppTextField(
            value = lastName,
            onValueChange = { lastName = it },
            placeholder = "Nhập họ"
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Tên
        Text(
            text = "Last Name",
            fontSize = 20.sp,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        AppTextField(
            value = firstName,
            onValueChange = { firstName = it },
            placeholder = "Nhập tên"
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Địa chỉ
        Text(
            text = "Address",
            fontSize = 20.sp,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        AppTextField(
            value = location,
            onValueChange = { location = it },
            placeholder = "Nhập địa chỉ"
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Phone Number",
            fontSize = 20.sp,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        PhoneNumberField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        ProfileScreen(navController = navController)
    }
}
