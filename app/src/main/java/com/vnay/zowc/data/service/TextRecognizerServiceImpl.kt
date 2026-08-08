package com.vnay.zowc.data.service

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.vnay.zowc.domain.service.TextRecognizerService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TextRecognizerServiceImpl(private val context: Context) : TextRecognizerService {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extractTextFromImage(imageUri: Uri): Result<String>  =
        suspendCancellableCoroutine{ continuation ->
            try{
                val inputImage = InputImage.fromFilePath(context, imageUri)

                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(Result.success(visionText.text))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            } catch (e: Exception){
                continuation.resume(Result.failure(e))
            }
        }

    override fun close() {
        recognizer.close()
    }
}