package com.example.raillog.presentation.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raillog.domain.repository.AIRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==================== DATA MODELS ====================

enum class MessageRole { USER, ASSISTANT }

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val role: MessageRole,
    val content: String
)

data class AIAssistantUiState(
    val inputText: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val canSend: Boolean get() = inputText.isNotBlank() && !isLoading
}

// ==================== VIEWMODEL ====================

class AIAssistantViewModel(
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIAssistantUiState())
    val uiState: StateFlow<AIAssistantUiState> = _uiState.asStateFlow()

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text, error = null) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return
        sendMessageInternal(text)
    }

    fun sendQuickMessage(prompt: String) {
        sendMessageInternal(prompt)
    }

    private fun sendMessageInternal(text: String) {
        // Tambah pesan user ke list
        val userMessage = ChatMessage(role = MessageRole.USER, content = text)
        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                inputText = "",
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            // Pilih handler berdasarkan keyword
            val result = when {
                containsKeywords(text, "verifikasi", "dokumen", "verify", "document") ->
                    aiRepository.verifyDocument(text)

                containsKeywords(text, "ringkas", "rangkum", "ringkasan", "summarize", "laporan") ->
                    aiRepository.summarizeInspection(text)

                containsKeywords(text, "pengadaan", "saran", "procurement", "rekomendasi", "prioritas") ->
                    aiRepository.suggestProcurement(text)

                containsKeywords(text, "anomali", "deteksi", "anomaly", "detect", "mencurigakan") ->
                    aiRepository.detectAnomalies(text)

                else ->
                    // General chat — gunakan suggestProcurement dengan prompt general
                    aiRepository.suggestProcurement(
                        "Jawab pertanyaan umum berikut terkait logistik kereta api:\n$text"
                    )
            }

            result.fold(
                onSuccess = { responseText ->
                    val aiMessage = ChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = responseText
                    )
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + aiMessage,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    val errorMessage = ChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = "Maaf, terjadi kesalahan: ${error.message ?: "Unknown error"}. Pastikan API key sudah dikonfigurasi di local.properties."
                    )
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + errorMessage,
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun clearConversation() {
        _uiState.update {
            AIAssistantUiState()
        }
    }

    private fun containsKeywords(text: String, vararg keywords: String): Boolean {
        val lower = text.lowercase()
        return keywords.any { lower.contains(it) }
    }
}

// ==================== LEGACY ENUMS (backward compat) ====================

enum class AIAction(val displayName: String, val description: String) {
    SUMMARIZE("Ringkas", "Buat ringkasan dari teks"),
    GENERATE_IDEAS("Ide", "Generate ide berdasarkan topik"),
    IMPROVE_WRITING("Perbaiki", "Perbaiki tulisan"),
    TRANSLATE("Terjemah", "Terjemahkan ke bahasa lain"),
    SUGGEST_TITLE("Judul", "Sarankan judul"),
    CHAT("Tanya", "Tanya AI tentang apapun")
}

sealed interface AIAssistantEvent {
    data class CopyToClipboard(val text: String) : AIAssistantEvent
    data class ApplyToNote(val text: String) : AIAssistantEvent
}