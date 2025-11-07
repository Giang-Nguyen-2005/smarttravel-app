package com.example.smarttravel.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.example.smarttravel.model.UserProfile

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {
    private suspend fun ensureUserExistInFirestore(user: FirebaseUser) {
        val userDocRef = firestore.collection("users").document(user.uid)
        val snapshot = userDocRef.get().await()

        if (!snapshot.exists()) {
            // Nếu chưa có document -> Tạo mới với ĐẦY ĐỦ các trường
            val userMap = hashMapOf(
                "email" to (user.email ?: ""),
                "display_name" to (user.displayName ?: user.email?.substringBefore("@") ?: "User"),
                "avatar_url" to (user.photoUrl?.toString() ?: ""),
                "phone_number" to (user.phoneNumber ?: ""),
                "location" to "",
                "created_at" to com.google.firebase.Timestamp.now(),
                "interests" to emptyList<String>(),
                "favorite_destination_ids" to emptyList<String>()
            )
            userDocRef.set(userMap).await()
        } else {
            // (Tùy chọn) Nếu đã có -> Cập nhật thông tin mới nhất từ Google (nếu cần)
            val updates = hashMapOf<String, Any>()
            if (!user.displayName.isNullOrEmpty()) updates["display_name"] = user.displayName!!
            if (user.photoUrl != null) updates["avatar_url"] = user.photoUrl.toString()

            if (updates.isNotEmpty()) {
                userDocRef.set(updates, SetOptions.merge()).await()
            }
        }
    }

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
                // LIÊN KẾT (User đã đăng nhập trước đó, nên đã có Firestore doc)
                val credential = EmailAuthProvider.getCredential(email, password)
                currentUser.linkWithCredential(credential).await()
                // Vẫn gọi ensure để chắc chắn cập nhật data nếu cần
                ensureUserExistInFirestore(currentUser)
                Result.success(Unit)
            } else {
                // TẠO MỚI
                val authResult =
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                // [MỚI] Tạo document bên Firestore ngay sau khi đăng ký thành công
                authResult.user?.let { ensureUserExistInFirestore(it) }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            // [MỚI] Kiểm tra và tạo doc nếu thiếu ngay sau khi đăng nhập
            authResult.user?.let { ensureUserExistInFirestore(it) }
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
                // LIÊN KẾT
                try {
                    currentUser.linkWithCredential(credential).await()
                    ensureUserExistInFirestore(currentUser) // [MỚI] Đảm bảo đồng bộ
                    Result.success(Unit)
                } catch (e: Exception) {
                    if (e.message?.contains("already linked") == true ||
                        e.message?.contains("already been linked") == true
                    ) {
                        ensureUserExistInFirestore(currentUser) // [MỚI]
                        Result.success(Unit)
                    } else {
                        Result.failure(e)
                    }
                }
            } else {
                // ĐĂNG NHẬP / TẠO MỚI BẰNG GOOGLE
                val methods = firebaseAuth.fetchSignInMethodsForEmail(email).await().signInMethods
                if (methods!!.contains("password")) {
                    Result.failure(Exception("Email này đã có mật khẩu. Vui lòng đăng nhập bằng mật khẩu, rồi vào Profile để liên kết Google."))
                } else {
                    val authResult = firebaseAuth.signInWithCredential(credential).await()
                    // [MỚI] Quan trọng: Tạo document Firestore cho user Google
                    authResult.user?.let { ensureUserExistInFirestore(it) }
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

    override fun getUserProfile(): Flow<UserProfile?> = callbackFlow {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docRef = firestore.collection("users").document(currentUser.uid)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Xử lý lỗi nếu cần
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val userProfile = snapshot.toObject(UserProfile::class.java)
                trySend(userProfile)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }
}