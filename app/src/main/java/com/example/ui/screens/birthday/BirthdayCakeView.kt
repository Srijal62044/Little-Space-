package com.example.ui.screens.birthday

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

enum class CakeFlavor(val label: String, val primaryColor: Color, val accentColor: Color, val emoji: String) {
    STRAWBERRY("Strawberry Pink", Color(0xFFFFB7C5), Color(0xFFE11D48), "🍓"),
    CHOCOLATE("Chocolate Truffle", Color(0xFF795548), Color(0xFF3E2723), "🍫"),
    VANILLA("Vanilla Cream", Color(0xFFFFF9C4), Color(0xFFFBC02D), "🍦"),
    RAINBOW("Rainbow Velvet", Color(0xFFC084FC), Color(0xFFEC4899), "🌈")
}

@Composable
fun BirthdayCakeView(
    modifier: Modifier = Modifier
) {
    var selectedFlavor by remember { mutableStateOf(CakeFlavor.STRAWBERRY) }
    var candleCount by remember { mutableIntStateOf(3) }
    val candleLitStates = remember(candleCount) { mutableStateListOf(*Array(candleCount) { true }) }
    var userWishText by remember { mutableStateOf("") }
    var isBlowingOut by remember { mutableStateOf(false) }
    var showWishGrantedCelebration by remember { mutableStateOf(false) }

    val allCandlesOut = candleLitStates.none { it }

    // Candle flame animation
    val infiniteTransition = rememberInfiniteTransition(label = "flame_flicker")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header info
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎂 Interactive Birthday Cake",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF881337)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (allCandlesOut) "✨ Make a wish! Your candles are blown out! 🎉" else "Tap candles or press 'Blow Out Candles' to make your wish!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF334155),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Interactive Cake Display
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(2.dp, selectedFlavor.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (allCandlesOut) {
                    ConfettiView(particleCount = 40)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Candles Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        for (i in 0 until candleCount) {
                            val isLit = candleLitStates.getOrElse(i) { true }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    if (i < candleLitStates.size) {
                                        candleLitStates[i] = !candleLitStates[i]
                                    }
                                }
                            ) {
                                // Flame / Smoke
                                AnimatedVisibility(
                                    visible = isLit,
                                    enter = fadeIn() + scaleIn(),
                                    exit = fadeOut() + scaleOut()
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .scale(flameScale)
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawCircle(
                                                color = Color(0xFFFFD54F).copy(alpha = 0.6f),
                                                radius = size.width / 2.2f
                                            )
                                            drawCircle(
                                                color = Color(0xFFFF6D00),
                                                radius = size.width / 3.5f
                                            )
                                            drawCircle(
                                                color = Color(0xFFFFF59D),
                                                radius = size.width / 6f
                                            )
                                        }
                                    }
                                }

                                if (!isLit) {
                                    Text(
                                        text = "💨",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }

                                // Candle Stick
                                Box(
                                    modifier = Modifier
                                        .width(10.dp)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFFFF80AB),
                                                    Color(0xFF82B1FF)
                                                )
                                            )
                                        )
                                        .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }

                    // Cake Layer 1 (Top Tier)
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(55.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .background(selectedFlavor.primaryColor)
                            .border(
                                2.dp,
                                selectedFlavor.accentColor.copy(alpha = 0.4f),
                                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✨ ${selectedFlavor.emoji} ✨",
                            fontSize = 18.sp
                        )
                    }

                    // Frosting Line
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(10.dp)
                            .background(Color.White, RoundedCornerShape(6.dp))
                    )

                    // Cake Layer 2 (Bottom Tier)
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(70.dp)
                            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        selectedFlavor.primaryColor,
                                        selectedFlavor.primaryColor.copy(alpha = 0.85f)
                                    )
                                )
                            )
                            .border(
                                2.dp,
                                selectedFlavor.accentColor.copy(alpha = 0.4f),
                                RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.95f),
                            shadowElevation = 2.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, selectedFlavor.accentColor.copy(alpha = 0.6f)),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = if (userWishText.isNotBlank()) "Wish: \"$userWishText\"" else "Happy Birthday! 🎉",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF881337),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Cake Plate
                    Box(
                        modifier = Modifier
                            .width(250.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE2E8F0))
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Customization Controls
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Flavor Selector
                Text(
                    text = "Select Cake Flavor:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CakeFlavor.values()) { flavor ->
                        val isSelected = selectedFlavor == flavor
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFlavor = flavor },
                            label = {
                                Text(
                                    text = "${flavor.emoji} ${flavor.label}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        if (flavor == CakeFlavor.CHOCOLATE) Color.White else Color(0xFF0F172A)
                                    } else Color(0xFF1E293B)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFFFF1F2),
                                selectedContainerColor = flavor.primaryColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFFBE123C) else Color(0xFFFECDD3)
                            )
                        )
                    }
                }

                // Wish Input
                OutlinedTextField(
                    value = userWishText,
                    onValueChange = { userWishText = it },
                    placeholder = { Text("Write your secret birthday wish here...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            for (i in candleLitStates.indices) {
                                candleLitStates[i] = false
                            }
                            showWishGrantedCelebration = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("blow_candles_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                    ) {
                        Text("🌬️ Blow Out Candles", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            for (i in candleLitStates.indices) {
                                candleLitStates[i] = true
                            }
                            showWishGrantedCelebration = false
                        },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Relight", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Relight", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
