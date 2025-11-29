package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.data.repository.PlanRepository
import com.example.smarttravel.data.repository.AiService
import com.example.smarttravel.data.model.TravelPlan
import com.example.smarttravel.model.Category
import com.example.smarttravel.model.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.smarttravel.data.repository.AuthRepository
import com.example.smarttravel.model.UserProfile
import java.time.LocalDate
import java.time.ZoneId
import org.json.JSONArray
import org.json.JSONObject

// --- GIỮ NGUYÊN CÁC DATA CLASS STATE ---
data class DestinationUiState(
    val destinations: List<Destination> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class RecentPlanUiState(
    val recentPlan: TravelPlan? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

data class AiSuggestionsUiState(
    val destinations: List<Destination> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val cachedDate: String? = null // Ngày của cache hiện tại
)

data class TrendingDestinationsUiState(
    val destinations: List<Destination> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val authRepository: AuthRepository,
    private val planRepository: PlanRepository,
    private val aiService: AiService
) : ViewModel() {

    // --- STATE ---
    // 1. Danh sách gốc (Tất cả địa điểm, không bị lọc)
    private var allDestinations: List<Destination> = emptyList()

    // 2. State cho UI (Danh sách đã được lọc để hiển thị)
    private val _destinationUiState = MutableStateFlow(DestinationUiState())
    val destinationUiState: StateFlow<DestinationUiState> = _destinationUiState.asStateFlow()

    private val _categoryUiState = MutableStateFlow(CategoryUiState())
    val categoryUiState: StateFlow<CategoryUiState> = _categoryUiState.asStateFlow()

    // 3. Category đang được chọn (Mặc định là "all" - Tất cả)
    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // State cho kế hoạch gần đây
    private val _recentPlanState = MutableStateFlow(RecentPlanUiState())
    val recentPlanState: StateFlow<RecentPlanUiState> = _recentPlanState.asStateFlow()

    // State cho gợi ý AI
    private val _aiSuggestionsState = MutableStateFlow(AiSuggestionsUiState())
    val aiSuggestionsState: StateFlow<AiSuggestionsUiState> = _aiSuggestionsState.asStateFlow()
    
    // State cho địa điểm thịnh hành (trending)
    private val _trendingDestinationsState = MutableStateFlow(TrendingDestinationsUiState())
    val trendingDestinationsState: StateFlow<TrendingDestinationsUiState> = _trendingDestinationsState.asStateFlow()
    
    // State cho bookmark destinations
    private val _savedDestinationIds = MutableStateFlow<Set<String>>(emptySet())
    val savedDestinationIds: StateFlow<Set<String>> = _savedDestinationIds.asStateFlow()
    
    // Cache gợi ý theo ngày
    private var cachedSuggestions: List<Destination> = emptyList()
    private var cachedDate: String? = null

    init {
        loadCategories()
        loadDestinations()
        loadUserProfile()
        loadRecentPlan()
        loadSavedDestinations()
        // Khởi tạo state từ cache nếu có
        initializeAiSuggestionsFromCache()
    }
    
    private fun initializeAiSuggestionsFromCache() {
        val today = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        if (cachedDate == today && cachedSuggestions.isNotEmpty()) {
            _aiSuggestionsState.value = AiSuggestionsUiState(
                destinations = cachedSuggestions,
                isLoading = false,
                cachedDate = today
            )
            android.util.Log.d("HomeViewModel", "Initialized AI suggestions from cache for $today")
        }
    }

    // --- HÀM LOAD DỮ LIỆU ---
    private fun loadCategories() {
        viewModelScope.launch {
            _categoryUiState.value = CategoryUiState(isLoading = true)
            destinationRepository.getCategories().collect { result ->
                if (result.isSuccess) {
                    val categories = result.getOrNull() ?: emptyList()
                    // THÊM MỤC "TẤT CẢ" VÀO ĐẦU DANH SÁCH
                    val allCategory = Category(id = "all", name = "Tất cả")
                    _categoryUiState.value = CategoryUiState(
                        categories = listOf(allCategory) + categories,
                        isLoading = false
                    )
                } else {
                    _categoryUiState.value = CategoryUiState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    private fun loadDestinations() {
        viewModelScope.launch {
            _destinationUiState.value = DestinationUiState(isLoading = true)
            _trendingDestinationsState.value = TrendingDestinationsUiState(isLoading = true)
            destinationRepository.getDestinations().collect { result ->
                if (result.isSuccess) {
                    // Lưu danh sách gốc
                    allDestinations = result.getOrNull() ?: emptyList()
                    // Lọc lần đầu (mặc định là "all" nên sẽ hiện hết)
                    filterDestinations(_selectedCategory.value)
                    // Tính toán trending destinations
                    calculateTrendingDestinations()
                } else {
                    _destinationUiState.value = DestinationUiState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                    _trendingDestinationsState.value = TrendingDestinationsUiState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    // --- HÀM LỌC (GỌI TỪ UI) ---
    fun onCategorySelected(categoryId: String) {
        _selectedCategory.value = categoryId
        filterDestinations(categoryId)
    }

    private fun filterDestinations(categoryId: String) {
        val filteredList = if (categoryId == "all") {
            allDestinations // Nếu chọn "Tất cả", hiển thị danh sách gốc
        } else {
            // Nếu chọn category khác, lọc theo category_id
            allDestinations.filter { it.category_id == categoryId }
        }
        // Cập nhật state cho UI
        _destinationUiState.value = DestinationUiState(
            destinations = filteredList,
            isLoading = false
        )
    }
    
    /**
     * Tính toán địa điểm thịnh hành dựa trên rating cao nhất
     * Logic: Sort theo rating giảm dần, lấy top 5-10 destinations
     */
    private fun calculateTrendingDestinations() {
        if (allDestinations.isEmpty()) {
            _trendingDestinationsState.value = TrendingDestinationsUiState(
                destinations = emptyList(),
                isLoading = false
            )
            return
        }
        
        // Sort theo rating giảm dần, sau đó lấy top 5
        val trending = allDestinations
            .sortedByDescending { it.rating } // Sort theo rating cao nhất
            .take(5) // Lấy top 5
        
        _trendingDestinationsState.value = TrendingDestinationsUiState(
            destinations = trending,
            isLoading = false
        )
        
        android.util.Log.d("HomeViewModel", "Calculated ${trending.size} trending destinations")
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.getUserProfile().collect { profile ->
                _userProfile.value = profile
            }
        }
    }
    
    fun loadAiSuggestions() {
        viewModelScope.launch {
            val today = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            
            // Kiểm tra cache - nếu đã có gợi ý cho hôm nay thì dùng lại
            if (cachedDate == today && cachedSuggestions.isNotEmpty()) {
                _aiSuggestionsState.value = AiSuggestionsUiState(
                    destinations = cachedSuggestions,
                    isLoading = false,
                    cachedDate = today
                )
                android.util.Log.d("HomeViewModel", "Using cached AI suggestions for $today")
                return@launch
            }
            
            _aiSuggestionsState.value = AiSuggestionsUiState(isLoading = true)
            
            val userProfile = _userProfile.value
            val interests = userProfile?.interests ?: emptyList()
            
            if (interests.isEmpty() || allDestinations.isEmpty()) {
                _aiSuggestionsState.value = AiSuggestionsUiState(
                    destinations = emptyList(),
                    isLoading = false,
                    error = if (interests.isEmpty()) "Vui lòng cập nhật sở thích trong Profile" else null,
                    cachedDate = today
                )
                return@launch
            }
            
            // Lấy các destination IDs từ plans gần đây
            val recentPlanDestinationIds = getRecentPlanDestinationIds()
            
            // Gọi AI để rank destinations (có xem xét plans gần đây)
            val rankingResult = aiService.rankDestinationsByInterests(
                interests = interests,
                destinations = allDestinations,
                recentPlanDestinationIds = recentPlanDestinationIds
            )
            
            if (rankingResult.isSuccess) {
                val rankedIds = rankingResult.getOrNull() ?: emptyList()
                
                // Lọc destinations theo ranked IDs
                val rankedDestinations = rankedIds.mapNotNull { id ->
                    allDestinations.firstOrNull { it.id == id }
                }
                
                // Lưu vào cache
                cachedSuggestions = rankedDestinations
                cachedDate = today
                
                _aiSuggestionsState.value = AiSuggestionsUiState(
                    destinations = rankedDestinations,
                    isLoading = false,
                    cachedDate = today
                )
                
                android.util.Log.d("HomeViewModel", "Loaded and cached AI suggestions for $today")
            } else {
                _aiSuggestionsState.value = AiSuggestionsUiState(
                    isLoading = false,
                    error = rankingResult.exceptionOrNull()?.message ?: "Lỗi không xác định",
                    cachedDate = today
                )
            }
        }
    }
    
    private suspend fun getRecentPlanDestinationIds(): List<String> {
        return try {
            val allPlansFlow = planRepository.getMyPlans()
            
            // Lấy giá trị đầu tiên từ Flow
            val firstResult = allPlansFlow.first()
            if (firstResult.isSuccess) {
                val plans = firstResult.getOrNull() ?: emptyList()
                val today = LocalDate.now()
                val thirtyDaysAgo = today.minusDays(30)
                
                // Lấy các plans trong vòng 30 ngày gần đây
                val recentPlans = plans.filter { plan ->
                    if (plan.createdAt == null) return@filter false
                    
                    val createdDate = plan.createdAt.toDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    
                    !createdDate.isBefore(thirtyDaysAgo)
                }
                
                // Lấy destination IDs từ các plans gần đây
                val destinationIds = recentPlans.mapNotNull { it.destinationId }.distinct()
                
                android.util.Log.d("HomeViewModel", "Found ${destinationIds.size} recent plan destinations")
                destinationIds
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Error getting recent plan destinations: ${e.message}", e)
            emptyList()
        }
    }

    private fun loadRecentPlan() {
        viewModelScope.launch {
            _recentPlanState.value = RecentPlanUiState(isLoading = true)
            planRepository.getMyPlans().collect { result ->
                if (result.isSuccess) {
                    val allPlans = result.getOrNull() ?: emptyList()
                    val today = LocalDate.now()
                    
                    // Tìm kế hoạch sắp tới hoặc đang diễn ra (chưa kết thúc)
                    val upcomingOrActivePlan = allPlans.firstOrNull { plan ->
                        if (plan.endDate == null) return@firstOrNull false
                        
                        val endDate = plan.endDate.toDate()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        
                        // Lấy kế hoạch chưa kết thúc (endDate >= today)
                        !endDate.isBefore(today)
                    }
                    
                    _recentPlanState.value = RecentPlanUiState(
                        recentPlan = upcomingOrActivePlan,
                        isLoading = false
                    )
                } else {
                    _recentPlanState.value = RecentPlanUiState(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
    
    private fun loadSavedDestinations() {
        viewModelScope.launch {
            authRepository.getSavedDestinationIds().collect { savedIds ->
                _savedDestinationIds.value = savedIds.toSet()
            }
        }
    }
    
    fun toggleBookmark(destinationId: String) {
        viewModelScope.launch {
            val isCurrentlyBookmarked = _savedDestinationIds.value.contains(destinationId)
            val result = if (isCurrentlyBookmarked) {
                authRepository.unsaveDestination(destinationId)
            } else {
                authRepository.saveDestination(destinationId)
            }
            
            // Cập nhật UI ngay lập tức để UX tốt hơn
            if (result.isSuccess) {
                val currentSet = _savedDestinationIds.value.toMutableSet()
                if (isCurrentlyBookmarked) {
                    currentSet.remove(destinationId)
                } else {
                    currentSet.add(destinationId)
                }
                _savedDestinationIds.value = currentSet
            } else {
                android.util.Log.e("HomeViewModel", "Error toggling bookmark: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}