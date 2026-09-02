package com.example.ui.screens.birthday

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

data class MemoryRewindItem(
    val id: String,
    val yearTag: String,
    val title: String,
    val memoryDesc: String,
    val emoji: String
)

@Composable
fun CountdownRewindView(
    modifier: Modifier = Modifier
) {
    val rewindMemories = remember {
        mutableStateListOf(
            MemoryRewindItem("1", "2025 • Milestone", "Cozy Sunset Birthday Celebration 🌅", "Celebrated with handwritten cards, delicious strawberry cake, and endless laughter.", "🎂"),
            MemoryRewindItem("2", "2024 • Milestone", "Surprise Candlelight Dinner 🕯️", "A beautiful surprise evening filled with favorite music tracks and warm memories.", "✨"),
            MemoryRewindItem("3", "2023 • Milestone", "First Handmade Scrapbook 📸", "Compiled the very first set of polaroid photos and travel memories.", "🌷")
        )
    }

    var showAddMilestoneDialog by remember { mutableStateOf(false) }
    var newYear by remember { mutableStateOf("2026") }
    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⏪ Birthday Moments & Milestones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF881337)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "A nostalgic timeline of past birthday celebrations & memories",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF334155),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Memory Rewind Timeline Section
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rewind Past Birthday Moments ⏪",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    IconButton(
                        onClick = { showAddMilestoneDialog = true },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFE4E6))
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Milestone", tint = Color(0xFFBE123C))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(rewindMemories) { memory ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFF1F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFFDA4AF), CircleShape)
                                ) {
                                    Text(text = memory.emoji, fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = memory.yearTag,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF9F1239)
                                    )
                                    Text(
                                        text = memory.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = memory.memoryDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF334155),
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Milestone Dialog
    if (showAddMilestoneDialog) {
        Dialog(onDismissRequest = { showAddMilestoneDialog = false }) {
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
                    Text(text = "Add Memory Rewind Milestone ⏪", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE11D48))

                    OutlinedTextField(
                        value = newYear,
                        onValueChange = { newYear = it },
                        label = { Text("Year / Tag") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Milestone Title") },
                        placeholder = { Text("e.g. Birthday Trip to the Beach") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Memory Details") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddMilestoneDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTitle.isNotBlank()) {
                                    rewindMemories.add(
                                        MemoryRewindItem(
                                            id = System.currentTimeMillis().toString(),
                                            yearTag = "$newYear • Milestone",
                                            title = newTitle,
                                            memoryDesc = newDesc.ifBlank { "Special milestone moment." },
                                            emoji = "🌟"
                                        )
                                    )
                                    newTitle = ""
                                    newDesc = ""
                                    showAddMilestoneDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                        ) {
                            Text("Save Milestone")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownTile(digit: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFBE123C),
            modifier = Modifier.size(54.dp, 58.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = digit,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF881337))
    }
}
