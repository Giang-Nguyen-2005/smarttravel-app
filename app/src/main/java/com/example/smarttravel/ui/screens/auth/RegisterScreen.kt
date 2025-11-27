package com.example.smarttravel.ui.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.smarttravel.ui.components.GoogleButton
import com.example.smarttravel.ui.theme.SmarttravelTheme
import com.example.smarttravel.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    val colorScheme = MaterialTheme.colorScheme
    var showLinkDialog by remember { mutableStateOf(false) }
    val authViewModel: AuthViewModel = hiltViewModel() // Sử dụng HiltViewModel
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- Google Sign-In config ---
    val webClientId = stringResource(R.string.default_web_client_id)
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken
                val email = account.email
                if (idToken != null && email != null) {
                    authViewModel.signInWithGoogle(idToken, email)
                } else {
                    Toast.makeText(context, "Không thể lấy thông tin Google.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Đăng ký Google thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Cần LaunchedEffect để xử lý các sự kiện từ ViewModel
    LaunchedEffect(authState) {
        val currentState = authState // Lưu vào biến local để smart cast
        when (currentState) {
            is AuthViewModel.AuthState.Success -> {
                val currentUser = authViewModel.getCurrentUser()
                if (currentUser != null) {
                    val providers = currentUser.providerData.map { it.providerId }
                    // Nếu đăng ký bằng Google, chuyển thẳng đến Home
                    if ("google.com" in providers) {
                        Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        // Đăng ký bằng email, hiển thị dialog hỏi link Google
                        showLinkDialog = true
                    }
                }
                authViewModel.resetAuthState()
            }
            is AuthViewModel.AuthState.Error -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
            }
            else -> {}
        }
    }

    // Lấy giá trị từ ViewModel
    val email = authViewModel.email
    val password = authViewModel.password
    val confirmPassword = authViewModel.confirmPassword // Thêm confirmPassword

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) } // Thêm cho confirmPassword

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    )
    {
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
                text = "Đăng ký",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = colorScheme.onBackground
            )
            Text(
                text = "Tạo tài khoản mới để khám phá ứng dụng",
                color = colorScheme.onSurfaceVariant,
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
                placeholder = "Mật khẩu",
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Xác nhận mật khẩu
            AppTextField(
                value = confirmPassword,
                onValueChange = { authViewModel.confirmPassword = it },
                placeholder = "Xác nhận mật khẩu",
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            PrimaryButton(
                text = if (authState is AuthViewModel.AuthState.Loading) "Đang đăng ký..." else "Đăng ký",
                onClick = { authViewModel.registerUser() }, // Gọi hàm đăng ký của ViewModel
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
                Text("Đã có tài khoản? ", color = colorScheme.onSurfaceVariant, fontSize = 18.sp)
                TextButton(
                    onClick = { navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true } // Xóa Register khỏi back stack
                        launchSingleTop = true
                    }},
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Đăng nhập", color = colorScheme.primary, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Hoặc đăng ký với", color = colorScheme.onSurfaceVariant, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(30.dp))

            GoogleButton(
                onClick = {
                    if (authState is AuthViewModel.AuthState.Loading) return@GoogleButton
                    coroutineScope.launch {
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    }
                },
                enabled = authState !is AuthViewModel.AuthState.Loading,
                text = "Đăng ký với Google",
                modifier = Modifier.padding(bottom = 40.dp)
            )
        }
    }
    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = { Text("Đăng ký thành công") },
            text = { Text("Bạn có muốn liên kết tài khoản Google để đăng nhập nhanh hơn không?") },
            confirmButton = {
                TextButton(onClick = {
                    showLinkDialog = false
                    // Nếu có token Google, gọi link, sau đó điều hướng Home
                    // Ví dụ:
                    // authViewModel.linkGoogleWithPassword(idToken, authViewModel.email, authViewModel.password)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }) {
                    Text("Có")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLinkDialog = false
                    // Không link, chuyển thẳng sang Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }) {
                    Text("Không")
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    SmarttravelTheme {
        RegisterScreen(navController = rememberNavController())
    }
}