package com.example.smarttravel.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

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
            _authState.value = AuthState.Error("Mật khẩu xác nhận không khớp.")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.registerUser(email, password)
            _authState.value = if (result.isSuccess) AuthState.Success
            else AuthState.Error(result.exceptionOrNull()?.message ?: "Đăng ký thất bại.")
        }
    }

    fun loginUser() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.loginUser(email, password)
            _authState.value = if (result.isSuccess) AuthState.Success
            else AuthState.Error(result.exceptionOrNull()?.message ?: "Đăng nhập thất bại.")
        }
    }

    fun signInWithGoogle(idToken: String, email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken, email)
            _authState.value = when {
                result.isSuccess -> AuthState.Success
                result.exceptionOrNull()?.message == "EXISTING_EMAIL_NEED_LINK" ->
                    AuthState.NeedPasswordForLink(email, idToken) // Luồng A
                else -> AuthState.Error(result.exceptionOrNull()?.message ?: "Đăng nhập Google thất bại.")
            }
        }
    }

    // Khi người dùng đã có tài khoản email, nhập mật khẩu để link Google
    fun linkGoogleWithPassword(idToken: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val loginResult = authRepository.loginUser(email, password)
            _authState.value = if (loginResult.isSuccess) {
                val linkResult = authRepository.linkGoogleAccount(idToken)
                if (linkResult.isSuccess) AuthState.Success
                else AuthState.Error(linkResult.exceptionOrNull()?.message ?: "Liên kết Google thất bại.")
            } else {
                AuthState.Error(loginResult.exceptionOrNull()?.message ?: "Đăng nhập thất bại.")
            }
        }
    }

    fun linkEmailPasswordAccount(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.linkEmailPasswordAccount(email, password)
            _authState.value = if (result.isSuccess) {
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Liên kết mật khẩu thất bại.")
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = AuthState.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
    fun getCurrentUser() = authRepository.getCurrentUser()

    fun changePassword(currentPassword: String, newPassword: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.changePassword(currentPassword, newPassword)
            _authState.value = if (result.isSuccess) {
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Đổi mật khẩu thất bại")
            }
        }
    }

    fun deleteAccount(password: String? = null) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.deleteAccount(password)
            _authState.value = if (result.isSuccess) {
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Xóa tài khoản thất bại")
            }
        }
    }
}