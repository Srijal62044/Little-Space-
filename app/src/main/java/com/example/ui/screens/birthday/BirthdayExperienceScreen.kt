package com.example.ui.screens.birthday

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BirthdayMemoryEntity
import com.example.ui.viewmodel.MainViewModel

@Composable
fun BirthdayExperienceScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val rewardConfig by viewModel.rewardConfig.collectAsStateWithLifecycle()
    val memories by viewModel.birthdayMemories.collectAsStateWithLifecycle()

    var activeFeatureTab by remember { mutableIntStateOf(0) } // 0: Cake, 1: Gift Unbox, 2: Scrapbook, 3: Rewind, 4: Balloon Pop, 5: Wishlist Planner, 6: Story Walkthrough
    var currentScene by remember { mutableIntStateOf(1) }
    var showFinalSurpriseDialog by remember { mutableStateOf(false) }

    val featureTabs = listOf(
        "🎂 Cake & Candles",
        "🎁 Gift Unbox",
        "📖 Scrapbook",
        "⏪ Rewind",
        "🎈 Pop Game",
        "📋 Planner",
        "💌 Story Walk"
    )

    // Celebratory pastel gradient background
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFF0F5), // Lavender blush
            Color(0xFFFFE4E1), // Misty rose
            Color(0xFFFFF5EE), // Seashell
            Color(0xFFFDF2F8)  // Soft pink
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .testTag("birthday_experience_screen")
    ) {
        // Floating celebratory confetti in the background
        ConfettiView(
            modifier = Modifier.fillMaxSize(),
            particleCount = 35
        )

        // Content container
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.85f))
                                .testTag("birthday_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFFE11D48)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 3.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDA4AF))
                        ) {
                            Text(
                                text = "🎉 Birthday Hub",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF881337),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Feature Selection Tabs Row
                    ScrollableTabRow(
                        selectedTabIndex = activeFeatureTab,
                        containerColor = Color.White,
                        contentColor = Color(0xFFBE123C),
                        edgePadding = 12.dp,
                        indicator = {},
                        divider = {}
                    ) {
                        featureTabs.forEachIndexed { index, title ->
                            val isSelected = activeFeatureTab == index
                            Tab(
                                selected = isSelected,
                                onClick = { activeFeatureTab = index },
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) Color(0xFFE11D48) else Color(0xFFFFF1F2)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFFBE123C) else Color(0xFFFECDD3),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else Color(0xFF4C0519)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                when (activeFeatureTab) {
                    0 -> BirthdayCakeView()
                    1 -> GiftUnboxingView()
                    2 -> ScrapbookBookletView()
                    3 -> CountdownRewindView()
                    4 -> BalloonPopView()
                    5 -> WishlistPlannerView()
                    6 -> {
                        // Original Story Walkthrough
                        AnimatedContent(
                            targetState = currentScene,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> -width } + fadeOut()
                                    )
                                } else {
                                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> width } + fadeOut()
                                    )
                                }
                            },
                            label = "birthday_scene_transition"
                        ) { scene ->
                            when (scene) {
                                1 -> Scene1Opening(
                                    title = rewardConfig.birthdayTitle,
                                    onNext = { currentScene = 2 }
                                )
                                2 -> Scene2PersonalMessage(
                                    message = rewardConfig.birthdayMessage,
                                    onBack = { currentScene = 1 },
                                    onNext = { currentScene = 3 }
                                )
                                3 -> Scene3MemoriesGallery(
                                    memories = memories,
                                    onBack = { currentScene = 2 },
                                    onNext = { currentScene = 4 }
                                )
                                4 -> Scene4BirthdayWish(
                                    wish = rewardConfig.birthdayWish,
                                    onBack = { currentScene = 3 },
                                    onNext = { currentScene = 5 }
                                )
                                5 -> Scene5FinalSurprise(
                                    onBack = { currentScene = 4 },
                                    onOpenSurprise = { showFinalSurpriseDialog = true }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Final Secret Surprise Dialog
        if (showFinalSurpriseDialog) {
            FinalSurpriseModal(
                finalNote = rewardConfig.birthdayFinalSurpriseNote,
                onDismiss = { showFinalSurpriseDialog = false }
            )
        }
    }
}

/* ========================================================================= */
/* SCENE 1 — OPENING                                                         */
/* ========================================================================= */
@Composable
private fun Scene1Opening(
    title: String,
    onNext: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_scale")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cake_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Floating Cake Icon with glowing halo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(130.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
                .shadow(elevation = 12.dp, shape = CircleShape)
                .border(3.dp, Color(0xFFFDA4AF), CircleShape)
        ) {
            Text(text = "🎂", fontSize = 64.sp)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFE4E6),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDA4AF)),
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = "✨ A Special Day Just For You ✨",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF9F1239),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF881337),
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Someone made a little digital gift box to celebrate your special day. Tap below to begin! 🌷",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF0F172A),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .testTag("birthday_open_surprise_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE11D48)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Open Your Surprise",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.White
                )
            }
        }
    }
}

