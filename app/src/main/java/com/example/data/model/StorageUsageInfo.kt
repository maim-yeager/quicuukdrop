package com.example.data.model

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

data class StorageUsageInfo(
    val quickDropBytes: Long = 0L,
    val totalDeviceBytes: Long = 128L * 1024 * 1024 * 1024,
    val freeDeviceBytes: Long = 64L * 1024 * 1024 * 1024,
    val receivedFilesCount: Int = 0
) {
    val quickDropFormatted: String
        get() = formatBytes(quickDropBytes)

    val totalDeviceFormatted: String
        get() = formatBytes(totalDeviceBytes)

    val freeDeviceFormatted: String
        get() = formatBytes(freeDeviceBytes)

    /**
     * Storage consumption fraction of QuickDrop compared to total device storage,
     * clamped between 0f and 1f.
     */
    val usageFraction: Float
        get() = if (totalDeviceBytes > 0) {
            (quickDropBytes.toDouble() / totalDeviceBytes.toDouble()).toFloat().coerceIn(0f, 1f)
        } else 0f

    val percentageFormatted: String
        get() {
            val pct = usageFraction * 100
            return if (pct < 0.01 && quickDropBytes > 0) {
                "< 0.1%"
            } else {
                DecimalFormat("#,##0.1").format(pct) + "%"
            }
        }

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.size - 1)
            return DecimalFormat("#,##0.#").format(bytes / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
        }
    }
}
