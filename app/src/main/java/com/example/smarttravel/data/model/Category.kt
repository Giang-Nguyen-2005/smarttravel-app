package com.example.smarttravel.model


import com.google.firebase.firestore.PropertyName

data class Category(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",
    val name: String = ""
)