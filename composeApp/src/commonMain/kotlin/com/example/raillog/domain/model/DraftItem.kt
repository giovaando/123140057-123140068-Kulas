package com.example.raillog.domain.model

data class DraftItem(
    val draftId: String,
    val projectTitle: String,
    val currentStep: Int,
    val lastUpdated: Long,
    val formStateJson: String
)