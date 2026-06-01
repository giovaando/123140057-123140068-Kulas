package com.example.raillog.presentation.screens.staff_main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raillog.data.local.datastore.DataStoreFactory
import com.example.raillog.presentation.screens.login.GlobalSessionManager
import com.example.raillog.domain.repository.SupplyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class StaffMainViewModel(
    private val supplyRepository: SupplyRepository,
    dataStoreFactory: DataStoreFactory
) : ViewModel() {

    val userPreferences = GlobalSessionManager.getPrefs(dataStoreFactory)

    val activeUserRole = userPreferences.userRole
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Staff Gudang")

    val allSupplyItems = supplyRepository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDrafts = supplyRepository.getAllDrafts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}