package com.example.raillog.presentation.screens.admin_main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.domain.model.SupplyStatus
import com.example.raillog.domain.model.TechnicalDocument
import com.example.raillog.domain.repository.SupplyRepository
import com.example.raillog.domain.repository.TechnicalDocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class VerificationDetailViewModel(
    private val supplyRepository: SupplyRepository,
    private val technicalDocumentRepository: TechnicalDocumentRepository
) : ViewModel() {

    private val _selectedItem = MutableStateFlow<SupplyItem?>(null)
    val selectedItem: StateFlow<SupplyItem?> = _selectedItem.asStateFlow()

    private val _selectedDocument =
        MutableStateFlow<TechnicalDocument?>(null)

    val selectedDocument: StateFlow<TechnicalDocument?> =
        _selectedDocument.asStateFlow()

    fun loadItem(id: Long) {

        viewModelScope.launch {

            supplyRepository
                .getItemById(id)
                .collect { item ->

                    _selectedItem.value = item

                    val documentTitle =
                        item?.documentRef

                    if (!documentTitle.isNullOrBlank()) {

                        val document =
                            technicalDocumentRepository
                                .getAllDocuments()
                                .firstOrNull()
                                ?.firstOrNull {
                                    it.title == documentTitle
                                }

                        _selectedDocument.value =
                            document
                    }
                }
        }
    }

    fun verifyItem(
        onSuccess: () -> Unit
    ) {

        viewModelScope.launch {

            _selectedItem.value?.let { item ->

                val updatedItem =
                    item.copy(
                        status = SupplyStatus.VERIFIED
                    )

                supplyRepository.updateItem(
                    updatedItem
                )

                onSuccess()
            }
        }
    }

    fun rejectItem(
        onSuccess: () -> Unit
    ) {

        viewModelScope.launch {

            _selectedItem.value?.let { item ->

                val updatedItem =
                    item.copy(
                        status = SupplyStatus.REJECTED
                    )

                supplyRepository.updateItem(
                    updatedItem
                )

                onSuccess()
            }
        }
    }
}