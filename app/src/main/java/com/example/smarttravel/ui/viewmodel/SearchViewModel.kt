package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.model.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Destination>>(emptyList())
    val searchResults: StateFlow<List<Destination>> = _searchResults.asStateFlow()

    private val _trendingDestinations = MutableStateFlow<List<Destination>>(emptyList())
    val trendingDestinations: StateFlow<List<Destination>> = _trendingDestinations.asStateFlow()

    // [SỬA] Lịch sử tìm kiếm động (Lưu trong RAM, reset khi tắt app)
    // Bạn có thể khởi tạo nó với vài giá trị mẫu nếu muốn không bị trống lúc đầu
    private val _searchHistory = MutableStateFlow<List<String>>(listOf("Đà Lạt", "Vịnh Hạ Long"))
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadTrendingDestinations()
    }

    private fun loadTrendingDestinations() {
        viewModelScope.launch {
            destinationRepository.getDestinations().collect { result ->
                if (result.isSuccess) {
                    // Lấy 6 địa điểm để hiển thị cho đầy đặn hơn
                    _trendingDestinations.value = result.getOrNull()?.take(6) ?: emptyList()
                }
            }
        }
    }

    fun onSearchTextChanged(query: String) {
        _searchText.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            destinationRepository.searchDestinations(query).collect { result ->
                _isLoading.value = false
                _searchResults.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    // [MỚI] Hàm thêm từ khóa vào lịch sử
    fun addToSearchHistory(query: String) {
        if (query.isBlank()) return
        val currentList = _searchHistory.value.toMutableList()
        // Xóa nếu đã tồn tại để đưa lên đầu
        currentList.remove(query)
        currentList.add(0, query)
        // Giới hạn lưu 10 lịch sử gần nhất
        if (currentList.size > 10) {
            currentList.removeAt(currentList.lastIndex)
        }
        _searchHistory.value = currentList
    }

    // [MỚI] Hàm xóa lịch sử (nếu cần)
    fun clearHistory() {
        _searchHistory.value = emptyList()
    }
}