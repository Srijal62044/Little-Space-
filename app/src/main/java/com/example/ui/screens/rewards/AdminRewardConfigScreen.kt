package com.example.ui.screens.rewards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BirthdayMemoryEntity
import com.example.data.local.entity.RewardConfigEntity
import com.example.notification.PushNotificationManager
import com.example.ui.components.CozyCard
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun AdminRewardConfigScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val config by viewModel.rewardConfig.collectAsStateWithLifecycle()
    val memories by viewModel.birthdayMemories.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()

    var isPushEnabled by remember(config) { mutableStateOf(config.isBirthdayNotificationEnabled) }
    var pushFrequency by remember(config) { mutableStateOf(config.notificationFrequency.ifBlank { "2_HOURS" }) }
    var allowFinal2355 by remember(config) { mutableStateOf(config.allowFinalNotification2355) }
    var showSchedulePreviewDialog by remember { mutableStateOf(false) }
    var selectedTestSlot by remember { mutableStateOf("00:00") }
    var testPushStatusMessage by remember { mutableStateOf<String?>(null) }
    var isSendingTestPush by remember { mutableStateOf(false) }

    // Daily Countdown Notification Settings
    var isCountdownEnabled by remember(config) { mutableStateOf(config.isCountdownNotificationEnabled) }
    var countdownHour by remember(config) { mutableIntStateOf(config.countdownNotificationHour) }
    var countdownMinute by remember(config) { mutableIntStateOf(config.countdownNotificationMinute) }
    var selectedCountdownTestDays by remember { mutableIntStateOf(8) }
    var countdownTestStatusMessage by remember { mutableStateOf<String?>(null) }
    var isSendingCountdownTestPush by remember { mutableStateOf(false) }

    var bdayMonth by remember(config) { mutableIntStateOf(config.birthdayMonth) }
    var bdayDay by remember(config) { mutableIntStateOf(config.birthdayDay) }
    var isTestMode by remember(config) { mutableStateOf(config.isBirthdayTestMode) }
    var streakRule by remember(config) { mutableStateOf(config.streakRule) }
    var adminPin by remember(config) { mutableStateOf(config.adminPin) }

    var bdayTitle by remember(config) { mutableStateOf(config.birthdayTitle) }
    var bdayMessage by remember(config) { mutableStateOf(config.birthdayMessage) }
    var bdayWish by remember(config) { mutableStateOf(config.birthdayWish) }
    var bdayFinalNote by remember(config) { mutableStateOf(config.birthdayFinalSurpriseNote) }

    // Milestone 7
    var r7Title by remember(config) { mutableStateOf(config.reward7Title) }
    var r7Desc by remember(config) { mutableStateOf(config.reward7Desc) }
    var r7Gift by remember(config) { mutableStateOf(config.reward7Gift) }
    var r7Type by remember(config) { mutableStateOf(config.reward7Type) }
    var r7Link by remember(config) { mutableStateOf(config.reward7Link) }

    // Milestone 30
    var r30Title by remember(config) { mutableStateOf(config.reward30Title) }
    var r30Desc by remember(config) { mutableStateOf(config.reward30Desc) }
    var r30Gift by remember(config) { mutableStateOf(config.reward30Gift) }
    var r30Type by remember(config) { mutableStateOf(config.reward30Type) }
    var r30Link by remember(config) { mutableStateOf(config.reward30Link) }

    // Milestone 50
    var r50Title by remember(config) { mutableStateOf(config.reward50Title) }
    var r50Desc by remember(config) { mutableStateOf(config.reward50Desc) }
    var r50Gift by remember(config) { mutableStateOf(config.reward50Gift) }
    var r50Type by remember(config) { mutableStateOf(config.reward50Type) }
    var r50Link by remember(config) { mutableStateOf(config.reward50Link) }

    // Milestone 100
    var r100Title by remember(config) { mutableStateOf(config.reward100Title) }
    var r100Desc by remember(config) { mutableStateOf(config.reward100Desc) }
    var r100Gift by remember(config) { mutableStateOf(config.reward100Gift) }
    var r100Type by remember(config) { mutableStateOf(config.reward100Type) }
    var r100Link by remember(config) { mutableStateOf(config.reward100Link) }

    var showAddMemoryDialog by remember { mutableStateOf(false) }
    var newMemoryEmoji by remember { mutableStateOf("🌸") }
    var newMemoryTitle by remember { mutableStateOf("") }
    var newMemoryCaption by remember { mutableStateOf("") }
    var newMemoryTag by remember { mutableStateOf("Special Moment") }

    fun saveAll() {
        val updated = config.copy(
            birthdayMonth = bdayMonth,
            birthdayDay = bdayDay,
            isBirthdayTestMode = isTestMode,
            streakRule = streakRule,
            adminPin = adminPin.ifBlank { "7890" },
            birthdayTitle = bdayTitle,
            birthdayMessage = bdayMessage,
            birthdayWish = bdayWish,
            birthdayFinalSurpriseNote = bdayFinalNote,
            reward7Title = r7Title,
            reward7Desc = r7Desc,
            reward7Gift = r7Gift,
            reward7Type = r7Type,
            reward7Link = r7Link,
            reward30Title = r30Title,
            reward30Desc = r30Desc,
            reward30Gift = r30Gift,
            reward30Type = r30Type,
            reward30Link = r30Link,
            reward50Title = r50Title,
            reward50Desc = r50Desc,
            reward50Gift = r50Gift,
            reward50Type = r50Type,
            reward50Link = r50Link,
            reward100Title = r100Title,
            reward100Desc = r100Desc,
            reward100Gift = r100Gift,
            reward100Type = r100Type,
            reward100Link = r100Link,
            isBirthdayNotificationEnabled = isPushEnabled,
            notificationFrequency = pushFrequency,
            allowFinalNotification2355 = allowFinal2355,
            isCountdownNotificationEnabled = isCountdownEnabled,
            countdownNotificationHour = countdownHour,
            countdownNotificationMinute = countdownMinute
        )
        viewModel.updateRewardConfig(updated)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            saveAll()
                            onBack()
                        },
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Admin Configuration 🔐",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage birthday & milestone surprise gifts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {
                        saveAll()
                        onBack()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("admin_save_button")
                ) {
                    Text("Save")
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
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // 1. QUICK TESTING TOOLS
            item {
                CozyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Column {
                        Text(
                            text = "⚡ Quick Testing Controls",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Instantly test streak triggers or birthday mode without waiting.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Preview Birthday Mode Now:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Switch(
                                checked = isTestMode,
                                onCheckedChange = {
                                    isTestMode = it
                                    viewModel.setBirthdayTestMode(it)
                                },
                                modifier = Modifier.testTag("admin_birthday_test_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Fast-forward Streak (Current: $currentStreak days):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0, 7, 30, 50, 100).forEach { days ->
                                OutlinedButton(
                                    onClick = { viewModel.setManualStreak(days) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("$days d", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 2. DAILY BIRTHDAY COUNTDOWN PUSH NOTIFICATIONS
            item {
                SectionHeader(title = "📅 Daily Birthday Countdown Push System")
            }

            item {
                val pushManager = remember { PushNotificationManager(context) }
                val isPermissionGranted = pushManager.isNotificationPermissionGranted()
                val currentTz = TimeZone.getDefault().id

                CozyCard(
                    modifier = Modifier.fillMaxWidth().testTag("admin_countdown_push_card"),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Switch & Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Countdown Notifications",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Birthday Countdown Notifications",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Sends 1 daily notification leading up to September 10",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isCountdownEnabled,
                                onCheckedChange = { isCountdownEnabled = it },
                                modifier = Modifier.testTag("admin_countdown_enabled_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Target Birthday & Calculation Info
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🎂 Target Birthday:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Priyanka • September 10 (Every Year)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• Dynamic calendar-day calculation in local timezone (anti-UTC drift)\n• 1 notification per day with server-side duplicate prevention\n• Missed notifications are never spammed all at once",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Configurable Notification Time
                        Text(
                            text = "Daily Notification Delivery Time",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Current: ${String.format("%02d:%02d", countdownHour, countdownMinute)} (${if (countdownHour < 12) "$countdownHour:00 AM" else "${if (countdownHour == 12) 12 else countdownHour - 12}:00 PM"} local time)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Preset Time Chips (Default: 10:00 AM)
                        val timePresets = listOf(
                            Pair(8, "08:00 AM"),
                            Pair(9, "09:00 AM"),
                            Pair(10, "10:00 AM (Default)"),
                            Pair(11, "11:00 AM"),
                            Pair(12, "12:00 PM"),
                            Pair(18, "06:00 PM"),
                            Pair(20, "08:00 PM")
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(timePresets) { (hour, label) ->
                                FilterChip(
                                    selected = countdownHour == hour && countdownMinute == 0,
                                    onClick = {
                                        countdownHour = hour
                                        countdownMinute = 0
                                    },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (hour == 10) FontWeight.Bold else FontWeight.Normal) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Test Countdown Push Notification
                        Text(
                            text = "🧪 Test Daily Countdown Push Notification",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Dispatches a real OS push notification without altering real countdown database state.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Select Days Remaining for Test:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val countdownTestSlots = listOf(
                            Pair(8, "8 Days (Sep 2)"),
                            Pair(7, "7 Days (Sep 3)"),
                            Pair(6, "6 Days (Sep 4)"),
                            Pair(5, "5 Days (Sep 5)"),
                            Pair(4, "4 Days (Sep 6)"),
                            Pair(3, "3 Days (Sep 7)"),
                            Pair(2, "2 Days (Sep 8)"),
                            Pair(1, "1 Day (Sep 9)"),
                            Pair(0, "Birthday (Sep 10)")
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(countdownTestSlots) { (days, label) ->
                                FilterChip(
                                    selected = selectedCountdownTestDays == days,
                                    onClick = { selectedCountdownTestDays = days },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }

                        if (countdownTestStatusMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = countdownTestStatusMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                isSendingCountdownTestPush = true
                                viewModel.sendTestCountdownPushNotification(selectedCountdownTestDays) { success ->
                                    isSendingCountdownTestPush = false
                                    countdownTestStatusMessage = if (success) {
                                        "🧪 Real test countdown push ($selectedCountdownTestDays Days) posted to OS! Tap to open Home & highlight card."
                                    } else {
                                        "Failed to post test countdown push. Check notification permissions."
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_send_test_countdown_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = !isSendingCountdownTestPush
                        ) {
                            if (isSendingCountdownTestPush) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sending Test Push...")
                            } else {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send Test Countdown Notification ($selectedCountdownTestDays d)")
                            }
                        }
                    }
                }
            }

            // 3. BIRTHDAY DAY NOTIFICATION MULTI-SERIES SCHEDULE (SEPTEMBER 10)
            item {
                SectionHeader(title = "🔔 Birthday Day Multi-Wish Schedule (10 Sept)")
            }

            item {
                val pushManager = remember { PushNotificationManager(context) }
                val isPermissionGranted = pushManager.isNotificationPermissionGranted()
                val currentTz = TimeZone.getDefault().id

                CozyCard(
                    modifier = Modifier.fillMaxWidth().testTag("admin_push_notification_card"),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Switch & Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Push Notifications",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Birthday Day Notification Series",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Sends multiple unique birthday wishes on 10 Sept",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isPushEnabled,
                                onCheckedChange = { isPushEnabled = it },
                                modifier = Modifier.testTag("admin_push_enabled_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Frequency Configuration
                        Text(
                            text = "Notification Frequency & Window",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = pushFrequency == "2_HOURS",
                                onClick = { pushFrequency = "2_HOURS" },
                                label = { Text("Every 2 hours (Default)", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = pushFrequency == "1_HOUR",
                                onClick = { pushFrequency = "1_HOUR" },
                                label = { Text("Every 1 hour", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Timing window summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Schedule Window:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Start: 12:00 AM → End: 11:55 PM",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 23:55 Final Notification toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Allow 23:55 Final Notification",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Sends 'One Last Birthday Wish' 5 minutes before midnight",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = allowFinal2355,
                                onCheckedChange = { allowFinal2355 = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Diagnostics Grid
                        Text(
                            text = "Diagnostics & Server State",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // 1. Permission status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Push Permission:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPermissionGranted) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    text = if (isPermissionGranted) "GRANTED 🟢" else "DENIED / NEEDED ⚠️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPermissionGranted) Color(0xFF166534) else Color(0xFF991B1B),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 2. User Timezone
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Device Timezone:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentTz,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 3. Sent slots for current year
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sent Slots (${config.lastBirthdayNotificationYear}):",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (config.sentBirthdaySlots.isNotBlank()) config.sentBirthdaySlots else "None yet (Ready)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 4. FCM Token status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FCM Registration Token:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (config.fcmToken.isNotBlank()) "${config.fcmToken.take(10)}..." else "Ready (Local FCM)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                if (config.fcmToken.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(config.fcmToken))
                                            testPushStatusMessage = "FCM Token copied to clipboard!"
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Token",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (testPushStatusMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = testPushStatusMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Test Slot Selector Row
                        Text(
                            text = "Select Wish Slot for Test Push:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val slots = com.example.notification.BirthdayNotificationMessages.SCHEDULE_SERIES
                            items(slots) { item ->
                                FilterChip(
                                    selected = selectedTestSlot == item.slotTime,
                                    onClick = { selectedTestSlot = item.slotTime },
                                    label = { Text(item.slotTime, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Send Test, Preview Schedule, Sync FCM
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isSendingTestPush = true
                                        viewModel.sendTestPushNotification(selectedTestSlot) { success ->
                                            isSendingTestPush = false
                                            testPushStatusMessage = if (success) {
                                                "🧪 Test push notification ($selectedTestSlot) dispatched! Tap it to open surprise."
                                            } else {
                                                "Failed to send test push. Check notification permissions."
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("admin_send_test_push_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Send Test Push ($selectedTestSlot)", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.syncPushRegistration()
                                        testPushStatusMessage = "FCM registration and background worker synchronized!"
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("admin_sync_fcm_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sync", fontSize = 12.sp)
                                }
                            }

                            // Preview Today's Notification Schedule Button
                            FilledTonalButton(
                                onClick = { showSchedulePreviewDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_preview_schedule_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ListAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Preview Today's Notification Schedule", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // 3. BIRTHDAY DATE & TEXT SETTINGS
            item {
                SectionHeader(title = "🎂 Birthday Configuration")
            }

            item {
                CozyCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = bdayMonth.toString(),
                                onValueChange = { bdayMonth = it.toIntOrNull()?.coerceIn(1, 12) ?: 1 },
                                label = { Text("Month (1-12)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = bdayDay.toString(),
                                onValueChange = { bdayDay = it.toIntOrNull()?.coerceIn(1, 31) ?: 1 },
                                label = { Text("Day (1-31)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = bdayTitle,
                            onValueChange = { bdayTitle = it },
                            label = { Text("Opening Scene Title") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = bdayMessage,
                            onValueChange = { bdayMessage = it },
                            label = { Text("Scene 2: Personal Message") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = bdayWish,
                            onValueChange = { bdayWish = it },
                            label = { Text("Scene 4: Birthday Wish") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = bdayFinalNote,
                            onValueChange = { bdayFinalNote = it },
                            label = { Text("Scene 5: Final Secret Surprise Note") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // 3. BIRTHDAY MEMORIES GALLERY
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = "📸 Birthday Memories Gallery")
                    IconButton(
                        onClick = { showAddMemoryDialog = true },
                        modifier = Modifier.testTag("admin_add_memory_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add Memory", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            items(memories) { memory ->
                CozyCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = memory.emoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = memory.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = memory.caption,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.deleteBirthdayMemory(memory) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // 4. STREAK RULES & REWARDS CONFIGURATION
            item {
                SectionHeader(title = "🌱 Streak Calculation Rules")
            }

            item {
                CozyCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "Daily Requirement to count towards streak:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        listOf(
                            "TASK_OR_HABIT" to "Complete at least 1 task OR 1 habit (Recommended)",
                            "AT_LEAST_ONE_TASK" to "Complete at least 1 task",
                            "AT_LEAST_ONE_HABIT" to "Complete at least 1 habit",
                            "ALL_HABITS" to "Complete ALL habits for the day"
                        ).forEach { (key, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { streakRule = key }
                                    .padding(vertical = 6.dp)
                            ) {
                                RadioButton(
                                    selected = streakRule == key,
                                    onClick = { streakRule = key }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = label, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // 5. MILESTONES (7, 30, 50, 100)
            item {
                SectionHeader(title = "🎁 Milestone Gifts & Surprises")
            }

            // 7 Days
            item {
                MilestoneAdminEditor(
                    days = 7,
                    title = r7Title, onTitleChange = { r7Title = it },
                    desc = r7Desc, onDescChange = { r7Desc = it },
                    gift = r7Gift, onGiftChange = { r7Gift = it },
                    type = r7Type, onTypeChange = { r7Type = it },
                    link = r7Link, onLinkChange = { r7Link = it },
                    isClaimed = config.reward7Claimed,
                    onResetClaim = { viewModel.updateRewardConfig(config.copy(reward7Claimed = false)) }
                )
            }

            // 30 Days
            item {
                MilestoneAdminEditor(
                    days = 30,
                    title = r30Title, onTitleChange = { r30Title = it },
                    desc = r30Desc, onDescChange = { r30Desc = it },
                    gift = r30Gift, onGiftChange = { r30Gift = it },
                    type = r30Type, onTypeChange = { r30Type = it },
                    link = r30Link, onLinkChange = { r30Link = it },
                    isClaimed = config.reward30Claimed,
                    onResetClaim = { viewModel.updateRewardConfig(config.copy(reward30Claimed = false)) }
                )
            }

            // 50 Days
            item {
                MilestoneAdminEditor(
                    days = 50,
                    title = r50Title, onTitleChange = { r50Title = it },
                    desc = r50Desc, onDescChange = { r50Desc = it },
                    gift = r50Gift, onGiftChange = { r50Gift = it },
                    type = r50Type, onTypeChange = { r50Type = it },
                    link = r50Link, onLinkChange = { r50Link = it },
                    isClaimed = config.reward50Claimed,
                    onResetClaim = { viewModel.updateRewardConfig(config.copy(reward50Claimed = false)) }
                )
            }

            // 100 Days
            item {
                MilestoneAdminEditor(
                    days = 100,
                    title = r100Title, onTitleChange = { r100Title = it },
                    desc = r100Desc, onDescChange = { r100Desc = it },
                    gift = r100Gift, onGiftChange = { r100Gift = it },
                    type = r100Type, onTypeChange = { r100Type = it },
                    link = r100Link, onLinkChange = { r100Link = it },
                    isClaimed = config.reward100Claimed,
                    onResetClaim = { viewModel.updateRewardConfig(config.copy(reward100Claimed = false)) }
                )
            }

            // 6. ADMIN PIN SETTINGS
            item {
                SectionHeader(title = "🔐 Admin PIN Security")
            }

            item {
                CozyCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Set secret PIN to prevent Priyanka from accidentally seeing upcoming surprises.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = adminPin,
                            onValueChange = { adminPin = it },
                            label = { Text("4-digit Secret PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }
    }

    // Add Memory Dialog
    if (showAddMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemoryDialog = false },
            title = { Text("Add Birthday Memory 📸") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newMemoryEmoji,
                        onValueChange = { newMemoryEmoji = it },
                        label = { Text("Emoji (e.g. 🌸, ☕, 🎂)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newMemoryTitle,
                        onValueChange = { newMemoryTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newMemoryTag,
                        onValueChange = { newMemoryTag = it },
                        label = { Text("Tag / Category") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newMemoryCaption,
                        onValueChange = { newMemoryCaption = it },
                        label = { Text("Caption") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newMemoryTitle.isNotBlank()) {
                            viewModel.addBirthdayMemory(
                                emoji = newMemoryEmoji,
                                title = newMemoryTitle,
                                caption = newMemoryCaption,
                                dateTag = newMemoryTag,
                                imageUrl = ""
                            )
                            newMemoryTitle = ""
                            newMemoryCaption = ""
                            showAddMemoryDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Schedule Preview Dialog
    if (showSchedulePreviewDialog) {
        AlertDialog(
            onDismissRequest = { showSchedulePreviewDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Birthday Notification Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Scheduled for September 10 in user's local timezone. Each notification delivers a unique message and works when app is closed:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val activeSlots = com.example.notification.BirthdayNotificationMessages.SCHEDULE_SERIES.filter { item ->
                        if (item.slotTime == "23:55" && !allowFinal2355) return@filter false
                        if (pushFrequency == "1_HOUR") return@filter true
                        (item.hour % 2 == 0) || item.slotTime == "23:55"
                    }

                    activeSlots.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = item.slotTime,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSchedulePreviewDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun MilestoneAdminEditor(
    days: Int,
    title: String, onTitleChange: (String) -> Unit,
    desc: String, onDescChange: (String) -> Unit,
    gift: String, onGiftChange: (String) -> Unit,
    type: String, onTypeChange: (String) -> Unit,
    link: String, onLinkChange: (String) -> Unit,
    isClaimed: Boolean,
    onResetClaim: () -> Unit
) {
    CozyCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏆 $days Days Milestone",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isClaimed) {
                    TextButton(onClick = onResetClaim) {
                        Text("Reset Claimed Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Milestone Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = desc,
                onValueChange = onDescChange,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onTypeChange("DIGITAL") }
                ) {
                    RadioButton(selected = type == "DIGITAL", onClick = { onTypeChange("DIGITAL") })
                    Text("Digital Gift", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onTypeChange("PHYSICAL") }
                ) {
                    RadioButton(selected = type == "PHYSICAL", onClick = { onTypeChange("PHYSICAL") })
                    Text("Physical Gift", style = MaterialTheme.typography.bodyMedium)
                }
            }

            OutlinedTextField(
                value = gift,
                onValueChange = onGiftChange,
                label = { Text("Gift Details / Prize (e.g. Favorite book / voucher)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = link,
                onValueChange = onLinkChange,
                label = { Text("Optional Link / URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
