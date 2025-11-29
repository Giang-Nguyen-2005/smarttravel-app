package com.example.smarttravel.model

import com.google.firebase.firestore.PropertyName

data class Destination(
    var id: String = "", // Code Repository sẽ gán Document ID vào đây
    val name: String = "",
    val description: String = "",

    @get:PropertyName("estimated_cost")
    @set:PropertyName("estimated_cost")
    var estimated_cost: Long = 0,

    @get:PropertyName("location_name")
    @set:PropertyName("location_name")
    var location_name: String = "",

    @get:PropertyName("category_id")
    @set:PropertyName("category_id")
    var category_id: String = "",

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    @get:PropertyName("rating")
    @set:PropertyName("rating")
    var rating: Double = 0.0,

    val images: List<String> = emptyList(),
    
    @get:PropertyName("created_by")
    @set:PropertyName("created_by")
    var created_by: String = "" // ID của admin tạo địa điểm này
)