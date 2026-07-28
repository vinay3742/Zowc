package com.vnay.zowc.domain.repository

import com.vnay.zowc.data.entity.DocumentChunk

interface DocumentRepository {
    suspend fun addDocument(name: String, content: String)
    suspend fun searchSimilarChunks(query: String, maxResult: Int = 3): List<DocumentChunk>
}