package com.example.raillog.presentation.screens.admin_main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.domain.model.SupplyStatus
import com.example.raillog.domain.repository.SupplyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VerificationDetailViewModel(
    private val repository: SupplyRepository
) : ViewModel() {

    private val _selectedItem = MutableStateFlow<SupplyItem?>(null)
    val selectedItem: StateFlow<SupplyItem?> = _selectedItem.asStateFlow()

    fun loadItem(id: Long) {
        viewModelScope.launch {
            repository.getItemById(id).collect { item ->
                _selectedItem.value = item
            }
        }
    }

    fun verifyItem(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _selectedItem.value?.let { item ->
                // UBAH DI SINI: Gunakan SupplyStatus.VERIFIED
                val updatedItem = item.copy(status = SupplyStatus.VERIFIED)

                repository.updateItem(updatedItem)
                onSuccess()
            }
        }
    }

    fun rejectItem(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _selectedItem.value?.let { item ->
                // Mengubah status menjadi REJECTED
                val updatedItem = item.copy(status = SupplyStatus.REJECTED)
                repository.updateItem(updatedItem)
                onSuccess()
            }
        }
    }
}