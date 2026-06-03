package com.example.raillog.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Helper untuk menampilkan notifikasi komponen kritis.
 *
 * Penggunaan:
 * ```kotlin
 * NotificationHelper.showCriticalAlert(context, "Bantalan Bogie", 3)
 * NotificationHelper.showPendingAlert(context, 5)
 * ```
 */
object NotificationHelper {

    private const val CHANNEL_CRITICAL = "raillog_critical"
    private const val CHANNEL_PENDING  = "raillog_pending"
    private const val CHANNEL_GENERAL  = "raillog_general"

    private const val NOTIF_ID_CRITICAL = 1001
    private const val NOTIF_ID_PENDING  = 1002

    /** Wajib dipanggil saat Application.onCreate() */
    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_CRITICAL,
                "Komponen Kritis",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi untuk komponen dengan prioritas CRITICAL"
                enableVibration(true)
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_PENDING,
                "Antrian Pending",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi untuk requisisi yang menunggu verifikasi"
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_GENERAL,
                "Informasi Umum",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi umum RailLog"
            }
        )
    }

    /**
     * Tampilkan notifikasi komponen kritis.
     * @param itemName Nama komponen
     * @param quantity Jumlah yang tersisa
     */
    fun showCriticalAlert(context: Context, itemName: String, quantity: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_CRITICAL)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Komponen Kritis!")
            .setContentText("$itemName — Stok tersisa: $quantity unit")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Komponen \"$itemName\" memiliki prioritas CRITICAL dengan stok tersisa $quantity unit. Segera lakukan pengadaan.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColorized(true)
            .setColor(0xFFEF4444.toInt())
            .build()

        NotificationManagerCompat.from(context).notify(
            NOTIF_ID_CRITICAL + itemName.hashCode(),
            notification
        )
    }

    /**
     * Tampilkan ringkasan notifikasi untuk beberapa komponen kritis sekaligus.
     * @param criticalItems List pasangan (nama, jumlah)
     */
    fun showCriticalSummary(context: Context, criticalItems: List<Pair<String, Int>>) {
        if (criticalItems.isEmpty()) return

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("${criticalItems.size} Komponen Membutuhkan Perhatian")

        criticalItems.take(5).forEach { (name, qty) ->
            inboxStyle.addLine("• $name — $qty unit")
        }

        if (criticalItems.size > 5) {
            inboxStyle.setSummaryText("+${criticalItems.size - 5} lainnya")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_CRITICAL)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ ${criticalItems.size} Komponen Kritis")
            .setContentText("Tap untuk melihat detail komponen yang perlu perhatian")
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(0xFFEF4444.toInt())
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID_CRITICAL, notification)
    }

    /**
     * Notifikasi untuk requisisi yang menunggu verifikasi admin.
     * @param pendingCount Jumlah item yang pending
     */
    fun showPendingAlert(context: Context, pendingCount: Int) {
        if (pendingCount == 0) return

        val notification = NotificationCompat.Builder(context, CHANNEL_PENDING)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("📋 $pendingCount Requisisi Menunggu")
            .setContentText("Ada $pendingCount permintaan yang belum diverifikasi admin.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID_PENDING, notification)
    }

    /** Batalkan semua notifikasi aktif */
    fun cancelAll(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}