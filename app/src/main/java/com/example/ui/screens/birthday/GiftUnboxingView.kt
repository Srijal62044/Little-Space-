package com.example.ui.screens.birthday

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class GiftCoupon(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val category: String,
    var isRedeemed: Boolean = false
)

@Composable
fun GiftUnboxingView(
    modifier: Modifier = Modifier
) {
    val defaultCoupons = remember {
        mutableStateListOf(
            GiftCoupon("1", "☕ Free Specialty Coffee Pass", "Valid for one delicious iced latte or cappuccino anytime!", "☕", "Treat"),
            GiftCoupon("2", "🎬 Cozy Movie Night Pass", "Pick any movie, complete with unlimited popcorn & snacks!", "🍿", "Entertainment"),
            GiftCoupon("3", "🥐 Breakfast in Bed Voucher", "A cozy warm morning breakfast cooked & served with love.", "🥞", "Special"),
            GiftCoupon("4", "💆 30-Minute Spa & Massage", "A relaxing head & shoulder massage session to unwind.", "💆", "Wellness"),
            GiftCoupon("5", "🍕 Unlimited Pizza Feast", "Order your favorite pizza toppings with no extra questions asked!", "🍕", "Food")
        )
    }

    var isGiftOpen by remember { mutableStateOf(false) }
    var currentRevealedCoupon by remember { mutableStateOf<GiftCoupon?>(null) }
    var showAddCouponDialog by remember { mutableStateOf(false) }

    var newCouponTitle by remember { mutableStateOf("") }
    var newCouponDesc by remember { mutableStateOf("") }
    var newCouponEmoji by remember { mutableStateOf("🎁") }

    // Floating box animation
    val infiniteTransition = rememberInfiniteTransition(label = "box_float")
    val boxRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "box_rotate"
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
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎁 Mystery Gift Box Unboxing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF881337)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tap the gift box ribbon to unwrap your surprise coupons!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { showAddCouponDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE4E6))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Coupon", tint = Color(0xFFE11D48))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Interactive Gift Box
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(2.dp, Color(0xFFFECDD3), RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isGiftOpen) {
                    ConfettiView(particleCount = 50)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedContent(
                        targetState = isGiftOpen,
                        transitionSpec = {
                            fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                        },
                        label = "gift_unbox_transition"
                    ) { isOpen ->
                        if (!isOpen) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    isGiftOpen = true
                                    currentRevealedCoupon = defaultCoupons.randomOrNull()
                                }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(140.dp)
                                        .rotate(boxRotation)
                                        .shadow(12.dp, RoundedCornerShape(24.dp))
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFFB7185), Color(0xFFE11D48))
                                            )
                                        )
                                        .border(3.dp, Color.White, RoundedCornerShape(24.dp))
                                ) {
                                    Text(text = "🎁", fontSize = 72.sp)
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFFFF1F2),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDA4AF)),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Redeem, contentDescription = "Unwrap", tint = Color(0xFFE11D48))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Tap to Unwrap Surprise Gift! ✨",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF9F1239)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Revealed Coupon Card
                            currentRevealedCoupon?.let { coupon ->
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color(0xFFFFF1F2),
                                    shadowElevation = 6.dp,
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .border(2.dp, Color(0xFFFDA4AF), RoundedCornerShape(24.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "🎉 YOU UNLOCKED 🎉", style = MaterialTheme.typography.labelMedium, color = Color(0xFFBE123C), fontWeight = FontWeight.ExtraBold)

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(text = coupon.emoji, fontSize = 56.sp)

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = coupon.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF881337),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = coupon.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Button(
                                            onClick = {
                                                isGiftOpen = false
                                                currentRevealedCoupon = defaultCoupons.randomOrNull()
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                                        ) {
                                            Text("Unwrap Another Gift 🎁", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Coupon Vault List
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Text(
                    text = "Your Birthday Gift Vault (${defaultCoupons.size} Coupons)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(defaultCoupons) { coupon ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (coupon.isRedeemed) Color(0xFFF1F5F9) else Color(0xFFFFF1F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (coupon.isRedeemed) Color(0xFFCBD5E1) else Color(0xFFFECDD3)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = coupon.emoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = coupon.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (coupon.isRedeemed) Color(0xFF64748B) else Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = coupon.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (coupon.isRedeemed) Color(0xFF94A3B8) else Color(0xFF334155),
                                            maxLines = 1
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        val index = defaultCoupons.indexOf(coupon)
                                        if (index >= 0) {
                                            defaultCoupons[index] = coupon.copy(isRedeemed = !coupon.isRedeemed)
                                        }
                                    }
                                ) {
                                    Text(
                                        text = if (coupon.isRedeemed) "Redeemed ✓" else "Redeem",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (coupon.isRedeemed) Color.Gray else Color(0xFFE11D48)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Coupon Dialog
    if (showAddCouponDialog) {
        Dialog(onDismissRequest = { showAddCouponDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Add Custom Birthday Coupon 🎁", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE11D48))

                    OutlinedTextField(
                        value = newCouponTitle,
                        onValueChange = { newCouponTitle = it },
                        label = { Text("Coupon Title") },
                        placeholder = { Text("e.g. Free Ice Cream Treat") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newCouponDesc,
                        onValueChange = { newCouponDesc = it },
                        label = { Text("Short Details") },
                        placeholder = { Text("e.g. 2 scoops of chocolate fudge") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddCouponDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newCouponTitle.isNotBlank()) {
                                    defaultCoupons.add(
                                        GiftCoupon(
                                            id = System.currentTimeMillis().toString(),
                                            title = newCouponTitle,
                                            description = newCouponDesc.ifBlank { "Special Birthday Voucher!" },
                                            emoji = "🎁",
                                            category = "Custom"
                                        )
                                    )
                                    newCouponTitle = ""
                                    newCouponDesc = ""
                                    showAddCouponDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                        ) {
                            Text("Save Coupon")
                        }
                    }
                }
            }
        }
    }
}
