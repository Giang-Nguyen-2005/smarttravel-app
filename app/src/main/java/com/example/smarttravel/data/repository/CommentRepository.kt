package com.example.smarttravel.data.repository

import com.example.smarttravel.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    /**
     * Lắng nghe bình luận real-time cho một destination
     */
    fun getComments(destinationId: String): Flow<List<Comment>>
    
    /**
     * Thêm bình luận mới
     */
    suspend fun addComment(destinationId: String, content: String): Result<Unit>
    
    /**
     * Xóa bình luận
     */
    suspend fun deleteComment(destinationId: String, commentId: String): Result<Unit>
}

