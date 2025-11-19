package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.data.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlanDetailUiState(
    val plan: TravelPlan? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanDetailUiState())
    val uiState: StateFlow<PlanDetailUiState> = _uiState.asStateFlow()

    private val planId: String? = savedStateHandle.get<String>("planId")

    init {
        loadPlan()
    }

    fun loadPlan() {
        if (planId == null) {
            _uiState.value = PlanDetailUiState(
                isLoading = false,
                error = "Không tìm thấy ID kế hoạch"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = PlanDetailUiState(isLoading = true)
            planRepository.getPlanById(planId).collect { result ->
                if (result.isSuccess) {
                    _uiState.value = PlanDetailUiState(
                        plan = result.getOrNull(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = PlanDetailUiState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                    )
                }
            }
        }
    }
}

