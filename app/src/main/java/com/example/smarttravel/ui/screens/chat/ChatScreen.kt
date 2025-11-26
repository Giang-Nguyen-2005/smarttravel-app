package com.example.smarttravel.ui.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.smarttravel.R
import com.example.smarttravel.ui.components.AppTopBar
import com.example.smarttravel.ui.viewmodel.ChatMessage
import com.example.smarttravel.ui.viewmodel.ChatViewModel
import com.example.smarttravel.ui.viewmodel.Sender
import kotlinx.coroutines.launch

// --- MÀU SẮC THEME ---
private val AppPrimaryColor = Color(0xFF037CAC)
private val BotBubbleColor = Color(0xFFF2F4F5)
private val InputBgColor = Color(0xFFF5F7FA)

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    var inputText by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto scroll
    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        if (uiState.messages.isNotEmpty() || uiState.isLoading) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        topBar = { ChatTopBar(onBackClick = { navController.popBackStack() }) },
        // ĐÃ BỎ BOTTOM BAR TẠI ĐÂY
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Danh sách tin nhắn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.isLoading) {
                    item { TypingAnimationIndicator() }
                }
                items(uiState.messages.reversed()) { message ->
                    ChatMessageItem(message = message)
                }
            }

            // 2. Ô nhập liệu (Đã bỏ nút +)
            ModernChatInput(
                text = inputText,
                onTextChanged = { inputText = it },
                onSendClick = {
                    if (inputText.isNotBlank() && !uiState.isLoading) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                enabled = !uiState.isLoading
            )
        }
    }
}

// --- CÁC COMPONENT CON ---

@Composable
private fun ChatTopBar(onBackClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 0.5.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTopBar(onBackClick = onBackClick)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Trợ lý AI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Online",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.size(46.dp))
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.sender == Sender.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val backgroundColor = if (isUser) AppPrimaryColor else BotBubbleColor
    val contentColor = if (isUser) Color.White else Color(0xFF1A1A1A)

    val bubbleShape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val maxBubbleWidth = screenWidth * 0.75f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Image(
                painter = painterResource(id = R.drawable.avatar),
                contentDescription = "Bot",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = alignment) {
            Surface(
                shape = bubbleShape,
                color = backgroundColor,
                modifier = Modifier.widthIn(max = maxBubbleWidth),
                shadowElevation = 0.dp
            ) {
                Text(
                    text = message.text,
                    color = contentColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

// === PHẦN CẬP NHẬT: ĐÃ BỎ NÚT DẤU CỘNG ===
@Composable
private fun ModernChatInput(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding() // Đảm bảo ô nhập nằm ngay trên bàn phím
            .navigationBarsPadding(), // Tránh bị che bởi navigation bar
        color = Color.White,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Ô nhập liệu dạng "Viên thuốc"
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(InputBgColor, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    onValueChange = onTextChanged,
                    placeholder = {
                        Text(
                            "Hỏi tôi về chuyến đi...",
                            color = Color.Gray.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp, max = 100.dp),
                    // --- SỬA LỖI TẠI ĐÂY ---
                    colors = TextFieldDefaults.colors(
                        // Trạng thái bình thường
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = AppPrimaryColor,

                        // Trạng thái Disabled (khi AI đang trả lời) -> Ép về trong suốt
                        disabledContainerColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        disabledTextColor = Color.Gray, // Màu chữ khi bị khóa (tùy chọn)
                        disabledPlaceholderColor = Color.Gray.copy(alpha = 0.6f) // Màu placeholder khi bị khóa
                    ),
                    maxLines = 4,
                    enabled = enabled,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Nút Gửi
            val isSendEnabled = enabled && text.isNotBlank()
            // Nếu đang loading (enabled = false), ta vẫn giữ màu xám nhạt (E0E0E0) chứ không để nó bị tối đi theo mặc định
            val buttonColor = if (isSendEnabled) AppPrimaryColor else Color(0xFFE0E0E0)
            val iconColor = if (isSendEnabled) Color.White else Color.Gray

            IconButton(
                onClick = onSendClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(buttonColor, CircleShape)
                    .padding(4.dp),
                enabled = isSendEnabled
            ) {
                // Nếu đang loading, có thể hiển thị CircularProgressIndicator nhỏ thay vì icon Send
                if (!enabled && text.isNotBlank()) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Gray,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gửi",
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TypingAnimationIndicator() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.typing))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.avatar),
            contentDescription = "Bot",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = BotBubbleColor,
            modifier = Modifier.height(36.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                if (composition != null) {
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Text("...", fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
        }
    }
}