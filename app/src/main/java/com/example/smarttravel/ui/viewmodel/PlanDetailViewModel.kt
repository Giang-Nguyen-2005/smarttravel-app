package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.model.Destination
import com.example.smarttravel.data.repository.PlanRepository
import com.example.smarttravel.data.repository.DestinationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlanDetailUiState(
    val plan: TravelPlan? = null,
    val destination: Destination? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val destinationRepository: DestinationRepository,
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
                    val plan = result.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        plan = plan,
                        isLoading = false
                    )
                    
                    // Lấy destination nếu có destinationId
                    plan?.destinationId?.let { destId ->
                        if (destId.isNotEmpty()) {
                            launch {
                                destinationRepository.getDestinationById(destId).collect { destResult ->
                                    if (destResult.isSuccess) {
                                        _uiState.value = _uiState.value.copy(
                                            destination = destResult.getOrNull()
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    _uiState.value = PlanDetailUiState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                    )
                }
            }
        }
    }
    
    fun deletePlan(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (planId == null) {
            onError("Không tìm thấy ID kế hoạch")
            return
        }
        
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

