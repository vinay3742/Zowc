package com.vnay.zowc.ui

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vnay.zowc.domain.model.ChatMessage
import com.vnay.zowc.domain.ChatService
import com.vnay.zowc.domain.repository.DocumentRepository
import com.vnay.zowc.domain.service.TextRecognizerService
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatService: ChatService,
    private val documentRepository: DocumentRepository,
    private val textRecognizerService: TextRecognizerService
) : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    private val _inputText = mutableStateOf("")
    val inputText: State<String> = _inputText

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _initializationError = mutableStateOf<String?>(null)
    val initializationError: State<String?> = _initializationError

    var isProcessingDocument by mutableStateOf(false)
        private set

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

            // 1. Fetch matching vector chunks from ObjectBox
            val matchingChunks = documentRepository.searchSimilarChunks(text, maxResult = 3)

            // 2. Wrap prompt with context if found
            val promptWithContext = if (matchingChunks.isNotEmpty()) {
                val contextText = matchingChunks.joinToString("\n---\n") { it.text }
                """
                Context information:
                $contextText

                Question: $text
                Answer using the provided context if relevant.
                """.trimIndent()
            } else {
                text
            }

            // 3. Send structured prompt using your exact collection strategy
            chatService.sendMessage(promptWithContext)
                .onStart {
                    _isLoading.value = true
                }
                .onCompletion {
                    _isLoading.value = false
                    if (fullText.isNotBlank()) {
                        _messages.add(ChatMessage(text = fullText, isUser = false))
                    }
                }
                .collect { chunk ->
                    fullText += chunk
                }
        }
    }

    fun processSelectedImage(uri: Uri) {
        viewModelScope.launch {
            isProcessingDocument = true

            val result = textRecognizerService.extractTextFromImage(uri)

            result.onSuccess { extractedText ->
                if (extractedText.isNotBlank()) {
                    val docName = "Doc_${System.currentTimeMillis()}"

                    documentRepository.addDocument(
                        name = docName,
                        content = extractedText
                    )
                }
            }.onFailure { error ->
                // Handle error
            }
            isProcessingDocument = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatService.close()
    }
}