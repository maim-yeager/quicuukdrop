package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.RainbowCreatorText
import com.example.ui.theme.GlassDarkBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val logoScale = remember { Animatable(0.8f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val glowIntensity = remember { Animatable(0.3f) }

    val infiniteTransition = rememberInfiniteTransition(label = "SplashParticles")
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ParticlesRotation"
    )

    LaunchedEffect(Unit) {
        // Step 1: Logo fades & scales in from 80% to 100%
        logoAlpha.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        logoScale.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))

        // Step 2: Glow becomes brighter
        glowIntensity.animateTo(0.85f, animationSpec = tween(500))

        // Step 3: "QuickDrop" title fades in
        textAlpha.animateTo(1f, animationSpec = tween(600))

        // Let user see the full rainbow creator animation and glass reflection
        delay(1200)

        // Smooth transition to Home screen
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Background subtle space/digital earth image or decorative ambient gradient
        Image(
            painter = painterResource(id = R.drawable.quickdrop_splash_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.65f),
            contentScale = ContentScale.Crop
        )

        // Dynamic glass reflection particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width * 0.45f

            for (i in 0 until 12) {
                val angle = Math.toRadians((i * 30.0 + particlePhase))
                val r = baseRadius * (0.6f + (i % 3) * 0.25f)
                val px = center.x + (r * cos(angle)).toFloat()
                val py = center.y + (r * sin(angle)).toFloat()
                val pAlpha = (0.3f + 0.4f * sin(Math.toRadians(particlePhase * 2.0 + i * 20.0))).coerceIn(0.1, 0.9).toFloat()

                drawCircle(
                    color = if (i % 2 == 0) NeonCyan.copy(alpha = pAlpha) else NeonPurple.copy(alpha = pAlpha),
                    radius = (2.5f + (i % 3) * 1.5f).dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Center Content: Logo + Title + Tagline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // QuickDrop Logo with Glowing Glass Frame
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NeonCyan.copy(alpha = glowIntensity.value * 0.6f),
                                    NeonPurple.copy(alpha = glowIntensity.value * 0.3f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.width * 0.85f
                            )
                        )
                    }
                    .clip(RoundedCornerShape(32.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.quickdrop_icon),
                    contentDescription = "QuickDrop Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name with Glass Reveal
            Text(
                text = "QuickDrop",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 38.sp,
                    letterSpacing = (-0.5).sp,
                    color = TextPrimary
                ),
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Fast. Offline. Private.",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    letterSpacing = 1.2.sp,
                    color = TextSecondary
                ),
                modifier = Modifier.alpha(textAlpha.value)
            )
        }

        // Bottom Animated Rainbow Creator Credit: "App Creator : NH MAIM"
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp)
        ) {
            RainbowCreatorText(
                prefixText = "App Creator : ",
                creatorName = "NH MAIM",
                fontSize = 14
            )
        }
    }
}