/* ========================================================================= */
/* SCENE 2 — PERSONAL MESSAGE                                                */
/* ========================================================================= */
@Composable
private fun Scene2PersonalMessage(
    message: String,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFFECDD3), RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "💌", fontSize = 42.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Dear Priyanka,",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF881337)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp,
                        fontStyle = FontStyle.Normal
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFF1F2)
                    ) {
                        Text(
                            text = "“May today bring you pure smiles and cozy serenity.” 🌸",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF881337),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE11D48)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48))
                ) {
                    Text(text = "← Back", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    modifier = Modifier.testTag("birthday_scene2_next")
                ) {
                    Text(text = "See Memories 📸 →", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/* ========================================================================= */
/* SCENE 3 — MEMORIES / GALLERY                                              */
/* ========================================================================= */
@Composable
private fun Scene3MemoriesGallery(
    memories: List<BirthdayMemoryEntity>,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { memories.size.coerceAtLeast(1) })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Memories & Milestones 📸✨",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF881337),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Swipe to look through some special moments",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF334155),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (memories.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "No memories added yet. (Owner can configure in admin)",
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val memory = memories[page]
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                        .border(2.dp, Color(0xFFFECDD3), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (memory.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = memory.imageUrl,
                                contentDescription = memory.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(16.dp))
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF1F2))
                            ) {
                                Text(text = memory.emoji, fontSize = 40.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFE4E6),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDA4AF))
                        ) {
                            Text(
                                text = memory.dateTag,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9F1239),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = memory.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = memory.caption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF334155),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dot indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(memories.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFFE11D48) else Color(0xFFFDA4AF))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE11D48)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48))
            ) {
                Text(text = "← Back", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onNext,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                modifier = Modifier.testTag("birthday_scene3_next")
            ) {
                Text(text = "Birthday Wish ✨ →", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/* ========================================================================= */
/* SCENE 4 — BIRTHDAY WISH                                                   */
/* ========================================================================= */
@Composable
private fun Scene4BirthdayWish(
    wish: String,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFFECDD3), RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🌷✨", fontSize = 38.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "A Wholesome Birthday Wish",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFBE123C),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = wish,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF1F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDA4AF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "“You are capable of amazing things, and loved by so many.”",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFF881337),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE11D48)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48))
                ) {
                    Text(text = "← Back", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    modifier = Modifier.testTag("birthday_scene4_next")
                ) {
                    Text(text = "Final Surprise 🎁 →", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/* ========================================================================= */
/* SCENE 5 — FINAL SURPRISE                                                  */
/* ========================================================================= */
@Composable
private fun Scene5FinalSurprise(
    onBack: () -> Unit,
    onOpenSurprise: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_final")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gift_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(110.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
                .shadow(10.dp, CircleShape)
                .border(2.5.dp, Color(0xFFFDA4AF), CircleShape)
        ) {
            Text(text = "🎁", fontSize = 52.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Okay… that's all for now. 😌🎁",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF881337),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Hope your day is wrapped in joy, peace, and unforgettable moments.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF0F172A),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onOpenSurprise,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .testTag("birthday_final_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "One last thing… ✨",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE11D48)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48))
        ) {
            Text(text = "← Back to Wish", fontWeight = FontWeight.SemiBold)
        }
    }
}

/* ========================================================================= */
/* FINAL SURPRISE POPUP MODAL                                                */
/* ========================================================================= */
@Composable
private fun FinalSurpriseModal(
    finalNote: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("birthday_final_modal")
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFF1F2), Color.White)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "🎉🌷🎂", fontSize = 36.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "A Secret Final Note ✨",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFBE123C),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = finalNote,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Thank You! 🌷",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
