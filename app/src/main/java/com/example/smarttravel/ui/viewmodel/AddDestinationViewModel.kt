package com.example.smarttravel.ui.viewmodel

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.model.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

data class AddDestinationUiState(
    val name: String = "",
    val description: String = "",
    val locationName: String = "",
    val estimatedCost: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val categoryId: String = "",
    val images: List<String> = emptyList(), // URLs ảnh (ảnh đầu tiên là ảnh chính)
    val imageUrl: String = "", // URL ảnh đang nhập
    val destinationId: String = "", // ID khi edit
    val isEditMode: Boolean = false, // Mode edit hay add mới
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AddDestinationViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    fun getCategories() = destinationRepository.getCategories()
    
    private val _uiState = MutableStateFlow(AddDestinationUiState())
    val uiState: StateFlow<AddDestinationUiState> = _uiState.asStateFlow()
    
    // Job để debounce geocoding
    private var geocodeJob: Job? = null
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }
    
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }
    
    fun updateLocationName(locationName: String) {
        _uiState.value = _uiState.value.copy(locationName = locationName)
        
        // Tự động geocode sau khi người dùng ngừng nhập (debounce 1.5 giây)
        geocodeJob?.cancel()
        if (locationName.isNotBlank() && locationName.length >= 3) {
            geocodeJob = viewModelScope.launch {
                delay(1500) // Đợi 1.5 giây sau khi người dùng ngừng nhập
                geocodeAddress(locationName)
            }
        }
    }
    
    fun updateEstimatedCost(cost: String) {
        _uiState.value = _uiState.value.copy(estimatedCost = cost)
    }
    
    fun updateLatitude(latitude: String) {
        _uiState.value = _uiState.value.copy(latitude = latitude)
    }
    
    fun updateLongitude(longitude: String) {
        _uiState.value = _uiState.value.copy(longitude = longitude)
    }
    
    fun updateLocation(lat: Double, lng: Double, address: String) {
        _uiState.value = _uiState.value.copy(
            latitude = lat.toString(),
            longitude = lng.toString(),
            locationName = address
        )
    }
    
    fun updateCategoryId(categoryId: String) {
        _uiState.value = _uiState.value.copy(categoryId = categoryId)
    }
    
    fun updateImageUrl(url: String) {
        _uiState.value = _uiState.value.copy(imageUrl = url)
    }
    
    fun addImage() {
        val url = _uiState.value.imageUrl.trim()
        if (url.isNotBlank() && !_uiState.value.images.contains(url)) {
            _uiState.value = _uiState.value.copy(
                images = _uiState.value.images + url,
                imageUrl = ""
            )
        }
    }
    
    fun removeImage(index: Int) {
        val newImages = _uiState.value.images.toMutableList()
        if (index in newImages.indices) {
            newImages.removeAt(index)
            _uiState.value = _uiState.value.copy(images = newImages)
        }
    }
    
    fun setMainImage(index: Int) {
        val currentImages = _uiState.value.images.toMutableList()
        if (index in currentImages.indices && index != 0) {
            // Đổi chỗ ảnh ở index với ảnh đầu tiên
            val mainImage = currentImages[0]
            currentImages[0] = currentImages[index]
            currentImages[index] = mainImage
            _uiState.value = _uiState.value.copy(images = currentImages)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun resetForm() {
        _uiState.value = AddDestinationUiState()
    }
    
    fun geocodeAddress(address: String) {
        if (address.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                withContext(Dispatchers.IO) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocationName(address, 1)
                    
                    if (addresses != null && addresses.isNotEmpty()) {
                        val addressObj = addresses[0]
                        val lat = addressObj.latitude
                        val lng = addressObj.longitude
                        
                        // Cập nhật tọa độ và địa chỉ
                        _uiState.value = _uiState.value.copy(
                            latitude = lat.toString(),
                            longitude = lng.toString(),
                            locationName = address,
                            isLoading = false
                        )
                        
                        android.util.Log.d("AddDestinationViewModel", "Geocoded: $address -> ($lat, $lng)")
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Không tìm thấy địa chỉ. Vui lòng thử lại hoặc chọn trên bản đồ."
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AddDestinationViewModel", "Error geocoding: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi khi tìm địa chỉ: ${e.message}. Vui lòng thử lại hoặc chọn trên bản đồ."
                )
            }
        }
    }
    
    fun loadDestinationForEdit(destinationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            destinationRepository.getDestinationById(destinationId).collect { result ->
                if (result.isSuccess) {
                    val destination = result.getOrNull()
                    if (destination != null) {
                        _uiState.value = AddDestinationUiState(
                            name = destination.name,
                            description = destination.description,
                            locationName = destination.location_name,
                            estimatedCost = destination.estimated_cost.toString(),
                            latitude = destination.latitude.toString(),
                            longitude = destination.longitude.toString(),
                            categoryId = destination.category_id,
                            images = destination.images,
                            destinationId = destinationId,
                            isEditMode = true,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Không tìm thấy địa điểm"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Lỗi khi tải địa điểm"
                    )
                }
            }
        }
    }
    
    fun submitDestination() {
        val state = _uiState.value
        
        android.util.Log.d("AddDestinationViewModel", "Submit destination - name: ${state.name}, location: ${state.locationName}, category: ${state.categoryId}, isEdit: ${state.isEditMode}")
        
        // Validation
        if (state.name.isBlank()) {
            android.util.Log.e("AddDestinationViewModel", "Validation failed: name is blank")
            _uiState.value = state.copy(error = "Vui lòng nhập tên địa điểm")
            return
        }
        if (state.locationName.isBlank() && (state.latitude.isBlank() || state.longitude.isBlank())) {
            android.util.Log.e("AddDestinationViewModel", "Validation failed: location is blank")
            _uiState.value = state.copy(error = "Vui lòng chọn vị trí trên bản đồ")
            return
        }
        if (state.categoryId.isBlank()) {
            android.util.Log.e("AddDestinationViewModel", "Validation failed: category is blank")
            _uiState.value = state.copy(error = "Vui lòng chọn danh mục")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            
            // Lấy danh sách URLs ảnh
            val finalImages = state.images
            
            // Parse cost (loại bỏ dấu phẩy, chấm)
            val cost = try {
                state.estimatedCost.replace(",", "").replace(".", "").toLongOrNull() ?: 0L
            } catch (e: Exception) {
                0L
            }
            
            // Parse coordinates
            val lat = state.latitude.toDoubleOrNull() ?: 0.0
            val lng = state.longitude.toDoubleOrNull() ?: 0.0
            
            android.util.Log.d("AddDestinationViewModel", "Creating destination - lat: $lat, lng: $lng, images: ${finalImages.size}")
            
            val destination = Destination(
                name = state.name,
                description = state.description,
                location_name = state.locationName.ifEmpty { "${lat}, ${lng}" },
                estimated_cost = cost,
                category_id = state.categoryId,
                latitude = lat,
                longitude = lng,
                rating = 0.0, // Rating mặc định, sẽ được tính từ user ratings
                images = if (finalImages.isEmpty()) listOf("") else finalImages
            )
            
            val result = if (state.isEditMode) {
                android.util.Log.d("AddDestinationViewModel", "Updating destination with ID: ${state.destinationId}")
                destinationRepository.updateDestination(state.destinationId, destination)
            } else {
                android.util.Log.d("AddDestinationViewModel", "Calling addDestination...")
                destinationRepository.addDestination(destination)
            }
            
            if (result.isSuccess) {
                val message = if (state.isEditMode) {
                    android.util.Log.d("AddDestinationViewModel", "Destination updated successfully")
                    "Cập nhật địa điểm thành công!"
                } else {
                    android.util.Log.d("AddDestinationViewModel", "Destination added successfully with ID: ${result.getOrNull()}")
                    "Thêm địa điểm thành công!"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    success = true,
                    error = null
                )
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Lỗi khi ${if (state.isEditMode) "cập nhật" else "thêm"} địa điểm"
                android.util.Log.e("AddDestinationViewModel", "Error: $errorMsg", result.exceptionOrNull())
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMsg
                )
            }
        }
    }
}

