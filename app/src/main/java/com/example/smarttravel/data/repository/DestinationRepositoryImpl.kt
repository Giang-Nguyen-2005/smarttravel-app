package com.example.smarttravel.data.repository

import com.example.smarttravel.model.Destination
import com.example.smarttravel.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DestinationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
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
                    // Gán ID của document vào object
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
                    // Gán Document ID vào object
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
        // Dùng addSnapshotListener để tự động cập nhật nếu dữ liệu trên Firestore thay đổi
        val listener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Result.failure(e))
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val destination = snapshot.toObject(Destination::class.java)
                // Quan trọng: Gán ID lại cho object để đảm bảo chính xác
                if (destination != null) {
                    trySend(Result.success(destination.copy(id = snapshot.id)))
                } else {
                    trySend(Result.failure(Exception("Lỗi parse dữ liệu địa điểm")))
                }
            } else {
                trySend(Result.failure(Exception("Không tìm thấy địa điểm với ID này")))
            }
        }
        awaitClose { listener.remove() }
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
}