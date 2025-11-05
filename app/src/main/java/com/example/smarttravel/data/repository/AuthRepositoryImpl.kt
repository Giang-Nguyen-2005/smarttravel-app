package com.example.smarttravel.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override fun getAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun registerUser(email: String, password: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser

            if (currentUser != null) {
                // ĐANG ĐĂNG NHẬP (BẰNG GOOGLE) -> LIÊN KẾT (LINK)
                val credential = EmailAuthProvider.getCredential(email, password)
                currentUser.linkWithCredential(credential).await()
                Result.success(Unit)
            } else {
                // CHƯA ĐĂNG NHẬP -> TẠO TÀI KHOẢN MỚI
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String, email: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val currentUser = firebaseAuth.currentUser

            if (currentUser != null) {
                // ĐANG ĐĂNG NHẬP (BẰNG EMAIL) -> LIÊN KẾT (LINK)
                try {
                    currentUser.linkWithCredential(credential).await()
                    Result.success(Unit)
                } catch (e: Exception) {
                    // Xử lý lỗi nếu đã liên kết rồi
                    if (e.message?.contains("already linked") == true ||
                        e.message?.contains("already been linked") == true
                    ) {
                        Result.success(Unit) // Coi như thành công
                    } else {
                        Result.failure(e)
                    }
                }
            } else {
                // CHƯA ĐĂNG NHẬP -> "KIỂM TRA TRƯỚC" (PRE-CHECK)

                // 1. Kiểm tra xem email này đã có nhà cung cấp "password" chưa
                val methods = firebaseAuth.fetchSignInMethodsForEmail(email).await().signInMethods

                if (methods!!.contains("password")) {
                    // 2. NẾU CÓ "password" -> BÁO LỖI
                    // Đây là bước ngăn chặn lỗi "ghi đè" (mất email)
                    Result.failure(Exception("Email này đã có mật khẩu. Vui lòng đăng nhập bằng mật khẩu, rồi vào Profile để liên kết Google."))

                } else {
                    // 3. NẾU KHÔNG CÓ "password" -> Đăng nhập/Tạo tài khoản Google (An toàn)
                    firebaseAuth.signInWithCredential(credential).await()
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}