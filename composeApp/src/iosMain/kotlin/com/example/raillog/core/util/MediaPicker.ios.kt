package com.example.raillog.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberMediaPicker(onMediaPicked: (String) -> Unit): MediaPicker {
    return remember {
        object : MediaPicker {
            override fun launchCamera() { onMediaPicked("iOS_Camera_Simulated.jpg") }
            override fun launchGallery() { onMediaPicked("iOS_Gallery_Simulated.pdf") }
        }
    }
}