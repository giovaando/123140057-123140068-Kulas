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
actual fun rememberMediaPicker(
    onMediaPicked: (fileName: String, base64Data: String?) -> Unit
): MediaPicker {
    val context = LocalContext.current

    // Kamera tetap pakai TakePicturePreview dulu
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        println("====== [KAMERA] Callback dipanggil, bitmap null? ${bitmap == null} ======")
        if (bitmap != null) {
            println("====== [KAMERA] Ukuran bitmap: ${bitmap.width}x${bitmap.height} ======")
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val base64String = Base64.encodeToString(
                outputStream.toByteArray(), Base64.NO_WRAP
            )
            println("====== [KAMERA] Ukuran base64: ${base64String.length} karakter ======")
            onMediaPicked("Camera_${System.currentTimeMillis()}.jpg", base64String)
        } else {
            println("====== [KAMERA] Bitmap null / dibatalkan ======")
            onMediaPicked("Error_Camera.jpg", null)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        println("====== [GALERI] Callback dipanggil, uri null? ${uri == null} ======")
        if (uri != null) {
            try {
                val mimeType = context.contentResolver.getType(uri)
                println("====== [GALERI] MIME type: $mimeType ======")
                val outputStream = ByteArrayOutputStream()

                if (mimeType == "application/pdf") {
                    val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                    if (fileDescriptor != null) {
                        val renderer = PdfRenderer(fileDescriptor)
                        val page = renderer.openPage(0)
                        val bitmap = Bitmap.createBitmap(
                            page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888
                        )
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                        page.close()
                        renderer.close()
                        fileDescriptor.close()

                        val base64String = Base64.encodeToString(
                            outputStream.toByteArray(), Base64.NO_WRAP
                        )
                        println("====== [GALERI PDF] base64: ${base64String.length} karakter ======")
                        onMediaPicked("Document_${System.currentTimeMillis()}.pdf", base64String)
                    }
                } else {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    if (bitmap == null) {
                        println("====== [GALERI] ERROR: Bitmap null setelah decode ======")
                        onMediaPicked("Error_File.jpg", null)
                        return@rememberLauncherForActivityResult
                    }

                    println("====== [GALERI IMG] Ukuran asli: ${bitmap.width}x${bitmap.height} ======")

                    val maxSize = 1024
                    val ratio = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
                    val scaledBitmap = if (ratio < 1.0f) {
                        Bitmap.createScaledBitmap(
                            bitmap,
                            (bitmap.width * ratio).toInt(),
                            (bitmap.height * ratio).toInt(),
                            true
                        )
                    } else {
                        bitmap
                    }

                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                    val base64String = Base64.encodeToString(
                        outputStream.toByteArray(), Base64.NO_WRAP
                    )

                    println("====== [GALERI IMG] Ukuran scaled: ${scaledBitmap.width}x${scaledBitmap.height} ======")
                    println("====== [GALERI IMG] base64: ${base64String.length} karakter ======")

                    onMediaPicked("Gallery_${System.currentTimeMillis()}.jpg", base64String)
                }
            } catch (e: Exception) {
                println("====== [GALERI] ERROR: ${e::class.simpleName} - ${e.message} ======")
                e.printStackTrace()
                onMediaPicked("Error_File.jpg", null)
            }
        } else {
            println("====== [GALERI] URI null / dibatalkan ======")
        }
    }

    return remember {
        object : MediaPicker {
            override fun launchCamera() {
                println("====== [MEDIAPICKER] launchCamera() dipanggil ======")
                cameraLauncher.launch(null)
            }
            override fun launchGallery() {
                println("====== [MEDIAPICKER] launchGallery() dipanggil ======")
                galleryLauncher.launch(arrayOf("image/*", "application/pdf"))
            }
        }
    }
}