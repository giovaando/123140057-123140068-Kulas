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

    // 2. Filter data PENDING
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

    // State untuk Pencarian dan Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(0)
    val selectedFilter: StateFlow<Int> = _selectedFilter.asStateFlow()

    // 4. Logika Pintar: Combine + Debounce
    val filteredPendingItems: StateFlow<List<SupplyItem>> = combine(
        allItems,
        _searchQuery.debounce(300L),
        _selectedFilter
    ) { items, query, filter ->
        items.filter { item ->
            // Filter Teks
            val matchesSearch = query.isEmpty() || item.name.contains(query, ignoreCase = true)

            // Filter Kategori/Status
            val matchesFilter = when (filter) {
                1 -> item.status.name == "PENDING"
                2 -> item.status.name == "VERIFIED"
                3 -> item.status.name == "REJECTED"
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Fungsi untuk diakses oleh UI
    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateSelectedFilter(index: Int) { _selectedFilter.value = index }
}
