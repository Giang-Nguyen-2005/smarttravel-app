package com.example.smarttravel.data.repository

interface AiService {
    suspend fun generateTravelPlan(
        destination: String,
        locationName: String,
        companion: String,
        startDate: String,
        endDate: String,
        budget: String,
        purposes: List<String>
    ): Result<String> // Trả về JSON string của planDetail
}

