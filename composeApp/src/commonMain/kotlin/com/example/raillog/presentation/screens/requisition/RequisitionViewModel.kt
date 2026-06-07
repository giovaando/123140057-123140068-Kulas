package com.example.raillog.presentation.screens.requisition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raillog.domain.model.PartCategory
import com.example.raillog.domain.model.Priority
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.domain.model.SupplyStatus
import com.example.raillog.domain.repository.SupplyRepository
import com.example.raillog.domain.repository.TechnicalDocumentRepository
import com.example.raillog.domain.repository.AIRepository
import com.example.raillog.data.remote.api.SystemPrompts
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

@Serializable
data class CatalogItemUI(
    val id: String, 
    val name: String, 
    val category: String,
    val stock: Int, 
    val isSafe: Boolean, 
    val reqQty: Int = 0,
    val unit: String = "Pcs"
)

@Serializable
data class RequisitionFormState(
    val isProcessingAI: Boolean = false,
    
    // Step 1: Identitas Detail (Industrial Audit Grade)
    val requestorName: String = "",
    val employeeId: String = "",
    val phoneNumber: String = "",
    val supervisorName: String = "",
    val department: String = "",
    val dateOfRequest: String = "",
    
    // Step 2: Proyek & Lokasi
    val projectType: String = "",
    val projectCode: String = "",
    val destinationSite: String = "",
    
    // Step 3: Katalog Material (Expanded Industrial Catalog)
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val catalogItems: List<CatalogItemUI> = listOf(
        CatalogItemUI("TRC-M-882", "Traction Motor 300kW AC", "Propulsion", 8, true, unit = "Unit"),
        CatalogItemUI("BOG-F-102", "Bogie Frame H-Type K1", "Bogie", 5, false, unit = "Set"),
        CatalogItemUI("RFL-R-054", "Rel Profile R54 (UIC 54)", "Infrastructure", 12, false, unit = "Batang"),
        CatalogItemUI("BRK-S-441", "Composite Brake Shoe Low-Noise", "Braking", 450, true, unit = "Pcs"),
        CatalogItemUI("PAN-S-001", "Pantograph Carbon Strip (Grade A)", "Electrical", 120, true, unit = "Pcs"),
        CatalogItemUI("CPL-M-011", "Automatic Coupler Head Scharfenberg", "Mechanical", 3, false, unit = "Unit"),
        CatalogItemUI("WHL-S-099", "Monoblock Wheelset 860mm", "Bogie", 24, true, unit = "Pcs"),
        CatalogItemUI("LGT-I-005", "LED Interior Lamp Cluster 24V", "Interior", 300, true, unit = "Pcs"),
        CatalogItemUI("FST-E-102", "Pandrol E-Clip Fastener", "Infrastructure", 5000, true, unit = "Pcs"),
        CatalogItemUI("WRN-110-T", "Torque Wrench Calibration Set", "Tools", 15, true, unit = "Set"),
        CatalogItemUI("INV-S-550", "Static Inverter Module 1500V", "Electrical", 2, false, unit = "Unit"),
        CatalogItemUI("COM-D-012", "Train Dispatcher Radio Unit", "Communication", 10, true, unit = "Pcs")
    ),
    
    // Step 4: Lembar Justifikasi
    val notes: String = "",
    val isProcessingPreCheck: Boolean = false,
    val aiPreCheckResult: String? = null,
    
    // Finalization
    val isSigned: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val isProjectCodeValid: Boolean get() {
        if (projectCode.isBlank()) return false
        val regex = Regex("^[A-Z0-9]+-[A-Z0-9]+-[A-Z0-9]+$")
        if (!regex.matches(projectCode)) return false
        val prefix = projectCode.split("-").firstOrNull()
        return when (projectType) {
            "LRT" -> prefix == "LRT"
            "MRT" -> prefix == "MRT"
            "KRL" -> prefix == "KRL" || prefix == "KCI"
            "HSR" -> prefix == "HSR" || prefix == "KCIC"
            "PASSENGER" -> prefix == "K1" || prefix == "K3" || prefix == "KAI"
            else -> true
        }
    }

    val canSubmit: Boolean get() = 
        requestorName.isNotBlank() && 
        employeeId.isNotBlank() && 
        phoneNumber.isNotBlank() &&
        isProjectCodeValid && 
        isSigned && 
        catalogItems.any { it.reqQty > 0 }
}

