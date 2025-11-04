package com.example.smarttravel.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel // <-- THÊM DÒNG NÀY
import kotlinx.coroutines.launch
import javax.inject.Inject // <-- THÊM DÒNG NÀY

// Đánh dấu ViewModel này để Hilt tự động tạo và cung cấp nó
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository // Hilt sẽ tự động cung cấp AuthRepositoryImpl
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }

    fun registerUser() {
        if (password != confirmPassword) {
            authState = AuthState.Error("Mật khẩu xác nhận không khớp.")
            return
        }
        authState = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.registerUser(email, password)
            if (result.isSuccess) {
                authState = AuthState.Success
            } else {
                authState = AuthState.Error(result.exceptionOrNull()?.message ?: "Đăng ký thất bại.")
            }
        }
    }

    fun loginUser() {
        authState = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.loginUser(email, password)
            if (result.isSuccess) {
                authState = AuthState.Success
            } else {
                authState = AuthState.Error(result.exceptionOrNull()?.message ?: "Đăng nhập thất bại.")
            }
        }
    }

    fun logout() {
        authRepository.logout()
        authState = AuthState.Idle
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.getCurrentUser() != null
    }

    fun resetAuthState() {
        authState = AuthState.Idle
    }
}