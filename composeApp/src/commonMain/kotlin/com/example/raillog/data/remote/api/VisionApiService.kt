package com.example.raillog.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

// --- DTO Request ke Google ---
@Serializable
data class VisionRequest(val requests: List<AnnotateImageRequest>)

@Serializable
data class AnnotateImageRequest(
    val image: VisionImage,
    val features: List<VisionFeature>
)

@Serializable
data class VisionImage(val content: String)

@Serializable
data class VisionFeature(val type: String = "DOCUMENT_TEXT_DETECTION")

// --- DTO Response dari Google ---
@Serializable
data class VisionResponse(val responses: List<AnnotateImageResponse>? = null)

@Serializable
data class AnnotateImageResponse(val fullTextAnnotation: TextAnnotation? = null)

@Serializable
data class TextAnnotation(val text: String)

// --- Service Class ---
class VisionApiService(private val client: HttpClient) {
    suspend fun extractTextFromImage(base64Image: String, apiKey: String): String {
        return try {
            println("====== [DETEKTIF] MEMULAI VISION API CALL ======")
            println("Panjang Base64 : ${base64Image.length} karakter")
            println("API Key (5 char pertama): ${apiKey.take(5)}...")

            val requestBody = VisionRequest(
                requests = listOf(
                    AnnotateImageRequest(
                        image = VisionImage(content = base64Image),
                        features = listOf(VisionFeature())
                    )
                )
            )

            println("====== [DETEKTIF] MENGIRIM KE GOOGLE VISION API ======")

            val response = client.post(
                "https://vision.googleapis.com/v1/images:annotate?key=$apiKey"
            ) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            println("====== [DETEKTIF] HTTP STATUS: ${response.status.value} ${response.status.description} ======")

            val responseString = response.bodyAsText()

            println("====== [DETEKTIF] RAW JSON DARI GOOGLE ======")
            println(responseString)
            println("=============================================")

            if (response.status.value !in 200..299) {
                throw Exception("Google API Error ${response.status.value}: $responseString")
            }

            val jsonParser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val visionResponse = jsonParser.decodeFromString<VisionResponse>(responseString)

            val extractedText = visionResponse.responses
                ?.firstOrNull()
                ?.fullTextAnnotation
                ?.text ?: ""

            println("====== [DETEKTIF] HASIL EKSTRAKSI TEKS ======")
            println(
                if (extractedText.isBlank())
                    "(kosong - tidak ada teks terdeteksi)"
                else
                    extractedText
            )
            println("=============================================")

            extractedText

        } catch (e: Exception) {
            println("====== [DETEKTIF] ERROR DI VISION SERVICE ======")
            println("Tipe Error : ${e::class.simpleName}")
            println("Pesan      : ${e.message}")
            println("Cause      : ${e.cause}")
            e.printStackTrace()
            println("================================================")
            throw e
        }
    }
}