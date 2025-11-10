package com.example.smarttravel.data.repository


import com.example.smarttravel.data.model.TravelPlan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

            documentRef.set(planToSave).await()
            Result.success(documentRef.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}