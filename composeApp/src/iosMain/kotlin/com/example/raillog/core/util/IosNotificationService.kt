package com.example.raillog.core.util

import com.example.raillog.domain.repository.NotificationService

class IosNotificationService : NotificationService {
    override fun showCriticalAlert(itemName: String, quantity: Int) {
        // iOS implementation will use UNUserNotificationCenter
        // Placeholder for Sprint 4 as per PRD "iOS Camera/gallery belum diimplementasi"
        // and focus is Android-first.
    }

    override fun showCriticalSummary(items: List<Pair<String, Int>>) {
        // Placeholder
    }

    override fun showPendingAlert(pendingCount: Int) {
        // Placeholder
    }
}
