package com.vnay.zowc.data.service

import android.content.Context
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.vnay.zowc.domain.service.EmbeddingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class EmbeddingService(
    private val context: Context,
    private val modelPath: String = "universal_sentence_encoder.tflite"
) : EmbeddingService {

    private var textEmbedder: TextEmbedder? = null

    private fun getOrInitEmbedder(): TextEmbedder{
        return textEmbedder ?: synchronized(this){
            textEmbedder ?: run {
                val baseOptions = BaseOptions.builder()
                    .setModelAssetPath(modelPath)
                    .build()

                val options = TextEmbedder.TextEmbedderOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setL2Normalize(true)
                    .build()

                TextEmbedder.createFromOptions(context, options).also {
                    textEmbedder = it
                }
            }
        }
    }

    override suspend fun generateEmbedding(text: String): Result<FloatArray> = withContext(
        Dispatchers.IO) {
        runCatching{
            val embedder = getOrInitEmbedder()
            val result = embedder.embed(text)

            result.embeddingResult().embeddings().firstOrNull()?.floatEmbedding()
                ?: throw  IllegalStateException("Failed to extract embedding vector")
        }
    }

    override suspend fun generateEmbeddings(texts: List<String>): Result<List<FloatArray>> = withContext(
        Dispatchers.IO) {
        runCatching {
            texts.map { text ->
                generateEmbedding(text).getOrThrow()
            }
        }
    }

    override fun calculatesSimilarity(vectorA: FloatArray, vectorB: FloatArray): Float {
        require(vectorA.size == vectorB.size) { "Vectors must have identical dimensions" }
        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f
        for ( i in vectorA.indices){
            dotProduct += vectorA[i] * vectorB[i]
            normA += vectorA[i] * vectorA[i]
            normB += vectorB[i] * vectorB[i]
        }
        val denominator = sqrt(normA) * sqrt(normB)
        return if (denominator > 0) dotProduct / denominator else 0f
    }

    override fun close() {
        textEmbedder?.close()
        textEmbedder = null
    }
}