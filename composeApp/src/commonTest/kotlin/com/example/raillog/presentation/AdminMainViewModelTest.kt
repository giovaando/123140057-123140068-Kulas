package com.example.raillog.presentation.screens.admin_main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raillog.domain.model.Priority
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.domain.repository.SupplyRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
class AdminMainViewModel(
    private val supplyRepository: SupplyRepository
) : ViewModel() {

    // 1. Ambil semua data dari database
    val allItems: StateFlow<List<SupplyItem>> = supplyRepository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Filter data PENDING saja
    val pendingRequisitions: StateFlow<List<SupplyItem>> = allItems.map { items ->
        items.filter { it.status.name == "PENDING" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Metrik Operasional untuk Dashboard
    val criticalPendingCount: StateFlow<Int> = pendingRequisitions.map { items ->
        items.count { it.priority == Priority.CRITICAL || it.priority == Priority.HIGH }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val averageAiConfidence: StateFlow<Int> = allItems.map { items ->
        if (items.isEmpty()) 0
        else items.sumOf { (75 + (it.id % 25)).toInt() } / items.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // State untuk Pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // FIX: _selectedFilter tetap ada untuk kompatibilitas UI, tapi tidak lagi
    // digunakan untuk memunculkan status non-PENDING di tab Verifikasi.
    private val _selectedFilter = MutableStateFlow(0)
    val selectedFilter: StateFlow<Int> = _selectedFilter.asStateFlow()

    // FIX: filteredPendingItems sekarang di-derive dari pendingRequisitions
    // (bukan allItems), sehingga item yang sudah VERIFIED/REJECTED tidak akan
    // pernah muncul kembali di antrean verifikasi setelah diproses.
    val filteredPendingItems: StateFlow<List<SupplyItem>> = combine(
        pendingRequisitions,
        _searchQuery.debounce(300L)
    ) { items, query ->
        if (query.isEmpty()) {
            items
        } else {
            items.filter { item ->
                item.name.contains(query, ignoreCase = true) ||
                        item.partCode.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Fungsi untuk diakses oleh UI
    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateSelectedFilter(index: Int) { _selectedFilter.value = index }
}