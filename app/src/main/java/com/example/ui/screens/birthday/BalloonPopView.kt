package com.example.ui.screens.birthday

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

data class FloatingBalloon(
    val id: Int,
    var xPercent: Float,
    var yPercent: Float,
    val sizeDp: Float,
    val color: Color,
    val secretWish: String,
    var isPopped: Boolean = false
)

@Composable
fun BalloonPopView(
    modifier: Modifier = Modifier
) {
    val balloonColors = remember {
        listOf(
            Color(0xFFFF6B81),
            Color(0xFFFFB86C),
            Color(0xFFFFD32A),
            Color(0xFF2ED573),
            Color(0xFF1E90FF),
            Color(0xFF9C88FF),
            Color(0xFFFF78CB)
        )
    }

    val wishes = remember {
        listOf(
            "✨ Wishing you boundless joy & laughter!",
            "🌷 May all your sweet dreams come true today!",
            "🎂 Happy Birthday to an absolute star!",
            "🎁 You deserve all the happiness in the world!",
            "💖 Stay bright, cheerful, and awesome!",
            "☕ Free cozy coffee pass unlocked!",
            "🌸 Celebrating your wonderful smile!",
            "👑 Pure royalty & sparkle vibes!"
        )
    }

    val balloons = remember {
        mutableStateListOf<FloatingBalloon>().apply {
            repeat(14) { index ->
                add(
                    FloatingBalloon(
                        id = index,
                        xPercent = (10 + (index % 4) * 22 + Random.nextInt(-5, 5)).toFloat() / 100f,
                        yPercent = (15 + (index / 4) * 20 + Random.nextInt(-5, 5)).toFloat() / 100f,
                        sizeDp = Random.nextInt(60, 85).toFloat(),
                        color = balloonColors[index % balloonColors.size],
                        secretWish = wishes[index % wishes.size]
                    )
                )
            }
        }
    }

    var popScore by remember { mutableIntStateOf(0) }
    var lastRevealedWish by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header & Score
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎈 Balloon-Popping Celebration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF881337)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tap floating balloons to pop & reveal hidden wishes!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFE4E6),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDA4AF))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎈 $popScore / ${balloons.size}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF9F1239), fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Interactive Balloon Canvas Area
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(2.dp, Color(0xFFFECDD3), RoundedCornerShape(24.dp))
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFF1F2), Color.White)
                        )
                    )
            ) {
                val boxWidth = maxWidth
                val boxHeight = maxHeight

                // Render Balloons
                balloons.forEach { balloon ->
                    if (!balloon.isPopped) {
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = boxWidth * balloon.xPercent,
                                    y = boxHeight * balloon.yPercent
                                )
                                .size(balloon.sizeDp.dp)
                                .clickable {
                                    balloon.isPopped = true
                                    popScore++
                                    lastRevealedWish = balloon.secretWish
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Balloon Body
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shadow(6.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.6f),
                                                balloon.color,
                                                balloon.color.copy(alpha = 0.9f)
                                            ),
                                            center = Offset(30f, 30f)
                                        )
                                    )
                                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🎈", fontSize = (balloon.sizeDp * 0.4f).sp)
                            }
                        }
                    }
                }

                // Center wish reveal snackbox overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = lastRevealedWish != null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                    lastRevealedWish?.let { wish ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFE11D48),
                            shadowElevation = 6.dp,
                            modifier = Modifier.fillMaxWidth(0.95f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = wish,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )

                                TextButton(onClick = { lastRevealedWish = null }) {
                                    Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    balloons.forEach { it.isPopped = false }
                    popScore = 0
                    lastRevealedWish = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("reinflate_balloons_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reinflate")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Re-inflate Balloons 🎈", fontWeight = FontWeight.Bold)
            }
        }
    }
}
