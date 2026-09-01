package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransferState
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.TransferProgressRing
import com.example.ui.theme.GlassDarkBackground
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary

@Composable
fun TransferScreen(
    transferState: TransferState,
    onCancelClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Transferring",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Transfer Gauge & Stats
            when (transferState) {
                is TransferState.InProgress -> {
                    TransferProgressRing(
                        progressFraction = transferState.progressFraction,
                        percent = transferState.progressPercent,
                        transferredFormatted = transferState.formattedTransferred,
                        totalFormatted = transferState.formattedTotal,
                        currentFileName = transferState.currentFileName,
                        speedFormatted = transferState.formattedSpeed,
                        timeRemainingFormatted = transferState.formattedTimeRemaining,
                        isOutgoing = transferState.isOutgoing,
                        deviceName = transferState.deviceName
                    )
                }
                is TransferState.Connecting -> {
                    TransferProgressRing(
                        progressFraction = 0.05f,
                        percent = 5,
                        transferredFormatted = "0 B",
                        totalFormatted = "Connecting...",
                        currentFileName = "Establishing secure P2P handshake",
                        speedFormatted = "-- MB/s",
                        timeRemainingFormatted = "Connecting...",
                        isOutgoing = true,
                        deviceName = transferState.device.deviceName
                    )
                }
                else -> {
                    TransferProgressRing(
                        progressFraction = 0.72f,
                        percent = 72,
                        transferredFormatted = "39.1 MB",
                        totalFormatted = "53.7 MB",
                        currentFileName = "Vacation Photo.jpg",
                        speedFormatted = "12.5 MB/s",
                        timeRemainingFormatted = "00:08 remaining",
                        isOutgoing = true,
                        deviceName = "Nearby Device"
                    )
                }
            }

            // Cancel Button
            LiquidGlassButton(
                text = "Cancel Transfer",
                onClick = onCancelClick,
                icon = Icons.Default.Close,
                gradientBrush = Brush.horizontalGradient(
                    listOf(
                        Color(0x33FF1744),
                        Color(0x66FF1744)
                    )
                ),
                glowColor = NeonPink,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }
    }
}
