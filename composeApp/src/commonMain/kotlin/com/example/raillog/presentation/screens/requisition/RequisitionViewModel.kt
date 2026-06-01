package com.example.raillog.presentation.screens.requisition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raillog.core.network.ApiConfig
import com.example.raillog.domain.model.PartCategory
import com.example.raillog.domain.model.Priority
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.domain.model.SupplyStatus
import com.example.raillog.domain.repository.SupplyRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlin.random.Random

// ==========================================
// STATE & UI MODELS
// ==========================================
@Serializable
data class CatalogItemUI(
    val id: String, val name: String, val category: String,
    val stock: Int, val isSafe: Boolean, val reqQty: Int = 0
)

@Serializable
data class RequisitionFormState(
    val hasScannedInitialDoc: Boolean = false,
    val isProcessingAI: Boolean = false,

    val requestorName: String = "",
    val employeeId: String = "",
    val department: String = "",
    val dateOfRequest: String = "",
    val projectType: String = "",
    val projectCode: String = "",
    val destinationSite: String = "",
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val catalogItems: List<CatalogItemUI> = listOf(
        CatalogItemUI("SLP-C-091", "Bantalan Beton Wika", "Infrastructure", 450, true),
        CatalogItemUI("RFL-R-054", "Rel Profile R54 (20m)", "Infrastructure", 12, false),
        CatalogItemUI("FST-E-102", "Penambat E-Clip", "Spare Parts", 5000, true),
        CatalogItemUI("BRG-992-A", "Heavy-Duty Steel Bearings", "Spare Parts", 240, true),
        CatalogItemUI("WRN-110-T", "Wrench Set Pro", "Tools", 15, true)
    ),
    val uploadedDocs: List<String> = emptyList(),
    val isSigned: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null
)

