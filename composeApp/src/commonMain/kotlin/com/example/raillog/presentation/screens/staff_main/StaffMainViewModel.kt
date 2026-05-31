package com.example.raillog.presentation.screens.staff_main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raillog.domain.model.DraftItem
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.domain.repository.SupplyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class StaffMainViewModel(
    private val repository: SupplyRepository
) : ViewModel() {

    private val _activeUserRole = MutableStateFlow("Giovan Lado (Staff Gudang)")
    val activeUserRole = _activeUserRole.asStateFlow()

    // Mengambil semua barang (requests) dari repository
    val allSupplyItems: StateFlow<List<SupplyItem>> = repository.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // TAMBAHAN BARU: Mengambil semua draft dari repository
    val allDrafts: StateFlow<List<DraftItem>> = repository.getAllDrafts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}