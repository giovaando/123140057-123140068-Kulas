package com.example.raillog.core.util

import androidx.compose.runtime.Composable

/**
 * expect declaration untuk request notifikasi permission.
 *
 * - Android 13+  : memunculkan dialog izin POST_NOTIFICATIONS
 * - Android < 13 : tidak melakukan apa-apa (permission otomatis granted)
 * - iOS          : tidak melakukan apa-apa (handling berbeda, bukan scope Sprint 3)
 *
 * Lokasi file ini: commonMain/kotlin/com/example/raillog/core/util/NotificationPermission.kt
 */
@Composable
expect fun RequestNotificationPermission()