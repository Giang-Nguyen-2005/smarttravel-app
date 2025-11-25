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

// --- MÀU SẮC THEME ---
private val AppPrimaryColor = Color(0xFF037CAC)
private val BgLightGray = Color(0xFFF5F7FA) // Màu nền cho các box con
private val TextDark = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF757575)

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: PlanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is SaveState.Success -> {
                Toast.makeText(context, "Đã tạo kế hoạch thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetSaveState()
                navController.navigate(Screen.Calendar.route) {
                    popUpTo(Screen.PlanRegisterFlow.route) { inclusive = true }
                }
            }
            is SaveState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            // Nút bấm cố định ở đáy (Tách khỏi LazyColumn để luôn hiển thị)
            Surface(
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PrimaryButton(
                        text = when (saveState) {
                            is SaveState.Loading -> "Đang lưu..."
                            is SaveState.GeneratingAI -> "Đang tạo lịch trình AI..." // Giả sử bạn có state này
                            else -> "Xác nhận hành trình"
                        },
                        onClick = {
                            if (saveState !is SaveState.Loading) {
                                viewModel.savePlan()
                            }
                        },
                        enabled = saveState !is SaveState.Loading,
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
                    color = TextDark,
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
                        color = TextDark,
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
                        color = TextDark,
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            .background(Color.LightGray)
                    )
                }

                // Gradient mờ để nút edit rõ hơn (nếu cần)
                // Nút Edit tròn nổi trên ảnh
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = AppPrimaryColor,
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
                    color = TextDark,
                    textAlign = TextAlign.Center
                )
                if (locationName.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = locationName,
                            fontSize = 14.sp,
                            color = TextGray
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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgLightGray),
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
                        .background(Color.White, CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AppPrimaryColor
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = AppPrimaryColor.copy(alpha = 0.6f),
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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgLightGray),
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
                        tint = AppPrimaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${purposes.size} Sở thích đã chọn",
                        fontSize = 14.sp,
                        color = TextGray
                    )
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = AppPrimaryColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (purposes.isEmpty()) {
                Text(
                    text = "Chưa chọn sở thích nào",
                    fontSize = 14.sp,
                    color = TextDark,
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
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Text(
                                text = purpose,
                                fontSize = 13.sp,
                                color = TextDark,
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