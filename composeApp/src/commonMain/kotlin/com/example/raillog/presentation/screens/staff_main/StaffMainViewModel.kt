package com.example.raillog.presentation.screens.staff_main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raillog.data.local.datastore.UserPreferences
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.domain.repository.SupplyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StaffMainViewModel(
    repository: SupplyRepository,
    userPreferences: UserPreferences
) : ViewModel() {

    // 1. Mengambil role/nama pengguna yang sedang login dari memori HP
    val activeUserRole: StateFlow<String> = userPreferences.userRole
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Staff")

    // 2. MENGAMBIL DATA ASLI DARI SQLDELIGHT SECARA REAL-TIME
    val allSupplyItems: StateFlow<List<SupplyItem>> = repository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}