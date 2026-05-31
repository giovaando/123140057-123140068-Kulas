package com.example.raillog.core.util

import androidx.compose.runtime.Composable

interface MediaPicker {
    fun launchCamera()
    fun launchGallery()
}

@Composable
expect fun rememberMediaPicker(onMediaPicked: (String) -> Unit): MediaPicker