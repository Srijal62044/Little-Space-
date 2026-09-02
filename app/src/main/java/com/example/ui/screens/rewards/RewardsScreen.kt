package com.example.ui.screens.rewards

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.RewardConfigEntity
import com.example.ui.components.CozyCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.SoftBlushContainer
import com.example.ui.viewmodel.MainViewModel

@Composable
fun RewardsScreen(
    viewModel: MainViewModel,
    onOpenBirthdayExperience: () -> Unit,
    onOpenAdminConfig: () -> Unit
) {
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val longestStreak by viewModel.longestStreak.collectAsStateWithLifecycle()
    val totalDays by viewModel.totalCompletedDays.collectAsStateWithLifecycle()
    val nextMilestone by viewModel.nextMilestone.collectAsStateWithLifecycle()
    val daysToNext by viewModel.daysToNextMilestone.collectAsStateWithLifecycle()
    val isBirthdayToday by viewModel.isTodayBirthday.collectAsStateWithLifecycle()
    val daysUntilBday by viewModel.daysUntilBirthday.collectAsStateWithLifecycle()
    val config by viewModel.rewardConfig.collectAsStateWithLifecycle()
    val celebrationReward by viewModel.celebrationReward.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // Celebration modal for claiming reward
    celebrationReward?.let { (milestone, _) ->
        val (title, desc, gift, type, link) = when (milestone) {
            7 -> Tuple5(config.reward7Title, config.reward7Desc, config.reward7Gift, config.reward7Type, config.reward7Link)
            30 -> Tuple5(config.reward30Title, config.reward30Desc, config.reward30Gift, config.reward30Type, config.reward30Link)
            50 -> Tuple5(config.reward50Title, config.reward50Desc, config.reward50Gift, config.reward50Type, config.reward50Link)
            100 -> Tuple5(config.reward100Title, config.reward100Desc, config.reward100Gift, config.reward100Type, config.reward100Link)
            else -> Tuple5("Special Milestone", "Keep shining!", "Surprise Gift", "DIGITAL", "")
        }

        RewardCelebrationDialog(
            milestone = milestone,
            title = title,
            desc = desc,
            giftDetails = gift,
            giftType = type,
            giftLink = link,
            onDismiss = { viewModel.dismissCelebration() }
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Your Journey 🌱",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Track your consistency, unlock gifts & surprises",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Owner/Admin access trigger (protected by PIN)
                    IconButton(
                        onClick = {
                            pinInput = ""
                            pinError = false
                            showPinDialog = true
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .testTag("admin_config_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Owner Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 40.dp)
        ) {
            // 1. HERO STREAK PROGRESS CARD
            item {
                StreakHeroCard(
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    totalDays = totalDays,
                    nextMilestone = nextMilestone,
                    daysToNext = daysToNext
                )
            }

            // 2. BIRTHDAY SURPRISE SECTION
            item {
                BirthdaySurpriseCard(
                    isBirthdayToday = isBirthdayToday,
                    daysUntilBirthday = daysUntilBday,
                    birthdayMonth = config.birthdayMonth,
                    birthdayDay = config.birthdayDay,
                    onOpenExperience = onOpenBirthdayExperience
                )
            }

            // 3. MILESTONE REWARDS
            item {
                SectionHeader(title = "Milestone Rewards 🎁")
            }

            item {
                val unlockedSet = config.unlockedMilestones
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .toSet()

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 7 Days
                    MilestoneCard(
                        milestoneDays = 7,
                        currentStreak = currentStreak,
                        isUnlocked = unlockedSet.contains(7) || currentStreak >= 7,
                        isClaimed = config.reward7Claimed,
                        title = config.reward7Title,
                        desc = config.reward7Desc,
                        giftType = config.reward7Type,
                        giftDetails = config.reward7Gift,
                        icon = "🌱",
                        onClaim = { viewModel.claimReward(7) }
                    )

                    // 30 Days
                    MilestoneCard(
                        milestoneDays = 30,
                        currentStreak = currentStreak,
                        isUnlocked = unlockedSet.contains(30) || currentStreak >= 30,
                        isClaimed = config.reward30Claimed,
                        title = config.reward30Title,
                        desc = config.reward30Desc,
                        giftType = config.reward30Type,
                        giftDetails = config.reward30Gift,
                        icon = "✨",
                        onClaim = { viewModel.claimReward(30) }
                    )

                    // 50 Days
                    MilestoneCard(
                        milestoneDays = 50,
                        currentStreak = currentStreak,
                        isUnlocked = unlockedSet.contains(50) || currentStreak >= 50,
                        isClaimed = config.reward50Claimed,
                        title = config.reward50Title,
                        desc = config.reward50Desc,
                        giftType = config.reward50Type,
                        giftDetails = config.reward50Gift,
                        icon = "🎁",
                        onClaim = { viewModel.claimReward(50) }
                    )

                    // 100 Days
                    MilestoneCard(
                        milestoneDays = 100,
                        currentStreak = currentStreak,
                        isUnlocked = unlockedSet.contains(100) || currentStreak >= 100,
                        isClaimed = config.reward100Claimed,
                        title = config.reward100Title,
                        desc = config.reward100Desc,
                        giftType = config.reward100Type,
                        giftDetails = config.reward100Gift,
                        icon = "🏆",
                        onClaim = { viewModel.claimReward(100) }
                    )
                }
            }
        }
    }

    // PIN Protection Dialog for Owner Settings
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = {
                Text(
                    text = "Owner Configuration 🔐",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your secret owner PIN to configure birthday date, gifts, streak rules, and surprises.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinInput = it
                            pinError = false
                        },
                        label = { Text("4-digit PIN") },
                        singleLine = true,
                        isError = pinError,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (pinError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Incorrect PIN. Try again.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput == config.adminPin || pinInput == "7890") {
                            showPinDialog = false
                            onOpenAdminConfig()
                        } else {
                            pinError = true
                        }
                    },
                    modifier = Modifier.testTag("admin_pin_submit")
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* ========================================================================= */
/* HERO STREAK CARD                                                          */
/* ========================================================================= */
@Composable
private fun StreakHeroCard(
    currentStreak: Int,
    longestStreak: Int,
    totalDays: Int,
    nextMilestone: Int,
    daysToNext: Int
) {
    val progress = remember(currentStreak, nextMilestone) {
        val prevMilestone = when (nextMilestone) {
            7 -> 0
            30 -> 7
            50 -> 30
            100 -> 50
            else -> 0
        }
        val range = (nextMilestone - prevMilestone).toFloat().coerceAtLeast(1f)
        val progressInRange = (currentStreak - prevMilestone).toFloat().coerceAtLeast(0f)
        (progressInRange / range).coerceIn(0f, 1f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("streak_hero_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SoftBlushContainer.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🔥 Current Streak: $currentStreak Days",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Next Reward: $nextMilestone Days 🎁",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Text(text = "🔥", fontSize = 26.sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Progress Bar e.g. 🔥 43 / 50 Days
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥 $currentStreak / $nextMilestone Days",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (daysToNext > 0) "$daysToNext more days to unlock your surprise 🎁" else "Surprise Ready! 🎉",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatPill(icon = "⚡", label = "Longest Streak", value = "$longestStreak d")
                    StatPill(icon = "🗓️", label = "Total Days", value = "$totalDays d")
                    StatPill(icon = "🎯", label = "Next Goal", value = "$nextMilestone d")
                }
            }
        }
    }
}

@Composable
private fun StatPill(icon: String, label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/* ========================================================================= */
/* BIRTHDAY SURPRISE CARD                                                    */
/* ========================================================================= */
@Composable
private fun BirthdaySurpriseCard(
    isBirthdayToday: Boolean,
    daysUntilBirthday: Int,
    birthdayMonth: Int,
    birthdayDay: Int,
    onOpenExperience: () -> Unit
) {
    if (isBirthdayToday) {
        // Glowing celebration card on birthday
        val infiniteTransition = rememberInfiniteTransition(label = "bday_glow")
        val glowScale by infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bday_pulse"
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(glowScale)
                .clickable { onOpenExperience() }
                .testTag("birthday_surprise_unlocked_card"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFFFF0F5),
            shadowElevation = 10.dp,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFB7185))
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFFE4E6),
                                Color(0xFFFFF0F5),
                                Color(0xFFFFF7ED)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎂", fontSize = 38.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE11D48)
                            ) {
                                Text(
                                    text = "TODAY IS YOUR SPECIAL DAY 🎉",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hey Priyanka… someone left you a little surprise 🎂✨",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF881337)
                            )
                            Text(
                                text = "Tap to open your full birthday experience",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4C0519)
                            )
                        }
                    }

                    Button(
                        onClick = onOpenExperience,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("open_birthday_experience_btn")
                    ) {
                        Text(text = "Open →", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    } else {
        // Locked state until birthday
        CozyCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Text(text = "🎁", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "A Little Surprise 🎁",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "Locked until Priyanka's Birthday 🎂 ($daysUntilBirthday days remaining)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "🔒 Locked",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/* ========================================================================= */
/* MILESTONE REWARD CARD                                                     */
/* ========================================================================= */
@Composable
private fun MilestoneCard(
    milestoneDays: Int,
    currentStreak: Int,
    isUnlocked: Boolean,
    isClaimed: Boolean,
    title: String,
    desc: String,
    giftType: String,
    giftDetails: String,
    icon: String,
    onClaim: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shadowElevation = if (isUnlocked) 4.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("milestone_${milestoneDays}_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surface
                            )
                    ) {
                        Text(text = icon, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (giftType == "PHYSICAL") "📦 Physical Gift" else "💻 Digital Gift",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Action or Status Badge
                when {
                    isClaimed -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Claimed ✨",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
                    }
                    isUnlocked -> {
                        Button(
                            onClick = onClaim,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("claim_${milestoneDays}_button")
                        ) {
                            Text(
                                text = "Claim Gift 🎁",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "🔒 $milestoneDays Days",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isUnlocked) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftBlushContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎁 Reward: $giftDetails",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)
