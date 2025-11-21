package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.data.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class PlanListUiState(
    val plans: List<TravelPlan> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PlanListViewModel @Inject constructor(
    private val planRepository: PlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanListUiState())
    val uiState: StateFlow<PlanListUiState> = _uiState.asStateFlow()

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            planRepository.getMyPlans()
                .map { result ->
                    if (result.isSuccess) {
                        val allPlans = result.getOrNull() ?: emptyList()
                        val today = LocalDate.now()
                        val threeDaysAgo = today.minusDays(3)
                        
                        // Filter: Chỉ giữ lại các kế hoạch chưa kết thúc hoặc mới kết thúc trong vòng 3 ngày
                        val activePlans = allPlans.filter { plan ->
                            if (plan.endDate == null) return@filter true // Giữ lại nếu không có endDate
                            
                            val endDate = plan.endDate.toDate()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            
                            // Chỉ giữ lại nếu endDate >= threeDaysAgo (chưa quá 3 ngày)
                            !endDate.isBefore(threeDaysAgo)
                        }
                        
                        android.util.Log.d("PlanListViewModel", "Filtered plans: ${activePlans.size} active out of ${allPlans.size} total")
                        
                        Result.success(activePlans)
                    } else {
                        result
                    }
                }
                .collect { result ->
                    if (result.isSuccess) {
                        _uiState.value = PlanListUiState(
                            plans = result.getOrNull() ?: emptyList(),
                            isLoading = false
                        )
                    } else {
                        _uiState.value = PlanListUiState(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                        )
                    }
                }
        }
    }
    
    fun deletePlan(planId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = planRepository.deletePlan(planId)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Lỗi không xác định")
            }
        }
    }
}

