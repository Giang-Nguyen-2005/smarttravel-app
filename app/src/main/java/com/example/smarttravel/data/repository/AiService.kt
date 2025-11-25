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

    // Gợi ý điểm đến dựa trên sở thích của user từ danh sách destinations có sẵn
    suspend fun rankDestinationsByInterests(
        interests: List<String>,
        destinations: List<com.example.smarttravel.model.Destination>,
        recentPlanDestinationIds: List<String> = emptyList() // Các điểm đến từ plans gần đây
    ): Result<List<String>> // Trả về danh sách destination IDs được rank theo mức độ phù hợp

}

