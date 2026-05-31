package com.example.raillog.presentation.screens.admin_main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.domain.model.SupplyStatus
import com.example.raillog.domain.repository.SupplyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AdminMainViewModel(private val repository: SupplyRepository) : ViewModel() {

    // Mengambil item dengan status PENDING untuk Antrean Verifikasi
    val pendingRequisitions: StateFlow<List<SupplyItem>> = repository.getAllItems()
        .map { items -> items.filter { it.status == SupplyStatus.PENDING } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Mengambil semua item untuk tab Inventory
    val allItems: StateFlow<List<SupplyItem>> = repository.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}