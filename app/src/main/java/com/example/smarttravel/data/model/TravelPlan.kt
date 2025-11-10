package com.example.smarttravel.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class TravelPlan(
    val id: String = "",
    val userId: String = "",
    val destinationId: String = "",
    val title: String = "", // Ví dụ: "Chuyến đi đến [Tên địa điểm]"
    val coverImageUrl: String = "", // Có thể lấy ảnh đầu tiên của destination

    val companion: String = "", // Từ GoWithScreen
    val budget: String = "", // Từ EconomyScreen
    val purposes: List<String> = emptyList(), // Từ PurposeScreen

    @ServerTimestamp
    val startDate: Timestamp? = null, // Từ PeriodScreen
    @ServerTimestamp
    val endDate: Timestamp? = null, // Từ PeriodScreen

    @ServerTimestamp
    val createdAt: Timestamp? = null,

    // Để trống, AI có thể điền sau
    val planDetail: List<Map<String, Any>> = emptyList()
)