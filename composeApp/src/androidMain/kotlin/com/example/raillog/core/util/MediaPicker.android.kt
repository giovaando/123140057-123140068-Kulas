package com.example.raillog.core.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberMediaPicker(onMediaPicked: (String) -> Unit): MediaPicker {
    // Memanggil Kamera Asli HP Android
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) onMediaPicked("Camera_Image_${System.currentTimeMillis()}.jpg")
    }

    // Memanggil File Manager / Galeri Asli HP Android
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onMediaPicked("Document_${System.currentTimeMillis()}.pdf")
    }

    return remember {
        object : MediaPicker {
            override fun launchCamera() { cameraLauncher.launch(null) }
            override fun launchGallery() { galleryLauncher.launch("*/*") }
        }
    }
}