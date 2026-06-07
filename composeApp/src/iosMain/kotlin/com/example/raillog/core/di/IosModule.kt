package com.example.raillog.core.di

import com.example.raillog.core.util.DatabaseDriverFactory
import com.example.raillog.core.util.IosNotificationService
import com.example.raillog.data.local.datastore.DataStoreFactory
import com.example.raillog.domain.repository.NotificationService
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * iOS-specific Koin module.
 *
 * Menyediakan dependencies platform yang dipakai di shared modules.
 */
val iosModule = module {
    single { DatabaseDriverFactory() }
    single { DataStoreFactory() }
    single { IosNotificationService() } bind NotificationService::class
}

/** Helper untuk dipanggil dari Swift code. */
fun initKoinIOS() {
    initKoin(platformModules = listOf(iosModule))
}
