package com.example.smarttravel.ui.wait

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarttravel.ui.components.AppTextField
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResetPasswordScreen(
    onBackClick: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // 🔙 Nút Back
        AppTopBar(onBackClick = onBackClick)

        // 📄 Nội dung chính
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

            // ✉️ Nhập email
            AppTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "NguyenVanA@example.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 🔘 Nút gửi
            PrimaryButton(
                text = "Gửi",
                onClick = {
                    if (email.isNotBlank()) {
                        showDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
        }

        // 📨 Hiển thị thông báo sau khi gửi
        if (showDialog) {
            EmailSentDialog(
                onDismiss = { showDialog = false },
                email = email
            )
        }
    }
}

/**
 * 📨 Hộp thông báo "Kiểm tra email của bạn"
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