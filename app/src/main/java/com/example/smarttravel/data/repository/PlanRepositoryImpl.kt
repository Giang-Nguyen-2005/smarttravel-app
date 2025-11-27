package com.example.smarttravel.data.repository


import com.example.smarttravel.data.model.TravelPlan
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : PlanRepository {

    override suspend fun savePlan(plan: TravelPlan): Result<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Người dùng chưa đăng nhập"))
            }

            val documentRef = firestore.collection("travel_plans").document()

            // Gán ID và userId trước khi lưu
            val planToSave = plan.copy(
                id = documentRef.id,
                userId = currentUser.uid
            )

            // Log để debug
            android.util.Log.d("PlanRepositoryImpl", "Saving plan: id=${planToSave.id}, userId=${planToSave.userId}, title=${planToSave.title}")
            android.util.Log.d("PlanRepositoryImpl", "startDate=${planToSave.startDate}, endDate=${planToSave.endDate}")

            documentRef.set(planToSave).await()
            
            // Verify sau khi lưu
            val savedDoc = documentRef.get().await()
            android.util.Log.d("PlanRepositoryImpl", "Plan saved successfully. Document exists: ${savedDoc.exists()}")
            if (savedDoc.exists()) {
                android.util.Log.d("PlanRepositoryImpl", "Saved userId: ${savedDoc.getString("userId")}")
                android.util.Log.d("PlanRepositoryImpl", "Saved startDate: ${savedDoc.getTimestamp("startDate")}")
                android.util.Log.d("PlanRepositoryImpl", "Saved endDate: ${savedDoc.getTimestamp("endDate")}")
            }
            
            Result.success(documentRef.id)

        } catch (e: Exception) {
            android.util.Log.e("PlanRepositoryImpl", "Error saving plan: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun updatePlanDetail(planId: String, planDetail: List<Map<String, Any>>): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Người dùng chưa đăng nhập"))
            }
            
            val documentRef = firestore.collection("travel_plans").document(planId)
            val snapshot = documentRef.get().await()
            
            if (!snapshot.exists()) {
                return Result.failure(Exception("Không tìm thấy kế hoạch"))
            }
            
            // Kiểm tra quyền sở hữu
            val userId = snapshot.getString("userId")
            if (userId != currentUser.uid) {
                return Result.failure(Exception("Không có quyền cập nhật kế hoạch này"))
            }
            
            documentRef.update("planDetail", planDetail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updatePlanDetailItem(
        planId: String,
        dayIndex: Int,
        itemType: String,
        activityIndex: Int?,
        newItem: Map<String, Any>
    ): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Người dùng chưa đăng nhập"))
            }
            
            val documentRef = firestore.collection("travel_plans").document(planId)
            val snapshot = documentRef.get().await()
            
            if (!snapshot.exists()) {
                return Result.failure(Exception("Không tìm thấy kế hoạch"))
            }
            
            // Kiểm tra quyền sở hữu
            val userId = snapshot.getString("userId")
            if (userId != currentUser.uid) {
                return Result.failure(Exception("Không có quyền cập nhật kế hoạch này"))
            }
            
            // Lấy planDetail hiện tại
            @Suppress("UNCHECKED_CAST")
            val planDetail = snapshot.get("planDetail") as? List<Map<String, Any>> ?: emptyList()
            
            if (dayIndex >= planDetail.size) {
                return Result.failure(Exception("Ngày không hợp lệ"))
            }
            
            // Tạo planDetail mới với item đã được cập nhật
            val updatedPlanDetail = planDetail.toMutableList()
            val dayMap = updatedPlanDetail[dayIndex].toMutableMap()
            
            when (itemType) {
                "hotel" -> {
                    dayMap["hotel"] = newItem
                }
                "activity" -> {
                    if (activityIndex == null) {
                        return Result.failure(Exception("activityIndex không được null khi itemType là activity"))
                    }
                    @Suppress("UNCHECKED_CAST")
                    val activities = (dayMap["activities"] as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                    if (activityIndex >= activities.size) {
                        return Result.failure(Exception("Activity index không hợp lệ"))
                    }
                    activities[activityIndex] = newItem
                    dayMap["activities"] = activities
                }
                else -> return Result.failure(Exception("Item type không hợp lệ: $itemType"))
            }
            
            updatedPlanDetail[dayIndex] = dayMap
            
            // Cập nhật vào Firestore
            documentRef.update("planDetail", updatedPlanDetail).await()
            android.util.Log.d("PlanRepositoryImpl", "PlanDetail item updated successfully: day=$dayIndex, type=$itemType")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("PlanRepositoryImpl", "Error updating planDetail item: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override fun getMyPlans(): Flow<Result<List<TravelPlan>>> = callbackFlow {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            android.util.Log.d("PlanRepositoryImpl", "getMyPlans: No current user")
            trySend(Result.success(emptyList()))
            close()
            return@callbackFlow
        }
        
        android.util.Log.d("PlanRepositoryImpl", "getMyPlans: Querying for userId=${currentUser.uid}")
        
        val snapshotListener = firestore.collection("travel_plans")
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("PlanRepositoryImpl", "getMyPlans error: ${e.message}", e)
                    trySend(Result.failure(e))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    android.util.Log.d("PlanRepositoryImpl", "getMyPlans: Found ${snapshot.documents.size} documents")
                    val plans = snapshot.toObjects(TravelPlan::class.java)
                    val plansWithIds = plans.mapIndexed { index, plan ->
                        val docId = snapshot.documents[index].id
                        android.util.Log.d("PlanRepositoryImpl", "Plan $index: id=$docId, title=${plan.title}, userId=${plan.userId}")
                        plan.copy(id = docId)
                    }
                    // Sắp xếp theo createdAt ở client-side (giảm dần - mới nhất trước)
                    val sortedPlans = plansWithIds.sortedByDescending { plan ->
                        plan.createdAt?.toDate()?.time ?: 0L
                    }
                    android.util.Log.d("PlanRepositoryImpl", "getMyPlans: Returning ${sortedPlans.size} plans")
                    trySend(Result.success(sortedPlans))
                } else {
                    android.util.Log.d("PlanRepositoryImpl", "getMyPlans: Snapshot is null")
                }
            }
        awaitClose { snapshotListener.remove() }
    }
    
    override fun getPlanById(planId: String): Flow<Result<TravelPlan>> = callbackFlow {
        val docRef = firestore.collection("travel_plans").document(planId)
        val listener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Result.failure(e))
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val plan = snapshot.toObject(TravelPlan::class.java)
                if (plan != null) {
                    trySend(Result.success(plan.copy(id = snapshot.id)))
                } else {
                    trySend(Result.failure(Exception("Lỗi parse dữ liệu kế hoạch")))
                }
            } else {
                trySend(Result.failure(Exception("Không tìm thấy kế hoạch với ID này")))
            }
        }
        awaitClose { listener.remove() }
    }
    
    override suspend fun hasOverlappingPlan(startDate: Timestamp, endDate: Timestamp): Result<Boolean> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Người dùng chưa đăng nhập"))
            }
            
            // Lấy tất cả kế hoạch của user hiện tại
            val snapshot = firestore.collection("travel_plans")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()
            
            // Kiểm tra xem có kế hoạch nào trùng ngày không
            // Hai khoảng thời gian trùng nhau nếu:
            // startDate mới <= endDate cũ VÀ endDate mới >= startDate cũ
            val hasOverlap = snapshot.documents.any { doc ->
                val plan = doc.toObject(TravelPlan::class.java)
                if (plan?.startDate != null && plan.endDate != null) {
                    val planStart = plan.startDate!!
                    val planEnd = plan.endDate!!
                    
                    // Kiểm tra overlap: startDate mới <= endDate cũ VÀ endDate mới >= startDate cũ
                    startDate.toDate().time <= planEnd.toDate().time &&
                    endDate.toDate().time >= planStart.toDate().time
                } else {
                    false
                }
            }
            
            Result.success(hasOverlap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deletePlan(planId: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Người dùng chưa đăng nhập"))
            }
            
            val documentRef = firestore.collection("travel_plans").document(planId)
            val snapshot = documentRef.get().await()
            
            if (!snapshot.exists()) {
                return Result.failure(Exception("Không tìm thấy kế hoạch"))
            }
            
            // Kiểm tra quyền sở hữu
            val userId = snapshot.getString("userId")
            if (userId != currentUser.uid) {
                return Result.failure(Exception("Không có quyền xóa kế hoạch này"))
            }
            
            documentRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("PlanRepositoryImpl", "Error deleting plan: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun hasPlanWithDestination(destinationId: String): Result<Boolean> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Người dùng chưa đăng nhập"))
            }
            
            val snapshot = firestore.collection("travel_plans")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("destinationId", destinationId)
                .limit(1)
                .get()
                .await()
            
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            android.util.Log.e("PlanRepositoryImpl", "Error checking plan with destination: ${e.message}", e)
            Result.failure(e)
        }
    }
}