package com.example.smarttravel.data.repository

import com.example.smarttravel.data.model.TravelPlan
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

interface PlanRepository {
    // Lưu một kế hoạch mới vào Firestore
    suspend fun savePlan(plan: TravelPlan): Result<String>
    
    // Cập nhật planDetail cho một plan đã tồn tại
    suspend fun updatePlanDetail(planId: String, planDetail: List<Map<String, Any>>): Result<Unit>

    // Lấy các kế hoạch đã tạo của user hiện tại
    fun getMyPlans(): kotlinx.coroutines.flow.Flow<Result<List<TravelPlan>>>
    
    // Lấy một kế hoạch theo ID
    fun getPlanById(planId: String): kotlinx.coroutines.flow.Flow<Result<TravelPlan>>
    
    // Kiểm tra xem có kế hoạch nào trùng ngày với khoảng thời gian cho trước không
    suspend fun hasOverlappingPlan(startDate: Timestamp, endDate: Timestamp): Result<Boolean>
}