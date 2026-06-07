package com.example.raillog.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Welcome : Route
    @Serializable data object Login : Route
    @Serializable data object Register : Route
    @Serializable data object StaffMain : Route
    @Serializable data class RequisitionWizard(val draftId: String? = null) : Route
    @Serializable data object Home : Route
    @Serializable data class AddSupply(val itemId: Long? = null) : Route
    @Serializable data class SupplyDetail(val itemId: Long) : Route
    @Serializable data class AIAssistant(val itemId: Long? = null, val initialText: String? = null) : Route

    // --- Pastikan 2 baris ini ada di dalam sealed interface Route ---
    @Serializable data object AdminMain : Route
    @Serializable data class VerificationDetail(val requisitionId: Long) : Route
}