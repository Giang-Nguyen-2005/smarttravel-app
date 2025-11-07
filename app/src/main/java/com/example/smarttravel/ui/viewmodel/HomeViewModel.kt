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
import com.example.smarttravel.data.repository.AuthRepository
import com.example.smarttravel.model.UserProfile

// --- GIỮ NGUYÊN CÁC DATA CLASS STATE ---
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
    private val destinationRepository: DestinationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // --- STATE ---
    // 1. Danh sách gốc (Tất cả địa điểm, không bị lọc)
    private var allDestinations: List<Destination> = emptyList()

    // 2. State cho UI (Danh sách đã được lọc để hiển thị)
    private val _destinationUiState = MutableStateFlow(DestinationUiState())
    val destinationUiState: StateFlow<DestinationUiState> = _destinationUiState.asStateFlow()

    private val _categoryUiState = MutableStateFlow(CategoryUiState())
    val categoryUiState: StateFlow<CategoryUiState> = _categoryUiState.asStateFlow()

    // 3. Category đang được chọn (Mặc định là "all" - Tất cả)
    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        loadCategories()
        loadDestinations()
        loadUserProfile()
    }

    // --- HÀM LOAD DỮ LIỆU ---
    private fun loadCategories() {
        viewModelScope.launch {
            _categoryUiState.value = CategoryUiState(isLoading = true)
            destinationRepository.getCategories().collect { result ->
                if (result.isSuccess) {
                    val categories = result.getOrNull() ?: emptyList()
                    // THÊM MỤC "TẤT CẢ" VÀO ĐẦU DANH SÁCH
                    val allCategory = Category(id = "all", name = "Tất cả")
                    _categoryUiState.value = CategoryUiState(
                        categories = listOf(allCategory) + categories,
                        isLoading = false
                    )
                } else {
                    _categoryUiState.value = CategoryUiState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    private fun loadDestinations() {
        viewModelScope.launch {
            _destinationUiState.value = DestinationUiState(isLoading = true)
            destinationRepository.getDestinations().collect { result ->
                if (result.isSuccess) {
                    // Lưu danh sách gốc
                    allDestinations = result.getOrNull() ?: emptyList()
                    // Lọc lần đầu (mặc định là "all" nên sẽ hiện hết)
                    filterDestinations(_selectedCategory.value)
                } else {
                    _destinationUiState.value = DestinationUiState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    // --- HÀM LỌC (GỌI TỪ UI) ---
    fun onCategorySelected(categoryId: String) {
        _selectedCategory.value = categoryId
        filterDestinations(categoryId)
    }

    private fun filterDestinations(categoryId: String) {
        val filteredList = if (categoryId == "all") {
            allDestinations // Nếu chọn "Tất cả", hiển thị danh sách gốc
        } else {
            // Nếu chọn category khác, lọc theo category_id
            allDestinations.filter { it.category_id == categoryId }
        }
        // Cập nhật state cho UI
        _destinationUiState.value = DestinationUiState(
            destinations = filteredList,
            isLoading = false
        )
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.getUserProfile().collect { profile ->
                _userProfile.value = profile
            }
        }
    }
}