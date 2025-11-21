package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.data.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class PreviousTripsUiState(
    val plans: List<TravelPlan> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PreviousTripsViewModel @Inject constructor(
    private val planRepository: PlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreviousTripsUiState())
    val uiState: StateFlow<PreviousTripsUiState> = _uiState.asStateFlow()

    init {
        loadPreviousTrips()
    }

    fun loadPreviousTrips() {
        viewModelScope.launch {
            _uiState.value = PreviousTripsUiState(isLoading = true)
            planRepository.getMyPlans().collect { result ->
                if (result.isSuccess) {
                    val allPlans = result.getOrNull() ?: emptyList()
                    val today = LocalDate.now()
                    
                    // Filter các chuyến đi đã kết thúc (endDate < today)
                    val previousTrips = allPlans.filter { plan ->
                        if (plan.endDate == null) return@filter false
                        
                        val endDate = plan.endDate.toDate()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        
                        // Chuyến đi đã kết thúc nếu endDate < today
                        endDate.isBefore(today)
                    }.sortedByDescending { plan ->
                        // Sắp xếp theo endDate giảm dần (mới kết thúc nhất trước)
                        plan.endDate?.toDate()?.time ?: 0L
                    }
                    
                    android.util.Log.d("PreviousTripsViewModel", "Found ${previousTrips.size} previous trips out of ${allPlans.size} total plans")
                    
                    _uiState.value = PreviousTripsUiState(
                        plans = previousTrips,
                        isLoading = false
                    )
                } else {
                    _uiState.value = PreviousTripsUiState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                    )
                }
            }
        }
    }
}

