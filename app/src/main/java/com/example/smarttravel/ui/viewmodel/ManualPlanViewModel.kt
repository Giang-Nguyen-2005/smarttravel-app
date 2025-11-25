package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.data.repository.PlanRepository
import com.example.smarttravel.ui.screens.add_plan.ActivityData
import com.example.smarttravel.ui.screens.add_plan.DayActivities
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject.Inject

data class ManualPlanUiState(
    val title: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val days: List<DayActivities> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val savedPlanId: String? = null
)

@HiltViewModel
class ManualPlanViewModel @Inject constructor(
    private val planRepository: PlanRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ManualPlanUiState())
    val uiState = _uiState.asStateFlow()
    
    fun initializePlan(startDate: LocalDate, endDate: LocalDate) {
        val daysList = mutableListOf<DayActivities>()
        var currentDate = startDate
        var dayIndex = 1
        while (!currentDate.isAfter(endDate)) {
            daysList.add(DayActivities(day = dayIndex, date = currentDate))
            currentDate = currentDate.plusDays(1)
            dayIndex++
        }
        
        _uiState.value = _uiState.value.copy(
            startDate = startDate,
            endDate = endDate,
            days = daysList
        )
    }
    
    fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }
    
    fun addActivity(dayIndex: Int, activity: ActivityData) {
        val currentState = _uiState.value
        
        // Kiểm tra validation
        if (currentState.days.isEmpty()) {
            android.util.Log.e("ManualPlanViewModel", "Cannot add activity: days list is empty. Initializing with default dates.")
            // Nếu days rỗng, khởi tạo với ngày hiện tại
            val today = LocalDate.now()
            initializePlan(today, today)
            // Sau khi khởi tạo, thử lại
            val newState = _uiState.value
            if (newState.days.isNotEmpty() && dayIndex < newState.days.size) {
                addActivity(dayIndex, activity)
            } else {
                android.util.Log.e("ManualPlanViewModel", "Failed to add activity: dayIndex=$dayIndex, days.size=${newState.days.size}")
            }
            return
        }
        
        if (dayIndex < 0 || dayIndex >= currentState.days.size) {
            android.util.Log.e("ManualPlanViewModel", "Invalid dayIndex: $dayIndex, days.size=${currentState.days.size}")
            return
        }
        
        val updatedDays = currentState.days.mapIndexed { index, dayData ->
            if (index == dayIndex) {
                // Tạo một bản sao mới của dayData với activities mới
                val newActivities = dayData.activities.toMutableList()
                newActivities.add(activity)
                dayData.copy(activities = newActivities)
            } else {
                dayData
            }
        }
        _uiState.value = currentState.copy(days = updatedDays)
        android.util.Log.d("ManualPlanViewModel", "Added activity to day $dayIndex: ${activity.name}, Total activities in day: ${updatedDays[dayIndex].activities.size}")
    }
    
    fun updateActivity(dayIndex: Int, activityIndex: Int, activity: ActivityData) {
        val updatedDays = _uiState.value.days.toMutableList()
        if (dayIndex < updatedDays.size && activityIndex < updatedDays[dayIndex].activities.size) {
            updatedDays[dayIndex].activities[activityIndex] = activity
            _uiState.value = _uiState.value.copy(days = updatedDays)
        }
    }
    
    fun deleteActivity(dayIndex: Int, activityIndex: Int) {
        val updatedDays = _uiState.value.days.toMutableList()
        if (dayIndex < updatedDays.size && activityIndex < updatedDays[dayIndex].activities.size) {
            updatedDays[dayIndex].activities.removeAt(activityIndex)
            _uiState.value = _uiState.value.copy(days = updatedDays)
        }
    }
    
    fun savePlan(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val currentState = _uiState.value
            if (currentState.startDate == null || currentState.endDate == null) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = "Vui lòng chọn ngày bắt đầu và ngày kết thúc"
                )
                onError("Vui lòng chọn ngày bắt đầu và ngày kết thúc")
                return@launch
            }
            
            // Chuyển đổi LocalDate sang Firebase Timestamp
            val startTimestamp = Timestamp(
                Date.from(currentState.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
            )
            val endTimestamp = Timestamp(
                Date.from(currentState.endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
            )
            
            // Tạo planDetail từ days
            val planDetail = currentState.days.mapIndexed { index, dayData ->
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val dateStr = dayData.date.format(dateFormatter)
                
                val activities = dayData.activities.map { activity ->
                    mapOf(
                        "time" to activity.time,
                        "type" to activity.category,
                        "name" to activity.name,
                        "location" to activity.address,
                        "description" to activity.note
                    )
                }
                
                mapOf(
                    "day" to dayData.day,
                    "date" to dateStr,
                    "title" to "Ngày ${dayData.day}",
                    "hotel" to mapOf<String, Any>(),
                    "activities" to activities
                )
            }
            
            val newPlan = TravelPlan(
                destinationId = "",
                title = currentState.title.ifEmpty { "Kế hoạch du lịch" },
                coverImageUrl = "",
                companion = "",
                budget = "",
                purposes = emptyList(),
                startDate = startTimestamp,
                endDate = endTimestamp,
                planDetail = planDetail
            )
            
            val result = planRepository.savePlan(newPlan)
            if (result.isSuccess) {
                val planId = result.getOrNull() ?: ""
                _uiState.value = currentState.copy(
                    isLoading = false,
                    isSaved = true,
                    savedPlanId = planId
                )
                onSuccess(planId)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = errorMsg
                )
                onError(errorMsg)
            }
        }
    }
}

