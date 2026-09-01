package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

@Entity(tableName = "transfer_history")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val deviceName: String,
    val isOutgoing: Boolean,
    val fileCount: Int,
    val totalBytes: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS", // "SUCCESS", "FAILED", "CANCELLED"
    val fileNames: String = "",
    val transferSpeedMBps: Double = 12.5
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedSize: String
        get() {
            if (totalBytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (log10(totalBytes.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.size - 1)
            return DecimalFormat("#,##0.#").format(totalBytes / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
        }
}
