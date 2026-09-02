package com.example.ui.screens.music

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.SongEntity
import com.example.data.provider.MusicSearchFilter
import com.example.ui.audio.VisualizerType
import com.example.ui.viewmodel.MainViewModel

enum class MusicTab {
    ONLINE, ALL, FAVORITES, PLAYLISTS, RECENT
}

enum class SortOption {
    RECENT_ADDED, RECENT_PLAYED, TITLE_AZ, ARTIST_AZ
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val allSongs by viewModel.allSongs.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayedSongs.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()
    val currentSong by viewModel.currentPlayingSong.collectAsState()
    val isPlaying by viewModel.isAudioPlaying.collectAsState()
    val frequencies by viewModel.visualizerFrequencies.collectAsState()

    // Online music states
    val onlineResults by viewModel.onlineSearchResults.collectAsState()
    val isOnlineSearching by viewModel.isOnlineSearching.collectAsState()
    val onlineError by viewModel.onlineSearchError.collectAsState()
    val selectedGenre by viewModel.selectedOnlineGenre.collectAsState()
    val onlineSearchFilter by viewModel.onlineSearchFilter.collectAsState()

    var selectedTab by remember { mutableStateOf(MusicTab.ONLINE) }
    var searchQuery by remember { mutableStateOf("") }
    var onlineSearchInput by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.RECENT_ADDED) }
    var showSortMenu by remember { mutableStateOf(false) }

    var songForPlaylist by remember { mutableStateOf<SongEntity?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var viewingPlaylist by remember { mutableStateOf<PlaylistEntity?>(null) }
    val playlistSongs by viewingPlaylist?.let { pl ->
        viewModel.getSongsForPlaylist(pl.id).collectAsState(initial = emptyList())
    } ?: remember { mutableStateOf(emptyList()) }

    // File Picker for local audio files (MP3, WAV, M4A, OGG, AAC)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importLocalAudioFiles(uris)
            selectedTab = MusicTab.ALL
        }
    }

    // Local library filter & sort
    val displayedLocalSongs = remember(allSongs, favoriteSongs, recentlyPlayed, selectedTab, searchQuery, sortOption) {
        val baseList = when (selectedTab) {
            MusicTab.ALL -> allSongs
            MusicTab.FAVORITES -> favoriteSongs
            MusicTab.PLAYLISTS -> emptyList()
            MusicTab.RECENT -> recentlyPlayed
            MusicTab.ONLINE -> emptyList()
        }

        val filtered = if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.album.contains(searchQuery, ignoreCase = true)
            }
        }

        when (sortOption) {
            SortOption.RECENT_ADDED -> filtered.sortedByDescending { it.id }
            SortOption.RECENT_PLAYED -> filtered.sortedByDescending { it.lastPlayedTimestamp }
            SortOption.TITLE_AZ -> filtered.sortedBy { it.title.lowercase() }
            SortOption.ARTIST_AZ -> filtered.sortedBy { it.artist.lowercase() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Music Studio 🎵", fontWeight = FontWeight.Bold)
                        if (isPlaying) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(18.dp)
                            ) {
                                VisualizerView(
                                    frequencies = frequencies,
                                    type = VisualizerType.BARS,
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Quick Remix Studio Button
                    FilledTonalIconButton(
                        onClick = { viewModel.openRemixStudio(true) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        modifier = Modifier.testTag("top_remix_studio_btn")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Remix Studio")
                    }

                    if (selectedTab != MusicTab.PLAYLISTS && selectedTab != MusicTab.ONLINE) {
                        // Sort dropdown for local music library
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Default.Sort, contentDescription = "Sort")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Recently Added") },
                                    onClick = { sortOption = SortOption.RECENT_ADDED; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Recently Played") },
                                    onClick = { sortOption = SortOption.RECENT_PLAYED; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Song Title (A-Z)") },
                                    onClick = { sortOption = SortOption.TITLE_AZ; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Artist (A-Z)") },
                                    onClick = { sortOption = SortOption.ARTIST_AZ; showSortMenu = false }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab != MusicTab.ONLINE) {
                // Prominent "+ Add Music" Button for local library
                ExtendedFloatingActionButton(
                    onClick = { audioPickerLauncher.launch("audio/*") },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    text = { Text("+ Import Audio", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(bottom = if (currentSong != null) 76.dp else 0.dp)
                        .testTag("add_music_fab")
                )
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top Navigation Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTab == MusicTab.ONLINE,
                        onClick = { selectedTab = MusicTab.ONLINE },
                        label = { Text("Discover 🌐") },
                        leadingIcon = { Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTab == MusicTab.ALL,
                        onClick = { selectedTab = MusicTab.ALL },
                        label = { Text("Library (${allSongs.size})") },
                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTab == MusicTab.FAVORITES,
                        onClick = { selectedTab = MusicTab.FAVORITES },
                        label = { Text("Favorites ♡ (${favoriteSongs.size})") },
                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTab == MusicTab.PLAYLISTS,
                        onClick = { selectedTab = MusicTab.PLAYLISTS },
                        label = { Text("Playlists 📑 (${playlists.size})") },
                        leadingIcon = { Icon(Icons.Default.QueueMusic, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTab == MusicTab.RECENT,
                        onClick = { selectedTab = MusicTab.RECENT },
                        label = { Text("Recently Played 🕒") },
                        leadingIcon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            if (viewingPlaylist != null) {
                PlaylistDetailView(
                    playlist = viewingPlaylist!!,
                    songs = playlistSongs,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    onBack = { viewingPlaylist = null },
                    onPlaySong = { song -> viewModel.playSong(song, playlistSongs) },
                    onPlayAll = {
                        if (playlistSongs.isNotEmpty()) {
                            viewModel.playSong(playlistSongs.first(), playlistSongs)
                        }
                    },
                    onShuffle = {
                        if (playlistSongs.isNotEmpty()) {
                            viewModel.toggleShuffle()
                            viewModel.playSong(playlistSongs.random(), playlistSongs)
                        }
                    },
                    onRemoveSong = { song ->
                        viewModel.removeSongFromPlaylist(viewingPlaylist!!.id, song.id)
                    },
                    onDeletePlaylist = {
                        viewModel.deletePlaylist(viewingPlaylist!!)
                        viewingPlaylist = null
                    }
                )
            } else when (selectedTab) {
                MusicTab.ONLINE -> {
                    // Online Music Search & Discovery
                    OnlineMusicView(
                        onlineResults = onlineResults,
                        isLoading = isOnlineSearching,
                        errorMessage = onlineError,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        searchInput = onlineSearchInput,
                        onSearchInputChange = { onlineSearchInput = it },
                        selectedGenre = selectedGenre,
                        filter = onlineSearchFilter,
                        onPerformSearch = { query, filter ->
                            focusManager.clearFocus()
                            viewModel.searchOnlineMusic(query, filter)
                        },
                        onSelectGenre = { genre ->
                            onlineSearchInput = ""
                            focusManager.clearFocus()
                            viewModel.loadOnlineDiscover(genre)
                        },
                        onPlaySong = { song ->
                            viewModel.playSong(song, onlineResults)
                        },
                        onToggleFavorite = { song ->
                            viewModel.toggleSongFavorite(song)
                        },
                        onAddToPlaylist = { song ->
                            songForPlaylist = song
                        },
                        onOpenRemix = { song ->
                            viewModel.playSong(song, onlineResults)
                            viewModel.openRemixStudio(true)
                        }
                    )
                }
                MusicTab.PLAYLISTS -> {
                    PlaylistsSection(
                        playlists = playlists,
                        onCreatePlaylist = { showCreatePlaylistDialog = true },
                        onSelectPlaylist = { pl -> viewingPlaylist = pl }
                    )
                }
                else -> {
                    // Local Library Views (All, Favorites, Recently Played)
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("local_music_search_input"),
                            placeholder = { Text("Search songs, artists, albums...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )

                        if (displayedLocalSongs.isEmpty()) {
                            EmptySongsView(
                                tab = selectedTab,
                                onAddMusic = { audioPickerLauncher.launch("audio/*") },
                                onSwitchToOnline = { selectedTab = MusicTab.ONLINE }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = if (currentSong != null) 90.dp else 16.dp)
                            ) {
                                items(displayedLocalSongs, key = { it.id }) { song ->
                                    val isCurrent = currentSong?.id == song.id || (song.externalId != null && currentSong?.externalId == song.externalId)
                                    SongItemCard(
                                        song = song,
                                        isCurrent = isCurrent,
                                        isPlaying = isPlaying && isCurrent,
                                        onPlay = { viewModel.playSong(song, displayedLocalSongs) },
                                        onToggleFavorite = { viewModel.toggleSongFavorite(song) },
                                        onAddToPlaylist = { songForPlaylist = song },
                                        onOpenRemix = {
                                            viewModel.playSong(song, displayedLocalSongs)
                                            viewModel.openRemixStudio(true)
                                        },
                                        onDelete = { viewModel.deleteSong(song) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name, icon, desc ->
                viewModel.createPlaylist(name, icon, desc)
            }
        )
    }

    songForPlaylist?.let { song ->
        AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onDismiss = { songForPlaylist = null },
            onAddToPlaylist = { plId ->
                viewModel.addSongToPlaylist(plId, song.id)
            },
            onCreateNew = {
                showCreatePlaylistDialog = true
            }
        )
    }
}

@Composable
private fun OnlineMusicView(
    onlineResults: List<SongEntity>,
    isLoading: Boolean,
    errorMessage: String?,
    currentSong: SongEntity?,
    isPlaying: Boolean,
    searchInput: String,
    onSearchInputChange: (String) -> Unit,
    selectedGenre: String,
    filter: MusicSearchFilter,
    onPerformSearch: (String, MusicSearchFilter) -> Unit,
    onSelectGenre: (String) -> Unit,
    onPlaySong: (SongEntity) -> Unit,
    onToggleFavorite: (SongEntity) -> Unit,
    onAddToPlaylist: (SongEntity) -> Unit,
    onOpenRemix: (SongEntity) -> Unit
) {
    val genres = listOf(
        "🔥 Top Hits",
        "🇮🇳 Bollywood",
        "🇵🇰 Pakistani",
        "🌾 Punjabi",
        "🪕 Sufi & Ghazals",
        "🌙 Romantic Chill",
        "🎧 Lo-Fi Beats",
        "🎤 Hip-Hop",
        "🌿 Indie",
        "✨ Global Pop",
        "🎸 Rock",
        "🎙️ R&B",
        "🎹 Electronic",
        "🎷 Jazz",
        "🎻 Classical"
    )

    var currentFilter by remember { mutableStateOf(filter) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Input Bar
        OutlinedTextField(
            value = searchInput,
            onValueChange = {
                onSearchInputChange(it)
                if (it.isBlank()) {
                    onPerformSearch("", currentFilter)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("online_music_search_input"),
            placeholder = { Text("Search any song, artist, album...") },
            leadingIcon = {
                Icon(Icons.Default.Public, contentDescription = "Online Search", tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchInput.isNotBlank()) {
                        IconButton(onClick = {
                            onSearchInputChange("")
                            onPerformSearch("", currentFilter)
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                    IconButton(
                        onClick = { onPerformSearch(searchInput, currentFilter) },
                        modifier = Modifier.testTag("online_search_submit_btn")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onPerformSearch(searchInput, currentFilter) }),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )

        // Filter Tags Row (All / Title / Artist)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filter:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilterChip(
                selected = currentFilter == MusicSearchFilter.ALL,
                onClick = {
                    currentFilter = MusicSearchFilter.ALL
                    if (searchInput.isNotBlank()) onPerformSearch(searchInput, MusicSearchFilter.ALL)
                },
                label = { Text("All", fontSize = 12.sp) },
                modifier = Modifier.height(28.dp)
            )
            FilterChip(
                selected = currentFilter == MusicSearchFilter.TITLE,
                onClick = {
                    currentFilter = MusicSearchFilter.TITLE
                    if (searchInput.isNotBlank()) onPerformSearch(searchInput, MusicSearchFilter.TITLE)
                },
                label = { Text("Song Title", fontSize = 12.sp) },
                modifier = Modifier.height(28.dp)
            )
            FilterChip(
                selected = currentFilter == MusicSearchFilter.ARTIST,
                onClick = {
                    currentFilter = MusicSearchFilter.ARTIST
                    if (searchInput.isNotBlank()) onPerformSearch(searchInput, MusicSearchFilter.ARTIST)
                },
                label = { Text("Artist", fontSize = 12.sp) },
                modifier = Modifier.height(28.dp)
            )
        }

        // Genre / Trending Exploration Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres) { g ->
                val cleanG = g.substringAfter(" ")
                val isSelected = selectedGenre.equals(cleanG, ignoreCase = true) || (cleanG == "Top Hits" && selectedGenre.contains("Top Hits", ignoreCase = true))
                SuggestionChip(
                    onClick = { onSelectGenre(cleanG) },
                    label = { Text(g, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Content Area
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Searching online music catalog...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp))
                            Text(
                                errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Button(
                                onClick = {
                                    if (searchInput.isNotBlank()) onPerformSearch(searchInput, currentFilter)
                                    else onSelectGenre(selectedGenre)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Retry Search")
                            }
                        }
                    }
                }
            }
            onlineResults.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔍", fontSize = 38.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("No Songs Found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Search full songs by 'Arijit Singh', 'Atif Aslam', 'Coke Studio', 'Sidhu Moosewala', 'Taylor Swift', 'Nusrat Fateh Ali Khan', or explore genres above.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = if (currentSong != null) 90.dp else 16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (searchInput.isNotBlank()) "Results for \"$searchInput\"" else "Trending in $selectedGenre",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${onlineResults.size} tracks",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(onlineResults, key = { it.externalId ?: it.uriString }) { song ->
                        val isCurrent = (currentSong?.externalId != null && currentSong.externalId == song.externalId) ||
                                (currentSong?.uriString == song.uriString)
                        SongItemCard(
                            song = song,
                            isCurrent = isCurrent,
                            isPlaying = isPlaying && isCurrent,
                            onPlay = { onPlaySong(song) },
                            onToggleFavorite = { onToggleFavorite(song) },
                            onAddToPlaylist = { onAddToPlaylist(song) },
                            onOpenRemix = { onOpenRemix(song) },
                            onDelete = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SongItemCard(
    song: SongEntity,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onOpenRemix: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val albumColor = remember(song.albumColorHex) {
        try {
            Color(android.graphics.Color.parseColor(song.albumColorHex))
        } catch (e: Exception) {
            Color(0xFF8B5CF6)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onPlay() }
            .testTag("song_item_${song.externalId ?: song.id}"),
        color = if (isCurrent) albumColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = if (isCurrent) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Album Artwork thumbnail (AsyncImage / Bitmap / Gradient fallback)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(albumColor, albumColor.copy(alpha = 0.6f), MaterialTheme.colorScheme.primary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = remember(song.artworkBase64) {
                    song.artworkBase64?.let { b64 ->
                        try {
                            val bytes = Base64.decode(b64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

                if (!song.artworkUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = song.artworkUrl,
                        contentDescription = song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Cover",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (isCurrent && isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Playing",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Song Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isCurrent) albumColor else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (song.isOnline) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ONLINE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Play / Pause Icon Button
            IconButton(
                onClick = onPlay,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (isCurrent && isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                    contentDescription = if (isCurrent && isPlaying) "Pause" else "Play",
                    tint = if (isCurrent) albumColor else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Favorite Button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (song.isFavorite) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // More Options Dropdown
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) },
                        text = { Text("Add to Playlist") },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                        text = { Text("Open in Remix Studio") },
                        onClick = {
                            showMenu = false
                            onOpenRemix()
                        }
                    )
                    if (!song.externalUrl.isNullOrBlank()) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.OpenInBrowser, contentDescription = null) },
                            text = { Text("Open Web Link") },
                            onClick = {
                                showMenu = false
                                try {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(song.externalUrl))
                                    context.startActivity(browserIntent)
                                } catch (e: Exception) {
                                    // ignore
                                }
                            }
                        )
                    }
                    if (onDelete != null) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            text = { Text("Delete from Library", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistsSection(
    playlists: List<PlaylistEntity>,
    onCreatePlaylist: () -> Unit,
    onSelectPlaylist: (PlaylistEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onCreatePlaylist,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Create New Playlist")
        }

        if (playlists.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No playlists created yet.\nTap above to create your first playlist!",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(playlists) { pl ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectPlaylist(pl) },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(pl.icon, fontSize = 28.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pl.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (pl.description.isNotBlank()) {
                                    Text(pl.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistDetailView(
    playlist: PlaylistEntity,
    songs: List<SongEntity>,
    currentSong: SongEntity?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlaySong: (SongEntity) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onRemoveSong: (SongEntity) -> Unit,
    onDeletePlaylist: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Playlist Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "${playlist.icon} ${playlist.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDeletePlaylist) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = MaterialTheme.colorScheme.error)
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onPlayAll,
                modifier = Modifier.weight(1f),
                enabled = songs.isNotEmpty()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Play All (${songs.size})")
            }

            FilledTonalButton(
                onClick = onShuffle,
                modifier = Modifier.weight(1f),
                enabled = songs.isNotEmpty()
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Shuffle")
            }
        }

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No songs added to this playlist yet.\nBrowse Discover or Library and tap '⋮' -> 'Add to Playlist'!",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(songs) { song ->
                    val isCurrent = currentSong?.id == song.id || (song.externalId != null && currentSong?.externalId == song.externalId)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPlaySong(song) },
                        color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isCurrent && isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(song.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Text(song.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onRemoveSong(song) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySongsView(
    tab: MusicTab,
    onAddMusic: () -> Unit,
    onSwitchToOnline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("🎧", fontSize = 42.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = when (tab) {
                MusicTab.FAVORITES -> "No Favorite Songs Yet"
                MusicTab.RECENT -> "No Recently Played Songs"
                else -> "Your Music Library is Empty"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = when (tab) {
                MusicTab.FAVORITES -> "Tap the heart ♡ on any song in Discover or Library to add it to your favorites."
                MusicTab.RECENT -> "Tracks you listen to will appear here for fast replay."
                else -> "Explore millions of online songs in Discover, or import your local audio files (MP3, WAV, M4A, OGG)."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onSwitchToOnline,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Public, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Search Online")
            }
            OutlinedButton(
                onClick = onAddMusic,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("+ Import")
            }
        }
    }
}
