package com.example.smarttravel.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepositoryImpl
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
        data class NeedPasswordForLink(val email: String, val idToken: String) : AuthState()
    }

    sealed class AuthCheckState {
        object Loading : AuthCheckState()
        data class LoggedIn(val user: FirebaseUser) : AuthCheckState()
        object LoggedOut : AuthCheckState()
    }

    private val _authCheckState = MutableStateFlow<AuthCheckState>(AuthCheckState.Loading)
    val authCheckState: StateFlow<AuthCheckState> = _authCheckState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getAuthState().collect { user ->
                _authCheckState.value = if (user != null) AuthCheckState.LoggedIn(user)
                else AuthCheckState.LoggedOut
            }
        }
    }

    // --- Actions ---
    fun registerUser() {
        if (password != confirmPassword) {
            authState = AuthState.Error("Mật khẩu xác nhận không khớp.")
            return
        }
        authState = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.registerUser(email, password)
            authState = if (result.isSuccess) AuthState.Success
            else AuthState.Error(result.exceptionOrNull()?.message ?: "Đăng ký thất bại.")
        }
    }

    fun loginUser() {
        authState = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.loginUser(email, password)
            authState = if (result.isSuccess) AuthState.Success
            else AuthState.Error(result.exceptionOrNull()?.message ?: "Đăng nhập thất bại.")
        }
    }

    fun signInWithGoogle(idToken: String, email: String) {
        authState = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken, email)
            authState = when {
                result.isSuccess -> AuthState.Success
                result.exceptionOrNull()?.message?.contains(
                    "vui lòng đăng nhập bằng email trước",
                    ignoreCase = true
                ) == true -> AuthState.NeedPasswordForLink(email, idToken)
                else -> AuthState.Error(result.exceptionOrNull()?.message ?: "Đăng nhập Google thất bại.")
            }
        }
    }

    // Khi người dùng đã có tài khoản email, nhập mật khẩu để link Google
    fun linkGoogleWithPassword(idToken: String, email: String, password: String) {
        viewModelScope.launch {
            val loginResult = authRepository.loginUser(email, password)
            if (loginResult.isSuccess) {
                val linkResult = authRepository.linkGoogleAccount(idToken)
                authState = if (linkResult.isSuccess) AuthState.Success
                else AuthState.Error(linkResult.exceptionOrNull()?.message ?: "Liên kết Google thất bại.")
            } else {
                authState = AuthState.Error(loginResult.exceptionOrNull()?.message ?: "Đăng nhập thất bại.")
            }
        }
    }

    // --- Mới: Link Email/Password vào tài khoản hiện tại ---
    fun linkEmailWithPassword(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.linkEmailPasswordAccount(email, password)
            authState = if (result.isSuccess) AuthState.Success
            else AuthState.Error(result.exceptionOrNull()?.message ?: "Liên kết Email/Password thất bại.")
        }
    }
    fun linkEmailPasswordAccount(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.linkEmailPasswordAccount(email, password)
            authState = if (result.isSuccess) {
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Liên kết mật khẩu thất bại.")
            }
        }
    }
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            authState = if (result.isSuccess) {
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Gửi email đặt lại mật khẩu thất bại.")
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
    fun getCurrentUser() = authRepository.getCurrentUser()
}