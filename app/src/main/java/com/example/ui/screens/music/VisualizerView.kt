package com.example.ui.screens.music

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.audio.VisualizerType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VisualizerView(
    frequencies: FloatArray,
    type: VisualizerType,
    accentColor: Color,
    modifier: Modifier = Modifier.fillMaxWidth().height(120.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0 || height <= 0) return@Canvas

        when (type) {
            VisualizerType.BARS -> drawBarsVisualizer(frequencies, accentColor, width, height)
            VisualizerType.WAVEFORM -> drawWaveformVisualizer(frequencies, accentColor, width, height)
            VisualizerType.CIRCULAR -> drawCircularVisualizer(frequencies, accentColor, width, height, pulse)
        }
    }
}

private fun DrawScope.drawBarsVisualizer(
    frequencies: FloatArray,
    accentColor: Color,
    width: Float,
    height: Float
) {
    val numBars = frequencies.size.coerceAtMost(32)
    val barSpacing = 4.dp.toPx()
    val totalSpacing = barSpacing * (numBars - 1)
    val barWidth = ((width - totalSpacing) / numBars).coerceAtLeast(2.dp.toPx())

    val brush = Brush.verticalGradient(
        colors = listOf(
            accentColor,
            accentColor.copy(alpha = 0.6f),
            Color(0xFF38BDF8)
        )
    )

    for (i in 0 until numBars) {
        val magnitude = frequencies[i].coerceIn(0.05f, 1.0f)
        val barHeight = (height * magnitude).coerceAtLeast(6.dp.toPx())
        val x = i * (barWidth + barSpacing)
        val y = height - barHeight

        drawRoundRect(
            brush = brush,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
        )

        // Subtle glowing cap
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = (barWidth / 3f).coerceAtLeast(1.5.dp.toPx()),
            center = Offset(x + barWidth / 2f, y + 2.dp.toPx())
        )
    }
}

private fun DrawScope.drawWaveformVisualizer(
    frequencies: FloatArray,
    accentColor: Color,
    width: Float,
    height: Float
) {
    val numPoints = frequencies.size
    if (numPoints < 2) return

    val path = Path()
    val midY = height / 2f
    val stepX = width / (numPoints - 1)

    path.moveTo(0f, midY)
    for (i in 0 until numPoints) {
        val amp = frequencies[i] * (height / 2f) * 0.85f
        val x = i * stepX
        val y = if (i % 2 == 0) midY - amp else midY + amp
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    // Glow outline
    drawPath(
        path = path,
        color = accentColor.copy(alpha = 0.35f),
        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    // Core wave
    drawPath(
        path = path,
        brush = Brush.horizontalGradient(
            listOf(accentColor, Color(0xFFF472B6), Color(0xFF38BDF8))
        ),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawCircularVisualizer(
    frequencies: FloatArray,
    accentColor: Color,
    width: Float,
    height: Float,
    pulse: Float
) {
    val center = Offset(width / 2f, height / 2f)
    val baseRadius = (minOf(width, height) / 3.2f) * pulse
    val numBars = frequencies.size

    drawCircle(
        color = accentColor.copy(alpha = 0.15f),
        radius = baseRadius * 0.9f,
        center = center
    )

    for (i in 0 until numBars) {
        val angle = (i.toFloat() / numBars) * 2f * PI.toFloat()
        val magnitude = frequencies[i].coerceIn(0.08f, 1f)
        val barLen = magnitude * 32.dp.toPx()

        val startX = center.x + cos(angle) * baseRadius
        val startY = center.y + sin(angle) * baseRadius
        val endX = center.x + cos(angle) * (baseRadius + barLen)
        val endY = center.y + sin(angle) * (baseRadius + barLen)

        drawLine(
            brush = Brush.linearGradient(
                listOf(accentColor, Color(0xFFF43F5E)),
                start = Offset(startX, startY),
                end = Offset(endX, endY)
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
