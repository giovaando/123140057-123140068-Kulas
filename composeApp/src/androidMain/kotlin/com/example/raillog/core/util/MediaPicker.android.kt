package com.example.raillog.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream

@Composable
actual fun rememberMediaPicker(onMediaPicked: (fileName: String, base64Data: String?) -> Unit): MediaPicker {
    val context = LocalContext.current

    // 📸 1. Menangkap Gambar dari Kamera Asli HP
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            onMediaPicked("Camera_Scan_${System.currentTimeMillis()}.jpg", base64String)
        }
    }

    // 📁 2. Memilih File dari Galeri (GAMBAR & PDF)
    // Menggunakan OpenDocument agar bisa memilih multiple MIME types
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                val mimeType = context.contentResolver.getType(uri)
                val outputStream = ByteArrayOutputStream()

                if (mimeType == "application/pdf") {
                    // --- LOGIKA UNTUK PDF ---
                    // Membaca file PDF dan mengubah Halaman 1 menjadi Gambar
                    val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                    if (fileDescriptor != null) {
                        val renderer = PdfRenderer(fileDescriptor)
                        val page = renderer.openPage(0) // Ambil halaman pertama (index 0)

                        // Render PDF ke Bitmap dengan latar belakang putih agar jelas dibaca AI
                        val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                        // Kompres hasil gambar dari PDF
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

                        page.close()
                        renderer.close()
                        fileDescriptor.close()

                        val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                        onMediaPicked("Document_${System.currentTimeMillis()}.pdf", base64String)
                    }
                } else {
                    // --- LOGIKA UNTUK GAMBAR (JPG/PNG) ---
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    // KOMPRESI PINTAR: Perkecil gambar jika dari galeri terlalu besar (misal 5MB+)
                    // Ini memastikan Google Vision tidak menolak gambar karena kebesaran
                    val ratio = 1024.0f / maxOf(bitmap.width, bitmap.height)
                    val scaledBitmap = if (ratio < 1.0f) {
                        Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
                    } else {
                        bitmap
                    }

                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

                    onMediaPicked("Gallery_${System.currentTimeMillis()}.jpg", base64String)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onMediaPicked("Error_File.jpg", null)
            }
        }
    }

    return remember {
        object : MediaPicker {
            override fun launchCamera() { cameraLauncher.launch(null) }
            override fun launchGallery() {
                // Mengizinkan file Gambar DAN PDF bisa diklik di File Manager
                galleryLauncher.launch(arrayOf("image/*", "application/pdf"))
            }
        }
    }
}