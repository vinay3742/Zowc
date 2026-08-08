package com.vnay.zowc.domain.service

import android.net.Uri

interface TextRecognizerService {
    suspend fun extractTextFromImage(imageUri: Uri): Result<String>
    fun close()
}