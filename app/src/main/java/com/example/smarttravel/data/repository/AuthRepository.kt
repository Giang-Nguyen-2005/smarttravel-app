package com.example.smarttravel.data.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun registerUser(email: String, password: String): Result<Unit>
    suspend fun loginUser(email: String, password: String): Result<Unit>
    fun getCurrentUser(): FirebaseUser?
    fun logout()
}