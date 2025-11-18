package com.example.smarttravel.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.data.repository.PlanRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val locationName: String = "", // <-- ĐÃ THÊM: Tên khu vực/tỉnh thành
    val companion: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val budget: String = "",
    val purposes: List<String> = emptyList(),
    val coverImageUrl: String = ""
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
    private val destinationRepository: DestinationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = _saveState.asStateFlow()

    init {
        // Lấy dữ liệu từ Navigation
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

        // Lấy thông tin ảnh và tên khu vực từ DestinationRepository
        if (destinationId.isNotEmpty()) {
            fetchDestinationDetails(destinationId)
        }
    }

    // Đổi tên hàm để phản ánh việc lấy nhiều chi tiết hơn
    private fun fetchDestinationDetails(destinationId: String) {
        viewModelScope.launch {
            // Gọi Repository để lấy chi tiết địa điểm
            destinationRepository.getDestinationById(destinationId).collect { result ->
                if (result.isSuccess) {
                    val destination = result.getOrNull()

                    val imageUrl = destination?.images?.firstOrNull() ?: ""
                    val locationName = destination?.location_name ?: "" // <-- LẤY LOCATION NAME

                    // Cập nhật cả ảnh và tên khu vực vào UI State
                    _uiState.update {
                        it.copy(
                            coverImageUrl = imageUrl,
                            locationName = locationName // <-- CẬP NHẬT LOCATION NAME
                        )
                    }
                }
            }
        }
    }

    // --- Các hàm cập nhật State từ UI ---
    fun setCompanion(companion: String) {
        _uiState.update { it.copy(companion = companion) }
    }

    fun setDates(start: LocalDate, end: LocalDate) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
    }

    fun setBudget(budget: String) {
        _uiState.update { it.copy(budget = budget) }
    }

    fun togglePurpose(purpose: String) {
        val currentPurposes = _uiState.value.purposes.toMutableList()
        if (currentPurposes.contains(purpose)) {
            currentPurposes.remove(purpose)
        } else {
            currentPurposes.add(purpose)
        }
        _uiState.update { it.copy(purposes = currentPurposes) }
    }

    // --- Hàm lưu cuối cùng ---
    fun savePlan() {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
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
                coverImageUrl = currentState.coverImageUrl,
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