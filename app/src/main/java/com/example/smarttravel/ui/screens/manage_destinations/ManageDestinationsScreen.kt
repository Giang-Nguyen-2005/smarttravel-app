package com.example.smarttravel.ui.screens.manage_destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.smarttravel.model.Destination
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.components.SkeletonManageDestinationItem
import com.example.smarttravel.ui.components.SkeletonList
import com.example.smarttravel.ui.viewmodel.ManageDestinationsViewModel
import com.example.smarttravel.util.NetworkUtil
import com.example.smarttravel.util.NetworkQuality

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageDestinationsScreen(
    navController: NavController,
    viewModel: ManageDestinationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    
    val context = LocalContext.current
    val networkUtil = remember { NetworkUtil(context) }
    val networkQuality = remember { 
        mutableStateOf(networkUtil.getNetworkQuality()) 
    }
    
    // Update network quality periodically
    LaunchedEffect(Unit) {
        while (true) {
            networkQuality.value = networkUtil.getNetworkQuality()
            kotlinx.coroutines.delay(2000) // Check every 2 seconds
        }
    }
    
    val shouldShowSkeleton = uiState.isLoading || 
        networkQuality.value == NetworkQuality.NONE || 
        networkQuality.value == NetworkQuality.POOR
    
    Scaffold(
        topBar = {
            AppTopBar(
                onBackClick = { navController.popBackStack() },
                title = "Quản lý địa điểm",
                containerColor = colorScheme.surfaceVariant
            )
        }
    ) { paddingValues ->
        when {
            shouldShowSkeleton -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(5) {
                        SkeletonManageDestinationItem()
                    }
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Lỗi: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadDestinations() }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.destinations.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Chưa có địa điểm nào",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Thêm địa điểm mới từ Settings",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.destinations) { destination ->
                            DestinationItem(
                                destination = destination,
                                onEditClick = {
                                    navController.navigate(Screen.EditDestination.createRoute(destination.id))
                                },
                                onDeleteClick = {
                                    viewModel.deleteDestination(destination.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DestinationItem(
    destination: Destination,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val imageUrl = destination.images.firstOrNull() ?: ""
    val isNetworkImage = imageUrl.startsWith("http") || imageUrl.startsWith("https://")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (isNetworkImage && imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Thông tin
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = destination.location_name,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFFFC107)
                    )
                    Text(
                        text = "${destination.rating}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Nút Edit và Delete
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Sửa",
                        tint = colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = colorScheme.error
                    )
                }
            }
        }
    }
}

