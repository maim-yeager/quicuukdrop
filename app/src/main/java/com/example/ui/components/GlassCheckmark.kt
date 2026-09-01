package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GlassCheckmark(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    var isStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isStarted = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0.2f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 300f),
        label = "CheckmarkScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "CheckmarkRipples")
    val ripplePulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RipplePulse"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Shockwave ripple background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f * 0.7f

            val r = baseRadius * ripplePulse
            val alpha = (1.35f - ripplePulse) / 0.55f

            drawCircle(
                color = NeonGreen.copy(alpha = (alpha * 0.4f).coerceIn(0f, 0.4f)),
                radius = r,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Sparkle particles
            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45.0 + ripplePulse * 30.0))
                val pRadius = baseRadius * (0.9f + ripplePulse * 0.3f)
                val px = center.x + (pRadius * cos(angle)).toFloat()
                val py = center.y + (pRadius * sin(angle)).toFloat()

                drawCircle(
                    color = Color.White.copy(alpha = (alpha * 0.8f).coerceIn(0f, 0.8f)),
                    radius = 2.5.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Center emerald glass orb
        Box(
            modifier = Modifier
                .size(size * 0.7f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonGreen.copy(alpha = 0.85f),
                            Color(0xFF064E3B).copy(alpha = 0.95f)
                        )
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                .drawBehind {
                    drawCircle(
                        color = NeonGreen.copy(alpha = 0.5f),
                        radius = this.size.width / 1.7f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success Check",
                tint = Color.White,
                modifier = Modifier.size(size * 0.4f)
            )
        }
    }
}
