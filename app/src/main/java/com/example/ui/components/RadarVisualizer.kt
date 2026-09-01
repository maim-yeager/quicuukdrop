package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarVisualizer(
    modifier: Modifier = Modifier,
    visualizerSize: Dp = 260.dp,
    centerIconTint: Color = NeonCyan,
    pulseColor: Color = NeonCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")

    // Rotation of radar sweep line
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSweepRotation"
    )

    // Pulsing waves 1, 2, 3
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse2"
    )

    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, delayMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse3"
    )

    Box(
        modifier = modifier
            .size(visualizerSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            // Concentric fixed glass rings
            val rings = listOf(0.3f, 0.55f, 0.8f, 0.98f)
            rings.forEach { factor ->
                drawCircle(
                    color = Color(0x334B6CB7),
                    radius = maxRadius * factor,
                    center = center,
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            // Animated expanding pulse circles
            listOf(pulse1, pulse2, pulse3).forEach { p ->
                val radius = maxRadius * p
                val alpha = ((1f - p) * 0.7f).coerceIn(0f, 0.7f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            pulseColor.copy(alpha = alpha * 0.15f),
                            pulseColor.copy(alpha = alpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius.coerceAtLeast(10f)
                    ),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Rotating sweep radar beam
            rotate(degrees = rotation, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            pulseColor.copy(alpha = 0.05f),
                            NeonPurple.copy(alpha = 0.2f),
                            pulseColor.copy(alpha = 0.6f)
                        ),
                        center = center
                    ),
                    startAngle = -60f,
                    sweepAngle = 60f,
                    useCenter = true,
                    size = this.size
                )
                // Leading bright sweep line
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color.White, pulseColor),
                        start = center,
                        end = Offset(center.x + maxRadius, center.y)
                    ),
                    start = center,
                    end = Offset(center.x + maxRadius, center.y),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Floating decorative particle sparks
            val particlePositions = listOf(
                Pair(0.45f, 45f),
                Pair(0.72f, 130f),
                Pair(0.60f, 220f),
                Pair(0.85f, 310f),
                Pair(0.35f, 180f)
            )

            particlePositions.forEach { (distFrac, angleDeg) ->
                val rad = Math.toRadians((angleDeg + rotation * 0.3).toDouble())
                val px = center.x + (maxRadius * distFrac * cos(rad)).toFloat()
                val py = center.y + (maxRadius * distFrac * sin(rad)).toFloat()

                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 3.dp.toPx(),
                    center = Offset(px, py)
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.4f),
                    radius = 7.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Center glass glowing orb with icon
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = 0.6f),
                            Color(0xFF0F172A).copy(alpha = 0.9f)
                        )
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                .drawBehind {
                    drawCircle(
                        color = centerIconTint.copy(alpha = 0.35f),
                        radius = this.size.width / 2f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WifiTethering,
                contentDescription = "Radar Active",
                tint = centerIconTint,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
