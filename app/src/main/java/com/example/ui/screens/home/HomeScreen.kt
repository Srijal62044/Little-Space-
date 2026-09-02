package com.example.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.ImportantDateEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.*
import com.example.ui.navigation.MainTab
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToTab: (MainTab) -> Unit,
    onOpenImportantDates: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGallery: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val todayHabitLogs by viewModel.todayHabitLogs.collectAsStateWithLifecycle()
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    val todayMood by viewModel.todayMood.collectAsStateWithLifecycle()
    val importantDates by viewModel.allDates.collectAsStateWithLifecycle()
    val dailyQuote by viewModel.dailyQuote.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val nextMilestone by viewModel.nextMilestone.collectAsStateWithLifecycle()
    val daysToNextMilestone by viewModel.daysToNextMilestone.collectAsStateWithLifecycle()
    val isTodayBirthday by viewModel.isTodayBirthday.collectAsStateWithLifecycle()
    val rewardConfig by viewModel.rewardConfig.collectAsStateWithLifecycle()

    val completedHabitIds = remember(todayHabitLogs) {
        todayHabitLogs.filter { it.isCompleted }.map { it.habitId }.toSet()
    }

    val todayTasks = remember(tasks, viewModel.todayDateString) {
        tasks.filter { it.dueDate == viewModel.todayDateString || it.dueDate.isBlank() }
    }

    val totalGoalsCount = todayTasks.size + habits.size
    val completedGoalsCount = todayTasks.count { it.isCompleted } + completedHabitIds.size
    val habitPercent = if (habits.isNotEmpty()) ((completedHabitIds.size.toFloat() / habits.size) * 100).toInt() else 0

    // Dynamic greeting
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingPrefix = when {
        hour in 5..11 -> "Good morning,"
        hour in 12..16 -> "Good afternoon,"
        hour in 17..21 -> "Good evening,"
        else -> "Cozy night,"
    }
    val greetingName = userProfile.name.ifBlank { "Priyanka" }
    val initialLetter = greetingName.take(1).uppercase()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // --- 1. Geometric Header ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$greetingPrefix\n$greetingName 🌷",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 28.sp,
                                lineHeight = 34.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Let’s make today a little easier.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Monogram Avatar Pill
                        Surface(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onOpenSettings() },
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = initialLetter,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Settings button
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .then(
                                    Modifier.border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(16.dp)
                                    )
                                )
                                .testTag("home_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // --- 1B. Prominent Birthday Countdown & Celebration Card ---
        item {
            BirthdayCountdownHomeCard(
                isTestMode = rewardConfig.isBirthdayTestMode,
                birthdayMonth = rewardConfig.birthdayMonth,
                birthdayDay = rewardConfig.birthdayDay,
                onOpenSurprise = { viewModel.openBirthdayExperience(true) }
            )
        }

        // --- 1C. Journey Streak Dashboard Card ---
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onNavigateToTab(MainTab.REWARDS) }
                    .testTag("home_streak_banner"),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        ) {
                            Text(text = "🔥", fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "🔥 Current Streak: $currentStreak Days",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (daysToNextMilestone > 0)
                                    "Next Reward: $nextMilestone Days 🎁 ($daysToNextMilestone days left)"
                                else
                                    "Next Reward: $nextMilestone Days 🎁 (Ready to claim!)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Journey",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- 2. Geometric Today's Tasks Section ---
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
                    .shadow(0.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TODAY’S TASKS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        val completedTodayCount = todayTasks.count { it.isCompleted }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "$completedTodayCount/${todayTasks.size} Done",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (todayTasks.isEmpty()) {
                        EmptyStateCard(
                            emoji = "✨",
                            title = "No tasks for today",
                            subtitle = "Enjoy your peaceful day or add a small task to get started.",
                            actionText = "+ Add a Task",
                            onActionClick = { onNavigateToTab(MainTab.TASKS) }
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            todayTasks.take(4).forEach { task ->
                                GeometricTaskItem(
                                    task = task,
                                    onToggle = { viewModel.toggleTask(task.id, !task.isCompleted) }
                                )
                            }
                        }
                        if (todayTasks.size > 4) {
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = { onNavigateToTab(MainTab.TASKS) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = "View all ${todayTasks.size} tasks →",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. 2-Column Balanced Geometric Grid: Habits & Mood ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // --- Habits Tile (Lavender) ---
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onNavigateToTab(MainTab.HABITS) },
                    color = GeoLavenderContainer,
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, GeoLavenderText.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "HABITS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GeoLavenderText,
                            letterSpacing = 1.sp
                        )

                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$habitPercent",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoLavenderText,
                                    lineHeight = 36.sp
                                )
                                Text(
                                    text = "%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoLavenderText,
                                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val activeHabitTitle = habits.firstOrNull()?.title ?: "Hydration & Health"
                            Text(
                                text = "💧 $activeHabitTitle",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = GeoLavenderText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // --- Mood Tile (Mint/Sage) ---
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .clip(RoundedCornerShape(28.dp)),
                    color = GeoMintContainer,
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, GeoMintText.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MOOD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GeoMintText,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val moodOptions = listOf(
                                "Low" to "😔",
                                "Okay" to "😐",
                                "Great" to "😊"
                            )
                            moodOptions.forEach { (name, emoji) ->
                                val isSelected = todayMood?.mood?.equals(name, ignoreCase = true) == true
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else Color.Transparent)
                                        .clickable { viewModel.recordMood(name, emoji) }
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = emoji,
                                        fontSize = if (isSelected) 24.sp else 20.sp,
                                        modifier = Modifier.then(
                                            if (!isSelected) Modifier.alpha(0.5f) else Modifier
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = todayMood?.let { "Feeling ${it.mood.lowercase()}!" } ?: "Check in today",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = GeoMintText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        // --- 4. Pia AI Assistant Hero Card ---
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .shadow(4.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                color = GeoDarkCardBg
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    // Ambient blurred glow circle top-right
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 16.dp, y = (-16).dp)
                            .size(100.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(GeoDarkCardGlow.copy(alpha = 0.25f), Color.Transparent)
                                )
                            )
                    )

                    Column {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "PIA AI",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "“I've noticed you have 2 hours free this afternoon. Shall we schedule your reading time?”",
                            style = MaterialTheme.typography.titleMedium,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            color = Color.White,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onNavigateToTab(MainTab.PIA) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("home_pia_yes_button")
                            ) {
                                Text(
                                    text = "Yes, please",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            FilledTonalButton(
                                onClick = { onNavigateToTab(MainTab.PIA) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.12f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("home_pia_chat_button")
                            ) {
                                Text(
                                    text = "Chat with Pia",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4.5 Photo Gallery Hero Banner ---
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onOpenGallery() },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📸 Photo Gallery", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Upload your memories & keep small notes",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = onOpenGallery,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 5. Daily Inspirational Little Note Bar ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "“Small progress is still progress. You’ve got this, $greetingName 🌷”",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Secret letter trigger
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.toggleLittleSurprise(true) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "💌", fontSize = 14.sp)
                    }
                }
            }
        }

        // --- 6. Important Dates & Countdowns Section ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                SectionHeader(
                    title = "Important Dates",
                    actionText = "See all (${importantDates.size})",
                    onActionClick = onOpenImportantDates
                )

                if (importantDates.isEmpty()) {
                    EmptyStateCard(
                        emoji = "🎂",
                        title = "No upcoming dates",
                        subtitle = "Add birthdays, anniversaries, exams or deadlines to track countdowns.",
                        actionText = "+ Add Date",
                        onActionClick = onOpenImportantDates
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(importantDates.take(4)) { dateItem ->
                            DateCountdownCard(dateItem = dateItem)
                        }
                    }
                }
            }
        }

        // --- 7. Quick Notes Preview ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                SectionHeader(
                    title = "Quick Notes",
                    actionText = "All Notes (${notes.size})",
                    onActionClick = { onOpenNotes() }
                )

                if (notes.isEmpty()) {
                    EmptyStateCard(
                        emoji = "📝",
                        title = "No notes yet",
                        subtitle = "Jot down sudden ideas, shopping lists, or reminders.",
                        actionText = "+ Add Note",
                        onActionClick = { onOpenNotes() }
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(notes.take(4)) { note ->
                            NoteSnippetCard(
                                note = note,
                                onClick = { onOpenNotes() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeometricTaskItem(
    task: TaskEntity,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Square rounded checkbox
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .then(
                        if (task.isCompleted) {
                            Modifier.background(MaterialTheme.colorScheme.primary)
                        } else {
                            Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(6.dp)
                                )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.dueTime.isNotBlank() || task.category.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (task.category.isNotBlank()) {
                            Text(
                                text = task.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                        if (task.dueTime.isNotBlank()) {
                            Text(
                                text = "⏰ ${task.dueTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            PriorityBadge(priority = task.priority)
        }
    }
}

@Composable
private fun DateCountdownCard(
    dateItem: ImportantDateEntity
) {
    val daysLeft = remember(dateItem.targetDate) {
        calculateDaysLeft(dateItem.targetDate)
    }

    CozyCard(
        modifier = Modifier.width(180.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = dateItem.icon, fontSize = 22.sp)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (daysLeft == 0) "Today 🎉" else if (daysLeft > 0) "$daysLeft d left" else "Passed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dateItem.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = dateItem.targetDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoteSnippetCard(
    note: NoteEntity,
    onClick: () -> Unit
) {
    CozyCard(
        modifier = Modifier.width(160.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline,
        onClick = onClick
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                if (note.isPinned) {
                    Text(text = "📌", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun calculateDaysLeft(targetDateStr: String): Int {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val target = sdf.parse(targetDateStr) ?: return 0
        val calTarget = Calendar.getInstance().apply {
            time = target
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMs = calTarget.timeInMillis - calToday.timeInMillis
        (diffMs / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) {
        0
    }
}

data class BirthdayCountdownState(
    val isBirthdayToday: Boolean,
    val targetYear: Int,
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long
)

fun calculateBirthdayCountdown(
    birthdayMonth: Int = 9,
    birthdayDay: Int = 10,
    isTestMode: Boolean = false,
    nowCalendar: Calendar = Calendar.getInstance()
): BirthdayCountdownState {
    val currentYear = nowCalendar.get(Calendar.YEAR)
    val currentMonth = nowCalendar.get(Calendar.MONTH) + 1 // 1-12
    val currentDay = nowCalendar.get(Calendar.DAY_OF_MONTH)

    // If test mode or today is exactly birthday day (September 10)
    if (isTestMode || (currentMonth == birthdayMonth && currentDay == birthdayDay)) {
        return BirthdayCountdownState(
            isBirthdayToday = true,
            targetYear = currentYear,
            days = 0,
            hours = 0,
            minutes = 0,
            seconds = 0
        )
    }

    // Birthday this year at 00:00:00.000 local time
    val bdayThisYear = Calendar.getInstance().apply {
        timeInMillis = nowCalendar.timeInMillis
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, birthdayMonth - 1)
        set(Calendar.DAY_OF_MONTH, birthdayDay)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val targetCal: Calendar
    val targetYear: Int

    if (nowCalendar.before(bdayThisYear)) {
        // Today is BEFORE September 10 of current year -> Target is September 10 of current year
        targetCal = bdayThisYear
        targetYear = currentYear
    } else {
        // September 10 of current year has passed -> Target is September 10 of next year
        targetCal = Calendar.getInstance().apply {
            timeInMillis = nowCalendar.timeInMillis
            set(Calendar.YEAR, currentYear + 1)
            set(Calendar.MONTH, birthdayMonth - 1)
            set(Calendar.DAY_OF_MONTH, birthdayDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        targetYear = currentYear + 1
    }

    val diffMillis = targetCal.timeInMillis - nowCalendar.timeInMillis
    val totalSeconds = (diffMillis / 1000L).coerceAtLeast(0)
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return BirthdayCountdownState(
        isBirthdayToday = false,
        targetYear = targetYear,
        days = days,
        hours = hours,
        minutes = minutes,
        seconds = seconds
    )
}

@Composable
fun BirthdayCountdownHomeCard(
    isTestMode: Boolean,
    birthdayMonth: Int,
    birthdayDay: Int,
    onOpenSurprise: () -> Unit,
    modifier: Modifier = Modifier
) {
    var countdown by remember(isTestMode, birthdayMonth, birthdayDay) {
        mutableStateOf(calculateBirthdayCountdown(birthdayMonth, birthdayDay, isTestMode))
    }

    LaunchedEffect(isTestMode, birthdayMonth, birthdayDay) {
        while (true) {
            countdown = calculateBirthdayCountdown(birthdayMonth, birthdayDay, isTestMode)
            delay(1000)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(24.dp))
            .testTag("home_birthday_countdown_card"),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFFFF5F7),
        border = BorderStroke(1.5.dp, Color(0xFFFECDD3)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (countdown.isBirthdayToday) {
                // Birthday Day State
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "🎂", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HAPPY BIRTHDAY, PRIYANKA! 🎉",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF9F1239),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "“Today is your special day.” ✨",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = Color(0xFF881337),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onOpenSurprise,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("home_open_birthday_surprise_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE11D48),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🎁 Open Your Birthday Surprise",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            } else {
                // Approaching Birthday Countdown State
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFE4E6))
                    ) {
                        Text(text = "🎂", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Priyanka's Birthday",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9F1239)
                        )
                        Text(
                            text = "10 September ${countdown.targetYear}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFBE123C),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Digit Tiles: Days, Hours, Minutes, Seconds
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HomeCountdownUnit(value = countdown.days.toString().padStart(2, '0'), label = "Days")
                    Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                    HomeCountdownUnit(value = countdown.hours.toString().padStart(2, '0'), label = "Hours")
                    Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                    HomeCountdownUnit(value = countdown.minutes.toString().padStart(2, '0'), label = "Minutes")
                    Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                    HomeCountdownUnit(value = countdown.seconds.toString().padStart(2, '0'), label = "Seconds")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "“Something special is getting closer... ✨”",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = Color(0xFF881337),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HomeCountdownUnit(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(62.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFFECDD3)),
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ),
                    color = Color(0xFFE11D48)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF9F1239)
        )
    }
}
