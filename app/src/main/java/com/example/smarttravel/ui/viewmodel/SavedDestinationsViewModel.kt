package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.AuthRepository
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.model.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedDestinationsUiState(
    val destinations: List<Destination> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SavedDestinationsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val destinationRepository: DestinationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedDestinationsUiState())
    val uiState: StateFlow<SavedDestinationsUiState> = _uiState.asStateFlow()

    init {
        loadSavedDestinations()
    }

    fun loadSavedDestinations() {
        viewModelScope.launch {
            _uiState.value = SavedDestinationsUiState(isLoading = true)
            
            combine(
                authRepository.getSavedDestinationIds(),
                destinationRepository.getDestinations()
            ) { savedIds, destinationsResult ->
                if (destinationsResult.isSuccess) {
                    val allDestinations = destinationsResult.getOrNull() ?: emptyList()
                    val savedDestinations = if (savedIds.isEmpty()) {
                        emptyList()
                    } else {
                        savedIds.mapNotNull { id ->
                            allDestinations.find { it.id == id }
                        }
                    }
                    SavedDestinationsUiState(
                        destinations = savedDestinations,
                        isLoading = false
                    )
                } else {
                    SavedDestinationsUiState(
                        isLoading = false,
                        error = destinationsResult.exceptionOrNull()?.message ?: "Lỗi không xác định"
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

