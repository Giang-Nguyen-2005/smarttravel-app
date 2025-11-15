package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.AuthRepository
import com.example.smarttravel.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _updateMessage = MutableStateFlow<String?>(null)
    val updateMessage: StateFlow<String?> = _updateMessage.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.getUserProfile().collect { profile ->
                _userProfile.value = profile
            }
        }
    }

    fun updateUserProfile(
        displayName: String,
        phoneNumber: String,
        location: String,
        interestsString: String
    ) {
        // 1. Xử lý chuỗi sở thích thành List<String>
        val interestsList = interestsString
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // 2. Tạo đối tượng UserProfile mới
        val updatedProfile = userProfile.value?.copy(
            displayName = displayName,
            phoneNumber = phoneNumber,
            location = location,
            interests = interestsList
        )

        // 3. Gửi đi cập nhật (Sử dụng updatedProfile)
        updatedProfile?.let { profile ->
            viewModelScope.launch {
                _isLoading.value = true // Bắt đầu tải
                try {
                    // 🚨 THAO TÁC QUAN TRỌNG: GỌI REPOSITORY ĐỂ LƯU 🚨
                    authRepository.updateUserProfile(profile)

                    _updateMessage.value = "Cập nhật thành công!"
                } catch (e: Exception) {
                    // Xử lý lỗi (ví dụ: mất mạng, lỗi Firestore)
                    _updateMessage.value = "Cập nhật thất bại: ${e.localizedMessage ?: "Lỗi không xác định"}"
                } finally {
                    _isLoading.value = false // Kết thúc tải
                }
            }
        }
    }

    // Hàm để reset thông báo sau khi đã hiển thị (ví dụ: sau khi hiện Toast)
    fun clearMessage() {
        _updateMessage.value = null
    }

    fun logout() {
        authRepository.logout()
    }
}