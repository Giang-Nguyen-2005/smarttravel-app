package com.example.smarttravel.ui.screens.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.smarttravel.R
import com.example.smarttravel.model.Destination
import com.example.smarttravel.navigation.Screen
import com.example.smarttravel.ui.components.PrimaryButton
import com.example.smarttravel.ui.components.OpenStreetMapView
import com.example.smarttravel.ui.components.CommentSection
import com.example.smarttravel.ui.viewmodel.DetailViewModel
import com.example.smarttravel.ui.viewmodel.DetailUiState
import com.example.smarttravel.ui.viewmodel.CommentViewModel
import java.net.URLEncoder
// --- THÊM IMPORT NÀY ---
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme.colorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    destinationId: String,
    viewModel: DetailViewModel = hiltViewModel(),
    commentViewModel: CommentViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()
    var selectedImageIndex by remember { mutableIntStateOf(-1) }
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )
    // Tự động back khi kéo hết tờ giấy xuống
    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) {
            navController.popBackStack()
        }
    }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val peekHeight = screenHeight * 0.65f
    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = peekHeight,
            sheetShape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            sheetContent = {
                if (uiState.destination != null) {
                    ContentSheet(
                        destination = uiState.destination!!,
                        navController = navController,
                        viewModel = viewModel,
                        uiState = uiState,
                        commentViewModel = commentViewModel,
                        // Khi click ảnh trong list, lưu lại vị trí (index)
                        onImageClick = { index -> selectedImageIndex = index }
                    )
                } else {
                    Spacer(modifier = Modifier.fillMaxWidth().height(peekHeight))
                }
            },
            sheetContainerColor = colorScheme.surface,
            // Hiệu ứng làm mờ nền khi đang xem ảnh (nếu thiết bị hỗ trợ)
            modifier = Modifier.blur(if (selectedImageIndex != -1) 20.dp else 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.error != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Lỗi: ${uiState.error}", color = Color.Red)
                            Button(onClick = { viewModel.loadDestination() }, modifier = Modifier.padding(top = 16.dp)) {
                                Text("Thử lại")
                            }
                        }
                    }
                    uiState.destination != null -> {
                        ImageHeader(
                            imageUrl = uiState.destination!!.images.firstOrNull() ?: "ha_long",
                            modifier = Modifier.fillMaxSize(),
                            // Click header thì mở ảnh đầu tiên (index 0)
                            onImageClick = { selectedImageIndex = 0 }
                        )
                    }
                }
                TopControls(
                    onBackClick = { navController.popBackStack() },
                    onBookmarkClick = { viewModel.toggleBookmark() },
                    isBookmarked = uiState.isBookmarked
                )
            }
        }
        // Dùng AnimatedVisibility để hiện ra mượt mà
        AnimatedVisibility(
            visible = selectedImageIndex != -1 && uiState.destination != null,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize() // Quan trọng: Phủ kín màn hình
        ) {
            if (uiState.destination != null) {
                GalleryImageViewer(
                    images = uiState.destination!!.images,
                    initialIndex = selectedImageIndex,
                    onDismiss = { selectedImageIndex = -1 }
                )
            }
        }
    }
}

