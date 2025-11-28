package com.example.smarttravel.model

// Model cho Comment - Firestore sẽ tự động serialize/deserialize
data class Comment(
    val id: String = "", // Document ID (không lưu trong Firestore, được set khi đọc)
    val destinationId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val content: String = "",
    val timestamp: Long = 0L // Timestamp để sắp xếp
)

