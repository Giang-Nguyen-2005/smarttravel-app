package com.example.smarttravel.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // (Các biến state cho form)
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    // (State cho kết quả của hành động: Lỗi, Thành công, v.v.)
    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }

    // (State để kiểm tra khi khởi động app - dùng cho SplashScreen)
    sealed class AuthCheckState {
        object Loading : AuthCheckState()
        data class LoggedIn(val user: FirebaseUser) : AuthCheckState()
        object LoggedOut : AuthCheckState()
    }

    private val _authCheckState = MutableStateFlow<AuthCheckState>(AuthCheckState.Loading)
    val authCheckState: StateFlow<AuthCheckState> = _authCheckState.asStateFlow()

    init {
        // Lắng nghe trạng thái đăng nhập
        viewModelScope.launch {
            authRepository.getAuthState().collect { user ->
                _authCheckState.value = if (user != null) {
                    AuthCheckState.LoggedIn(user)
                } else {
                    AuthCheckState.LoggedOut
                }
            }
        }
    }

    // --- CÁC HÀM HÀNH ĐỘNG (ACTION) ---

    fun registerUser() {
        if (password != confirmPassword) {
            authState = AuthState.Error("Mật khẩu xác nhận không khớp.")
            return
        }
        authState = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.registerUser(email, password)
            authState = if (result.isSuccess) {
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Đăng ký thất bại.")
            }
        }
    }

    fun loginUser() {
        authState = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.loginUser(email, password)
            authState = if (result.isSuccess) {
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Đăng nhập thất bại.")
            }
        }
    }

    fun signInWithGoogle(idToken: String, email: String) {
        authState = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken, email)
            authState = if (result.isSuccess) {
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Đăng nhập Google thất bại.")
            }
        }
    }

    fun sendPasswordResetEmail() {
        if (email.isBlank()) {
            authState = AuthState.Error("Vui lòng nhập email.")
            return
        }
        authState = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            authState = if (result.isSuccess) {
                // Bạn có thể muốn một state riêng cho 'ResetEmailSent'
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Gửi email thất bại.")
            }
        }
    }

    fun logout() {
        authRepository.logout()
        authState = AuthState.Idle
    }

    fun resetAuthState() {
        authState = AuthState.Idle
    }
}