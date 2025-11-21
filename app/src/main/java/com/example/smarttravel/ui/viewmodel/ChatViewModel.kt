package com.example.smarttravel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravel.data.repository.AiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ChatMessage(
    val id: String,
    val sender: Sender,
    val text: String,
    val time: String,
    val isSent: Boolean = true // Chỉ dùng cho User
)

enum class Sender {
    USER, BOT
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val aiService: AiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Thêm tin nhắn chào mừng ban đầu
        addWelcomeMessage()
    }

    private fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = Sender.BOT,
            text = "Xin chào! 👋\n\nTôi có thể giúp bạn:\n• Tư vấn địa điểm du lịch\n• Gợi ý lịch trình\n• Tư vấn ngân sách\n• Gợi ý món ăn đặc sản\n• Tư vấn khách sạn\n\nBạn muốn hỏi gì?",
            time = getCurrentTime(),
            isSent = true
        )
        _uiState.value = _uiState.value.copy(
            messages = listOf(welcomeMessage)
        )
    }

    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = Sender.USER,
            text = messageText.trim(),
            time = getCurrentTime(),
            isSent = true
        )

        // Thêm tin nhắn user vào danh sách
        val currentMessages = _uiState.value.messages
        _uiState.value = _uiState.value.copy(
            messages = currentMessages + userMessage,
            isLoading = true,
            error = null
        )

        // Gọi AI để nhận phản hồi
        viewModelScope.launch {
            try {
                // Lấy lịch sử hội thoại (chỉ lấy 10 tin nhắn gần nhất để tránh quá dài)
                val recentMessages = currentMessages.takeLast(10)
                val conversationHistory = mutableListOf<Pair<String, String>>()
                
                // Chuyển đổi lịch sử thành format (user, bot)
                var i = 0
                while (i < recentMessages.size - 1) {
                    val userMsg = recentMessages[i]
                    val botMsg = recentMessages[i + 1]
                    if (userMsg.sender == Sender.USER && botMsg.sender == Sender.BOT) {
                        conversationHistory.add(userMsg.text to botMsg.text)
                    }
                    i++
                }

                val result = aiService.sendChatMessage(
                    message = messageText.trim(),
                    conversationHistory = conversationHistory
                )

                if (result.isSuccess) {
                    val botResponse = result.getOrNull() ?: "Xin lỗi, tôi không thể trả lời câu hỏi này."
                    val botMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sender = Sender.BOT,
                        text = botResponse,
                        time = getCurrentTime(),
                        isSent = true
                    )

                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + botMessage,
                        isLoading = false
                    )
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = error?.message ?: "Lỗi không xác định"
                    android.util.Log.e("ChatViewModel", "Error sending message: $errorMessage")
                    android.util.Log.e("ChatViewModel", "Error stack trace: ${error?.stackTraceToString()}")
                    
                    // Tạo thông báo lỗi thân thiện hơn
                    val friendlyErrorMessage = when {
                        errorMessage.contains("API key", ignoreCase = true) -> 
                            "Lỗi xác thực API. Vui lòng kiểm tra cấu hình."
                        errorMessage.contains("quota", ignoreCase = true) || errorMessage.contains("limit", ignoreCase = true) -> 
                            "Đã vượt quá giới hạn sử dụng. Vui lòng thử lại sau."
                        errorMessage.contains("network", ignoreCase = true) || errorMessage.contains("connection", ignoreCase = true) -> 
                            "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet và thử lại."
                        else -> 
                            "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.\n\nChi tiết: ${errorMessage.take(100)}"
                    }
                    
                    val errorChatMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sender = Sender.BOT,
                        text = friendlyErrorMessage,
                        time = getCurrentTime(),
                        isSent = true
                    )

                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + errorChatMessage,
                        isLoading = false,
                        error = errorMessage
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Exception sending message: ${e.message}", e)
                
                val errorMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = Sender.BOT,
                    text = "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.",
                    time = getCurrentTime(),
                    isSent = true
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMessage,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

