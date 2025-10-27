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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smarttravel.R // <-- Import R
import com.example.smarttravel.ui.components.AppTopBar
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import com.example.smarttravel.ui.theme.SmarttravelTheme
import kotlinx.coroutines.launch

// --- Dữ liệu giả ---
enum class Sender { USER, BOT }
data class ChatMessage(
    val id: Int,
    val sender: Sender,
    val text: String,
    val time: String,
    val isSent: Boolean = true // Chỉ dùng cho User
)

val dummyMessages = listOf(
    ChatMessage(5, Sender.BOT, "Ngày 1: Tham quan Hồ Xuân Hương và Dinh Bảo Đại (chi phí khoảng 50k). Ăn trưa với đặc sản bánh tráng nướng (khoảng 30k). Chiều khám phá Thung Lũng Tình Yêu (50k).\nNgày 2: Đi Cầu Đất Farm (100k) và thưởng thức cà phê tại một quán địa phương (50k). Chiều thư giãn tại Thác Datanla (40k).", "9:30", true),
    ChatMessage(4, Sender.BOT, "Với 3 ngày ở Đà Lạt và ngân sách 4 triệu, mình gợi ý lịch trình như sau:", "9:30", true),
    ChatMessage(3, Sender.BOT, "Chào!", "9:30", true),
    ChatMessage(2, Sender.USER, "Tôi muốn đi Đà Lạt 3 ngày với ngân sách khoảng 4 triệu. Có gợi ý gì không?", "9:30", true),
    ChatMessage(1, Sender.USER, "Xin chào!", "9:24", true)
)
// --- Kết thúc dữ liệu giả ---

@Composable
fun ChatScreen(navController: NavController) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState() // Để cuộn xuống cuối
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. Top Bar
        ChatTopBar(onBackClick = { navController.popBackStack() })

        // 2. Danh sách tin nhắn (cuộn ngược)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f) // Chiếm hết không gian còn lại
                .fillMaxWidth(),
            reverseLayout = true, // Tin nhắn mới nhất ở dưới
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Khoảng cách giữa các tin nhắn
        ) {
            items(dummyMessages) { message ->
                ChatMessageItem(message = message)
            }

            // Dấu phân cách ngày (ví dụ)
            item {
                Text(
                    text = "Hôm nay",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Tự động cuộn xuống cuối khi có tin nhắn mới (Cần logic thực tế)
        LaunchedEffect(dummyMessages.size) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }

        // 3. Ô nhập liệu
        ChatInput(
            text = inputText,
            onTextChanged = { inputText = it },
            onSendClick = {
                // TODO: Xử lý gửi tin nhắn
                inputText = "" // Xóa text sau khi gửi
            }
        )
    }
}

// --- CÁC COMPONENT CON ---

@Composable
private fun ChatTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        AppTopBar(onBackClick = onBackClick)
        Text(
            text = "ChatBot",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.size(36.dp))
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.sender == Sender.USER
    val alignment: Alignment.Horizontal =
        if (isUser) Alignment.End else Alignment.Start
    val backgroundColor = if (isUser) Color(0xFFB5EBFF) else Color(0xFFF0F0F0)
    val textColor = Color.Black
    val timeAlignment = if (isUser) Alignment.End else Alignment.Start
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 0.dp,
        bottomEnd = if (isUser) 0.dp else 16.dp
    )

    // Get screen width
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Image(
                painter = painterResource(id = R.drawable.avatar),
                contentDescription = "Bot Avatar",
                modifier = Modifier.size(32.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = alignment,
            modifier = Modifier
                // Limit the maximum width to 2/3 of the screen
                .widthIn(max = screenWidth * (2f / 3f))
        ) {

            Box(
                modifier = Modifier
                    .background(backgroundColor, shape)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.align(timeAlignment)
            ) {
                Text(
                    text = message.time,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                if (isUser && message.isSent) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sent",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInput(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface( // Thêm Surface để có shadow
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = { Text("Viết tin nhắn", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
                maxLines = 4
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