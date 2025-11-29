package com.example.smarttravel.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class UserProfile(
    @DocumentId
    val id: String = "",
    @get:PropertyName("email")
    val email: String = "",
    @get:PropertyName("display_name")
    @set:PropertyName("display_name")
    var displayName: String = "",
    @get:PropertyName("avatar_url")
    @set:PropertyName("avatar_url")
    var avatarUrl: String = "",
    // 👇 Các trường MỚI được thêm 👇
    @get:PropertyName("phone_number")
    @set:PropertyName("phone_number")
    var phoneNumber: String = "",
    @get:PropertyName("location")
    @set:PropertyName("location")
    var location: String = "",
    // Chuẩn bị sẵn cho tính năng Yêu thích (Option 2)
    @get:PropertyName("favorite_destination_ids")
    @set:PropertyName("favorite_destination_ids")
    var favoriteDestinationIds: List<String> = emptyList(),

    // 💡 TRƯỜNG MỚI ĐÃ ĐƯỢC THÊM VÀO 💡
    @get:PropertyName("interests")
    @set:PropertyName("interests")
    var interests: List<String> = emptyList(), // Hoặc List<Any> nếu kiểu dữ liệu đa dạng hơn
    
    @get:PropertyName("is_admin")
    @set:PropertyName("is_admin")
    var isAdmin: Boolean = false
)