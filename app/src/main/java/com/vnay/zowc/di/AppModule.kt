package com.vnay.zowc.di

import com.vnay.zowc.data.local.ObjectBox
import com.vnay.zowc.data.repository.DocumentRepositoryImpl
import com.vnay.zowc.data.service.LiteRTChatService
import com.vnay.zowc.data.service.TextRecognizerServiceImpl
import com.vnay.zowc.domain.repository.DocumentRepository
import com.vnay.zowc.domain.ChatService
import com.vnay.zowc.domain.service.TextRecognizerService
import com.vnay.zowc.ui.ChatViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {
    // 1. Singleton for ObjectBox store or helper
    single { ObjectBox }

    // 2. Services
    single<ChatService>{ LiteRTChatService(androidContext()) }
    single<TextRecognizerService>{ TextRecognizerServiceImpl(androidContext()) }

    // 3. Repository
    single<DocumentRepository>{ DocumentRepositoryImpl() }

    // 4. ViewModel (Injects both ChatService and DocumentRepository)
    viewModel{
        ChatViewModel(
            chatService = get(),
            documentRepository = get(),
            textRecognizerService = get()
        )
    }
}