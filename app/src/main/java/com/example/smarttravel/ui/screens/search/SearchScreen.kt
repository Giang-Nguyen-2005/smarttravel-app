package com.example.smarttravel.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.components.AppBottomBar
import com.example.smarttravel.ui.components.DestinationCard
import com.example.smarttravel.ui.viewmodel.SearchViewModel
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material.icons.filled.Done

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchText by viewModel.searchText.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val trendingDestinations by viewModel.trendingDestinations.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState() // [MỚI] Lấy lịch sử thật
    val isLoading by viewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        bottomBar = {
            // [SỬA LỖI] Luôn hiển thị AppBottomBar, không dùng điều kiện if nữa
            AppBottomBar(navController = navController, currentRoute = Screen.Search.route)
        }
    ) { paddingValues ->
        val colorScheme = MaterialTheme.colorScheme
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(paddingValues)
        ) {
            // 1. Thanh tìm kiếm
            ModernSearchBar(
                searchText = searchText,
                onSearchChanged = { viewModel.onSearchTextChanged(it) },
                onBackClick = {
                    if (searchText.isNotEmpty()) {
                        viewModel.onSearchTextChanged("")
                    } else {
                        // Nếu đang ở tab Search của BottomBar thì không cần popBackStack
                        // trừ khi bạn muốn nó quay về Home
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                onClearClick = { viewModel.onSearchTextChanged("") },
                onSearchAction = {
                    focusManager.clearFocus()
                    viewModel.addToSearchHistory(searchText) // Lưu lịch sử khi bấm Enter
                },
                modifier = Modifier.padding(16.dp)
            )

            // 2. Nội dung chính
            Box(modifier = Modifier.fillMaxSize()) {
                if (searchText.isBlank()) {
                    // --- TRẠNG THÁI 1: CHƯA NHẬP GÌ ---
                    PreSearchContent(
                        searchHistory = searchHistory,
                        trendingDestinations = trendingDestinations,
                        onHistoryClick = { query -> viewModel.onSearchTextChanged(query) },
                        onClearHistory = { viewModel.clearHistory() }, // [MỚI] Nút xóa lịch sử
                        onDestinationClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
                    )
                } else {
                    // --- TRẠNG THÁI 2: ĐANG TÌM KIẾM ---
                    SearchResultsContent(
                        isLoading = isLoading,
                        searchResults = searchResults,
                        onItemClick = { destination ->
                            // [MỚI] Lưu tên địa điểm vào lịch sử khi click chọn
                            viewModel.addToSearchHistory(destination.name)
                            navController.navigate(Screen.Detail.createRoute(destination.id))
                        }
                    )
                }
            }
        }
    }
}

// ================== CÁC COMPONENT CON ==================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSearchBar(
    searchText: String,
    onSearchChanged: (String) -> Unit,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    onSearchAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = searchText,
        onValueChange = onSearchChanged,
        placeholder = {
            Text("Tìm địa điểm du lịch", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
        },
        leadingIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchAction() }),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp))
    )
}

@Composable
fun PreSearchContent(
    searchHistory: List<String>,
    trendingDestinations: List<com.example.smarttravel.model.Destination>,
    onHistoryClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDestinationClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // --- PHẦN 1: Lịch sử tìm kiếm (Nằm ngoài Grid) ---
        if (searchHistory.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tìm kiếm gần đây",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClearHistory, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp), // Padding trong cho LazyRow
                modifier = Modifier.fillMaxWidth()
            ) {
                items(searchHistory) { query ->
                    SuggestionChip(
                        onClick = { onHistoryClick(query) },
                        label = { Text(query) },
                        icon = {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = CircleShape,
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = null
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- PHẦN 2: Địa điểm phổ biến (Header) ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                Icons.Default.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Địa điểm phổ biến",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // --- PHẦN 3: Lưới địa điểm (Chiếm hết không gian còn lại) ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Quan trọng: Để nó chiếm hết phần dưới và cho phép cuộn nếu dài
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp,top=0.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(trendingDestinations) { destination ->
                DestinationCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDestinationClick(destination.id) },
                    imageUrl = destination.images.firstOrNull() ?: "ha_long",
                    title = destination.name,
                    location = destination.location_name,
                    rating = destination.rating
                )
            }
        }
    }
}

@Composable
fun SearchResultsContent(
    isLoading: Boolean,
    searchResults: List<com.example.smarttravel.model.Destination>,
    onItemClick: (com.example.smarttravel.model.Destination) -> Unit
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
        }
    } else if (searchResults.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Không tìm thấy kết quả nào.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column {
            Text(
                text = "Kết quả (${searchResults.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(searchResults) { destination ->
                    DestinationCard(
                        modifier = Modifier.clickable { onItemClick(destination) },
                        imageUrl = destination.images.firstOrNull() ?: "ha_long",
                        title = destination.name,
                        location = destination.location_name,
                        rating = destination.rating
                    )
                }
            }
        }
    }
}