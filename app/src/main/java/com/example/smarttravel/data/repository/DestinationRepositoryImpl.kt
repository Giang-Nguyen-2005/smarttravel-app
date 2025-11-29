package com.example.smarttravel.data.repository

import com.example.smarttravel.data.model.UserRating
import com.example.smarttravel.model.Destination
import com.example.smarttravel.model.Category
import com.example.smarttravel.util.NetworkUtil
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
class DestinationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val networkUtil: NetworkUtil
) : DestinationRepository {

    override fun getCategories(): Flow<Result<List<Category>>> = callbackFlow {
        val snapshotListener = firestore.collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(Result.failure(e))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val categories = snapshot.toObjects(Category::class.java)
                    val categoriesWithIds = categories.mapIndexed { index, category ->
                        category.copy(id = snapshot.documents[index].id)
                    }
                    trySend(Result.success(categoriesWithIds))
                }
            }
        awaitClose { snapshotListener.remove() }
    }

    override fun getDestinations(): Flow<Result<List<Destination>>> = callbackFlow {
        val snapshotListener = firestore.collection("destinations")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(Result.failure(e))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val destinations = snapshot.toObjects(Destination::class.java)
                    val destinationsWithIds = destinations.mapIndexed { index, dest ->
                        dest.copy(id = snapshot.documents[index].id)
                    }
                    trySend(Result.success(destinationsWithIds))
                }
            }
        awaitClose { snapshotListener.remove() }
    }

    override fun getDestinationById(id: String): Flow<Result<Destination>> = callbackFlow {
        val docRef = firestore.collection("destinations").document(id)
        val snapshotListener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Result.failure(e))
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val destination = snapshot.toObject(Destination::class.java)
                if (destination != null) {
                    val destinationWithId = destination.copy(id = snapshot.id)
                    trySend(Result.success(destinationWithId))
                } else {
                    trySend(Result.failure(Exception("Không tìm thấy địa điểm với ID này")))
                }
            } else {
                if (!networkUtil.isNetworkAvailable()) {
                    trySend(Result.failure(Exception("Không tìm thấy địa điểm và không có kết nối mạng")))
                } else {
                    trySend(Result.failure(Exception("Không tìm thấy địa điểm với ID này")))
                }
            }
        }
        awaitClose { snapshotListener.remove() }
    }

    override fun searchDestinations(query: String): Flow<Result<List<Destination>>> = callbackFlow {
        // Cách đơn giản: Lấy tất cả về rồi lọc (Client-side filtering).
        // Phù hợp khi dữ liệu chưa quá lớn (dưới vài nghìn địa điểm).
        val snapshotListener = firestore.collection("destinations")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(Result.failure(e))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val allDestinations = snapshot.toObjects(Destination::class.java)
                    // Gán ID
                    val destinationsWithIds = allDestinations.mapIndexed { index, dest ->
                        dest.copy(id = snapshot.documents[index].id)
                    }

                    // LOGIC LỌC: Tìm theo tên HOẶC địa điểm (không phân biệt hoa thường)
                    val filteredList = if (query.isBlank()) {
                        emptyList() // Nếu không nhập gì thì trả về rỗng (hoặc trả về tất cả tùy bạn)
                    } else {
                        destinationsWithIds.filter {
                            it.name.contains(query, ignoreCase = true) ||
                                    it.location_name.contains(query, ignoreCase = true)
                        }
                    }
                    trySend(Result.success(filteredList))
                }
            }
            awaitClose { snapshotListener.remove() }
    }

    override fun getDestinationsByIds(ids: List<String>): Flow<Result<List<Destination>>> = callbackFlow {
        if (ids.isEmpty()) {
            trySend(Result.success(emptyList()))
            close()
            return@callbackFlow
        }
        
        val idsSet = ids.toSet()
        val snapshotListener = firestore.collection("destinations")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(Result.failure(e))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val allDestinations = snapshot.toObjects(Destination::class.java)
                    val destinationsWithIds = allDestinations.mapIndexed { index, dest ->
                        dest.copy(id = snapshot.documents[index].id)
                    }
                    // Lọc và sắp xếp theo thứ tự của ids
                    val filteredDestinations = ids.mapNotNull { id ->
                        destinationsWithIds.find { it.id == id }
                    }
                    trySend(Result.success(filteredDestinations))
                }
            }
        awaitClose { snapshotListener.remove() }
    }
    
    // Rating methods
    override suspend fun saveUserRating(destinationId: String, userId: String, rating: Double): Result<Unit> {
        return try {
            android.util.Log.d("DestinationRepositoryImpl", "saveUserRating: destinationId=$destinationId, userId=$userId, rating=$rating")
            
            // Validate rating
            if (rating < 1.0 || rating > 5.0) {
                android.util.Log.e("DestinationRepositoryImpl", "Invalid rating: $rating")
                return Result.failure(Exception("Rating phải từ 1.0 đến 5.0"))
            }
            
            // Kiểm tra xem đã có rating chưa
            android.util.Log.d("DestinationRepositoryImpl", "Checking for existing rating...")
            val existingRatingQuery = firestore.collection("user_ratings")
                .whereEqualTo("destination_id", destinationId)
                .whereEqualTo("user_id", userId)
                .limit(1)
                .get()
                .await()
            
            val now = Timestamp.now()
            
            if (existingRatingQuery.isEmpty) {
                // Tạo rating mới
                android.util.Log.d("DestinationRepositoryImpl", "Creating new rating...")
                val ratingData = hashMapOf(
                    "destination_id" to destinationId,
                    "user_id" to userId,
                    "rating" to rating,
                    "created_at" to now,
                    "updated_at" to now
                )
                val docRef = firestore.collection("user_ratings").add(ratingData).await()
                android.util.Log.d("DestinationRepositoryImpl", "Rating created with ID: ${docRef.id}")
            } else {
                // Cập nhật rating hiện có
                val docId = existingRatingQuery.documents[0].id
                android.util.Log.d("DestinationRepositoryImpl", "Updating existing rating with ID: $docId")
                firestore.collection("user_ratings").document(docId).update(
                    "rating", rating,
                    "updated_at", now
                ).await()
                android.util.Log.d("DestinationRepositoryImpl", "Rating updated successfully")
            }
            
            // Cập nhật rating trung bình của destination
            android.util.Log.d("DestinationRepositoryImpl", "Updating destination average rating...")
            updateDestinationRating(destinationId)
            
            android.util.Log.d("DestinationRepositoryImpl", "saveUserRating: Success")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("DestinationRepositoryImpl", "Error saving user rating: ${e.message}", e)
            android.util.Log.e("DestinationRepositoryImpl", "Error type: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }
    
    override suspend fun getUserRating(destinationId: String, userId: String): Result<UserRating?> {
        return try {
            val snapshot = firestore.collection("user_ratings")
                .whereEqualTo("destination_id", destinationId)
                .whereEqualTo("user_id", userId)
                .limit(1)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                Result.success(null)
            } else {
                val doc = snapshot.documents[0]
                val rating = doc.toObject(UserRating::class.java)
                Result.success(rating?.copy(id = doc.id))
            }
        } catch (e: Exception) {
            android.util.Log.e("DestinationRepositoryImpl", "Error getting user rating: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override fun getAverageRating(destinationId: String): Flow<Result<Double>> = callbackFlow {
        val snapshotListener = firestore.collection("user_ratings")
            .whereEqualTo("destination_id", destinationId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(Result.failure(e))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val ratings = snapshot.documents.mapNotNull { doc ->
                        doc.getDouble("rating")
                    }
                    val average = if (ratings.isNotEmpty()) {
                        ratings.average()
                    } else {
                        0.0
                    }
                    trySend(Result.success(average))
                }
            }
        awaitClose { snapshotListener.remove() }
    }
    
    override suspend fun updateDestinationRating(destinationId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection("user_ratings")
                .whereEqualTo("destination_id", destinationId)
                .get()
                .await()
            
            val ratings = snapshot.documents.mapNotNull { doc ->
                doc.getDouble("rating")
            }
            
            val averageRating = if (ratings.isNotEmpty()) {
                ratings.average()
            } else {
                0.0
            }
            
            // Làm tròn đến 1 chữ số thập phân
            val roundedRating = String.format("%.1f", averageRating).toDouble()
            
            // Cập nhật rating vào destination
            firestore.collection("destinations").document(destinationId)
                .update("rating", roundedRating)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("DestinationRepositoryImpl", "Error updating destination rating: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun addDestination(destination: Destination): Result<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Người dùng chưa đăng nhập"))
            }
            
            android.util.Log.d("DestinationRepositoryImpl", "Adding destination: ${destination.name}")
            
            // Tạo document mới
            val docRef = firestore.collection("destinations").document()
            val destinationToSave = destination.copy(
                id = docRef.id,
                created_by = currentUser.uid // Lưu ID của user tạo
            )
            
            // Lưu vào Firestore
            docRef.set(destinationToSave).await()
            
            android.util.Log.d("DestinationRepositoryImpl", "Destination added successfully with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            android.util.Log.e("DestinationRepositoryImpl", "Error adding destination: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun updateDestination(destinationId: String, destination: Destination): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Người dùng chưa đăng nhập"))
            }
            
            android.util.Log.d("DestinationRepositoryImpl", "Updating destination: $destinationId - ${destination.name}")
            
            // Lấy destination hiện tại để giữ lại created_by
            val currentDoc = firestore.collection("destinations").document(destinationId).get().await()
            val existingCreatedBy = if (currentDoc.exists()) {
                currentDoc.getString("created_by") ?: ""
            } else {
                currentUser.uid // Nếu không tìm thấy, dùng user hiện tại
            }
            
            // Cập nhật destination (không cập nhật id và giữ lại created_by)
            val destinationToUpdate = destination.copy(
                id = destinationId,
                created_by = existingCreatedBy
            )
            firestore.collection("destinations").document(destinationId)
                .set(destinationToUpdate)
                .await()
            
            android.util.Log.d("DestinationRepositoryImpl", "Destination updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("DestinationRepositoryImpl", "Error updating destination: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun deleteDestination(destinationId: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Người dùng chưa đăng nhập"))
            }
            
            android.util.Log.d("DestinationRepositoryImpl", "Deleting destination: $destinationId")
            
            firestore.collection("destinations").document(destinationId)
                .delete()
                .await()
            
            android.util.Log.d("DestinationRepositoryImpl", "Destination deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("DestinationRepositoryImpl", "Error deleting destination: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override fun getDestinationsByCreator(userId: String): Flow<Result<List<Destination>>> = callbackFlow {
        val snapshotListener = firestore.collection("destinations")
            .whereEqualTo("created_by", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(Result.failure(e))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val destinations = snapshot.toObjects(Destination::class.java)
                    val destinationsWithIds = destinations.mapIndexed { index, dest ->
                        dest.copy(id = snapshot.documents[index].id)
                    }
                    trySend(Result.success(destinationsWithIds))
                }
            }
        awaitClose { snapshotListener.remove() }
    }
}