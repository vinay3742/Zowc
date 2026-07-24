package com.vnay.zowc.data.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id
import io.objectbox.annotation.VectorDistanceType

@Entity
data class DocumentChunk(
    @Id
    var id: Long = 0,

    var documentName: String = "",

    var text: String = "",

    // HNSW index for vector similarity search
    // Note: Set 'dimensions' to match embedding output size (e.g. 384 for MobileBERT/MiniLM models)
    @HnswIndex(
        dimensions = 384,
        distanceType = VectorDistanceType.COSINE
    )
    var embedding: FloatArray? = null
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DocumentChunk
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}