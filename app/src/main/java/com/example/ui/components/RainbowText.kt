package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RainbowColors
import com.example.ui.theme.quickDropColors

@Composable
fun RainbowCreatorText(
    modifier: Modifier = Modifier,
    prefixText: String = "App Creator : ",
    creatorName: String = "NH MAIM",
    fontSize: Int = 14,
    showPrefix: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RainbowTransition")
    val offsetProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RainbowSweep"
    )

    val rainbowBrush = Brush.linearGradient(
        colors = RainbowColors + RainbowColors,
        start = androidx.compose.ui.geometry.Offset(offsetProgress, 0f),
        end = androidx.compose.ui.geometry.Offset(offsetProgress + 500f, 300f)
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showPrefix) {
            Text(
                text = prefixText,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = fontSize.sp,
                    color = quickDropColors().textSecondary,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Text(
            text = creatorName,
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (fontSize + 1).sp,
                letterSpacing = 1.sp
            ),
            modifier = Modifier
                .graphicsLayer(alpha = 0.99f)
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(
                            brush = rainbowBrush,
                            blendMode = BlendMode.SrcIn
                        )
                    }
                }
        )
    }
}
