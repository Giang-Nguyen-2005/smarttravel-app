package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.AuthRepository
import com.example.smarttravel.data.repository.CommentRepository
import com.example.smarttravel.model.Comment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommentUiState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSubmitting: Boolean = false,
    val canComment: Boolean = false, // Có thể bình luận (đã đăng nhập)
    val currentUserId: String? = null // ID của user hiện tại
)

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val destinationId: String? = savedStateHandle["destinationId"]
    
    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState: StateFlow<CommentUiState> = _uiState.asStateFlow()

    init {
        checkCanComment()
        loadComments()
    }

    private fun checkCanComment() {
        val currentUser = authRepository.getCurrentUser()
        _uiState.value = _uiState.value.copy(
            canComment = currentUser != null,
            currentUserId = currentUser?.uid
        )
    }

    private fun loadComments() {
        if (destinationId == null) {
            android.util.Log.e("CommentViewModel", "destinationId is null!")
            _uiState.value = CommentUiState(error = "Không tìm thấy ID địa điểm")
            return
        }

        android.util.Log.d("CommentViewModel", "Loading comments for destinationId: $destinationId")
        
        viewModelScope.launch {
            var isFirstEmission = true
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            commentRepository.getComments(destinationId).collect { comments ->
                android.util.Log.d("CommentViewModel", "Received ${comments.size} comments")
                
                // Lần đầu tiên nhận data (kể cả empty), tắt loading
                if (isFirstEmission) {
                    isFirstEmission = false
                    android.util.Log.d("CommentViewModel", "First emission, setting isLoading = false")
                    _uiState.value = _uiState.value.copy(
                        comments = comments,
                        isLoading = false,
                        error = null
                    )
                } else {
                    // Các lần sau (real-time updates), chỉ cập nhật comments
                    android.util.Log.d("CommentViewModel", "Real-time update, ${comments.size} comments")
                    _uiState.value = _uiState.value.copy(
                        comments = comments,
                        error = null
                    )
                }
            }
        }
    }

    fun addComment(content: String) {
        if (destinationId == null || content.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            
            val result = commentRepository.addComment(destinationId, content)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isSubmitting = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = result.exceptionOrNull()?.message ?: "Lỗi khi thêm bình luận"
                )
            }
        }
    }

    fun deleteComment(commentId: String) {
        if (destinationId == null) return

        viewModelScope.launch {
            val result = commentRepository.deleteComment(destinationId, commentId)
            
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Lỗi khi xóa bình luận"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

