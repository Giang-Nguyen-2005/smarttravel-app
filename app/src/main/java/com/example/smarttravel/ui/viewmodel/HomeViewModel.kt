package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.model.Category
import com.example.smarttravel.model.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Trạng thái cho UI (Loading, Success, Error)
data class DestinationUiState(
    val destinations: List<Destination> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository // Hilt tự inject
) : ViewModel() {

    // State cho Destinations
    private val _destinationUiState = MutableStateFlow(DestinationUiState())
    val destinationUiState: StateFlow<DestinationUiState> = _destinationUiState.asStateFlow()

    // State cho Categories
    private val _categoryUiState = MutableStateFlow(CategoryUiState())
    val categoryUiState: StateFlow<CategoryUiState> = _categoryUiState.asStateFlow()

    init {
        // Tự động tải dữ liệu khi ViewModel được tạo
        loadDestinations()
        loadCategories()
    }

    private fun loadDestinations() {
        viewModelScope.launch {
            _destinationUiState.value = DestinationUiState(isLoading = true) // Báo UI là đang load
            destinationRepository.getDestinations().collect { result ->
                if (result.isSuccess) {
                    _destinationUiState.value = DestinationUiState(
                        destinations = result.getOrNull() ?: emptyList(),
                        isLoading = false // Load xong
                    )
                } else {
                    _destinationUiState.value = DestinationUiState(
                        isLoading = false, // Load lỗi
                        error = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                    )
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categoryUiState.value = CategoryUiState(isLoading = true)
            destinationRepository.getCategories().collect { result ->
                if (result.isSuccess) {
                    _categoryUiState.value = CategoryUiState(
                        categories = result.getOrNull() ?: emptyList(),
                        isLoading = false
                    )
                } else {
                    _categoryUiState.value = CategoryUiState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                    )
                }
            }
        }
    }
}