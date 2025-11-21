package com.example.smarttravel.ui.screens.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.smarttravel.R
import com.example.smarttravel.ui.components.AppTopBar
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import com.example.smarttravel.ui.theme.SmarttravelTheme
import com.example.smarttravel.ui.viewmodel.ChatMessage
import com.example.smarttravel.ui.viewmodel.Sender
import com.example.smarttravel.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    var inputText by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Tự động cuộn xuống cuối khi có tin nhắn mới
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // 1. Top Bar
            ChatTopBar(onBackClick = { navController.popBackStack() })

            // 2. Danh sách tin nhắn (cuộn ngược)
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true, // Tin nhắn mới nhất ở dưới
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Typing animation phải ở đầu tiên để hiển thị ở dưới cùng (vì reverseLayout = true)
                    // Đây là vị trí mà tin nhắn bot tiếp theo sẽ xuất hiện
                    if (uiState.isLoading) {
                        item {
                            TypingAnimationIndicator()
                        }
                    }
                    
                    items(uiState.messages.reversed()) { message ->
                        ChatMessageItem(message = message)
                    }
                }
            }

            // 3. Ô nhập liệu
            ChatInput(
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
        shadowElevation = 2.dp,
        color = Color(0xFFFAFCFC)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppTopBar(onBackClick = onBackClick)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ChatBot",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Hỗ trợ du lịch 24/7",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.sender == Sender.USER
    val alignment: Alignment.Horizontal =
        if (isUser) Alignment.End else Alignment.Start
    val backgroundColor = if (isUser) 
        Color(0xFFB5EBFF) // Màu xanh như ban đầu
    else 
        Color(0xFFEDFCFF) // Màu cho tin nhắn bot
    val textColor = if (isUser) 
        Color.Black
    else 
        MaterialTheme.colorScheme.onSurface
    val timeAlignment = if (isUser) Alignment.End else Alignment.Start
    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isUser) 18.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 18.dp
    )

    // Get screen width
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            // Avatar với shadow
            Card(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar),
                    contentDescription = "Bot Avatar",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = alignment,
            modifier = Modifier
                .widthIn(max = screenWidth * 0.75f)
        ) {
            // Message bubble với elevation
            Card(
                modifier = Modifier,
                shape = shape,
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.align(timeAlignment)
            ) {
                Text(
                    text = message.time,
                    color = Color.Gray.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
                if (isUser && message.isSent) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sent",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun ChatInput(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color(0xFFFAFCFC),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = { 
                    Text(
                        "Nhập tin nhắn...", 
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 15.sp
                    ) 
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                maxLines = 4,
                enabled = enabled,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = onSendClick,
                modifier = Modifier.size(44.dp),
                containerColor = if (enabled && text.isNotBlank()) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = if (enabled && text.isNotBlank()) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    Color.Gray.copy(alpha = 0.5f)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Gửi",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Typing animation indicator - vị trí avatar bot tiếp theo (trong LazyColumn, ở vị trí tin nhắn bot sẽ xuất hiện)
@Composable
private fun TypingAnimationIndicator() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.typing))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar bot
        Card(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.avatar),
                contentDescription = "Bot Avatar",
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        
        // Lottie typing animation
        Box(
            modifier = Modifier.size(50.dp)
        ) {
            LottieAnimation(
                composition = composition,
                iterations = Int.MAX_VALUE,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        ChatScreen(navController = navController)
    }
}