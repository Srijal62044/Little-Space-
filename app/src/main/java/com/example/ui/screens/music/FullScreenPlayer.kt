package com.example.ui.screens.music

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.SongEntity
import com.example.ui.audio.RepeatMode
import com.example.ui.audio.VisualizerType
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPlayer(
    viewModel: MainViewModel,
    currentSong: SongEntity?,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    queue: List<SongEntity>,
    frequencies: FloatArray,
    visualizerType: VisualizerType,
    onClose: () -> Unit
) {
    var showQueueSheet by remember { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    if (currentSong == null) {
        onClose()
        return
    }

    val albumColor = remember(currentSong.albumColorHex) {
        try {
            Color(android.graphics.Color.parseColor(currentSong.albumColorHex))
        } catch (e: Exception) {
            Color(0xFF8B5CF6)
        }
    }

    // Vinyl rotation animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "rotation"
    )

    val displayedProgress = if (isSeeking) seekPosition else {
        if (durationMs > 0) (progressMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val currentSeconds = if (isSeeking) (seekPosition * durationMs / 1000).toLong() else progressMs / 1000
    val durationSeconds = durationMs / 1000
    val remainingSeconds = (durationSeconds - currentSeconds).coerceAtLeast(0)

    fun formatTime(sec: Long): String {
        val m = sec / 60
        val s = sec % 60
        return String.format("%d:%02d", m, s)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("full_screen_player"),
        color = Color(0xFF090D16)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Ambient glowing gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                albumColor.copy(alpha = 0.45f),
                                Color(0xFF0F172A).copy(alpha = 0.95f),
                                Color(0xFF050811)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("player_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse Player",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = albumColor.copy(alpha = 0.9f),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = currentSong.album,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { showQueueSheet = true },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Queue",
                            tint = Color.White
                        )
                    }
                }

                // Vinyl Album Artwork / Artwork Centerpiece
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer neon glowing circle
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(32.dp, CircleShape, spotColor = albumColor)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        albumColor.copy(alpha = 0.6f),
                                        Color(0xFF1E293B),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                    )

                    // Rotating Vinyl Disk
                    val currentRotation = if (isPlaying) rotationAngle else 0f
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .clip(CircleShape)
                            .rotate(currentRotation)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF18181B),
                                        Color(0xFF27272A),
                                        Color(0xFF09090B),
                                        Color(0xFF27272A),
                                        Color(0xFF18181B)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner center artwork
                        val bitmap = remember(currentSong.artworkBase64) {
                            currentSong.artworkBase64?.let { b64 ->
                                try {
                                    val bytes = Base64.decode(b64, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(albumColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!currentSong.artworkUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentSong.artworkUrl,
                                    contentDescription = currentSong.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = currentSong.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Cover",
                                    tint = Color.White,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            // Vinyl spindle hole
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF090D16))
                            )
                        }
                    }
                }

                // Reactive Audio Visualizer + Type Switcher
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    VisualizerView(
                        frequencies = frequencies,
                        type = visualizerType,
                        accentColor = albumColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    )

                    // Visualizer mode pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        VisualizerType.entries.forEach { vType ->
                            val isSelected = visualizerType == vType
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setVisualizerType(vType) },
                                color = if (isSelected) albumColor.copy(alpha = 0.25f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = vType.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Song Info & Favorite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = currentSong.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (currentSong.isOnline) {
                                Surface(
                                    color = Color(0xFFEC4899).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Online",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFF71A5),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${currentSong.artist} • ${currentSong.album}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleSongFavorite(currentSong) },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("player_favorite_btn")
                    ) {
                        Icon(
                            imageVector = if (currentSong.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (currentSong.isFavorite) Color(0xFFF43F5E) else Color.White
                        )
                    }
                }

                // Scrubber / Progress Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = displayedProgress,
                        onValueChange = {
                            isSeeking = true
                            seekPosition = it
                        },
                        onValueChangeFinished = {
                            val targetMs = (seekPosition * durationMs).toLong()
                            viewModel.seekAudioTo(targetMs)
                            isSeeking = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = albumColor,
                            inactiveTrackColor = Color(0xFF334155)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentSeconds),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "-${formatTime(remainingSeconds)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Main Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) albumColor else Color(0xFF64748B)
                        )
                    }

                    // Previous / Rewind 10s
                    IconButton(
                        onClick = { viewModel.playPreviousSong() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Play / Pause FAB
                    FilledIconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(16.dp, CircleShape, spotColor = albumColor)
                            .testTag("player_main_play_pause"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = albumColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = { viewModel.playNextSong() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Repeat Mode
                    IconButton(onClick = { viewModel.cycleRepeatMode() }) {
                        Icon(
                            imageVector = when (repeatMode) {
                                RepeatMode.OFF -> Icons.Default.Repeat
                                RepeatMode.ALL -> Icons.Default.Repeat
                                RepeatMode.ONE -> Icons.Default.RepeatOne
                            },
                            contentDescription = "Repeat",
                            tint = if (repeatMode != RepeatMode.OFF) albumColor else Color(0xFF64748B)
                        )
                    }
                }

                // Remix Studio Action Pill + Quick Seek Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.seekAudioRelative(-10) }) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10s",
                            tint = Color(0xFF94A3B8)
                        )
                    }

                    // Remix Studio Trigger
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { viewModel.openRemixStudio(true) }
                            .testTag("open_remix_studio_btn"),
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(24.dp),
                        tonalElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Remix Studio",
                                tint = Color(0xFFEC4899),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Remix Studio 🎛️",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.seekAudioRelative(10) }) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }

    // Queue Sheet
    if (showQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            containerColor = Color(0xFF0F172A),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Playing Queue 🎵 (${queue.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(queue) { index, song ->
                        val isCurrent = song.id == currentSong.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.playSong(song, queue)
                                    showQueueSheet = false
                                },
                            color = if (isCurrent) albumColor.copy(alpha = 0.2f) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isCurrent) albumColor else Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) Color.White else Color(0xFFE2E8F0),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1
                                    )
                                }

                                if (isCurrent && isPlaying) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "Playing",
                                        tint = albumColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
