package com.vnay.zowc.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vnay.zowc.domain.model.ChatMessage
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val messages = viewModel.messages
    val inputText = viewModel.inputText.value
    val isLoading = viewModel.isLoading.value
    val listState = rememberLazyListState()

    val backgroundGradient = remember {
        Brush.verticalGradient(
            listOf(
                Color.White,
                Color.White,
                Color.DarkGray.copy(alpha = 0.4f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (messages.isEmpty()) {
                    WelcomeScreen(modifier = Modifier.weight(1f))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        state = listState
                    ) {
                        items(
                            items = messages,
                            key = { it.id }
                        ) { message ->
                            ChatBubble(message)
                        }
                        if (isLoading && (messages.isEmpty() || messages.last().isUser)) {
                            item {
                                TypingIndicator()
                            }
                        }
                    }
                }

                // Inside ChatScreen.kt (inside Column, above ChatInput or top of screen)
                val isProcessingDocument = viewModel.isProcessingDocument

                if (isProcessingDocument) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Processing document and generating vectors...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                ChatInput(
                    text = inputText,
                    onTextChange = viewModel::onInputTextChange,
                    onSend = viewModel::sendMessage,
                    onStop = viewModel::stopGeneration,
                    onImageSelected ={ uri -> viewModel.processSelectedImage(uri)},
                    isLoading = isLoading,
                    enabled = !isLoading
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Multi-color Diamond/Sparkle
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = Color.Unspecified
            )
            // Simplified multi-color effect using overlaying icons or just a custom tint
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = Color.DarkGray
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(24.dp).padding(top = 4.dp),
                tint = Color.Gray
            )
            Spacer(Modifier.width(12.dp))
        }

        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(20.dp),
            color = if (isUser) Color(0xFFF0F2F5) else Color.Transparent
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = Color.Black.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 26.sp
                )
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(start = 36.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "zowc is thinking...",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    isLoading: Boolean,
    enabled: Boolean
) {
    // Image picker launcher
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ){uri: Uri? ->
//        uri?.let { onImageSelected(it) }
//    }

    TextField(
        value = text,
        onValueChange = onTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .navigationBarsPadding()
            .imePadding()
            .heightIn(min = 68.dp),
        placeholder = {
            Text(
                "Ask ZOWC",
                color = Color.Gray.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
            )
        },
        shape = RoundedCornerShape(34.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        enabled = enabled,
        singleLine = true,
//        leadingIcon = {
//            IconButton( // Image Picker Button
//                onClick = { imagePickerLauncher.launch("image/*") },
//                enabled = enabled
//            ) {
//                Icon(
//                    imageVector = Icons.Rounded.Add,
//                    contentDescription = "Add",
//                    modifier = Modifier.size(26.dp),
//                    tint = Color.Black.copy(alpha = 0.7f)
//                )
//            }
//        },
        trailingIcon = {
            Row(
                modifier = Modifier.padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isLoading) {
                    IconButton(onClick = onStop) {
                        Icon(
                            imageVector = Icons.Rounded.Stop,
                            contentDescription = "Stop",
                            tint = Color.Gray
                        )
                    }
                } else if (text.isNotBlank()) {
                    IconButton(onClick = onSend) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send",
                            tint = Color.Gray
                        )
                    }
                } else {
                    IconButton(onClick = { /* TODO: Mic */ }) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Voice",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black.copy(alpha = 0.7f)
                        )
                    }

                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = Color.Gray
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = "Visual",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    )
}
