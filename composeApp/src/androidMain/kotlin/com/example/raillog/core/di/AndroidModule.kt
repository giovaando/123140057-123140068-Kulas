package com.example.raillog.core.di

import com.example.raillog.core.util.CriticalItemsNotifier
import com.example.raillog.core.util.DatabaseDriverFactory
import com.example.raillog.data.local.datastore.DataStoreFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific Koin module.
 *
 * Menyediakan dependencies yang membutuhkan `Context`:
 * - DatabaseDriverFactory : untuk SQLDelight driver
 * - DataStoreFactory      : untuk lokasi file preferences
 * - CriticalItemsNotifier : untuk notifikasi komponen kritis
 *
 * Catatan: GetCriticalItemsUseCase didaftarkan di useCaseModule (AppModule.kt)
 * agar tetap di shared module dan tidak duplikat.
 *
 * Lokasi: androidMain/core/di/AndroidModule.kt
 */
val androidModule = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { DataStoreFactory(androidContext()) }
    single { CriticalItemsNotifier(androidContext(), get()) }
}