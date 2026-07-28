package com.vnay.zowc.data.repository

import com.vnay.zowc.data.entity.DocumentChunk
import com.vnay.zowc.data.entity.DocumentChunk_
import com.vnay.zowc.data.local.ObjectBox
import com.vnay.zowc.domain.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentRepositoryImpl : DocumentRepository{
    private val box = ObjectBox.documentChunkBox

    override suspend fun addDocument(name: String, content: String) = withContext(Dispatchers.IO) {
        // 1. Split content into readable chunks
        val textChunks = content.chunked(300)

        val entityList = textChunks.map { chunkText ->
            // 2. Generate embedding float array (placeholder until embedding model service is wired)
            val dummyEmbedding = FloatArray(384) { 0f }

            DocumentChunk(
                documentName = name,
                text = chunkText,
                embedding = dummyEmbedding
            )
        }

        // 3. Save to ObjectBox
        box.put(entityList)
    }

    override suspend fun searchSimilarChunks(query: String, maxResult: Int): List<DocumentChunk> = withContext(Dispatchers.IO) {
        val queryVector = FloatArray(384) { 0f } // Query embedding vector

        // Query ObjectBox HNSW Index for nearest neighbor matches
        box.query()
            .nearestNeighbors(DocumentChunk_.embedding, queryVector, maxResult)
            .build()
            .find()
    }
}