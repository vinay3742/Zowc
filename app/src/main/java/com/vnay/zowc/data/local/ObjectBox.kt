package com.vnay.zowc.data.local

import android.content.Context
import com.vnay.zowc.data.entity.DocumentChunk
import com.vnay.zowc.data.entity.MyObjectBox
import io.objectbox.Box
import io.objectbox.BoxStore


object ObjectBox {
    lateinit var store: BoxStore
        private set

    fun init(context: Context) {
        if (::store.isInitialized && !store.isClosed) return

        store = MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
    }
    // Convenience accessor for the DocumentChunk Box
    val documentChunkBox: Box<DocumentChunk>
        get() = store.boxFor(DocumentChunk::class.java)
}