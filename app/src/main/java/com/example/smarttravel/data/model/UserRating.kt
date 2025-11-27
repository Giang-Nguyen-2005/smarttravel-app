package com.example.smarttravel.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class UserRating(
    val id: String = "",
    
    @get:PropertyName("destination_id")
    val destinationId: String = "",
    
    @get:PropertyName("user_id")
    val userId: String = "",
    
    val rating: Double = 0.0, // 1.0 - 5.0
    
    @get:PropertyName("created_at")
    val createdAt: Timestamp = Timestamp.now(),
    
    @get:PropertyName("updated_at")
    val updatedAt: Timestamp = Timestamp.now()
)

