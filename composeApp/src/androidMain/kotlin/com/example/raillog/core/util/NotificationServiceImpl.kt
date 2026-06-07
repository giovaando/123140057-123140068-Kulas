package com.example.raillog.core.util

import android.content.Context
import com.example.raillog.domain.repository.NotificationService

class NotificationServiceImpl(private val context: Context) : NotificationService {
    
    override fun showCriticalAlert(itemName: String, quantity: Int) {
        NotificationHelper.showCriticalAlert(context, itemName, quantity)
    }

    override fun showCriticalSummary(items: List<Pair<String, Int>>) {
        NotificationHelper.showCriticalSummary(context, items)
    }

    override fun showPendingAlert(pendingCount: Int) {
        NotificationHelper.showPendingAlert(context, pendingCount)
    }
}
