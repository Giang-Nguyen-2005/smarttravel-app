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
    
    // Tạo gợi ý thay thế cho hotel hoặc activity
    suspend fun generateAlternativeSuggestion(
        destination: String,
        locationName: String,
        itemType: String, // "hotel" hoặc "activity"
        currentItem: Map<String, Any>, // Item hiện tại (hotel hoặc activity)
        budget: String,
        dayNumber: Int,
        date: String
    ): Result<Map<String, Any>> // Trả về hotel hoặc activity mới

    // Method mới cho chat
    suspend fun sendChatMessage(
        message: String,
        conversationHistory: List<Pair<String, String>> = emptyList() // (user, bot) pairs
    ): Result<String>

}

