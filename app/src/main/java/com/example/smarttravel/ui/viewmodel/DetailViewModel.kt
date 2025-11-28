package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.AuthRepository
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.data.repository.PlanRepository
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
    val isBookmarked: Boolean = false,
    val canRate: Boolean = false, // Có thể đánh giá (đã có plan với destination này)
    val userRating: Double? = null, // Rating hiện tại của user (1.0 - 5.0)
    val isRatingLoading: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val authRepository: AuthRepository,
    private val planRepository: PlanRepository,
    savedStateHandle: SavedStateHandle // Dùng để lấy tham số từ Navigation
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Lấy destinationId từ arguments của Navigation
    private val destinationId: String? = savedStateHandle["destinationId"]

    init {
        loadDestination()
        checkBookmarkStatus()
        checkCanRate()
        loadUserRating()
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
    
    private fun checkCanRate() {
        if (destinationId == null) {
            android.util.Log.e("DetailViewModel", "checkCanRate: destinationId is null")
            return
        }
        
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                android.util.Log.d("DetailViewModel", "checkCanRate: No current user, canRate = false")
                _uiState.value = _uiState.value.copy(canRate = false)
                return@launch
            }
            
            android.util.Log.d("DetailViewModel", "checkCanRate: Checking plan for destinationId: $destinationId")
            val result = planRepository.hasPlanWithDestination(destinationId)
            if (result.isSuccess) {
                val hasPlan = result.getOrNull() ?: false
                android.util.Log.d("DetailViewModel", "checkCanRate: hasPlan = $hasPlan")
                _uiState.value = _uiState.value.copy(
                    canRate = hasPlan
                )
            } else {
                android.util.Log.e("DetailViewModel", "checkCanRate: Error checking plan: ${result.exceptionOrNull()?.message}")
                // Nếu có lỗi, vẫn cho phép đánh giá (fallback)
                _uiState.value = _uiState.value.copy(canRate = true)
            }
        }
    }
    
    private fun loadUserRating() {
        if (destinationId == null) return
        
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                return@launch
            }
            
            val result = destinationRepository.getUserRating(destinationId, currentUser.uid)
            if (result.isSuccess) {
                val userRating = result.getOrNull()
                _uiState.value = _uiState.value.copy(
                    userRating = userRating?.rating
                )
            }
        }
    }
    
    fun submitRating(rating: Double) {
        if (destinationId == null) {
            android.util.Log.e("DetailViewModel", "submitRating: destinationId is null")
            return
        }
        
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                android.util.Log.e("DetailViewModel", "submitRating: No current user")
                _uiState.value = _uiState.value.copy(
                    error = "Vui lòng đăng nhập để đánh giá"
                )
                return@launch
            }
            
            android.util.Log.d("DetailViewModel", "submitRating: Submitting rating $rating for destinationId: $destinationId")
            _uiState.value = _uiState.value.copy(isRatingLoading = true, error = null)
            
            val result = destinationRepository.saveUserRating(
                destinationId = destinationId,
                userId = currentUser.uid,
                rating = rating
            )
            
            if (result.isSuccess) {
                android.util.Log.d("DetailViewModel", "submitRating: Success")
                // Cập nhật UI ngay lập tức
                _uiState.value = _uiState.value.copy(
                    userRating = rating,
                    isRatingLoading = false,
                    error = null
                )
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                android.util.Log.e("DetailViewModel", "submitRating: Error - $errorMessage")
                _uiState.value = _uiState.value.copy(
                    isRatingLoading = false,
                    error = "Lỗi khi đánh giá: $errorMessage"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}