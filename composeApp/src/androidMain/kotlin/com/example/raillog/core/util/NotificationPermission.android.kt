package com.example.raillog.core.util

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * actual implementation untuk Android.
 *
 * PENTING: File lama RequestNotificationPermission.kt di androidMain/core/util/
 * HARUS DIHAPUS sebelum menggunakan file ini, karena keduanya mendefinisikan
 * fungsi dengan nama yang sama dan akan menyebabkan konflik.
 *
 * Lokasi file ini:
 * androidMain/kotlin/com/example/raillog/core/util/NotificationPermission.android.kt
 */
@Composable
actual fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* granted atau tidak, lanjut saja */ }

        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}