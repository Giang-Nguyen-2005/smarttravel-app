package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.AuthRepository
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.model.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

// Trạng thái UI cho màn hình chi tiết
data class DetailUiState(
    val destination: Destination? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isBookmarked: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle // Dùng để lấy tham số từ Navigation
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Lấy destinationId từ arguments của Navigation
    private val destinationId: String? = savedStateHandle["destinationId"]

    init {
        loadDestination()
        checkBookmarkStatus()
    }

    fun loadDestination() {
        if (destinationId == null) {
            _uiState.value = DetailUiState(isLoading = false, error = "Không tìm thấy ID địa điểm")
            return
        }

        viewModelScope.launch {
            _uiState.value = DetailUiState(isLoading = true)
            destinationRepository.getDestinationById(destinationId).collect { result ->
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        destination = result.getOrNull(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = DetailUiState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                    )
                }
            }
        }
    }

    private fun checkBookmarkStatus() {
        if (destinationId == null) return
        
        viewModelScope.launch {
            authRepository.getSavedDestinationIds().collect { savedIds ->
                _uiState.value = _uiState.value.copy(
                    isBookmarked = savedIds.contains(destinationId)
                )
            }
        }
    }

    fun toggleBookmark() {
        val currentDestination = _uiState.value.destination
        if (currentDestination == null || destinationId == null) return

        viewModelScope.launch {
            val isCurrentlyBookmarked = _uiState.value.isBookmarked
            val result = if (isCurrentlyBookmarked) {
                authRepository.unsaveDestination(destinationId)
            } else {
                authRepository.saveDestination(destinationId)
            }
            
            // Cập nhật UI ngay lập tức để UX tốt hơn
            _uiState.value = _uiState.value.copy(
                isBookmarked = !isCurrentlyBookmarked
            )
        }
    }
}