package com.example.hotelbooking



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: GeminiViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText = rememberTextFieldState()
    val listState = rememberLazyListState()

    // เลื่อนไปข้อความล่าสุดทุกครั้งที่มีข้อความใหม่
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("ถาม Gemini") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // แสดงรายการข้อความทั้งหมด
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatBubble(message)
                }

                // แสดงตัวโหลดตอนที่รอคำตอบจาก Gemini
                if (uiState.isLoading) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }
                    }
                }

                uiState.errorMessage?.let { error ->
                    item {
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            HorizontalDivider() // แสดงเส้นคั่น

            // -------------- ส่วนพิมพ์คำถาม + ปุ่มส่ง --------------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    state = inputText,
                    modifier = Modifier.weight(1f),
                    label = { Text("พิมพ์คำถาม...") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    lineLimits = TextFieldLineLimits.SingleLine
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = !uiState.isLoading,
                    onClick = {
                        viewModel.sendPrompt(inputText.text.toString())
                        inputText.clearText()
                    }
                ) {
                    Text("ส่ง")
                }
            }
        }
    }
}

// แสดงข้อความแชท ผู้ใช้พิมพ์ข้อความอยู่ทางขวา, Gemini ข้อความอยู่ทางซ้าย
@Composable
private fun ChatBubble(message: ChatMessage) {
    val bubbleColor = if (message.isFromUser)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    val textColor = if (message.isFromUser)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(12.dp))
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Text(text = message.text, color = textColor)
        }
    }
}
