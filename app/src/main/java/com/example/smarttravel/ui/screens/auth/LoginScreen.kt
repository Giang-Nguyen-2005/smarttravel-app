package com.example.smarttravel.ui.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.components.SocialButton
import com.example.smarttravel.ui.theme.SmarttravelTheme
import com.example.smarttravel.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {

    val authViewModel: AuthViewModel = hiltViewModel()
    val authState = authViewModel.authState
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- CẤU HÌNH GOOGLE SIGN-IN ---
    val webClientId = stringResource(R.string.default_web_client_id)

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // --- BẮT ĐẦU: SỬA LOGIC LAUNCHER ---
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Đăng nhập Google thành công
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken
                val email = account.email // <-- LẤY EMAIL TỪ ACCOUNT

                // Kiểm tra cả 2 không null
                if (idToken != null && email != null) {
                    // GỌI HÀM MỚI VỚI 2 THAM SỐ
                    authViewModel.signInWithGoogle(idToken, email)
                } else {
                    Toast.makeText(context, "Không thể lấy thông tin (Token/Email) từ Google.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: ApiException) {
                // Đăng nhập Google thất bại
                Toast.makeText(context, "Đăng nhập Google thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Người dùng hủy (nhấn back), không cần thông báo
        }
    }
    // --- KẾT THÚC: SỬA LOGIC LAUNCHER ---
    // --- KẾT THÚC CẤU HÌNH GOOGLE SIGN-IN ---


    // Xử lý trạng thái Auth
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
                authViewModel.resetAuthState()
            }
            is AuthViewModel.AuthState.Error -> {
                Toast.makeText(context, authState.message, Toast.LENGTH_LONG).show() // <-- Đổi sang LONG
                authViewModel.resetAuthState() // <-- Reset để người dùng thử lại
            }
            else -> {} // Không làm gì cho Idle, Loading
        }
    }

    val email = authViewModel.email
    val password = authViewModel.password
    var passwordVisible by remember { mutableStateOf(false) }

    // (Xóa bỏ Alert Dialog của code cũ)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
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
                .padding(top = 120.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

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
                onValueChange = { authViewModel.email = it },
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
                onValueChange = { authViewModel.password = it },
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

            // Quên mật khẩu?
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = { navController.navigate(Screen.ResetPassword.route) }) {
                    Text("Quên mật khẩu?", color = Color(0xFF1E88E5), fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nút Đăng nhập
            PrimaryButton(
                text = if (authState is AuthViewModel.AuthState.Loading) "Đang đăng nhập..." else "Đăng nhập",
                onClick = { authViewModel.loginUser() },
                enabled = authState !is AuthViewModel.AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )


            Spacer(modifier = Modifier.height(30.dp))

            // Link Đăng Ký
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chưa có tài khoản? ", color = Color.Gray, fontSize = 18.sp)
                TextButton(
                    onClick = { navController.navigate(Screen.Register.route) },
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
                // Nút GOOGLE
                SocialButton(
                    iconRes = R.drawable.icon_google,
                    onClick = {
                        // Kiểm tra xem có đang loading không
                        if (authState is AuthViewModel.AuthState.Loading) return@SocialButton

                        coroutineScope.launch {
                            // Đăng xuất khỏi Google client trước để luôn hiện cửa sổ chọn tài khoản
                            googleSignInClient.signOut().addOnCompleteListener {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        }
                    }
                )

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