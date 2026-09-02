package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.NotificationHelper
import com.example.notification.PushNotificationManager
import com.example.ui.gallery.GalleryScreen
import com.example.ui.navigation.MainTab
import com.example.ui.screens.birthday.BirthdayExperienceScreen
import com.example.ui.screens.dates.ImportantDatesScreen
import com.example.ui.screens.habits.HabitsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.music.FullScreenPlayer
import com.example.ui.screens.music.MiniPlayer
import com.example.ui.screens.music.MusicScreen
import com.example.ui.screens.music.RemixStudioSheet
import com.example.ui.screens.notes.NotesScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.pia.PiaScreen
import com.example.ui.screens.rewards.AdminRewardConfigScreen
import com.example.ui.screens.rewards.RewardsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.surprise.LittleSurpriseDialog
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.theme.LittleSpaceTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var pushManager: PushNotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        pushManager = PushNotificationManager(applicationContext)
        pushManager.initialize()

        handleBirthdayIntent(intent)

        setContent {
            val context = LocalContext.current
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            val showLittleSurprise by viewModel.showLittleSurprise.collectAsStateWithLifecycle()
            val isBirthdayExperienceOpen by viewModel.isBirthdayExperienceOpen.collectAsStateWithLifecycle()
            val isAdminConfigOpen by viewModel.isAdminConfigOpen.collectAsStateWithLifecycle()

            // Request Notification Permission on Android 13+ (API 33+)
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    pushManager.syncTokenAndRegistration()
                }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!pushManager.isNotificationPermissionGranted()) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            // Music states
            val currentPlayingSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
            val isAudioPlaying by viewModel.isAudioPlaying.collectAsStateWithLifecycle()
            val audioProgressMs by viewModel.audioPositionMs.collectAsStateWithLifecycle()
            val audioDurationMs by viewModel.audioDurationMs.collectAsStateWithLifecycle()
            val isAudioShuffle by viewModel.isAudioShuffle.collectAsStateWithLifecycle()
            val audioRepeatMode by viewModel.audioRepeatMode.collectAsStateWithLifecycle()
            val audioQueue by viewModel.audioQueue.collectAsStateWithLifecycle()
            val remixState by viewModel.remixState.collectAsStateWithLifecycle()
            val remixPresets by viewModel.remixPresets.collectAsStateWithLifecycle()
            val visualizerFrequencies by viewModel.visualizerFrequencies.collectAsStateWithLifecycle()
            val visualizerType by viewModel.visualizerType.collectAsStateWithLifecycle()
            val isFullScreenPlayerOpen by viewModel.isFullScreenPlayerOpen.collectAsStateWithLifecycle()
            val isRemixStudioOpen by viewModel.isRemixStudioOpen.collectAsStateWithLifecycle()

            var forceOnboarding by remember { mutableStateOf(false) }
            var currentScreen by remember { mutableStateOf<String>("MAIN") } // "MAIN", "DATES", "SETTINGS", "GALLERY", "GALLERY_STUDIO"
            var studioPhotoPaths by remember { mutableStateOf<List<String>>(emptyList()) }
            var selectedTab by remember { mutableStateOf(MainTab.HOME) }

            LittleSpaceTheme(
                selectedTheme = userProfile.favoriteTheme,
                isDarkMode = userProfile.isDarkMode,
                followSystem = userProfile.followSystemTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isBirthdayExperienceOpen) {
                        // Full Screen Birthday Experience (Separate immersive mini-experience)
                        BirthdayExperienceScreen(
                            viewModel = viewModel,
                            onClose = { viewModel.openBirthdayExperience(false) }
                        )
                    } else if (isAdminConfigOpen) {
                        // Admin / Owner configuration for Birthday & Milestone Surprises
                        AdminRewardConfigScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.openAdminConfig(false) }
                        )
                    } else if (!userProfile.isOnboardingCompleted || forceOnboarding) {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onFinish = {
                                forceOnboarding = false
                                currentScreen = "MAIN"
                            }
                        )
                    } else {
                        when (currentScreen) {
                            "DATES" -> {
                                ImportantDatesScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = "MAIN" }
                                )
                            }
                            "NOTES" -> {
                                NotesScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = "MAIN" }
                                )
                            }
                            "SETTINGS" -> {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = "MAIN" },
                                    onRerunOnboarding = {
                                        currentScreen = "MAIN"
                                        forceOnboarding = true
                                    }
                                )
                            }
                            "GALLERY" -> {
                                GalleryScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = "MAIN" }
                                )
                            }
                            else -> {
                                Scaffold(
                                    bottomBar = {
                                        Column {
                                            // Mini Player above Bottom Bar
                                            MiniPlayer(
                                                currentSong = currentPlayingSong,
                                                isPlaying = isAudioPlaying,
                                                progressMs = audioProgressMs,
                                                durationMs = audioDurationMs,
                                                frequencies = visualizerFrequencies,
                                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                                onNext = { viewModel.playNextSong() },
                                                onClick = { viewModel.openFullScreenPlayer(true) }
                                            )

                                            Surface(
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                            ) {
                                                NavigationBar(
                                                    modifier = Modifier.testTag("bottom_nav_bar"),
                                                    containerColor = MaterialTheme.colorScheme.surface,
                                                    tonalElevation = 0.dp
                                                ) {
                                                    MainTab.entries.forEach { tab ->
                                                        val isSelected = selectedTab == tab
                                                        NavigationBarItem(
                                                            selected = isSelected,
                                                            onClick = { selectedTab = tab },
                                                            icon = {
                                                                Icon(
                                                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                                    contentDescription = tab.title
                                                                )
                                                            },
                                                            label = {
                                                                Text(
                                                                    text = tab.title,
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                                )
                                                            },
                                                            colors = NavigationBarItemDefaults.colors(
                                                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                selectedTextColor = MaterialTheme.colorScheme.onBackground,
                                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                            ),
                                                            modifier = Modifier.testTag(tab.testTag)
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
                                    ) {
                                        when (selectedTab) {
                                            MainTab.HOME -> HomeScreen(
                                                viewModel = viewModel,
                                                onNavigateToTab = { tab -> selectedTab = tab },
                                                onOpenImportantDates = { currentScreen = "DATES" },
                                                onOpenNotes = { currentScreen = "NOTES" },
                                                onOpenSettings = { currentScreen = "SETTINGS" },
                                                 onOpenGallery = { currentScreen = "GALLERY" }
                                            )
                                            MainTab.TASKS -> TasksScreen(
                                                viewModel = viewModel
                                            )
                                            MainTab.HABITS -> HabitsScreen(
                                                viewModel = viewModel
                                            )
                                            MainTab.MUSIC -> MusicScreen(
                                                viewModel = viewModel
                                            )
                                            MainTab.REWARDS -> RewardsScreen(
                                                viewModel = viewModel,
                                                onOpenBirthdayExperience = { viewModel.openBirthdayExperience(true) },
                                                onOpenAdminConfig = { viewModel.openAdminConfig(true) }
                                            )
                                            MainTab.PIA -> PiaScreen(
                                                viewModel = viewModel
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Full-Screen Music Player Modal
                    if (isFullScreenPlayerOpen && currentPlayingSong != null) {
                        FullScreenPlayer(
                            viewModel = viewModel,
                            currentSong = currentPlayingSong,
                            isPlaying = isAudioPlaying,
                            progressMs = audioProgressMs,
                            durationMs = audioDurationMs,
                            isShuffle = isAudioShuffle,
                            repeatMode = audioRepeatMode,
                            queue = audioQueue,
                            frequencies = visualizerFrequencies,
                            visualizerType = visualizerType,
                            onClose = { viewModel.openFullScreenPlayer(false) }
                        )
                    }

                    // Remix Studio Modal Bottom Sheet
                    if (isRemixStudioOpen) {
                        RemixStudioSheet(
                            viewModel = viewModel,
                            remixState = remixState,
                            customPresets = remixPresets,
                            onDismiss = { viewModel.openRemixStudio(false) }
                        )
                    }

                    // Little Surprise Wholesome Modal Dialog
                    if (showLittleSurprise) {
                        LittleSurpriseDialog(
                            onDismiss = { viewModel.toggleLittleSurprise(false) }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleBirthdayIntent(intent)
    }

    private fun handleBirthdayIntent(intent: Intent?) {
        if (intent == null) return
        val openBirthday = intent.getBooleanExtra(NotificationHelper.EXTRA_OPEN_BIRTHDAY_SURPRISE, false)
        val action = intent.action
        if (openBirthday || action == "ACTION_OPEN_BIRTHDAY_SURPRISE" || action == "ACTION_TEST_BIRTHDAY_NOTIFICATION") {
            viewModel.openBirthdayExperience(true)
        }
    }
}
