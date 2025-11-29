package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.model.Destination
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageDestinationsUiState(
    val destinations: List<Destination> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ManageDestinationsViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ManageDestinationsUiState())
    val uiState: StateFlow<ManageDestinationsUiState> = _uiState.asStateFlow()
    
    init {
        loadDestinations()
    }
    
    fun loadDestinations() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            _uiState.value = ManageDestinationsUiState(
                isLoading = false,
                error = "Người dùng chưa đăng nhập"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = ManageDestinationsUiState(isLoading = true)
            // Chỉ load destinations do user hiện tại tạo
            destinationRepository.getDestinationsByCreator(currentUser.uid).collect { result ->
                if (result.isSuccess) {
                    _uiState.value = ManageDestinationsUiState(
                        destinations = result.getOrNull() ?: emptyList(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = ManageDestinationsUiState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                    )
                }
            }
        }
    }
    
    fun deleteDestination(destinationId: String) {
        viewModelScope.launch {
            val result = destinationRepository.deleteDestination(destinationId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Lỗi khi xóa địa điểm"
                )
            }
            // Reload danh sách sau khi xóa
            loadDestinations()
        }
    }
}

