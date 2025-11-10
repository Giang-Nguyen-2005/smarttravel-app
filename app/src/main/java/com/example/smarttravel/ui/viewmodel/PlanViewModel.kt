package com.example.smarttravel.ui.viewmodel


import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.data.repository.PlanRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

// Trạng thái lưu trữ dữ liệu đang xây dựng
data class PlanUiState(
    val destinationId: String = "",
    val destinationName: String = "",
    val companion: String = "Chỉ mình tôi", // Giá trị mặc định
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val budget: String = "Cân bằng", // Giá trị mặc định
    val purposes: List<String> = emptyList()
)

// Trạng thái của việc lưu
sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    savedStateHandle: SavedStateHandle // Dùng để nhận destinationId từ navigation
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = _saveState.asStateFlow()

    // Danh sách sở thích (cho PurposeScreen)
    val purposes = mutableStateListOf<String>()

    init {
        // Lấy dữ liệu (destinationId, destinationName) được truyền từ DetailScreen
        val destinationId: String = savedStateHandle.get("destinationId") ?: ""
        val destinationName: String = try {
            URLDecoder.decode(savedStateHandle.get("destinationName") ?: "", "UTF-8")
        } catch (e: Exception) {
            ""
        }

        _uiState.value = _uiState.value.copy(
            destinationId = destinationId,
            destinationName = destinationName
        )
    }

    // --- Các hàm để cập nhật State từ UI ---

    fun setCompanion(companion: String) {
        _uiState.value = _uiState.value.copy(companion = companion)
    }

    fun setDates(start: LocalDate, end: LocalDate) {
        _uiState.value = _uiState.value.copy(startDate = start, endDate = end)
    }

    fun setBudget(budget: String) {
        _uiState.value = _uiState.value.copy(budget = budget)
    }

    // (PurposeScreen sẽ cập nhật trực tiếp vào `purposes` list)

    // --- Hàm lưu cuối cùng ---

    fun savePlan() {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading

            // Cập nhật state lần cuối từ list sở thích
            _uiState.value = _uiState.value.copy(purposes = purposes.toList())

            val currentState = _uiState.value

            // Chuyển đổi LocalDate sang Firebase Timestamp
            val startTimestamp = currentState.startDate?.let {
                Timestamp(Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            }
            val endTimestamp = currentState.endDate?.let {
                Timestamp(Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            }

            val newPlan = TravelPlan(
                destinationId = currentState.destinationId,
                title = "Chuyến đi đến ${currentState.destinationName}",
                // TODO: Lấy ảnh từ destination (cần DestinationRepository)
                // coverImageUrl = ...
                companion = currentState.companion,
                budget = currentState.budget,
                purposes = currentState.purposes,
                startDate = startTimestamp,
                endDate = endTimestamp
            )

            val result = planRepository.savePlan(newPlan)
            if (result.isSuccess) {
                _saveState.value = SaveState.Success
            } else {
                _saveState.value = SaveState.Error(result.exceptionOrNull()?.message ?: "Lỗi không xác định")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}