package com.example.hotelbooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// ChatMessage เก็บข้อความ 1 บทสนทนา
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean // true คือผู้ใช้พิมพ์, false คือคำตอบจาก Gemini
)

// เก็บประวัติการสนทนาทั้งหมด
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class GeminiViewModel : ViewModel() {

    // ✅ แก้ไขชื่อโมเดลเป็น "gemini-1.5-flash" (ประมวลผลเร็ว เสถียร ไม่ติด Quota ง่าย)
    private val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-3.8-flash")

    // startChat() สร้างเซสชันสนทนา เพื่อจำบริบทข้อความก่อนหน้า
    private val chat = generativeModel.startChat()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // ส่งข้อความให้ Gemini
    fun sendPrompt(prompt: String) {
        if (prompt.isBlank()) return

        // 1. เพิ่มข้อความฝั่งผู้ใช้เข้า UI ทันที
        val userMessage = ChatMessage(text = prompt, isFromUser = true)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                // 2. ส่งคำถามไปยัง Gemini Chat
                val response = chat.sendMessage(content { text(prompt) })
                val modelMessage = ChatMessage(
                    text = response.text ?: "(ไม่มีคำตอบจากระบบ)",
                    isFromUser = false
                )

                // 3. แสดงคำตอบจาก Gemini
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + modelMessage,
                    isLoading = false
                )
            } catch (e: Exception) {
                // 4. แสดงข้อผิดพลาดหากเรียก API ไม่สำเร็จ
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "เกิดข้อผิดพลาด: ${e.localizedMessage ?: e.message}"
                )
            }
        }
    }
}