class RequisitionViewModel(private val repository: SupplyRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RequisitionFormState())
    val uiState: StateFlow<RequisitionFormState> = _uiState.asStateFlow()

    private var currentDraftId = "DRAFT_${Random.nextInt(100000, 999999)}"

    fun loadDraft(draftId: String, onStepLoaded: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val drafts = repository.getAllDrafts().firstOrNull()
                val targetDraft = drafts?.find { it.draftId == draftId }

                if (targetDraft != null) {
                    currentDraftId = draftId
                    val savedState = Json.decodeFromString<RequisitionFormState>(
                        targetDraft.formStateJson
                    )
                    _uiState.value = savedState
                    onStepLoaded(targetDraft.currentStep)
                }
            } catch (e: Exception) {
                println("Gagal memuat draft: ${e.message}")
            }
        }
    }

    // ==========================================
    // AI OCR LOGIC (POWERED BY GEMINI 2.0 FLASH)
    // ==========================================
    fun processInitialDocument(base64Image: String, fileName: String) {

        println("====== [VIEWMODEL] base64 diterima : ${base64Image.length} karakter ======")
        println("====== [VIEWMODEL] fileName         : $fileName ======")

        if (base64Image.isBlank()) {
            _uiState.update {
                it.copy(
                    isProcessingAI = false,
                    errorMessage = "Gambar kosong. Coba ambil foto ulang atau pilih file lain."
                )
            }
            return
        }

        _uiState.update { it.copy(isProcessingAI = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                println("====== [GEMINI] MEMULAI PROSES SCAN ======")

                val httpClient = HttpClient {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }

                // Prompt khusus agar Gemini membaca seperti OCR
                val prompt = """
                    Kamu adalah sistem OCR ahli. Ekstrak semua teks dari gambar dokumen Surat Jalan / SPK ini.
                    Kembalikan HANYA teks mentah persis seperti aslinya, pertahankan angka dan nama barang, tanpa penjelasan tambahan.
                """.trimIndent()

                // Merakit JSON Body menggunakan format standar KMP
                val requestBody = buildJsonObject {
                    putJsonArray("contents") {
                        addJsonObject {
                            putJsonArray("parts") {
                                addJsonObject { put("text", prompt) }
                                addJsonObject {
                                    putJsonObject("inline_data") {
                                        put("mime_type", "image/jpeg") // Sesuaikan jika PDF di-convert ke JPEG/PNG
                                        put("data", base64Image)
                                    }
                                }
                            }
                        }
                    }
                }

                println("====== [GEMINI] MENGIRIM KE GOOGLE GEMINI API ======")
                val response = httpClient.post(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${ApiConfig.geminiApiKey}"
                ) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

                val rawResponse = response.bodyAsText()
                httpClient.close()

                if (response.status.value !in 200..299) {
                    throw Exception("Gemini API Error ${response.status.value}: $rawResponse")
                }

                // Parsing hasil balasan dari Gemini
                val jsonParser = Json { ignoreUnknownKeys = true }
                val jsonResponse = jsonParser.parseToJsonElement(rawResponse).jsonObject

                val extractedText = jsonResponse["candidates"]
                    ?.jsonArray?.getOrNull(0)
                    ?.jsonObject?.get("content")
                    ?.jsonObject?.get("parts")
                    ?.jsonArray?.getOrNull(0)
                    ?.jsonObject?.get("text")
                    ?.jsonPrimitive?.content ?: ""

                println("====== [GEMINI] HASIL BACAAN TEKS ======")
                println(extractedText)
                println("========================================")

                if (extractedText.isBlank()) {
                    throw Exception("Gemini tidak mendeteksi teks dari dokumen ini.")
                }

                // REGEX PARSING MENGGUNAKAN HASIL GEMINI
                val qtyBantalan = Regex("Bantalan Beton Wika.*?(\\d+)")
                    .find(extractedText)?.groupValues?.get(1)?.toInt() ?: 0
                val qtyRel = Regex("Rel Profile R54.*?(\\d+)")
                    .find(extractedText)?.groupValues?.get(1)?.toInt() ?: 0
                val qtyPenambat = Regex("Penambat E-Clip.*?(\\d+)")
                    .find(extractedText)?.groupValues?.get(1)?.toInt() ?: 0
                val qtyBearings = Regex("Heavy-Duty Steel Bearings.*?(\\d+)")
                    .find(extractedText)?.groupValues?.get(1)?.toInt() ?: 0
                val qtyWrench = Regex("Wrench Set Pro.*?(\\d+)")
                    .find(extractedText)?.groupValues?.get(1)?.toInt() ?: 0

                val extractedProjectCode = Regex("Kode Proyek:?\\s*([A-Z0-9-]+)")
                    .find(extractedText)?.groupValues?.get(1) ?: ""
                val extractedSite = Regex("Tujuan:?\\s*([A-Za-z\\s]+)")
                    .find(extractedText)?.groupValues?.get(1)?.trim() ?: ""
                val extractedDate = Regex("Tanggal:?\\s*([0-9/\\-]+)")
                    .find(extractedText)?.groupValues?.get(1) ?: ""

                println("====== [GEMINI] HASIL EKSTRAKSI REGEX ======")
                println("Project Code : $extractedProjectCode")
                println("Tujuan       : $extractedSite")
                println("Tanggal      : $extractedDate")
                println("Bantalan     : $qtyBantalan")
                println("Rel          : $qtyRel")
                println("Penambat     : $qtyPenambat")
                println("============================================")

                // INJEKSI KE FORM UI STATE
                _uiState.update { state ->
                    val updatedCatalog = state.catalogItems.map { item ->
                        when (item.id) {
                            "SLP-C-091" -> item.copy(reqQty = qtyBantalan)
                            "RFL-R-054" -> item.copy(reqQty = qtyRel)
                            "FST-E-102" -> item.copy(reqQty = qtyPenambat)
                            "BRG-992-A" -> item.copy(reqQty = qtyBearings)
                            "WRN-110-T" -> item.copy(reqQty = qtyWrench)
                            else -> item
                        }
                    }

                    state.copy(
                        isProcessingAI = false,
                        hasScannedInitialDoc = true,
                        requestorName = "Giovan Lado",
                        employeeId = "RLN-123140068",
                        department = "Track Infrastructure",
                        dateOfRequest = extractedDate,
                        projectType = if (extractedProjectCode.contains("LRT")) "LRT" else "KRL",
                        projectCode = extractedProjectCode,
                        destinationSite = extractedSite,
                        catalogItems = updatedCatalog,
                        uploadedDocs = listOf(fileName)
                    )
                }

            } catch (e: Exception) {
                println("====== [GEMINI] ERROR ======")
                println("Pesan      : ${e.message}")
                e.printStackTrace()
                println("============================")

                _uiState.update {
                    it.copy(
                        isProcessingAI = false,
                        errorMessage = "Gagal memproses AI: ${e.message}"
                    )
                }
            }
        }
    }

    fun skipInitialScan() {
        _uiState.update { it.copy(hasScannedInitialDoc = true) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(requestorName = name) }
    }

    fun updateEmployeeId(id: String) {
        _uiState.update { it.copy(employeeId = id) }
    }

    fun updateDepartment(dept: String) {
        _uiState.update { it.copy(department = dept) }
    }

    fun updateDate(date: String) {
        _uiState.update { it.copy(dateOfRequest = date) }
    }

    fun updateProjectType(type: String) {
        _uiState.update { it.copy(projectType = type) }
    }

    fun updateProjectCode(code: String) {
        _uiState.update { it.copy(projectCode = code) }
    }

    fun updateDestinationSite(site: String) {
        _uiState.update { it.copy(destinationSite = site) }
    }

    fun updateCategoryFilter(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateItemQuantity(itemId: String, isAdd: Boolean) {
        _uiState.update { state ->
            val updatedCatalog = state.catalogItems.map { item ->
                if (item.id == itemId) {
                    val newQty = if (isAdd) item.reqQty + 1 else maxOf(0, item.reqQty - 1)
                    item.copy(reqQty = newQty)
                } else item
            }
            state.copy(catalogItems = updatedCatalog)
        }
    }

    fun addUploadedDocument(docName: String) {
        _uiState.update {
            val currentDocs = it.uploadedDocs.toMutableList()
            currentDocs.add(docName)
            it.copy(uploadedDocs = currentDocs)
        }
    }

    fun setSignedStatus(signed: Boolean) {
        _uiState.update { it.copy(isSigned = signed) }
    }

    fun saveDraftAutomatically(currentStep: Int) {
        val currentState = _uiState.value
        if (currentState.requestorName.isBlank() && currentState.projectCode.isBlank()) return

        viewModelScope.launch {
            try {
                val jsonString = Json.encodeToString(currentState)
                repository.saveDraft(
                    draftId = currentDraftId,
                    projectTitle = currentState.projectCode.ifBlank { "Untitled Draft" },
                    currentStep = currentStep,
                    lastUpdated = Clock.System.now().toEpochMilliseconds(),
                    formStateJson = jsonString
                )
            } catch (e: Exception) {
                println("Gagal menyimpan draft: ${e.message}")
            }
        }
    }

    fun submitRequisition() {
        val currentState = _uiState.value

        if (currentState.projectCode.isBlank() || currentState.requestorName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Mohon lengkapi Nama dan Project Code!") }
            return
        }
        if (!currentState.isSigned) {
            _uiState.update { it.copy(errorMessage = "Tanda tangan wajib dibubuhkan!") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                delay(1000)
                val requestedItems = currentState.catalogItems.filter { it.reqQty > 0 }
                val totalQuantity = if (requestedItems.isNotEmpty()) {
                    requestedItems.sumOf { it.reqQty }
                } else 1

                val dynamicName = if (requestedItems.isNotEmpty()) {
                    if (requestedItems.size > 1) {
                        "${requestedItems.first().name} & ${requestedItems.size - 1} lainnya"
                    } else {
                        requestedItems.first().name
                    }
                } else {
                    "Project: ${currentState.projectType} Requisition"
                }

                val newItem = SupplyItem(
                    id = 0L,
                    partCode = currentState.projectCode,
                    name = dynamicName,
                    category = PartCategory.INFRASTRUCTURE,
                    quantity = totalQuantity,
                    unit = "Unit",
                    supplier = "Internal Depo",
                    status = SupplyStatus.PENDING,
                    priority = Priority.HIGH,
                    documentRef = currentState.uploadedDocs.firstOrNull(),
                    notes = "",
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now()
                )

                repository.insertItem(newItem)
                repository.deleteDraft(currentDraftId)

                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Gagal mengirim data: ${e.message}"
                    )
                }
            }
        }
    }
}