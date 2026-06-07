package com.example.raillog.core.util

import android.content.Context
import com.example.raillog.domain.model.Priority
import com.example.raillog.domain.repository.SupplyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Memantau repository secara reaktif dan menampilkan notifikasi
 * ketika ada komponen dengan prioritas CRITICAL atau HIGH.
 *
 * Dipanggil dari [RaillogApplication.onCreate()].
 */
class CriticalItemsNotifier(
    private val context: Context,
    private val repository: SupplyRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Set ID item yang sudah pernah dinotifikasikan agar tidak spam
    private val notifiedIds = mutableSetOf<Long>()

    fun start() {
        scope.launch {
            repository.getAllItems()
                .map { items ->
                    items.filter {
                        it.priority == Priority.CRITICAL || it.priority == Priority.HIGH
                    }
                }
                .distinctUntilChanged()
                .collect { criticalItems ->
                    // Hanya notifikasi item yang belum pernah dinotifikasikan
                    val newItems = criticalItems.filter { it.id !in notifiedIds }
                    if (newItems.isNotEmpty()) {
                        notifiedIds.addAll(newItems.map { it.id })

                        if (newItems.size == 1) {
                            val item = newItems.first()
                            NotificationHelper.showCriticalAlert(
                                context = context,
                                itemName = item.name,
                                quantity = item.quantity
                            )
                        } else {
                            NotificationHelper.showCriticalSummary(
                                context = context,
                                criticalItems = newItems.map { Pair(it.name, it.quantity) }
                            )
                        }
                    }
                }
        }
    }
}