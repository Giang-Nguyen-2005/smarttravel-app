package com.example.smarttravel.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.data.repository.AiService
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.data.repository.PlanRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
    object GeneratingAI : SaveState() // Đang tạo gợi ý AI
    data class Success(val planId: String) : SaveState() // Trả về planId để navigate
    data class Error(val message: String) : SaveState()
}

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val destinationRepository: DestinationRepository,
    private val aiService: AiService,
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

            // Kiểm tra ngày bắt đầu và kết thúc
            if (currentState.startDate == null || currentState.endDate == null) {
                _saveState.value = SaveState.Error("Vui lòng chọn ngày bắt đầu và ngày kết thúc")
                return@launch
            }

            // Chuyển đổi LocalDate sang Firebase Timestamp
            val startTimestamp = Timestamp(Date.from(currentState.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            val endTimestamp = Timestamp(Date.from(currentState.endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            
            // Kiểm tra ngày kết thúc phải sau ngày bắt đầu
            if (endTimestamp.toDate().time < startTimestamp.toDate().time) {
                _saveState.value = SaveState.Error("Ngày kết thúc phải sau ngày bắt đầu")
                return@launch
            }

            // Kiểm tra xem có kế hoạch nào trùng ngày không
            val overlapResult = planRepository.hasOverlappingPlan(startTimestamp, endTimestamp)
            if (overlapResult.isFailure) {
                _saveState.value = SaveState.Error("Lỗi kiểm tra trùng ngày: ${overlapResult.exceptionOrNull()?.message ?: "Lỗi không xác định"}")
                return@launch
            }
            
            val hasOverlap = overlapResult.getOrNull() ?: false
            if (hasOverlap) {
                _saveState.value = SaveState.Error("Bạn đã có kế hoạch du lịch trong khoảng thời gian này. Vui lòng chọn ngày khác.")
                return@launch
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

            // Lưu plan ban đầu
            val result = planRepository.savePlan(newPlan)
            if (result.isSuccess) {
                val planId = result.getOrNull() ?: ""
                
                // Chuyển sang trạng thái đang tạo AI
                _saveState.value = SaveState.GeneratingAI
                
                // Gọi AI để tạo planDetail
                generatePlanDetailWithAI(planId, currentState)
            } else {
                _saveState.value = SaveState.Error(result.exceptionOrNull()?.message ?: "Lỗi không xác định")
            }
        }
    }
    
    private suspend fun generatePlanDetailWithAI(planId: String, currentState: PlanUiState) {
        try {
            // Format dates
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val startDateStr = currentState.startDate?.format(dateFormatter) ?: ""
            val endDateStr = currentState.endDate?.format(dateFormatter) ?: ""
            
            android.util.Log.d("PlanViewModel", "Calling AI with: destination=${currentState.destinationName}, location=${currentState.locationName}")
            
            // Gọi AI
            val aiResult = aiService.generateTravelPlan(
                destination = currentState.destinationName,
                locationName = currentState.locationName,
                companion = currentState.companion,
                startDate = startDateStr,
                endDate = endDateStr,
                budget = currentState.budget,
                purposes = currentState.purposes
            )
            
            if (aiResult.isSuccess) {
                val aiResponse = aiResult.getOrNull() ?: ""
                android.util.Log.d("PlanViewModel", "AI response received, length: ${aiResponse.length}")
                android.util.Log.d("PlanViewModel", "AI response preview: ${aiResponse.take(500)}")
                
                val planDetail = parseAIResponse(aiResponse)
                android.util.Log.d("PlanViewModel", "Parsed planDetail: ${planDetail.size} days")
                
                if (planDetail.isNotEmpty()) {
                    // Cập nhật planDetail vào Firestore
                    val updateResult = planRepository.updatePlanDetail(planId, planDetail)
                    if (updateResult.isSuccess) {
                        android.util.Log.d("PlanViewModel", "PlanDetail updated successfully")
                        _saveState.value = SaveState.Success(planId)
                    } else {
                        android.util.Log.e("PlanViewModel", "Failed to update planDetail: ${updateResult.exceptionOrNull()?.message}")
                        // Nếu cập nhật thất bại, vẫn coi là thành công (plan đã được lưu)
                        _saveState.value = SaveState.Success(planId)
                    }
                } else {
                    android.util.Log.w("PlanViewModel", "Parsed planDetail is empty, AI response might be invalid")
                    // Nếu parse thất bại, vẫn coi là thành công (plan đã được lưu)
                    _saveState.value = SaveState.Success(planId)
                }
            } else {
                val error = aiResult.exceptionOrNull()
                android.util.Log.e("PlanViewModel", "AI call failed: ${error?.message}")
                android.util.Log.e("PlanViewModel", "Error details: ${error?.stackTraceToString()}")
                
                // Nếu AI thất bại, vẫn coi là thành công (plan đã được lưu)
                // Nhưng không có planDetail, user sẽ thấy "Đang tạo gợi ý AI..."
                _saveState.value = SaveState.Success(planId)
            }
        } catch (e: Exception) {
            android.util.Log.e("PlanViewModel", "Exception in generatePlanDetailWithAI: ${e.message}", e)
            // Nếu có lỗi, vẫn coi là thành công (plan đã được lưu)
            _saveState.value = SaveState.Success(planId)
        }
    }
    
    private fun parseAIResponse(response: String): List<Map<String, Any>> {
        return try {
            // Loại bỏ markdown code blocks nếu có
            var jsonString = response.trim()
            if (jsonString.startsWith("```json")) {
                jsonString = jsonString.removePrefix("```json").trim()
            }
            if (jsonString.startsWith("```")) {
                jsonString = jsonString.removePrefix("```").trim()
            }
            if (jsonString.endsWith("```")) {
                jsonString = jsonString.removeSuffix("```").trim()
            }
            
            val jsonArray = JSONArray(jsonString)
            val planDetail = mutableListOf<Map<String, Any>>()
            
            for (i in 0 until jsonArray.length()) {
                val dayObj = jsonArray.getJSONObject(i)
                val activitiesArray = dayObj.getJSONArray("activities")
                val activities = mutableListOf<Map<String, Any>>()
                
                // Parse activities với đầy đủ fields
                for (j in 0 until activitiesArray.length()) {
                    val activityObj = activitiesArray.getJSONObject(j)
                    val activityMap = mutableMapOf<String, Any>(
                        "time" to activityObj.optString("time", ""),
                        "type" to activityObj.optString("type", ""),
                        "name" to activityObj.optString("name", ""),
                        "location" to activityObj.optString("location", ""),
                        "description" to activityObj.optString("description", "")
                    )
                    
                    // Parse recommendedDishes nếu có
                    if (activityObj.has("recommendedDishes")) {
                        val dishesArray = activityObj.getJSONArray("recommendedDishes")
                        val dishes = mutableListOf<String>()
                        for (k in 0 until dishesArray.length()) {
                            dishes.add(dishesArray.getString(k))
                        }
                        activityMap["recommendedDishes"] = dishes
                    }
                    
                    // Parse tips nếu có
                    if (activityObj.has("tips")) {
                        activityMap["tips"] = activityObj.optString("tips", "")
                    }
                    
                    activities.add(activityMap)
                }
                
                // Parse hotel nếu có
                val dayMap = mutableMapOf<String, Any>(
                    "day" to dayObj.optInt("day", i + 1),
                    "date" to dayObj.optString("date", ""),
                    "title" to dayObj.optString("title", "Ngày ${i + 1}"),
                    "activities" to activities
                )
                
                if (dayObj.has("hotel")) {
                    val hotelObj = dayObj.getJSONObject("hotel")
                    val hotelMap = mapOf(
                        "name" to hotelObj.optString("name", ""),
                        "location" to hotelObj.optString("location", ""),
                        "price" to hotelObj.optString("price", ""),
                        "rating" to hotelObj.optString("rating", ""),
                        "description" to hotelObj.optString("description", "")
                    )
                    dayMap["hotel"] = hotelMap
                }
                
                planDetail.add(dayMap)
            }
            
            planDetail
        } catch (e: Exception) {
            // Log lỗi để debug
            android.util.Log.e("PlanViewModel", "Error parsing AI response: ${e.message}", e)
            android.util.Log.e("PlanViewModel", "Response: $response")
            // Nếu parse thất bại, trả về empty list
            emptyList()
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}