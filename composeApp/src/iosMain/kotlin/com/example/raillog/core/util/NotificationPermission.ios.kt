package com.example.raillog.core.util

import androidx.compose.runtime.Composable

/**
 * actual implementation untuk iOS.
 * Notifikasi iOS ditangani secara berbeda (UNUserNotificationCenter),
 * bukan scope Sprint 3 — dikosongkan agar build iOS tetap sukses.
 *
 * Lokasi file ini: iosMain/kotlin/com/example/raillog/core/util/NotificationPermission.ios.kt
 */
@Composable
actual fun RequestNotificationPermission() {
    // No-op untuk iOS
}