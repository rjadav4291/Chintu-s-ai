package com.chintu.ai.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ChintuOrb(
    modifier: Modifier = Modifier,
    status: String = "READY"
) {
    val transition = rememberInfiniteTransition(
        label = "chintu_orb"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier
                .size(180.dp)
                .alpha(0.35f * pulse)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ChintuColors.Accent.copy(alpha = 0.55f),
                        ChintuColors.Primary.copy(alpha = 0.20f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension / 2f
            )
        }

        Canvas(
            modifier = Modifier.size(150.dp)
        ) {
            val center = Offset(
                size.width / 2f,
                size.height / 2f
            )

            val radius = size.minDimension / 2.7f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ChintuColors.Secondary,
                        ChintuColors.Primary,
                        ChintuColors.Accent
                    )
                ),
                radius = radius
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.18f),
                radius = radius * 1.18f,
                style = Stroke(width = 2.dp.toPx())
            )

            val angle = Math.toRadians(rotation.toDouble())

            repeat(3) { index ->

                val extraAngle =
                    angle + (index * Math.PI * 2.0 / 3.0)

                val x =
                    center.x + cos(extraAngle).toFloat() * radius * 1.25f

                val y =
                    center.y + sin(extraAngle).toFloat() * radius * 1.25f

                drawCircle(
                    color = Color.White.copy(alpha = 0.75f),
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        Text(
            text = "C",
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
