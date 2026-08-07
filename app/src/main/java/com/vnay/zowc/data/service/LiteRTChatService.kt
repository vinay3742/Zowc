package com.vnay.zowc.data.service

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.vnay.zowc.domain.ChatService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class LiteRTChatService(private val context: Context) : ChatService {

    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private val modelName = "gemma3-1b-it-int4.litertlm"

    override suspend fun initialize() {
        withContext(Dispatchers.IO) {
            val modelFile = File(context.filesDir, modelName)
            if (!modelFile.exists()) {
                copyModelFromAssets(modelFile)
            }

            val config = EngineConfig(
                modelPath = modelFile.absolutePath,
                backend = Backend.CPU()
            )
            val newEngine = Engine(config)
            newEngine.initialize()
            engine = newEngine
            conversation = newEngine.createConversation()
        }
    }

    private fun copyModelFromAssets(destFile: File) {
        context.assets.open("models/$modelName").use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    override fun sendMessage(text: String): Flow<String> {
        val currentConversation = conversation ?: throw IllegalStateException("ChatService not initialized")

        return currentConversation.sendMessageAsync(text)
            .map { it.toString() }
            .onCompletion { cause ->
                // If generation was canceled by user, safely recreate conversation to unstick C++ engine state
                if (cause is CancellationException) {
                    resetConversation()
                }
            }
            .catch { cause ->
                resetConversation()
                throw cause
            }
            .flowOn(Dispatchers.IO)
    }

    private fun resetConversation() {
        try {
            conversation?.close()
        } catch (_: Exception) { }
        conversation = engine?.createConversation()
    }

    override fun close() {
        conversation?.close()
        engine?.close()
        conversation = null
        engine = null
    }
}