// ================== CÁC COMPONENT CON ==================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryImageViewer(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { images.size }
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.9f)) // Nền overlay mờ 90%
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Chặn click xuyên qua xuống dưới */ },
        contentAlignment = Alignment.Center
    ) {
        // HorizontalPager cho phép lướt trái phải
        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val imageUrl = images[page]
            val isNetworkImage = imageUrl.startsWith("http") || imageUrl.startsWith("https://")
            // Hiển thị 1 ảnh trong pager
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isNetworkImage) {
                    val context = LocalContext.current
                    val configuration = LocalConfiguration.current
                    val density = LocalDensity.current
                    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx().toInt() }
                    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx().toInt() }
                    
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            // Load ảnh với kích thước lớn hơn để tránh bị nhòe
                            .size(kotlin.math.max(screenWidth, screenHeight) * 2) // Kích thước tối đa
                            .allowHardware(false)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            // Hiệu ứng zoom nhẹ khi lướt (tùy chọn)
                            .graphicsLayer {
                                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                val scaleFactor = 1f - (0.1f * kotlin.math.abs(pageOffset)).coerceIn(0f, 1f)
                                scaleX = scaleFactor
                                scaleY = scaleFactor
                            },
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Ảnh local (fallback)
                    val context = LocalContext.current
                    val resId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
                    val painter = if (resId != 0) painterResource(id = resId) else painterResource(id = R.drawable.ic_launcher_foreground)
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        // Nút đóng (X) ở góc trên phải
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .padding(top = 32.dp) // Tránh tai thỏ
                .background(colorScheme.surface.copy(alpha = 0.7f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = colorScheme.onSurface
            )
        }
        // Chỉ báo số trang (ví dụ: 1/5) ở dưới đáy
        Text(
            text = "${pagerState.currentPage + 1} / ${images.size}",
            color = colorScheme.onSurface.copy(alpha = 0.9f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .background(colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ImageHeader(imageUrl: String, modifier: Modifier = Modifier, onImageClick: () -> Unit = {}) {
    val isNetworkImage = imageUrl.startsWith("http") || imageUrl.startsWith("https://")
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    // Tính toán kích thước màn hình để load ảnh chất lượng cao
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx().toInt() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx().toInt() }
    
    Box(modifier = modifier.clickable { onImageClick() }) {
        if (isNetworkImage) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    // Load ảnh với kích thước lớn hơn để tránh bị nhòe khi scale
                    .size(screenWidth * 2) // Kích thước tối đa (chiều rộng hoặc chiều cao, lấy giá trị lớn hơn)
                    .allowHardware(false) // Tắt hardware acceleration để render tốt hơn
                    .build(),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val resId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
            val painter = if (resId != 0) painterResource(id = resId) else painterResource(id = R.drawable.ic_launcher_foreground)
            Image(
                painter = painter,
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        // Bỏ overlay mờ để background hiển thị rõ ràng
    }
}

@Composable
fun TopControls(onBackClick: () -> Unit, onBookmarkClick: () -> Unit, isBookmarked: Boolean = false) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorScheme.surface.copy(alpha = 0.7f))
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isBookmarked) Color(0xFFFFC107).copy(alpha = 0.9f)
                    else colorScheme.surface.copy(alpha = 0.7f)
                )
                .clickable { onBookmarkClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Bookmark",
                tint = colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ContentSheet(
    destination: Destination,
    navController: NavController,
    viewModel: DetailViewModel,
    uiState: DetailUiState,
    commentViewModel: CommentViewModel,
    onImageClick: (Int) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // PADDING_BOTTOM_FIXED: Bù cho nút "Xem thêm" (khoảng 24dp)
    // PADDING_FOR_BUTTON: Khoảng trống cần thiết để nội dung cuộn lên trên nút cố định ở đáy
    val PADDING_FOR_BUTTON = 110.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = screenHeight * 0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                // Đảm bảo nội dung cuộn lên trên thanh nút và Navigation Bar
                .padding(
                    bottom = PADDING_FOR_BUTTON
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = destination.location_name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            InfoRow(destination = destination)
            Spacer(modifier = Modifier.height(24.dp))
            
            // Rating Section
            RatingSection(
                canRate = uiState.canRate,
                userRating = uiState.userRating,
                isLoading = uiState.isRatingLoading,
                error = uiState.error,
                onRatingClick = { rating -> viewModel.submitRating(rating) },
                onErrorDismiss = { viewModel.clearError() }
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bản đồ địa điểm
            if (destination.latitude != 0.0 && destination.longitude != 0.0) {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = "Vị trí",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OpenStreetMapView(
                        latitude = destination.latitude,
                        longitude = destination.longitude,
                        locationName = destination.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            if (destination.images.size > 1) {
                Text(
                    text = "Ảnh khác",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                // Truyền callback nhận index
                GalleryRow(galleryUrls = destination.images, onImageClick = onImageClick)
                Spacer(modifier = Modifier.height(24.dp))
            }
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Mô tả",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExpandableText(
                    text = destination.description
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // Phần bình luận
            CommentSection(
                viewModel = commentViewModel
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                // Giữ nguyên navigationBarsPadding() ở đây, nhưng nó chỉ hoạt động nếu layout là Edge-to-Edge.
                // Tuy nhiên, việc tăng PADDING_FOR_BUTTON đã bù đắp cho điều này.
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        ),
                        startY = 0f,
                        endY = 100f
                    )
                )
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding() // FIX: Đảm bảo padding Navigation Bar áp dụng cho nút
        ) {
            PrimaryButton(
                text = "Xem Gợi Ý Hành Trình",
                onClick = {
                    // Cần encode tên địa điểm để truyền qua URL
                    val encodedName = URLEncoder.encode(destination.name, "UTF-8")
                    navController.navigate( // Giờ đã có thể dùng navController
                        Screen.PlanRegisterFlow.createRoute(destination.id, encodedName)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun InfoRow(destination: Destination) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "${destination.rating}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Box(modifier = Modifier.size(1.dp, 24.dp).background(Color.LightGray.copy(alpha = 0.5f)))
        Text(
            text = "${java.text.NumberFormat.getIntegerInstance(java.util.Locale("vi", "VN")).format(destination.estimated_cost)}đ",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GalleryRow(galleryUrls: List<String>, onImageClick: (Int) -> Unit = {}) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Dùng itemsIndexed để lấy vị trí (index) của ảnh
        itemsIndexed(galleryUrls) { index, imageUrl ->
            val isNetworkImage = imageUrl.startsWith("http") || imageUrl.startsWith("https://")
            val imageModifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onImageClick(index) } // Truyền index khi click
            if (isNetworkImage) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                )
            } else {
                val context = LocalContext.current
                val resId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
                val painter = if (resId != 0) painterResource(id = resId) else painterResource(id = R.drawable.ic_launcher_foreground)
                Image(painter = painter, contentDescription = null, contentScale = ContentScale.Crop, modifier = imageModifier)
            }
        }
    }
}

@Composable
fun RatingSection(
    canRate: Boolean,
    userRating: Double?,
    isLoading: Boolean,
    error: String? = null,
    onRatingClick: (Double) -> Unit,
    onErrorDismiss: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Đánh giá của bạn",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Hiển thị lỗi nếu có
        error?.let { errorMessage ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFEBEE)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onErrorDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (!canRate) {
            // Chưa có kế hoạch với destination này
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tạo kế hoạch để đánh giá địa điểm này",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Có thể đánh giá
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hiển thị 5 sao
                (1..5).forEach { starValue ->
                    val ratingValue = starValue.toDouble()
                    val isSelected = userRating != null && ratingValue <= userRating
                    
                    IconButton(
                        onClick = { 
                            if (!isLoading) {
                                onRatingClick(ratingValue)
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "${starValue} sao",
                            tint = if (isSelected) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                if (isLoading) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else if (userRating != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${userRating.toInt()} sao",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    minimizedMaxLines: Int = 4
) {
    var isExpanded by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    val isClickable = remember(textLayoutResult) {
        (textLayoutResult?.hasVisualOverflow ?: false)
    }
    val isCollapsed by remember(isExpanded, isClickable) {
        derivedStateOf { !isExpanded && isClickable }
    }
    Column(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            lineHeight = 24.sp,
            maxLines = if (isExpanded) Int.MAX_VALUE else minimizedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult = it }
        )
        if (isClickable) {
            Text(
                text = if (isExpanded) "Thu gọn" else "Xem thêm",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { isExpanded = !isExpanded }
                    .padding(top = 4.dp)
            )
        }
    }
}