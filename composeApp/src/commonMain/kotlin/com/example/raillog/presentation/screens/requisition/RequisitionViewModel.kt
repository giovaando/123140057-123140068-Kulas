package com.example.raillog.presentation.screens.requisition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raillog.domain.model.PartCategory
import com.example.raillog.domain.model.Priority
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.domain.model.SupplyStatus
import com.example.raillog.domain.repository.SupplyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock // IMPORT WAKTU KMP
import kotlin.random.Random

@Serializable
data class CatalogItemUI(
    val id: String, val name: String, val category: String,
    val stock: Int, val isSafe: Boolean, val reqQty: Int = 0
)

@Serializable
data class RequisitionFormState(
    val requestorName: String = "",
    val employeeId: String = "",
    val department: String = "",
    val dateOfRequest: String = "",
    val projectType: String = "LRT",
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
    val isUploadingDoc: Boolean = false,
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
                    val savedState =
                        Json.decodeFromString<RequisitionFormState>(targetDraft.formStateJson)
                    _uiState.value = savedState
                    onStepLoaded(targetDraft.currentStep)
                }
            } catch (e: Exception) {
                println("Gagal memuat draft: ${e.message}")
            }
        }
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
            _uiState.update { it.copy(errorMessage = "Tanda tangan wajib diisi di Final Review!") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                delay(1000)

                // 1. Ambil daftar barang yang kuantitasnya lebih dari 0
                val requestedItems = currentState.catalogItems.filter { it.reqQty > 0 }

                // 2. Hitung total kuantitas dari semua barang yang dipilih
                val totalQuantity = if (requestedItems.isNotEmpty()) {
                    requestedItems.sumOf { it.reqQty }
                } else {
                    1 // Fallback minimal 1 jika entah bagaimana kosong
                }

                // 3. Buat nama pengajuan yang lebih dinamis berdasarkan barang yang dipilih
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
                    name = dynamicName, // NAMA SEKARANG DINAMIS
                    category = PartCategory.INFRASTRUCTURE,
                    quantity = totalQuantity, // KUANTITAS SEKARANG MENGAMBIL DARI INPUTAN
                    unit = "Unit",
                    supplier = "Internal Depo",
                    status = SupplyStatus.PENDING,
                    priority = Priority.HIGH,
                    documentRef = null,
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
                        errorMessage = "Gagal: ${e.message}"
                    )
                }
            }
        }
    }
}