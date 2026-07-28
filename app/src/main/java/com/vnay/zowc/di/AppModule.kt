package com.vnay.zowc.di

import com.vnay.zowc.data.local.ObjectBox
import com.vnay.zowc.data.repository.DocumentRepositoryImpl
import com.vnay.zowc.data.service.LiteRTChatService
import com.vnay.zowc.domain.repository.DocumentRepository
import com.vnay.zowc.domain.ChatService
import com.vnay.zowc.ui.ChatViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {
    // 1. Singleton for ObjectBox store or helper
    single { ObjectBox }

    // 2. ChatService instance (uses androidContext() for LiteRTChatService)
    single<ChatService>{ LiteRTChatService(androidContext()) }

    // 3. DocumentRepository binding
    single<DocumentRepository>{ DocumentRepositoryImpl() }

    // 4. ViewModel (Injects both ChatService and DocumentRepository)
    viewModel{
        ChatViewModel(chatService = get(), documentRepository = get())
    }
}