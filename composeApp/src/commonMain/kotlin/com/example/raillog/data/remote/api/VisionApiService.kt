package com.example.raillog.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
            val requestBody = VisionRequest(
                requests = listOf(
                    AnnotateImageRequest(
                        image = VisionImage(content = base64Image),
                        features = listOf(VisionFeature())
                    )
                )
            )

            val response = client.post("https://vision.googleapis.com/v1/images:annotate?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            // ========================================================
            // ALAT SADAP SUPER: Membaca file mentah (RAW JSON) dari Google
            // ========================================================
            val responseString = response.bodyAsText()
            println("====== [DETEKTIF] RAW JSON DARI GOOGLE ======")
            println(responseString)
            println("=============================================")

            if (response.status.value !in 200..299) {
                throw Exception("Google API Error ${response.status.value}: $responseString")
            }

            // Mapping JSON mentah ke dalam objek Kotlin
            val jsonParser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val visionResponse = jsonParser.decodeFromString<VisionResponse>(responseString)

            visionResponse.responses?.firstOrNull()?.fullTextAnnotation?.text ?: ""

        } catch (e: Exception) {
            println("====== [DETEKTIF] ERROR DI VISION SERVICE ======")
            println("Pesan: ${e.message}")
            throw e
        }
    }
}