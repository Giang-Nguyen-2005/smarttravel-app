package com.example.smarttravel.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // <-- Cần cho hiltViewModel()
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smarttravel.R
import com.example.smarttravel.navigation.Screen // Đảm bảo đường dẫn này đúng
import com.example.smarttravel.ui.components.AppTextField
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.components.SocialButton
import com.example.smarttravel.ui.theme.SmarttravelTheme
import com.example.smarttravel.ui.viewmodel.AuthViewModel

// Xóa @OptIn(ExperimentalMaterial3Api::class) nếu không cần thiết
@Composable
fun LoginScreen(navController: NavController) { // Đổi lại thành nhận NavController

    val authViewModel: AuthViewModel = hiltViewModel() // Sử dụng HiltViewModel
    val authState = authViewModel.authState
    val context = LocalContext.current

    // Cần LaunchedEffect để xử lý các sự kiện từ ViewModel
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home.route) { // Chuyển đến Home Screen
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
                authViewModel.resetAuthState()
            }
            is AuthViewModel.AuthState.Error -> {
                Toast.makeText(context, authState.message, Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
            }
            else -> {} // Do nothing for Idle or Loading
        }
    }

    // Lấy giá trị từ ViewModel (được quản lý bởi mutableStateOf bên trong ViewModel)
    val email = authViewModel.email
    val password = authViewModel.password
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()) // Đảm bảo có thể cuộn
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
        )
        {
            AppTopBar(onBackClick = { navController.popBackStack() })
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp) // Điều chỉnh top padding cho cả Column
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Thêm khoảng cách

            Text(
                text = "Đăng nhập",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Text(
                text = "Vui lòng đăng nhập để tiếp tục khám phá",
                color = Color.Gray,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 20.dp, bottom = 40.dp)
            )

            // Email
            AppTextField(
                value = email,
                onValueChange = { authViewModel.email = it }, // Cập nhật ViewModel
                placeholder = "NguyenVanA@example.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mật khẩu
            AppTextField(
                value = password,
                onValueChange = { authViewModel.password = it }, // Cập nhật ViewModel
                placeholder = "********",
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible)
                        Icons.Default.VisibilityOff
                    else
                        Icons.Default.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = { /* TODO: Navigate to Forgot Password */ }) {
                    Text("Quên mật khẩu?", color = Color(0xFF1E88E5), fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = if (authState is AuthViewModel.AuthState.Loading) "Đang đăng nhập..." else "Đăng nhập",
                onClick = { authViewModel.loginUser() }, // Gọi hàm đăng nhập của ViewModel
                enabled = authState !is AuthViewModel.AuthState.Loading, // Vô hiệu hóa nút khi đang tải
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chưa có tài khoản? ", color = Color.Gray, fontSize = 18.sp)
                TextButton(
                    onClick = { navController.navigate(Screen.Register.route) }, // Chuyển đến màn hình đăng ký
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Đăng Ký", color = Color(0xFF1E88E5 ), fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Hoặc đăng nhập với", color = Color.Gray, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SocialButton(iconRes = R.drawable.icon_google, onClick = {})
                Spacer(modifier = Modifier.width(16.dp))
                SocialButton(iconRes = R.drawable.icon_instagram, onClick = {})
                Spacer(modifier = Modifier.width(16.dp))
                SocialButton(iconRes = R.drawable.icon_facebook, onClick = {})
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    SmarttravelTheme {
        LoginScreen(navController = rememberNavController())
    }
}