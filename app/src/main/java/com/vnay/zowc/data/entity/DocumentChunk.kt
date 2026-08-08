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
    // Note: Dimensions match universal_sentence_encoder.tflite output size
    @HnswIndex(
        dimensions = 512,
        distanceType = VectorDistanceType.COSINE,
    )
    var embedding: FloatArray? = null
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentChunk

        if (id != other.id) return false
        if (documentName != other.documentName) return false
        if (text != other.text) return false
        if (embedding != null){
            if (other.embedding == null) return false
            if (!embedding.contentEquals(other.embedding)) return false
        } else if (other.embedding != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + documentName.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + (embedding?.contentHashCode() ?: 0)
        return result
    }
}