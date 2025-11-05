package com.example.smarttravel.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smarttravel.ui.components.AppTextField
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.theme.SmarttravelTheme
import com.example.smarttravel.ui.viewmodel.AuthViewModel

@Composable
fun ResetPasswordScreen(navController: NavController) {

    // --- SỬ DỤNG VIEWMODEL ---
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState = authViewModel.authState
    val email = authViewModel.email
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }

    // --- XỬ LÝ KẾT QUẢ TỪ VIEWMODEL ---
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                showDialog = true // Mở dialog khi thành công
                // ViewModel sẽ được reset khi dialog đóng
            }
            is AuthViewModel.AuthState.Error -> {
                Toast.makeText(context, authState.message, Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
            }
            else -> {} // Idle or Loading
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
        ) {
            // Nút back này đã được hoàn thiện
            AppTopBar(onBackClick = { navController.popBackStack() })
        }

        // Nội dung chính
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 150.dp)
        ) {
            Text(
                text = "Quên mật khẩu",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
            Text(
                text = "Vui lòng nhập email để đặt lại mật khẩu",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 36.dp)
            )

            // Nhập email
            AppTextField(
                value = email,
                onValueChange = { authViewModel.email = it }, // Cập nhật ViewModel
                placeholder = "NguyenVanA@example.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
            )
            Spacer(modifier = Modifier.height(28.dp))

            // Nút gửi
            PrimaryButton(
                text = if (authState is AuthViewModel.AuthState.Loading) "Đang gửi..." else "Gửi",
                onClick = {
                    authViewModel.sendPasswordResetEmail() // Gọi ViewModel
                },
                enabled = authState !is AuthViewModel.AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
        }

        // Hiển thị thông báo sau khi gửi
        if (showDialog) {
            EmailSentDialog(
                onDismiss = {
                    showDialog = false
                    authViewModel.resetAuthState()
                    navController.popBackStack() // Tự động quay về Login
                },
                email = email
            )
        }
    }
}

/**
 *  Hộp thông báo "Kiểm tra email của bạn" (Giữ nguyên)
 */
@Composable
fun EmailSentDialog(
    onDismiss: () -> Unit,
    email: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon tròn
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Kiểm tra email của bạn",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chúng tôi đã gửi hướng dẫn khôi phục mật khẩu đến:\n$email",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        shape = MaterialTheme.shapes.large,
        containerColor = Color.White
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResetPasswordScreenPreview() {
    SmarttravelTheme {
        // Dùng NavController giả để Preview
        ResetPasswordScreen(navController = rememberNavController())
    }
}