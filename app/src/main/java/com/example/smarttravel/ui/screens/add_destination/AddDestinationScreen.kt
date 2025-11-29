package com.example.smarttravel.ui.screens.add_destination

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.smarttravel.model.Category
import com.example.smarttravel.ui.viewmodel.AddDestinationViewModel
import androidx.compose.runtime.collectAsState
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.smarttravel.ui.components.LocationPickerDialog
import com.example.smarttravel.ui.components.OpenStreetMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDestinationScreen(
    navController: NavController,
    destinationId: String? = null, // ID khi edit
    viewModel: AddDestinationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    
    // Load categories
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var expandedCategory by remember { mutableStateOf(false) }
    
    // Location picker
    var showLocationPicker by remember { mutableStateOf(false) }
    
    // Load destination nếu đang edit
    LaunchedEffect(destinationId) {
        destinationId?.let { id ->
            if (id.isNotEmpty()) {
                viewModel.loadDestinationForEdit(id)
            }
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.getCategories().collect { result ->
            if (result.isSuccess) {
                categories = result.getOrNull() ?: emptyList()
            }
        }
    }
    
    // Handle success
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            val message = if (uiState.isEditMode) {
                "Cập nhật địa điểm thành công!"
            } else {
                "Thêm địa điểm thành công!"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }
    
    // Handle error - show toast
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Sửa địa điểm du lịch" else "Thêm địa điểm du lịch") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Error message
            if (uiState.error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.error ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Đóng",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Tên địa điểm *
            item {
                Column {
                    Text(
                        text = "Tên địa điểm *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập tên địa điểm") },
                        singleLine = true,
                        shape = RoundedCornerShape(15.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorScheme.surfaceVariant,
                            focusedContainerColor = colorScheme.surfaceVariant,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = colorScheme.primary
                        )
                    )
                }
            }
            
            // Mô tả
            item {
                Column {
                    Text(
                        text = "Mô tả",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Nhập mô tả về địa điểm") },
                        maxLines = 5,
                        shape = RoundedCornerShape(15.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorScheme.surfaceVariant,
                            focusedContainerColor = colorScheme.surfaceVariant,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = colorScheme.primary
                        )
                    )
                }
            }
            
            // Địa điểm *
            item {
                Column {
                    Text(
                        text = "Địa điểm *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.locationName,
                        onValueChange = { viewModel.updateLocationName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ví dụ: Lâm Đồng, Việt Nam") },
                        singleLine = true,
                        shape = RoundedCornerShape(15.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorScheme.surfaceVariant,
                            focusedContainerColor = colorScheme.surfaceVariant,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = colorScheme.primary
                        )
                    )
                }
            }
            
            // Danh mục *
            item {
                Column {
                    Text(
                        text = "Danh mục *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = !expandedCategory }
                    ) {
                        OutlinedTextField(
                            value = categories.find { it.id == uiState.categoryId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            placeholder = { Text("Chọn danh mục") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                            shape = RoundedCornerShape(15.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = colorScheme.surfaceVariant,
                                focusedContainerColor = colorScheme.surfaceVariant,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = colorScheme.primary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        viewModel.updateCategoryId(category.id)
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Chi phí ước tính
            item {
                Column {
                    Text(
                        text = "Chi phí ước tính (VNĐ)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.estimatedCost,
                        onValueChange = { viewModel.updateEstimatedCost(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ví dụ: 2000000") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(15.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorScheme.surfaceVariant,
                            focusedContainerColor = colorScheme.surfaceVariant,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = colorScheme.primary
                        )
                    )
                }
            }
            
            // Vị trí (có thể nhập địa chỉ hoặc chọn trên bản đồ)
            item {
                Column {
                    Text(
                        text = "Vị trí *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.locationName,
                            onValueChange = { viewModel.updateLocationName(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Nhập địa chỉ (ví dụ: Đà Lạt, Lâm Đồng)") },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null)
                            },
                            trailingIcon = {
                                if (uiState.isLoading && uiState.locationName.isNotBlank()) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else if (uiState.locationName.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            // Geocode địa chỉ thủ công (nếu tự động không hoạt động)
                                            viewModel.geocodeAddress(uiState.locationName)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Tìm trên bản đồ"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(15.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = colorScheme.surfaceVariant,
                                focusedContainerColor = colorScheme.surfaceVariant,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = colorScheme.primary
                            )
                        )
                        Button(
                            onClick = { 
                                showLocationPicker = true 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Chọn trên bản đồ",
                                modifier = Modifier.size(18.dp),
                                tint = colorScheme.onPrimary
                            )
                        }
                    }
                    
                    // Hiển thị bản đồ preview nếu đã có tọa độ
                    val lat = uiState.latitude.toDoubleOrNull()
                    val lng = uiState.longitude.toDoubleOrNull()
                    if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OpenStreetMapView(
                            latitude = lat,
                            longitude = lng,
                            locationName = uiState.locationName.ifEmpty { "Địa điểm đã chọn" },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            showOpenExternalButton = false
                        )
                    }
                }
            }
            
            // Danh sách ảnh
            item {
                Column {
                    Text(
                        text = "Hình ảnh (URL)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Input URL ảnh
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.imageUrl,
                            onValueChange = { viewModel.updateImageUrl(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Nhập URL ảnh") },
                            singleLine = true,
                            shape = RoundedCornerShape(15.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = colorScheme.surfaceVariant,
                                focusedContainerColor = colorScheme.surfaceVariant,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = colorScheme.primary
                            )
                        )
                        IconButton(
                            onClick = { viewModel.addImage() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Thêm ảnh",
                                tint = colorScheme.onPrimary
                            )
                        }
                    }
                    
                    // Hiển thị danh sách ảnh đã thêm
                    if (uiState.images.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Ảnh chính (ảnh đầu tiên)
                        if (uiState.images.isNotEmpty()) {
                            Text(
                                text = "Ảnh chính",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(uiState.images[0])
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        ) {
                                            val state = painter.state
                                            when {
                                                state is coil.compose.AsyncImagePainter.State.Loading -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(colorScheme.surfaceVariant),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(24.dp),
                                                            strokeWidth = 2.dp
                                                        )
                                                    }
                                                }
                                                state is coil.compose.AsyncImagePainter.State.Error -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(colorScheme.surfaceVariant),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.BrokenImage,
                                                            contentDescription = "Lỗi tải ảnh",
                                                            tint = colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                    }
                                                }
                                                else -> {
                                                    SubcomposeAsyncImageContent()
                                                }
                                            }
                                        }
                                        
                                        IconButton(
                                            onClick = { viewModel.removeImage(0) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .background(
                                                    colorScheme.error,
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Xóa",
                                                tint = colorScheme.onError,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                            }
                        }
                        
                        // Ảnh khác (từ ảnh thứ 2 trở đi)
                        if (uiState.images.size > 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ảnh khác (nhấn để đặt làm ảnh chính)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(uiState.images.drop(1)) { index, imageUrl ->
                                    val actualIndex = index + 1 // +1 vì đã drop ảnh đầu
                                    Card(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(100.dp)
                                            .clickable { viewModel.setMainImage(actualIndex) },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            SubcomposeAsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(imageUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            ) {
                                                val state = painter.state
                                                when {
                                                    state is coil.compose.AsyncImagePainter.State.Loading -> {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(colorScheme.surfaceVariant),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            CircularProgressIndicator(
                                                                modifier = Modifier.size(24.dp),
                                                                strokeWidth = 2.dp
                                                            )
                                                        }
                                                    }
                                                    state is coil.compose.AsyncImagePainter.State.Error -> {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(colorScheme.surfaceVariant),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.BrokenImage,
                                                                contentDescription = "Lỗi tải ảnh",
                                                                tint = colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.size(32.dp)
                                                            )
                                                        }
                                                    }
                                                    else -> {
                                                        SubcomposeAsyncImageContent()
                                                    }
                                                }
                                            }
                                            
                                            IconButton(
                                                onClick = { viewModel.removeImage(actualIndex) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(24.dp)
                                                    .background(
                                                        colorScheme.error,
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Xóa",
                                                    tint = colorScheme.onError,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            
            // Nút Lưu
            item {
                Button(
                    onClick = { viewModel.submitDestination() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(15.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Thêm địa điểm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    // Location Picker Dialog
        if (showLocationPicker) {
            LocationPickerDialog(
                onDismiss = { showLocationPicker = false },
                onConfirm = { lat, lng, address ->
                    viewModel.updateLocation(lat, lng, address)
                    showLocationPicker = false
                },
                initialLat = uiState.latitude.toDoubleOrNull(),
                initialLng = uiState.longitude.toDoubleOrNull(),
                initialAddress = uiState.locationName
            )
        }
}

