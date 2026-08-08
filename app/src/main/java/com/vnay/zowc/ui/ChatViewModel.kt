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
import com.vnay.zowc.domain.service.SpeechService
import com.vnay.zowc.domain.service.TextRecognizerService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatService: ChatService,
    private val documentRepository: DocumentRepository,
    private val textRecognizerService: TextRecognizerService,
    private val speechService: SpeechService
) : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    private val _inputText = mutableStateOf("")
    val inputText: State<String> = _inputText

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isListening = mutableStateOf(false)
    val isListening: State<Boolean> = _isListening

    private val _initializationError = mutableStateOf<String?>(null)
    val initializationError: State<String?> = _initializationError

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    var isProcessingDocument by mutableStateOf(false)
        private set

    private var generationJob: Job? = null

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

        generationJob = viewModelScope.launch {
            var fullText = ""

            try {
                _isLoading.value = true

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

                // 3. Send structured prompt and stream updates to UI with throttling
                var lastUpdateTimestamp = 0L
                val throttleInterval = 60L // Update UI every 60ms (~16 FPS)

                chatService.sendMessage(promptWithContext)
                    .onCompletion {
                        // Ensure the final state is always pushed to UI
                        updateLastAiMessage(fullText)
                    }
                    .collect { chunk ->
                        fullText += chunk
                        
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastUpdateTimestamp >= throttleInterval) {
                            updateLastAiMessage(fullText)
                            lastUpdateTimestamp = currentTime
                        }
                    }
            } catch (e: CancellationException) {
                // Triggered when stop button is pressed
            } catch (e: Exception) {
                // Prevent crashes from engine errors
                val errorMsg = if (fullText.isBlank()) "Error: ${e.localizedMessage}" else "$fullText\n\n[Error: ${e.localizedMessage}]"
                updateLastAiMessage(errorMsg)
            } finally {
                _isLoading.value = false
                generationJob = null
            }
        }
    }

    private fun updateLastAiMessage(text: String) {
        val lastMessage = _messages.lastOrNull()
        if (lastMessage != null && !lastMessage.isUser) {
            // Replace the last AI message with updated text
            _messages[_messages.size - 1] = lastMessage.copy(text = text)
        } else {
            // Add a new AI message if the last one was from user
            _messages.add(ChatMessage(text = text, isUser = false))
        }
    }

    fun stopGeneration() {
        generationJob?.cancel()
        generationJob = null
        _isLoading.value = false
    }

    fun toggleVoiceInput() {
        if (_isListening.value) {
            speechService.stopListening()
            _isListening.value = false
        } else {
            _isListening.value = true
            speechService.startListening(
                onPartialResults = { partialText ->
                    _inputText.value = partialText
                },
                onResults = { finalResult ->
                    _inputText.value = finalResult
                    _isListening.value = false
                    sendMessage()
                },
                onError = { error ->
                    _isListening.value = false
                    viewModelScope.launch {
                        _uiEvent.emit(error)
                    }
                }
            )
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
        documentRepository.close()
        textRecognizerService.close()
        speechService.stopListening()
    }
}