class RequisitionViewModel(
    private val repository: SupplyRepository,
    private val technicalDocumentRepository: TechnicalDocumentRepository,
    private val aiRepository: AIRepository
) : ViewModel() {

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
                    val savedState = Json.decodeFromString<RequisitionFormState>(targetDraft.formStateJson)
                    _uiState.value = savedState
                    onStepLoaded(targetDraft.currentStep)
                }
            } catch (e: Exception) { println("Gagal muat draf: ${e.message}") }
        }
    }

    // Update Functions
    fun updateName(v: String) = _uiState.update { it.copy(requestorName = v) }
    fun updateEmployeeId(v: String) = _uiState.update { it.copy(employeeId = v) }
    fun updatePhone(v: String) = _uiState.update { it.copy(phoneNumber = v) }
    fun updateSupervisor(v: String) = _uiState.update { it.copy(supervisorName = v) }
    fun updateDepartment(v: String) = _uiState.update { it.copy(department = v) }
    fun updateDate(v: String) = _uiState.update { it.copy(dateOfRequest = v) }
    fun updateProjectType(v: String) = _uiState.update { it.copy(projectType = v) }
    fun updateProjectCode(v: String) = _uiState.update { it.copy(projectCode = v.uppercase()) }
    fun updateDestinationSite(v: String) = _uiState.update { it.copy(destinationSite = v) }
    fun updateNotes(v: String) = _uiState.update { it.copy(notes = v) }
    fun updateSearchQuery(v: String) = _uiState.update { it.copy(searchQuery = v) }

    // Manual Quantity Update (Direct typing support)
    fun updateItemQuantity(itemId: String, quantity: Int) {
        _uiState.update { state ->
            val updated = state.catalogItems.map { 
                if (it.id == itemId) it.copy(reqQty = maxOf(0, quantity)) else it 
            }
            state.copy(catalogItems = updated)
        }
    }

    fun setSignedStatus(signed: Boolean) = _uiState.update { it.copy(isSigned = signed) }

    fun runPreSubmitCheck() {
        val currentState = _uiState.value
        val requestedItems = currentState.catalogItems.filter { it.reqQty > 0 }
        if (requestedItems.isEmpty()) {
            _uiState.update { it.copy(aiPreCheckResult = "Silakan pilih minimal 1 item untuk divalidasi.") }
            return
        }
        _uiState.update { it.copy(isProcessingPreCheck = true, aiPreCheckResult = null) }
        viewModelScope.launch {
            try {
                val itemsStr = requestedItems.joinToString("\n") { "- ${it.name}: ${it.reqQty} ${it.unit}" }
                val prompt = """
                    Evaluasi draf pengadaan material kereta api:
                    - Kode Proyek: ${currentState.projectCode} (${currentState.projectType})
                    - Justifikasi: ${currentState.notes}
                    - Barang:
                    $itemsStr
                """.trimIndent()
                val result = aiRepository.chat(prompt, SystemPrompts.PRE_SUBMIT_CHECK)
                result.fold(
                    onSuccess = { res -> _uiState.update { it.copy(isProcessingPreCheck = false, aiPreCheckResult = res) } },
                    onFailure = { err -> _uiState.update { it.copy(isProcessingPreCheck = false, aiPreCheckResult = "Gagal AI: ${err.message}") } }
                )
            } catch (e: Exception) { _uiState.update { it.copy(isProcessingPreCheck = false, aiPreCheckResult = "Error: ${e.message}") } }
        }
    }

    fun saveDraftAutomatically(step: Int) {
        val currentState = _uiState.value
        if (currentState.requestorName.isBlank() && currentState.projectCode.isBlank()) return
        viewModelScope.launch {
            try {
                val json = Json.encodeToString(currentState)
                repository.saveDraft(currentDraftId, currentState.projectCode.ifBlank { "Audit Requisition" }, step, Clock.System.now().toEpochMilliseconds(), json)
            } catch (e: Exception) { println("Gagal simpan draf: ${e.message}") }
        }
    }

    fun submitRequisition() {
        val currentState = _uiState.value
        if (!currentState.canSubmit) {
            _uiState.update { it.copy(errorMessage = "Mohon lengkapi Nama, HP, NIP, Kode Proyek Valid, dan Tanda Tangan.") }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                delay(1000)
                val requested = currentState.catalogItems.filter { it.reqQty > 0 }
                val totalQty = requested.sumOf { it.reqQty }
                val title = if (requested.size > 1) "${requested.first().name} & ${requested.size - 1} lainnya" else requested.first().name
                
                // SMART PRIORITY LOGIC:
                // 1. Cek jika catatan mengandung kata kunci urgen
                val isUrgentInNotes = currentState.notes.uppercase().contains("URGENT") || currentState.notes.uppercase().contains("DARURAT")
                // 2. Cek jika ada barang yang diminta memiliki stok rendah (isSafe == false)
                val hasLowStockItem = requested.any { !it.isSafe }
                
                val finalPriority = when {
                    isUrgentInNotes || hasLowStockItem -> Priority.CRITICAL
                    else -> Priority.NORMAL
                }

                val newItem = SupplyItem(
                    id = 0L,
                    partCode = currentState.projectCode,
                    name = title,
                    category = PartCategory.fromString(requested.first().category.uppercase()),
                    quantity = totalQty,
                    unit = requested.first().unit,
                    supplier = "Internal Log Center",
                    status = SupplyStatus.PENDING,
                    priority = finalPriority,
                    notes = "Pengaju: ${currentState.requestorName}\nSupervisor: ${currentState.supervisorName}\nWA: ${currentState.phoneNumber}\nJustifikasi: ${currentState.notes}",
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now()
                )
                repository.insertItem(newItem)
                repository.deleteDraft(currentDraftId)
                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            } catch (e: Exception) { _uiState.update { it.copy(isSubmitting = false, errorMessage = "Error: ${e.message}") } }
        }
    }
}
