package com.example.smarttravel.data.repository

import com.example.smarttravel.model.Destination
import com.example.smarttravel.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
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

                    // --- THÊM PHẦN NÀY ĐỂ SỬA LỖI ---
                    // Gán ID của document (ví dụ: "nui", "bien") vào trường `id` của object
                    val categoriesWithIds = categories.mapIndexed { index, category ->
                        category.copy(id = snapshot.documents[index].id)
                    }
                    // Gửi danh sách ĐÃ CÓ ID
                    trySend(Result.success(categoriesWithIds))
                    // --- KẾT THÚC PHẦN SỬA ---
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
                    // Convert sang List<Destination>
                    val destinations = snapshot.toObjects(Destination::class.java)

                    // **QUAN TRỌNG:** Gán Document ID vào trường `id` của mỗi object
                    val destinationsWithIds = destinations.mapIndexed { index, dest ->
                        dest.copy(id = snapshot.documents[index].id)
                    }
                    trySend(Result.success(destinationsWithIds))
                }
            }
        awaitClose { snapshotListener.remove() }
    }
}