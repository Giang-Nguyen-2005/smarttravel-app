package com.example.smarttravel.ui.screens.planregister

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
// THÊM IMPORT ICON NÀY CHO BIỂU TƯỢNG TRÁI TIM
import androidx.compose.material.icons.filled.FavoriteBorder
// THÊM IMPORT ICON NÀY CHO BIỂU TƯỢNG ĐỊA ĐIỂM
import androidx.compose.material.icons.outlined.LocationOn
// THÊM IMPORT ICON NÀY CHO BIỂU TƯỢNG NGƯỜI ĐỒNG HÀNH
import androidx.compose.material.icons.outlined.Groups
// THÊM IMPORT ICON NÀY CHO BIỂU TƯỢNG THỜI GIAN
import androidx.compose.material.icons.outlined.CalendarMonth
// THÊM IMPORT ICON NÀY CHO BIỂU TƯỢNG NGÂN SÁCH
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
import androidx.compose.foundation.border

import androidx.compose.foundation.layout.Arrangement

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
                Toast.makeText(context, "Đã tạo kế hoạch với gợi ý AI!", Toast.LENGTH_SHORT).show()
                viewModel.resetSaveState()
                // Chuyển đến trang Kế hoạch thay vì Home
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
        // Đã xóa bottomBar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // --- HEADER CỐ ĐỊNH (STICKY) DÙNG BOX ĐỂ CĂN GIỮA TUYỆT ĐỐI ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // Đảm bảo nền trắng khi cuộn
                    .padding(horizontal = 24.dp)
                    .padding(top = 60.dp, bottom = 16.dp) // Điều chỉnh padding
            ) {
                // 1. Nút Back (Căn trái)
                Box(modifier = Modifier.align(Alignment.CenterStart)) {
                    AppTopBar(onBackClick = { navController.popBackStack() })
                }

                // 2. Tiêu đề (Căn giữa tuyệt đối)
                Text(
                    text = "Tóm tắt đánh giá",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // --- NỘI DUNG CÓ THỂ CUỘN (LazyColumn) ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Thêm một Spacer đầu tiên để tạo khoảng trống dưới Header cố định
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Cụm 1: Địa điểm
                item {
                    EditableTitleRow(
                        icon = Icons.Outlined.LocationOn,
                        label = "Địa điểm",
                        // Truyền tên khu vực/tỉnh thành
                        locationName = uiState.locationName,
                        // Tên địa điểm chính (Thành phố Đà Lạt)
                        value = uiState.destinationName.ifEmpty { "Chưa chọn" },
                        onEditClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        showImage = true,
                        imageUrl = uiState.coverImageUrl
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item { CenteredDivider() }

                // Cụm 2: Người đồng hành
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableTitleRow(
                        icon = Icons.Outlined.Groups,
                        label = "Người đồng hành",
                        value = uiState.companion,
                        onEditClick = { navController.popBackStack(Screen.GoWith.route, false) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item { CenteredDivider() }

                // Cụm 3: Thời gian
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    val dateText = if (uiState.startDate != null && uiState.endDate != null) {
                        "${uiState.startDate} -> ${uiState.endDate}"
                    } else "Chưa chọn ngày"
                    EditableTitleRow(
                        icon = Icons.Outlined.CalendarMonth,
                        label = "Thời gian",
                        value = dateText,
                        onEditClick = { navController.popBackStack(Screen.Period.route, false) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item { CenteredDivider() }

                // CỤM 4: SỞ THÍCH
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableSummaryTags(
                        purposes = uiState.purposes,
                        onEditClick = { navController.popBackStack(Screen.Purpose.route, false) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                // KẾT THÚC CỤM 4

                item { CenteredDivider() }

                // Cụm 5: Ngân sách
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableTitleRow(
                        icon = Icons.Outlined.MonetizationOn,
                        label = "Ngân sách",
                        value = uiState.budget,
                        onEditClick = { navController.popBackStack(Screen.Economy.route, false) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }

            // Nút bấm cố định ở đáy
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // Đảm bảo nền trắng khi cuộn
                    .padding(16.dp)
            ) {
                PrimaryButton(
                    text = when (saveState) {
                        is SaveState.Loading -> "Đang lưu..."
                        is SaveState.GeneratingAI -> "Đang tạo gợi ý AI..."
                        else -> "Xác nhận hành trình"
                    },
                    onClick = {
                        if (saveState !is SaveState.Loading && saveState !is SaveState.GeneratingAI) {
                            viewModel.savePlan()
                        }
                    },
                    enabled = saveState !is SaveState.Loading && saveState !is SaveState.GeneratingAI,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CenteredDivider() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        HorizontalDivider(
            // Đã điều chỉnh chiều rộng lên 0.9f
            modifier = Modifier.fillMaxWidth(0.9f),
            color = Color.Gray.copy(alpha = 0.3f),
            thickness = 1.dp
        )
    }
}

// HÀM CHUNG CHO CÁC MỤC CÓ TIÊU ĐỀ ICON VÀ GIÁ TRỊ DẠNG THẺ
@Composable
fun EditableTitleRow(
    icon: ImageVector,
    label: String,
    value: String,
    onEditClick: () -> Unit,
    showImage: Boolean = false,
    imageUrl: String = "",
    // THÊM THAM SỐ locationName MỚI
    locationName: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // HÀNG TIÊU ĐỀ + NÚT EDIT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tiêu đề: Icon + Text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            // Nút Edit
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Chỉnh sửa $label",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showImage) {
            // CÁCH HIỂN THỊ ĐẶC BIỆT CHO ĐỊA ĐIỂM (CÓ ẢNH VÀ TEXT LỚN)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Ảnh địa điểm",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                // Cột chứa Tên địa điểm và Tên khu vực
                Column(modifier = Modifier.weight(1f)) {
                    // Tên địa điểm chính (ví dụ: Thành phố Đà Lạt)
                    Text(
                        text = value.ifEmpty { "Chưa chọn" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    // Tên khu vực (ví dụ: Lâm Đồng, Việt Nam)
                    if (locationName.isNotEmpty()) {
                        Text(
                            text = locationName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        } else {
            // ÁP DỤNG KHUNG BOX 85% VÀ PURPOSECHIP CHO CÁC GIÁ TRỊ CÒN LẠI
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sử dụng PurposeChip để đóng khung cho giá trị
                    PurposeChip(text = value.ifEmpty { "Chưa chọn" })
                }
            }
        }
    }
}

// PurposeChip giữ nguyên
@Composable
private fun PurposeChip(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// EditableSummaryTags giữ nguyên
@Composable
fun EditableSummaryTags(
    purposes: List<String>,
    onEditClick: () -> Unit
) {
    // Top-level Column
    Column(modifier = Modifier.fillMaxWidth()) {
        // Hàng chứa Tiêu đề (Sở thích + Icon) và Nút Edit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tiêu đề: Heart Icon + Text "Sở thích"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder, // Heart Icon
                    contentDescription = "Sở thích",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sở thích",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black // Màu đen
                )
            }

            // Nút Edit
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Chỉnh sửa Sở thích",
                    tint = Color.Black
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (purposes.isEmpty()) {
            // Hiển thị khung cho trường hợp chưa chọn
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PurposeChip(text = "Chưa chọn")
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    // Giới hạn chiều rộng 85% của khối này
                    modifier = Modifier.fillMaxWidth(0.85f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    purposes.forEach { purpose ->
                        PurposeChip(text = purpose)
                    }
                }
            }
        }
    }
}