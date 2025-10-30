package com.example.smarttravel.ui.wait

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarttravel.R
import com.example.smarttravel.ui.components.AppTextField
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.components.SocialButton
import com.example.smarttravel.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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

            AppTopBar(onBackClick = onBackClick)
        }

        // 📄 Nội dung chính
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 150.dp)
        ) {
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

            // ✉️ Email
            AppTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "NguyenVanA@example.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().height(65.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🔒 Mật khẩu
            AppTextField(
                value = password,
                onValueChange = { password = it },
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
                modifier = Modifier.fillMaxWidth().height(65.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = onForgotPassword) {
                    Text("Quên mật khẩu?", color = Color(0xFF1E88E5), fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = "Đăng nhập",
                onClick = onLoginClick,
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
                TextButton(onClick = onSignUpClick, contentPadding = PaddingValues(0.dp)) {
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
