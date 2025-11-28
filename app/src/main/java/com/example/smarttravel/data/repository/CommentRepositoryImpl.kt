package com.example.smarttravel.data.repository

import com.example.smarttravel.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : CommentRepository {

    override fun getComments(destinationId: String): Flow<List<Comment>> = callbackFlow {
        android.util.Log.d("CommentRepository", "Getting comments for destinationId: $destinationId")
        
        // Tạm thời bỏ orderBy để tránh lỗi index, sẽ sắp xếp ở client-side
        val snapshotListener = firestore.collection("comments")
            .whereEqualTo("destinationId", destinationId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("CommentRepository", "Error getting comments: ${e.message}", e)
                    android.util.Log.e("CommentRepository", "Error code: ${e.code}", e)
                    
                    // Nếu lỗi là về index, log thông báo rõ ràng
                    if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                        android.util.Log.e("CommentRepository", "Cần tạo Firestore index! Xem logcat để có link tạo index.")
                    }
                    
                    trySend(emptyList()) // Emit empty list để UI không bị stuck
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    android.util.Log.d("CommentRepository", "Received ${snapshot.documents.size} comments")
                    val comments = snapshot.toObjects(Comment::class.java)
                    // Gán ID từ document ID
                    val commentsWithIds = comments.mapIndexed { index, comment ->
                        comment.copy(id = snapshot.documents[index].id)
                    }
                    // Sắp xếp ở client-side theo timestamp giảm dần (mới nhất trước)
                    val sortedComments = commentsWithIds.sortedByDescending { it.timestamp }
                    android.util.Log.d("CommentRepository", "Sending ${sortedComments.size} sorted comments")
                    trySend(sortedComments)
                } else {
                    android.util.Log.d("CommentRepository", "Snapshot is null, sending empty list")
                    trySend(emptyList())
                }
            }
        
        awaitClose {
            snapshotListener.remove()
            android.util.Log.d("CommentRepository", "Snapshot listener removed")
        }
    }

    override suspend fun addComment(destinationId: String, content: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("Người dùng chưa đăng nhập"))

            // Lấy thông tin user từ Firestore
            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            val userName = userDoc.getString("display_name") ?: currentUser.displayName ?: "Người dùng"
            val userAvatarUrl = userDoc.getString("avatar_url") ?: currentUser.photoUrl?.toString() ?: ""

            // Tạo comment (id sẽ được Firestore tự động generate)
            val comment = Comment(
                id = "", // Firestore sẽ tự động tạo document ID
                destinationId = destinationId,
                userId = currentUser.uid,
                userName = userName,
                userAvatarUrl = userAvatarUrl,
                content = content.trim(),
                timestamp = System.currentTimeMillis()
            )

            // Lưu comment vào Firestore
            val docRef = firestore.collection("comments").document()
            docRef.set(comment).await()
            
            android.util.Log.d("CommentRepository", "Comment added successfully: ${docRef.id}")

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("CommentRepository", "Error adding comment: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(destinationId: String, commentId: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("Người dùng chưa đăng nhập"))

            // Lấy comment từ Firestore để kiểm tra quyền
            val commentDoc = firestore.collection("comments").document(commentId).get().await()
            
            if (!commentDoc.exists()) {
                return Result.failure(Exception("Không tìm thấy bình luận"))
            }

            val comment = commentDoc.toObject(Comment::class.java)
            if (comment == null) {
                return Result.failure(Exception("Không thể đọc dữ liệu bình luận"))
            }

            // Chỉ cho phép xóa bình luận của chính mình
            if (comment.userId != currentUser.uid) {
                return Result.failure(Exception("Bạn không có quyền xóa bình luận này"))
            }

            // Xóa comment từ Firestore
            firestore.collection("comments").document(commentId).delete().await()
            
            android.util.Log.d("CommentRepository", "Comment deleted successfully: $commentId")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("CommentRepository", "Error deleting comment: ${e.message}", e)
            Result.failure(e)
        }
    }
}

