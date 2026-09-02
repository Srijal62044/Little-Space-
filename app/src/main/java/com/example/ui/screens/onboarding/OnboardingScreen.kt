package com.example.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CozyCard
import com.example.ui.viewmodel.MainViewModel

@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    onFinish: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val totalSteps = 8

    var name by remember { mutableStateOf("Priyanka") }
    var wakeTime by remember { mutableStateOf("07:30 AM") }
    var sleepTime by remember { mutableStateOf("11:00 PM") }
    var selectedActivities by remember { mutableStateOf(setOf("Morning Tea 🍵", "Deep Study / Work 📚", "Reading 📖", "Evening Walk 🚶")) }
    var selectedHabits by remember { mutableStateOf(setOf("💧 Drink water", "📚 Study", "🚶 Walk", "📖 Read", "🌙 Sleep on time")) }
    var selectedReminders by remember { mutableStateOf(setOf("Study / Task deadlines", "Hydration nudges", "Important dates")) }
    var selectedGoals by remember { mutableStateOf(setOf("Consistent study routine", "Balanced daily lifestyle", "Mindful wellbeing")) }
    var selectedTheme by remember { mutableStateOf("Rose Blush") }
    var reminderStrictness by remember { mutableStateOf("Gentle") }

    val scrollState = rememberScrollState()

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
                    if (step > 0) {
                        IconButton(
                            onClick = { step-- },
                            modifier = Modifier.testTag("onboarding_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Text(
                        text = "Step ${step + 1} of $totalSteps",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    TextButton(
                        onClick = {
                            viewModel.completeOnboarding(
                                name = name,
                                wakeTime = wakeTime,
                                sleepTime = sleepTime,
                                activities = selectedActivities.joinToString(", "),
                                habits = selectedHabits.joinToString(", "),
                                reminders = selectedReminders.joinToString(", "),
                                goals = selectedGoals.joinToString(", "),
                                theme = selectedTheme,
                                strictness = reminderStrictness
                            )
                            onFinish()
                        },
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (step + 1) / totalSteps.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (step < totalSteps - 1) {
                                step++
                            } else {
                                viewModel.completeOnboarding(
                                    name = name,
                                    wakeTime = wakeTime,
                                    sleepTime = sleepTime,
                                    activities = selectedActivities.joinToString(", "),
                                    habits = selectedHabits.joinToString(", "),
                                    reminders = selectedReminders.joinToString(", "),
                                    goals = selectedGoals.joinToString(", "),
                                    theme = selectedTheme,
                                    strictness = reminderStrictness
                                )
                                onFinish()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_next_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (step == totalSteps - 1) "Enter Little Space 🌷" else "Continue ✨",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { width -> if (targetState > initialState) width else -width } togetherWith
                            fadeOut() + slideOutHorizontally { width -> if (targetState > initialState) -width else width }
                },
                label = "onboarding_step"
            ) { currentStep ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentStep) {
                        0 -> {
                            // Step 1: Welcome & Wake Up
                            Text(text = "🌷", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Let's get to know you ✨",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Welcome to your little personal corner. What time do you usually wake up?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(28.dp))

                            val wakeOptions = listOf("06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM", "09:00 AM")
                            wakeOptions.forEach { time ->
                                SelectableOptionCard(
                                    title = time,
                                    subtitle = "Morning wake up time",
                                    isSelected = wakeTime == time,
                                    onClick = { wakeTime = time }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                        1 -> {
                            // Step 2: Sleep Time
                            Text(text = "🌙", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Bedtime Routine",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "What time do you usually wind down and sleep?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(28.dp))

                            val sleepOptions = listOf("10:00 PM", "10:30 PM", "11:00 PM", "11:30 PM", "12:00 AM", "12:30 AM")
                            sleepOptions.forEach { time ->
                                SelectableOptionCard(
                                    title = time,
                                    subtitle = "Night rest time",
                                    isSelected = sleepTime == time,
                                    onClick = { sleepTime = time }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                        2 -> {
                            // Step 3: Important Daily Activities
                            Text(text = "🗓️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Daily Activities",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select the activities that matter most in your routine:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            val activityOptions = listOf(
                                "Morning Tea / Coffee 🍵",
                                "Deep Study / Work 📚",
                                "Reading a book 📖",
                                "Workout or Yoga 🧘",
                                "Evening Walk 🚶",
                                "Skincare Routine 🧴",
                                "Journaling ✍️",
                                "Creative Time 🎨"
                            )

                            activityOptions.forEach { act ->
                                val isSelected = selectedActivities.contains(act)
                                SelectableOptionCard(
                                    title = act,
                                    subtitle = null,
                                    isSelected = isSelected,
                                    onClick = {
                                        selectedActivities = if (isSelected) {
                                            selectedActivities - act
                                        } else {
                                            selectedActivities + act
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        3 -> {
                            // Step 4: Habits to Maintain
                            Text(text = "🌱", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Daily Habits",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Which habits would you love to keep track of?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            val habitOptions = listOf(
                                "💧 Drink enough water",
                                "📚 Study regularly",
                                "🚶 Walk 5,000+ steps",
                                "📖 Read 15 mins",
                                "🧘 Relax & breathe",
                                "🌙 Sleep on time",
                                "🥗 Healthy meals",
                                "🧴 Morning & evening skincare"
                            )

                            habitOptions.forEach { habit ->
                                val isSelected = selectedHabits.contains(habit)
                                SelectableOptionCard(
                                    title = habit,
                                    subtitle = null,
                                    isSelected = isSelected,
                                    onClick = {
                                        selectedHabits = if (isSelected) selectedHabits - habit else selectedHabits + habit
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        4 -> {
                            // Step 5: Reminders Needed
                            Text(text = "⏰", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Reminder Needs",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "What kind of reminders do you usually find helpful?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            val reminderOptions = listOf(
                                "Study / Task deadlines & exams 📚",
                                "Hydration nudges throughout the day 💧",
                                "Daily habit check-ins 🌱",
                                "Birthdays & special events 🎂",
                                "Wind-down & bedtime calm alert 🌙"
                            )

                            reminderOptions.forEach { rem ->
                                val isSelected = selectedReminders.contains(rem)
                                SelectableOptionCard(
                                    title = rem,
                                    subtitle = null,
                                    isSelected = isSelected,
                                    onClick = {
                                        selectedReminders = if (isSelected) selectedReminders - rem else selectedReminders + rem
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        5 -> {
                            // Step 6: Current Goals
                            Text(text = "🎯", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Current Goals",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "What is your main focus for this season?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            val goalOptions = listOf(
                                "Excel in my studies / career 📚",
                                "Build consistent, calm daily habits 🌱",
                                "Stay hydrated & maintain wellness 💧",
                                "Reduce stress and keep a balanced rhythm 🧘",
                                "Better time management without burnout ⏱️"
                            )

                            goalOptions.forEach { goal ->
                                val isSelected = selectedGoals.contains(goal)
                                SelectableOptionCard(
                                    title = goal,
                                    subtitle = null,
                                    isSelected = isSelected,
                                    onClick = {
                                        selectedGoals = if (isSelected) selectedGoals - goal else selectedGoals + goal
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        6 -> {
                            // Step 7: Favourite Theme
                            Text(text = "🎨", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Cozy Theme",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Choose the aesthetic palette you like best:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            val themes = listOf(
                                Triple("Rose Blush", "🌸 Soft blush, rose gold & cream (Default)", Color(0xFFFB7185)),
                                Triple("Matcha Sage", "🌿 Calming matcha green & warm linen", Color(0xFF34D399)),
                                Triple("Lavender Dream", "💜 Gentle lilac & soft violet", Color(0xFFA78BFA)),
                                Triple("Warm Peach", "🍑 Soft peach & apricot sunset", Color(0xFFFB923C)),
                                Triple("Cozy Latte", "☕ Warm oatmeal & caramel taupe", Color(0xFFB45309))
                            )

                            themes.forEach { (thm, desc, color) ->
                                CozyCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    backgroundColor = if (selectedTheme == thm) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                    borderColor = if (selectedTheme == thm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    onClick = { selectedTheme = thm }
                                ) {
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
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Text(
                                                    text = thm,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = desc,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        if (selectedTheme == thm) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        7 -> {
                            // Step 8: Reminder Strictness
                            Text(text = "✨", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Reminder Style",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "How would you like Priyanka's Little Space to remind you?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            SelectableOptionCard(
                                title = "Gentle & Supportive 🌷",
                                subtitle = "Soft encouragements, mindful check-ins, zero pressure.",
                                isSelected = reminderStrictness == "Gentle",
                                onClick = { reminderStrictness = "Gentle" }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SelectableOptionCard(
                                title = "Structured & Focused 🎯",
                                subtitle = "Clear timestamps, structured checklists, focus milestones.",
                                isSelected = reminderStrictness == "Strict",
                                onClick = { reminderStrictness = "Strict" }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SelectableOptionCard(
    title: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    CozyCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
