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
import com.google.firebase.auth.FirebaseAuthUserCollisionException

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private suspend fun ensureUserExistInFirestore(user: FirebaseUser) {
        val userDocRef = firestore.collection("users").document(user.uid)
        val snapshot = userDocRef.get().await()
        if (!snapshot.exists()) {
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
            val updates = hashMapOf<String, Any>()
            if (!user.displayName.isNullOrEmpty()) updates["display_name"] = user.displayName!!
            if (user.photoUrl != null) updates["avatar_url"] = user.photoUrl.toString()
            if (updates.isNotEmpty()) userDocRef.set(updates, SetOptions.merge()).await()
        }
    }

    override fun getAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }

    override suspend fun registerUser(email: String, password: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val credential = EmailAuthProvider.getCredential(email, password)
                currentUser.linkWithCredential(credential).await()
                ensureUserExistInFirestore(currentUser)
                Result.success(Unit)
            } else {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
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
            authResult.user?.let { ensureUserExistInFirestore(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String, email: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val fetchResult = firebaseAuth.fetchSignInMethodsForEmail(email).await()
            val signInMethods = fetchResult.signInMethods ?: emptyList()

            // Nếu email đã có password → yêu cầu người dùng nhập password để link
            if ("password" in signInMethods && firebaseAuth.currentUser == null) {
                return Result.failure(
                    Exception("EXISTING_EMAIL_NEED_LINK") // báo lên ViewModel
                )
            }

            // Nếu chưa tồn tại user → đăng nhập bằng Google
            val result = firebaseAuth.signInWithCredential(credential).await()
            result.user?.let { ensureUserExistInFirestore(it) }
            Result.success(Unit)

        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("EXISTING_EMAIL_NEED_LINK"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Link Google ---
    override suspend fun linkGoogleAccount(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("Không có người dùng hiện tại để liên kết."))
            currentUser.linkWithCredential(credential).await()
            ensureUserExistInFirestore(currentUser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Link Email/Password ---
    override suspend fun linkEmailPasswordAccount(email: String, password: String): Result<Unit> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("Không có người dùng hiện tại để liên kết."))

            currentUser.linkWithCredential(credential).await()
            ensureUserExistInFirestore(currentUser)
            Result.success(Unit)
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

    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    override fun logout() = firebaseAuth.signOut()

    override fun getUserProfile(): Flow<UserProfile?> = callbackFlow {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docRef = firestore.collection("users").document(currentUser.uid)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                val userProfile = snapshot.toObject(UserProfile::class.java)
                trySend(userProfile)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }
    override suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Người dùng chưa đăng nhập"))
            }
            // Chỉ cho phép update document của chính mình
            // Kiểm tra userProfile.id nếu có, nhưng chủ yếu dựa vào currentUser.uid
            if (userProfile.id.isNotEmpty() && currentUser.uid != userProfile.id) {
                return Result.failure(Exception("Không có quyền cập nhật hồ sơ này"))
            }

            // Sử dụng set() với merge để chỉ cập nhật các trường thay đổi
            firestore.collection("users").document(currentUser.uid)
                .set(userProfile, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
