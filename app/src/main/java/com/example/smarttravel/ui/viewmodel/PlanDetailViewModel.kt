package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.model.Destination
import com.example.smarttravel.data.repository.PlanRepository
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.data.repository.AiService
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
    val error: String? = null,
    val generatingAlternative: Pair<Int, String>? = null // (dayIndex, itemType) đang được generate
)

@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val destinationRepository: DestinationRepository,
    private val aiService: AiService,
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
    
    fun requestAlternativeSuggestion(
        dayIndex: Int,
        itemType: String, // "hotel" hoặc "activity"
        activityIndex: Int? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val plan = _uiState.value.plan
        val destination = _uiState.value.destination
        
        if (plan == null || planId == null) {
            onError("Không tìm thấy thông tin kế hoạch")
            return
        }
        
        if (dayIndex >= plan.planDetail.size) {
            onError("Ngày không hợp lệ")
            return
        }
        
        // Lấy thông tin item hiện tại
        @Suppress("UNCHECKED_CAST")
        val dayMap = plan.planDetail[dayIndex] as? Map<String, Any> ?: run {
            onError("Không tìm thấy thông tin ngày")
            return
        }
        
        val currentItem = when (itemType) {
            "hotel" -> dayMap["hotel"] as? Map<String, Any>
            "activity" -> {
                if (activityIndex == null) {
                    onError("Activity index không được null")
                    return
                }
                @Suppress("UNCHECKED_CAST")
                val activities = dayMap["activities"] as? List<Map<String, Any>> ?: emptyList()
                if (activityIndex >= activities.size) {
                    onError("Activity index không hợp lệ")
                    return
                }
                activities[activityIndex]
            }
            else -> {
                onError("Item type không hợp lệ")
                return
            }
        } ?: run {
            onError("Không tìm thấy item để thay thế")
            return
        }
        
        // Set loading state
        _uiState.value = _uiState.value.copy(
            generatingAlternative = Pair(dayIndex, itemType)
        )
        
        viewModelScope.launch {
            try {
                val destinationName = destination?.name ?: plan.title.replace("Chuyến đi đến ", "")
                val locationName = destination?.location_name ?: ""
                val date = dayMap["date"] as? String ?: ""
                val dayNumber = (dayMap["day"] as? Number)?.toInt() ?: (dayIndex + 1)
                
                // Gọi AI để tạo gợi ý thay thế
                val aiResult = aiService.generateAlternativeSuggestion(
                    destination = destinationName,
                    locationName = locationName,
                    itemType = itemType,
                    currentItem = currentItem as Map<String, Any>,
                    budget = plan.budget,
                    dayNumber = dayNumber,
                    date = date
                )
                
                if (aiResult.isSuccess) {
                    val newItem = aiResult.getOrNull() ?: run {
                        onError("Không nhận được gợi ý từ AI")
                        _uiState.value = _uiState.value.copy(generatingAlternative = null)
                        return@launch
                    }
                    
                    // Cập nhật vào Firestore
                    val updateResult = planRepository.updatePlanDetailItem(
                        planId = planId,
                        dayIndex = dayIndex,
                        itemType = itemType,
                        activityIndex = activityIndex,
                        newItem = newItem
                    )
                    
                    if (updateResult.isSuccess) {
                        android.util.Log.d("PlanDetailViewModel", "Alternative suggestion updated successfully")
                        _uiState.value = _uiState.value.copy(generatingAlternative = null)
                        onSuccess()
                    } else {
                        val errorMsg = updateResult.exceptionOrNull()?.message ?: "Lỗi không xác định"
                        android.util.Log.e("PlanDetailViewModel", "Failed to update: $errorMsg")
                        _uiState.value = _uiState.value.copy(generatingAlternative = null)
                        onError(errorMsg)
                    }
                } else {
                    val errorMsg = aiResult.exceptionOrNull()?.message ?: "Lỗi không xác định"
                    android.util.Log.e("PlanDetailViewModel", "AI call failed: $errorMsg")
                    _uiState.value = _uiState.value.copy(generatingAlternative = null)
                    onError("Không thể tạo gợi ý: $errorMsg")
                }
            } catch (e: Exception) {
                android.util.Log.e("PlanDetailViewModel", "Exception in requestAlternativeSuggestion: ${e.message}", e)
                _uiState.value = _uiState.value.copy(generatingAlternative = null)
                onError("Lỗi: ${e.message ?: "Lỗi không xác định"}")
            }
        }
    }
}

