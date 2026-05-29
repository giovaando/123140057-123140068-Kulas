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
import kotlinx.coroutines.launch

// 1. DATA MODEL
data class RequisitionFormState(
    val requestorName: String = "Giovan Lado",
    val employeeId: String = "RLN-123140068",
    val department: String = "Rolling Stock Maintenance",
    val dateOfRequest: String = "29/05/2026",

    val projectType: String = "LRT",
    val projectCode: String = "",
    val destinationSite: String = "",

    val selectedMaterialsCount: Int = 0,
    val hasUploadedBlueprint: Boolean = false,
    val isSigned: Boolean = false,

    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null
)

// 2. VIEWMODEL (Sudah Di-inject dengan Repository SQLDelight)
class RequisitionViewModel(
    private val repository: SupplyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequisitionFormState())
    val uiState: StateFlow<RequisitionFormState> = _uiState.asStateFlow()

    fun updateProjectCode(code: String) {
        _uiState.update { it.copy(projectCode = code) }
    }

    fun updateProjectType(type: String) {
        _uiState.update { it.copy(projectType = type) }
    }

    fun updateDestinationSite(site: String) {
        _uiState.update { it.copy(destinationSite = site) }
    }

    fun signDocument() {
        _uiState.update { it.copy(isSigned = true) }
    }

    // FUNGSI UTAMA: MENYIMPAN KE SQLDELIGHT
    fun submitRequisition() {
        val currentState = _uiState.value

        // 1. Validasi
        if (currentState.projectCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Project Code tidak boleh kosong!") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        // 2. Eksekusi penyimpanan ke Database di latar belakang (Coroutine)
        viewModelScope.launch {
            try {
                // Efek loading buatan agar UI terlihat natural
                delay(1000)

                // Mapping data form ke tabel database SupplyItem kita
                val newItem = SupplyItem(
                    id = 0L,
                    name = "Project: ${currentState.projectType} Requisition",
                    partCode = currentState.projectCode,
                    priority = Priority.HIGH,
                    status = SupplyStatus.PENDING,
                    category = PartCategory.INFRASTRUCTURE,
                    quantity = 1,
                    supplier = "Internal Depo",
                    unit = "Unit"
                )

                // Simpan ke SQLDelight!
                repository.insertItem(newItem)

                // Beri sinyal sukses ke UI
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitSuccess = true
                    )
                }
            } catch (e: Exception) {
                // Jika terjadi error pada database (misal memori penuh)
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Gagal menyimpan ke database: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}