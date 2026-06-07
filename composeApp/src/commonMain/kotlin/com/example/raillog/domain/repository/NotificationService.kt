package com.example.raillog.domain.repository

interface NotificationService {
    fun showCriticalAlert(itemName: String, quantity: Int)
    fun showCriticalSummary(items: List<Pair<String, Int>>)
    fun showPendingAlert(pendingCount: Int)
}