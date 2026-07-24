package com.vnay.zowc.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vnay.zowc.domain.model.ChatMessage
import com.vnay.zowc.domain.ChatService
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ChatViewModel(private val chatService: ChatService) : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    private val _inputText = mutableStateOf("")
    val inputText: State<String> = _inputText

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _initializationError = mutableStateOf<String?>(null)
    val initializationError: State<String?> = _initializationError

    init {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                chatService.initialize()
                _initializationError.value = null
            } catch (e: Exception) {
                _initializationError.value = e.localizedMessage ?: "Initialization Failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onInputTextChange(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        _messages.add(ChatMessage(text = text, isUser = true))
        _inputText.value = ""

        viewModelScope.launch {
            var fullText = ""

            chatService.sendMessage(text)
                .onStart {
                    _isLoading.value = true
                }
                .onCompletion {
                    _isLoading.value = false
                    // 1. Wait until the stream is completely finished
                    // 2. Wrap the accumulated text into a single ChatMessage
                    // 3. Push it to the list once, triggering exactly one UI update
                    if (fullText.isNotBlank()){
                        _messages.add(ChatMessage(text = fullText, isUser = false))
                    }
                }
                .collect { chunk ->
                    // Silently gather the tokens in the background
                    fullText += chunk
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatService.close()
    }
}
