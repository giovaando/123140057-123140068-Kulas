package com.example.raillog

import android.app.Application
import com.example.raillog.core.di.androidModule
import com.example.raillog.core.di.initKoin
import com.example.raillog.core.util.CriticalItemsNotifier
import com.example.raillog.core.util.NotificationHelper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext

/**
 * Lokasi: androidMain/kotlin/com/example/raillog/RaillogApplication.kt
 *
 * Koin 4.x menghapus org.koin.android.ext.koin.get().
 * Gantinya pakai GlobalContext.get().get<T>() yang tersedia di koin-core
 * dan bekerja di semua versi Koin 3.x maupun 4.x.
 */
class RaillogApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Inisialisasi Koin DI
        initKoin(
            platformModules = listOf(androidModule)
        ) {
            androidLogger()
            androidContext(this@RaillogApplication)
        }

        // 2. Buat notification channels (wajib untuk Android 8+)
        NotificationHelper.createChannels(this)

        // 3. Ambil CriticalItemsNotifier via GlobalContext setelah Koin siap
        GlobalContext.get().get<CriticalItemsNotifier>().start()
    }
}