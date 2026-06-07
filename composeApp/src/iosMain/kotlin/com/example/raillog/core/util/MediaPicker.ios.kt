package com.example.raillog.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberMediaPicker(onMediaPicked: (fileName: String, base64Data: String?) -> Unit): MediaPicker {
    return remember {
        object : MediaPicker {
            override fun launchCamera() {
                // TODO: Implementasi kamera khusus iOS (sementara dikosongkan agar build sukses)
                println("Kamera iOS belum diimplementasi")
            }

            override fun launchGallery() {
                // TODO: Implementasi galeri khusus iOS (sementara dikosongkan agar build sukses)
                println("Galeri iOS belum diimplementasi")
            }
        }
    }
}