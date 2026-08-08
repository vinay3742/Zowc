package com.vnay.zowc.domain.service

interface EmbeddingService {
    suspend fun generateEmbedding(text: String): Result<FloatArray>
    suspend fun generateEmbeddings(texts: List<String>): Result<List<FloatArray>>
    fun calculatesSimilarity(vectorA: FloatArray, vectorB: FloatArray): Float
    fun close()
}