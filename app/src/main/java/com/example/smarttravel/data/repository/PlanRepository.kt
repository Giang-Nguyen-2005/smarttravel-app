package com.example.smarttravel.data.repository

import com.example.smarttravel.data.model.TravelPlan
import kotlinx.coroutines.flow.Flow

interface PlanRepository {
    // Lưu một kế hoạch mới vào Firestore
    suspend fun savePlan(plan: TravelPlan): Result<String>

    // (Tùy chọn) Lấy các kế hoạch đã tạo
    // fun getMyPlans(): Flow<Result<List<TravelPlan>>>
}