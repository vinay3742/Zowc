package com.vnay.zowc.domain.service

interface SpeechService {
    fun startListening(
        onPartialResults: (String) -> Unit,
        onResults: (String) -> Unit,
        onError: (String) -> Unit
    )
    fun stopListening()
    fun isAvailable(): Boolean
}
