package com.example.smarttravel.data.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    //Lắng nghe trạng thái đăng nhập (dùng cho SplashScreen).
    fun getAuthState(): Flow<FirebaseUser?>

    // Đăng ký MỚI hoặc LIÊN KẾT Email/Pass (nếu đã đăng nhập bằng Google).
    suspend fun registerUser(email: String, password: String): Result<Unit>

    // Đăng nhập bằng Email/Password.
    suspend fun loginUser(email: String, password: String): Result<Unit>

    // Đăng nhập MỚI hoặc LIÊN KẾT Google (nếu đã đăng nhập bằng Email).
    suspend fun signInWithGoogle(idToken: String, email: String): Result<Unit>

    // Gửi email reset mật khẩu.
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    // Lấy user hiện tại (cho các hàm kiểm tra đơn giản).
    fun getCurrentUser(): FirebaseUser?

    //Đăng xuất.
    fun logout()
}