package com.example.data.model

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

sealed interface TransferState {
    data object Idle : TransferState

    data class Searching(val countFound: Int = 0) : TransferState

    data class Connecting(val device: NearbyDevice) : TransferState

    data class ConnectionRequested(
        val endpointId: String,
        val deviceName: String,
        val fileCount: Int,
        val totalBytes: Long,
        val isIncoming: Boolean
    ) : TransferState

    data class InProgress(
        val deviceName: String,
        val isOutgoing: Boolean,
        val totalFiles: Int,
        val currentFileIndex: Int,
        val currentFileName: String,
        val bytesTransferred: Long,
        val totalBytes: Long,
        val speedMBps: Double,
        val secondsRemaining: Long
    ) : TransferState {
        val progressFraction: Float
            get() = if (totalBytes > 0) (bytesTransferred.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

        val progressPercent: Int
            get() = (progressFraction * 100).toInt()

        val formattedTransferred: String
            get() = formatBytes(bytesTransferred)

        val formattedTotal: String
            get() = formatBytes(totalBytes)

        val formattedSpeed: String
            get() = String.format(java.util.Locale.US, "%.1f MB/s", speedMBps)

        val formattedTimeRemaining: String
            get() {
                val mins = secondsRemaining / 60
                val secs = secondsRemaining % 60
                return String.format(java.util.Locale.US, "%02d:%02d remaining", mins, secs)
            }
    }

    data class Completed(
        val deviceName: String,
        val isOutgoing: Boolean,
        val fileCount: Int,
        val totalBytes: Long,
        val durationSeconds: Long
    ) : TransferState {
        val formattedTotal: String
            get() = formatBytes(totalBytes)
    }

    data class Failed(val errorReason: String) : TransferState

    data object Cancelled : TransferState
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    return DecimalFormat("#,##0.#").format(bytes / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
}
