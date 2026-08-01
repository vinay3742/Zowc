package com.vnay.zowc.data.repository

import com.vnay.zowc.data.entity.DocumentChunk
import com.vnay.zowc.data.entity.DocumentChunk_
import com.vnay.zowc.data.local.ObjectBox
import com.vnay.zowc.data.service.EmbeddingService
import com.vnay.zowc.domain.repository.DocumentRepository
import io.objectbox.Box
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentRepositoryImpl(
    private val box: Box<DocumentChunk>,
    private val embeddingService: EmbeddingService
) : DocumentRepository{
    override suspend fun addDocument(name: String, content: String) = withContext(Dispatchers.IO) {
        if (content.isBlank()) return@withContext

        // 1. Chunk the content
        val textChunks = content.chunked(300)

        // 2. Generate real embeddings
        val embeddingResult = embeddingService.generateEmbeddings(textChunks).getOrNull()

        if (embeddingResult != null){
            val entityList = textChunks.mapIndexed { index, chunkText ->
                DocumentChunk(
                    documentName = name,
                    text = chunkText,
                    embedding = embeddingResult[index]
                )
            }

            // 3. Save to ObjectBox
            box.put(entityList)
        }
    }

    override suspend fun searchSimilarChunks(query: String, maxResult: Int): List<DocumentChunk> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        // 1. Generate query embedding vector
        val queryVector = embeddingService.generateEmbedding(query).getOrNull()
            ?: return@withContext emptyList()

        // 2. Query ObjectBox HNSW for nearest neighbors
        box.query(DocumentChunk_.embedding.nearestNeighbors(queryVector, maxResult))
            .build()
            .find()
    }
}