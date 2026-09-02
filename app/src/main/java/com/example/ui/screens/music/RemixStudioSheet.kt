package com.example.ui.screens.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RemixPresetEntity
import com.example.ui.audio.RemixState
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemixStudioSheet(
    viewModel: MainViewModel,
    remixState: RemixState,
    customPresets: List<RemixPresetEntity>,
    onDismiss: () -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentSong by viewModel.currentPlayingSong.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        contentColor = Color.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Studio",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Remix Studio 🎛️",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Real-time DSP Audio FX Engine",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { viewModel.resetRemixEffects() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF334155),
                        contentColor = Color(0xFFE2E8F0)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("remix_reset_effects_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (currentSong?.isOnline == true) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Streaming online audio track. Real-time Remix Studio DSP effects apply to local and imported audio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            // Ready-made presets
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "READY-MADE PRESETS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp
                )
                val presets = listOf(
                    "Normal" to "🎧 Normal",
                    "Bass Boost" to "🔥 Bass Boost",
                    "Night" to "🌙 Night",
                    "Dreamy" to "✨ Dreamy",
                    "Vocal" to "🎤 Vocal",
                    "Club" to "💥 Club",
                    "Chill" to "🎶 Chill"
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { (key, label) ->
                        val isSelected = remixState.activePresetName == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.applyPreset(key) },
                            label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFFE2E8F0),
                                selectedContainerColor = Color(0xFF8B5CF6),
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0xFF334155),
                                selectedBorderColor = Color(0xFFA78BFA)
                            )
                        )
                    }
                }
            }

            // Custom Presets
            if (customPresets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SAVED CUSTOM PRESETS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(customPresets) { preset ->
                            val isSelected = remixState.activePresetName == preset.name
                            InputChip(
                                selected = isSelected,
                                onClick = { viewModel.applyCustomPreset(preset) },
                                label = { Text(preset.name) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete",
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { viewModel.deleteRemixPreset(preset) },
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color(0xFFE2E8F0),
                                    selectedContainerColor = Color(0xFFEC4899),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Divider(color = Color(0xFF334155))

            // SLIDERS SECTION
            RemixSliderCard(
                title = "BASS",
                icon = "🔊",
                value = remixState.bass,
                valueRange = -100f..100f,
                displayValue = if (remixState.bass > 0) "+${remixState.bass.toInt()}" else "${remixState.bass.toInt()}",
                accentColor = Color(0xFFF43F5E),
                onValueChange = { viewModel.updateBass(it) }
            )

            RemixSliderCard(
                title = "TREBLE",
                icon = "✨",
                value = remixState.treble,
                valueRange = -100f..100f,
                displayValue = if (remixState.treble > 0) "+${remixState.treble.toInt()}" else "${remixState.treble.toInt()}",
                accentColor = Color(0xFF38BDF8),
                onValueChange = { viewModel.updateTreble(it) }
            )

            RemixSliderCard(
                title = "VOCAL PRESENCE",
                icon = "🎤",
                value = remixState.vocal,
                valueRange = -100f..100f,
                displayValue = if (remixState.vocal > 0) "+${remixState.vocal.toInt()}" else "${remixState.vocal.toInt()}",
                accentColor = Color(0xFFFBBF24),
                onValueChange = { viewModel.updateVocal(it) }
            )

            RemixSliderCard(
                title = "REVERB (SPACE / ROOM)",
                icon = "🌌",
                value = remixState.reverb,
                valueRange = 0f..100f,
                displayValue = "${remixState.reverb.toInt()}%",
                accentColor = Color(0xFFA855F7),
                onValueChange = { viewModel.updateReverb(it) }
            )

            RemixSliderCard(
                title = "ECHO / DELAY",
                icon = "🔁",
                value = remixState.echoDelay,
                valueRange = 0f..100f,
                displayValue = "${remixState.echoDelay.toInt()}%",
                accentColor = Color(0xFF06B6D4),
                onValueChange = { viewModel.updateEchoDelay(it) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    RemixSliderCard(
                        title = "SPEED",
                        icon = "⚡",
                        value = remixState.speed,
                        valueRange = 0.5f..2.0f,
                        displayValue = String.format("%.2fx", remixState.speed),
                        accentColor = Color(0xFF10B981),
                        onValueChange = { viewModel.updateSpeed(it) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    RemixSliderCard(
                        title = "PITCH",
                        icon = "🎵",
                        value = remixState.pitch,
                        valueRange = 0.5f..2.0f,
                        displayValue = String.format("%.2fx", remixState.pitch),
                        accentColor = Color(0xFFEC4899),
                        onValueChange = { viewModel.updatePitch(it) }
                    )
                }
            }

            RemixSliderCard(
                title = "MASTER VOLUME",
                icon = "📢",
                value = remixState.volume,
                valueRange = 0f..100f,
                displayValue = "${remixState.volume.toInt()}%",
                accentColor = Color(0xFF6366F1),
                onValueChange = { viewModel.updateVolume(it) }
            )

            RemixSliderCard(
                title = "AUDIO BALANCE",
                icon = "⚖️",
                value = remixState.balance,
                valueRange = -100f..100f,
                displayValue = when {
                    remixState.balance < -10f -> "L ${-remixState.balance.toInt()}%"
                    remixState.balance > 10f -> "R ${remixState.balance.toInt()}%"
                    else -> "Center"
                },
                accentColor = Color(0xFF14B8A6),
                onValueChange = { viewModel.updateBalance(it) }
            )

            // Save Preset Button
            Button(
                onClick = {
                    presetNameInput = "Priyanka's Mix ${System.currentTimeMillis() % 1000}"
                    showSaveDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_custom_remix_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
                Spacer(Modifier.width(8.dp))
                Text("Save Current Remix as Preset 💾", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Custom Remix Preset") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Save current audio effects configuration (bass, treble, reverb, speed, balance) to your presets list.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text("Preset Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCustomRemixPreset(presetNameInput)
                        showSaveDialog = false
                    }
                ) {
                    Text("Save Preset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RemixSliderCard(
    title: String,
    icon: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    accentColor: Color,
    onValueChange: (Float) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(icon, fontSize = 16.sp)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0)
                    )
                }

                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = displayValue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = Color(0xFF334155)
                )
            )
        }
    }
}
