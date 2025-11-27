package com.example.smarttravel.ui.screens.planregister

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.viewmodel.PlanViewModel
import com.example.smarttravel.ui.viewmodel.SaveState

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: PlanViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val context = LocalContext.current

    // Không cần LaunchedEffect nữa vì đã navigate ngay trong onClick
    // Chỉ xử lý error nếu có
    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is SaveState.Error -> {
                // Nếu có lỗi, quay lại màn hình trước
                navController.popBackStack()
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = colorScheme.background,
        bottomBar = {
            // Nút bấm cố định ở đáy (Tách khỏi LazyColumn để luôn hiển thị)
            Surface(
                shadowElevation = 8.dp,
                color = colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PrimaryButton(
                        text = "Xác nhận hành trình",
                        onClick = {
                            if (saveState !is SaveState.Loading && saveState !is SaveState.GeneratingAI) {
                                // Navigate đến màn hình AI Generating ngay lập tức
                                // Pass planId tạm thời là "temp" (sẽ được update sau)
                                navController.navigate(Screen.AiGenerating.createRoute("temp")) {
                                    popUpTo(Screen.PlanRegisterFlow.route) { inclusive = false }
                                }
                                // Bắt đầu save plan
                                viewModel.savePlan()
                            }
                        },
                        enabled = saveState !is SaveState.Loading && saveState !is SaveState.GeneratingAI,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                AppTopBar(onBackClick = { navController.popBackStack() })
                Text(
                    text = "Tóm tắt chuyến đi",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // --- NỘI DUNG ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                // 1. Card Địa điểm (Hero Section)
                item {
                    LocationSummaryCard(
                        destinationName = uiState.destinationName.ifEmpty { "Chưa chọn địa điểm" },
                        locationName = uiState.locationName,
                        imageUrl = uiState.coverImageUrl,
                        onEditClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 2. Thông tin chi tiết (Nhóm vào 1 khối)
                item {
                    Text(
                        text = "Thông tin chi tiết",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Người đồng hành
                        DetailSummaryItem(
                            icon = Icons.Outlined.Groups,
                            label = "Người đồng hành",
                            value = uiState.companion.ifEmpty { "Chưa chọn" },
                            onEditClick = { navController.popBackStack(Screen.GoWith.route, false) }
                        )

                        // Thời gian
                        val dateText = if (uiState.startDate != null && uiState.endDate != null) {
                            "${uiState.startDate} - ${uiState.endDate}"
                        } else "Chưa chọn ngày"
                        DetailSummaryItem(
                            icon = Icons.Outlined.CalendarMonth,
                            label = "Thời gian",
                            value = dateText,
                            onEditClick = { navController.popBackStack(Screen.Period.route, false) }
                        )

                        // Ngân sách
                        DetailSummaryItem(
                            icon = Icons.Outlined.MonetizationOn,
                            label = "Ngân sách dự kiến",
                            value = uiState.budget.ifEmpty { "Chưa chọn" },
                            onEditClick = { navController.popBackStack(Screen.Economy.route, false) }
                        )
                    }
                }

                // 3. Sở thích (Tags)
                item {
                    Text(
                        text = "Sở thích & Mong muốn",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    InterestsSummaryCard(
                        purposes = uiState.purposes,
                        onEditClick = { navController.popBackStack(Screen.Purpose.route, false) }
                    )
                }
            }
        }
    }
}

// --- COMPONENTS CON ---

@Composable
fun LocationSummaryCard(
    destinationName: String,
    locationName: String,
    imageUrl: String,
    onEditClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(modifier = Modifier.height(180.dp)) {
                // Ảnh nền
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                // Gradient mờ để nút edit rõ hơn (nếu cần)
                // Nút Edit tròn nổi trên ảnh
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Thông tin text bên dưới ảnh
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = destinationName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (locationName.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = locationName,
                            fontSize = 14.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSummaryItem(
    icon: ImageVector,
    label: String,
    value: String,
    onEditClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() } // Cho phép click toàn bộ dòng để sửa
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Icon Box xanh nhạt
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colorScheme.surface, CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class) // Cần cho FlowRow
@Composable
fun InterestsSummaryCard(
    purposes: List<String>,
    onEditClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${purposes.size} Sở thích đã chọn",
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (purposes.isEmpty()) {
                Text(
                    text = "Chưa chọn sở thích nào",
                    fontSize = 14.sp,
                    color = colorScheme.onSurface,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            } else {
                // FlowRow tự động xuống dòng
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    purposes.forEach { purpose ->
                        Surface(
                            color = colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = purpose,
                                fontSize = 13.sp,
                                color = colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}