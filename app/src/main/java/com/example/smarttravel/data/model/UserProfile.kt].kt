package com.example.smarttravel.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class UserProfile(
    @DocumentId
    val id: String = "",
    @get:PropertyName("display_name")
    @set:PropertyName("display_name")
    var displayName: String = "",
    @get:PropertyName("avatar_url")
    @set:PropertyName("avatar_url")
    var avatarUrl: String = "",
    val email: String = ""
)