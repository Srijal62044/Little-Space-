package com.example.ui.screens.birthday

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    val size: Float,
    val color: Color,
    var speedY: Float,
    var speedX: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    val isCircle: Boolean
)

@Composable
fun ConfettiView(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    colors: List<Color> = listOf(
        Color(0xFFFF6B81),
        Color(0xFFFFB86C),
        Color(0xFFFFD32A),
        Color(0xFF2ED573),
        Color(0xFF1E90FF),
        Color(0xFF9C88FF),
        Color(0xFFFF78CB),
        Color(0xFFFFC048)
    )
) {
    val particles = remember {
        List(particleCount) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 14f + 6f,
                color = colors[Random.nextInt(colors.size)],
                speedY = Random.nextFloat() * 0.003f + 0.0015f,
                speedX = (Random.nextFloat() - 0.5f) * 0.002f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 6f,
                isCircle = Random.nextBoolean()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti_loop")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_anim"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            p.y += p.speedY
            p.x += p.speedX
            p.rotation += p.rotationSpeed

            if (p.y > 1.1f) {
                p.y = -0.1f
                p.x = Random.nextFloat()
            }
            if (p.x < -0.1f) p.x = 1.1f
            if (p.x > 1.1f) p.x = -0.1f

            val px = p.x * width
            val py = p.y * height

            rotate(p.rotation, pivot = Offset(px, py)) {
                if (p.isCircle) {
                    drawCircle(
                        color = p.color.copy(alpha = 0.85f),
                        radius = p.size / 2,
                        center = Offset(px, py)
                    )
                } else {
                    drawRect(
                        color = p.color.copy(alpha = 0.85f),
                        topLeft = Offset(px - p.size / 2, py - p.size / 3),
                        size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.6f)
                    )
                }
            }
        }
    }
}
