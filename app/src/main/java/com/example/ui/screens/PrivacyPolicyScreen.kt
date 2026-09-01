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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.RainbowCreatorText
import com.example.ui.theme.GlassDarkBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBackClick
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Privacy Policy",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Intro Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0x331E293B),
                borderColor = Color(0x5500F0FF)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "100% Offline & Private",
                        color = NeonCyan,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "QuickDrop is architected from the ground up as a pure offline peer-to-peer file sharing utility. We do not operate external database servers, analytics collectors, or cloud intermediaries.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Policy Sections
            PolicyItem(
                title = "1. Information Collection",
                content = "QuickDrop does NOT collect, harvest, store, or transmit any personal identification information (PII). No account registration, phone number, or email address is ever requested."
            )

            PolicyItem(
                title = "2. File Access & Transmission",
                content = "File access is strictly restricted to files explicitly chosen by you through the file picker. Files are transferred directly between peer devices over local hardware (Wi-Fi Direct / Nearby Connections) and are never sent to external servers."
            )

            PolicyItem(
                title = "3. Device Discovery & Bluetooth",
                content = "Nearby device scanning uses local Bluetooth Low Energy and high-bandwidth local Wi-Fi hotspots solely to discover nearby receiver devices. No location telemetry or persistent device identifiers are recorded or uploaded."
            )

            PolicyItem(
                title = "4. Permissions Usage",
                content = "Permissions for Nearby Devices, Bluetooth, and Storage are strictly utilized to establish local device handshakes and write received files to your device's Download directory."
            )

            PolicyItem(
                title = "5. Third-Party Services",
                content = "QuickDrop contains no third-party tracking SDKs, marketing software, or advertisements. Your files and data belong exclusively to you."
            )

            PolicyItem(
                title = "6. Security & Encryption",
                content = "P2P handshakes utilize standard Google Nearby Connections secure authentication payloads, preventing unauthorized eavesdropping on local networks."
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Rainbow Creator Branding
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                RainbowCreatorText(
                    prefixText = "Privacy Policy • App Creator : ",
                    creatorName = "NH MAIM",
                    fontSize = 13
                )
            }
        }
    }
}

@Composable
private fun PolicyItem(title: String, content: String) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = Color(0x1F1E293B)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
