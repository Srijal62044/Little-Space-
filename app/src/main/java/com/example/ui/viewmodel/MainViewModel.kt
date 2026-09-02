package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val taskRepo = TaskRepository(db.taskDao())
    val habitRepo = HabitRepository(db.habitDao())
    val noteRepo = NoteRepository(db.noteDao())
    val moodRepo = MoodRepository(db.moodDao())
    val dateRepo = ImportantDateRepository(db.importantDateDao())
    val profileRepo = UserProfileRepository(db.userProfileDao())
    val chatRepo = ChatRepository(db.chatDao())
    val rewardRepo = RewardRepository(db.rewardConfigDao(), db.dailyActivityDao(), db.birthdayMemoryDao())
    val musicRepo = MusicRepository(db.musicDao())
    val galleryRepo = GalleryRepository(db.galleryDao())
    val onlineMusicProvider: com.example.data.provider.MusicProvider = com.example.data.provider.SaavnMusicProvider()
    val audioPlayerManager = com.example.ui.audio.AudioPlayerManager(application)
    val aiPlannerRepo = AiPlannerRepository()

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String = dateFormat.format(Date())

    // Observables
    val userProfile: StateFlow<UserProfileEntity> = profileRepo.userProfile
        .map { it ?: UserProfileEntity() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfileEntity())

    val rewardConfig: StateFlow<RewardConfigEntity> = rewardRepo.rewardConfig
        .map { it ?: RewardConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RewardConfigEntity())

    val allActivities: StateFlow<List<DailyActivityEntity>> = rewardRepo.allActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val birthdayMemories: StateFlow<List<BirthdayMemoryEntity>> = rewardRepo.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = taskRepo.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabits: StateFlow<List<HabitEntity>> = habitRepo.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayHabitLogs: StateFlow<List<HabitLogEntity>> = habitRepo.getLogsForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<NoteEntity>> = noteRepo.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMood: StateFlow<MoodEntryEntity?> = moodRepo.getTodayMood(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allDates: StateFlow<List<ImportantDateEntity>> = dateRepo.allDates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = chatRepo.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Music State
    val allSongs: StateFlow<List<SongEntity>> = musicRepo.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<SongEntity>> = musicRepo.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedSongs: StateFlow<List<SongEntity>> = musicRepo.recentlyPlayed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<PlaylistEntity>> = musicRepo.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val remixPresets: StateFlow<List<RemixPresetEntity>> = musicRepo.allRemixPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gallery State
    val allGalleryCreations: StateFlow<List<GalleryCreationEntity>> = galleryRepo.allCreations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteGalleryCreations: StateFlow<List<GalleryCreationEntity>> = galleryRepo.favoriteCreations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Online Music (Public API) State
    private val _onlineSearchResults = MutableStateFlow<List<SongEntity>>(emptyList())
    val onlineSearchResults: StateFlow<List<SongEntity>> = _onlineSearchResults.asStateFlow()

    private val _isOnlineSearching = MutableStateFlow(false)
    val isOnlineSearching: StateFlow<Boolean> = _isOnlineSearching.asStateFlow()

    private val _onlineSearchError = MutableStateFlow<String?>(null)
    val onlineSearchError: StateFlow<String?> = _onlineSearchError.asStateFlow()

    private val _currentOnlineQuery = MutableStateFlow("")
    val currentOnlineQuery: StateFlow<String> = _currentOnlineQuery.asStateFlow()

    private val _selectedOnlineGenre = MutableStateFlow("Top Hits")
    val selectedOnlineGenre: StateFlow<String> = _selectedOnlineGenre.asStateFlow()

    private val _onlineSearchFilter = MutableStateFlow(com.example.data.provider.MusicSearchFilter.ALL)
    val onlineSearchFilter: StateFlow<com.example.data.provider.MusicSearchFilter> = _onlineSearchFilter.asStateFlow()

    val currentPlayingSong = audioPlayerManager.currentSong
    val isAudioPlaying = audioPlayerManager.isPlaying
    val audioPositionMs = audioPlayerManager.currentPositionMs
    val audioDurationMs = audioPlayerManager.durationMs
    val isAudioShuffle = audioPlayerManager.isShuffle
    val audioRepeatMode = audioPlayerManager.repeatMode
    val audioQueue = audioPlayerManager.queue
    val remixState = audioPlayerManager.remixState
    val visualizerFrequencies = audioPlayerManager.visualizerFrequencies
    val visualizerType = audioPlayerManager.visualizerType
    val isFullScreenPlayerOpen = audioPlayerManager.isFullScreenPlayerOpen
    val isRemixStudioOpen = audioPlayerManager.isRemixStudioOpen

    // Computed Streak and Milestones
    val currentStreak: StateFlow<Int> = allActivities.map { activities ->
        computeCurrentStreak(activities)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val longestStreak: StateFlow<Int> = allActivities.map { activities ->
        computeLongestStreak(activities)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCompletedDays: StateFlow<Int> = allActivities.map { activities ->
        activities.count { it.isStreakAchieved }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val nextMilestone: StateFlow<Int> = currentStreak.map { streak ->
        when {
            streak < 7 -> 7
            streak < 30 -> 30
            streak < 50 -> 50
            streak < 100 -> 100
            else -> 100
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)

    val daysToNextMilestone: StateFlow<Int> = combine(currentStreak, nextMilestone) { streak, milestone ->
        (milestone - streak).coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)

    // Real-time Birthday status & countdown
    val isTodayBirthday: StateFlow<Boolean> = rewardConfig.map { config ->
        val now = Calendar.getInstance()
        val curMonth = now.get(Calendar.MONTH) + 1
        val curDay = now.get(Calendar.DAY_OF_MONTH)
        config.isBirthdayTestMode || (curMonth == config.birthdayMonth && curDay == config.birthdayDay)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val daysUntilBirthday: StateFlow<Int> = rewardConfig.map { config ->
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val bday = Calendar.getInstance().apply {
            set(Calendar.MONTH, config.birthdayMonth - 1)
            set(Calendar.DAY_OF_MONTH, config.birthdayDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (bday.before(todayStart)) {
            bday.add(Calendar.YEAR, 1)
        }
        val diffMillis = bday.timeInMillis - todayStart.timeInMillis
        (diffMillis / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)

    // UI state
    private val _isBirthdayExperienceOpen = MutableStateFlow(false)
    val isBirthdayExperienceOpen: StateFlow<Boolean> = _isBirthdayExperienceOpen.asStateFlow()

    private val _isAdminConfigOpen = MutableStateFlow(false)
    val isAdminConfigOpen: StateFlow<Boolean> = _isAdminConfigOpen.asStateFlow()

    private val _celebrationReward = MutableStateFlow<Pair<Int, String>?>(null) // milestone to title
    val celebrationReward: StateFlow<Pair<Int, String>?> = _celebrationReward.asStateFlow()

    private val _highlightCountdown = MutableStateFlow(false)
    val highlightCountdown: StateFlow<Boolean> = _highlightCountdown.asStateFlow()

    private val _highlightCountdownDaysRemaining = MutableStateFlow<Int?>(null)
    val highlightCountdownDaysRemaining: StateFlow<Int?> = _highlightCountdownDaysRemaining.asStateFlow()

    fun setHighlightCountdown(highlight: Boolean, daysRemaining: Int? = null) {
        _highlightCountdown.value = highlight
        _highlightCountdownDaysRemaining.value = daysRemaining
    }

    // UI state
    private val _isPiaThinking = MutableStateFlow(false)
    val isPiaThinking: StateFlow<Boolean> = _isPiaThinking.asStateFlow()

    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _generatedPlan = MutableStateFlow<String?>(null)
    val generatedPlan: StateFlow<String?> = _generatedPlan.asStateFlow()

    private val _showLittleSurprise = MutableStateFlow(false)
    val showLittleSurprise: StateFlow<Boolean> = _showLittleSurprise.asStateFlow()

    private val motivationalQuotes = listOf(
        "Small progress is still progress.",
        "Don’t try to finish everything. Just start with one thing.",
        "You’ve got this, Priyanka 🌷",
        "Take a deep breath. Today has plenty of grace for you.",
        "Protect your peace and celebrate the small wins.",
        "Consistency is a love letter to your future self.",
        "A gentle step forward is all that's required today."
    )

    private val _dailyQuote = MutableStateFlow(getQuoteForToday())
    val dailyQuote: StateFlow<String> = _dailyQuote.asStateFlow()

    init {
        seedInitialDataIfEmpty()
        loadOnlineDiscover("Top Hits")
        observeDailyActivities()
    }

    private fun getQuoteForToday(): String {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return motivationalQuotes[dayOfYear % motivationalQuotes.size]
    }

    private fun seedInitialDataIfEmpty() {
        viewModelScope.launch {
            val profile = profileRepo.getUserProfileOnce()
            if (profile == null) {
                // Initialize default profile
                profileRepo.saveUserProfile(UserProfileEntity())
            }

            // If no habits exist, seed initial default habits for Priyanka
            val initialHabits = habitRepo.allHabits.first()
            if (initialHabits.isEmpty()) {
                val defaults = listOf(
                    HabitEntity(title = "Drink enough water", icon = "💧", category = "Health", reminderTime = "08:30"),
                    HabitEntity(title = "Daily study / Focus", icon = "📚", category = "Study", reminderTime = "10:00"),
                    HabitEntity(title = "Evening walk", icon = "🚶", category = "Health", reminderTime = "18:00"),
                    HabitEntity(title = "Read a chapter", icon = "📖", category = "Mind", reminderTime = "21:30"),
                    HabitEntity(title = "Relax & Unwind", icon = "🧘", category = "Wellness", reminderTime = "22:00"),
                    HabitEntity(title = "Sleep on time", icon = "🌙", category = "Routine", reminderTime = "23:00")
                )
                defaults.forEach { habitRepo.insertHabit(it) }
            }

            // If no tasks exist, seed initial helpful tasks
            val initialTasks = taskRepo.allTasks.first()
            if (initialTasks.isEmpty()) {
                seedStarterTasks()
            }

            // Seed sample notes
            val initialNotes = noteRepo.allNotes.first()
            if (initialNotes.isEmpty()) {
                val defaultNotes = listOf(
                    NoteEntity(
                        title = "🌷 Little Space Reminders",
                        content = "• Be gentle with yourself on busy days.\n• Focus on one milestone at a time.\n• Keep hydrated!",
                        category = "Ideas",
                        colorTag = "Blush",
                        isPinned = true
                    ),
                    NoteEntity(
                        title = "📚 Current Focus Topics",
                        content = "Key chapters and revision topics for this week. Keep short summaries after each section.",
                        category = "Remember",
                        colorTag = "Sage",
                        isPinned = false
                    ),
                    NoteEntity(
                        title = "🛍️ Cozy Shopping List",
                        content = "1. Green tea & herbal blend\n2. Scented candle / lavender\n3. Cute notebook",
                        category = "Shopping",
                        colorTag = "Peach",
                        isPinned = false
                    )
                )
                defaultNotes.forEach { noteRepo.insertNote(it) }
            }

            // Seed sample important dates
            val initialDates = dateRepo.allDates.first()
            if (initialDates.isEmpty()) {
                val calExam = Calendar.getInstance()
                calExam.add(Calendar.DAY_OF_YEAR, 5)
                val examDate = dateFormat.format(calExam.time)

                val defaultDates = listOf(
                    ImportantDateEntity(
                        title = "Priyanka's Birthday 🎂",
                        targetDate = "2026-09-10",
                        category = "Birthday",
                        icon = "🎂",
                        notes = "10th September 2026 - Special celebration day!"
                    ),
                    ImportantDateEntity(
                        title = "Semester Milestone Exam 📚",
                        targetDate = examDate,
                        category = "Deadline",
                        icon = "📚",
                        notes = "Final review and prep"
                    )
                )
                defaultDates.forEach { dateRepo.insertDate(it) }
            }

            // Seed initial greeting message in chat
            val initialChat = chatRepo.allMessages.first()
            if (initialChat.isEmpty()) {
                chatRepo.insertMessage(
                    ChatMessageEntity(
                        sender = "pia",
                        text = "Hey Priyanka! 👋 How can I help you today?"
                    )
                )
            }

            // Seed initial reward config if empty or update old defaults
            val initialConfig = rewardRepo.getRewardConfigOnce()
            if (initialConfig == null) {
                rewardRepo.saveRewardConfig(
                    RewardConfigEntity(
                        birthdayMonth = 9,
                        birthdayDay = 10,
                        adminPin = "7890",
                        unlockedMilestones = ""
                    )
                )
            } else {
                val updatedUnlocked = if (initialConfig.unlockedMilestones == "7") "" else initialConfig.unlockedMilestones
                rewardRepo.saveRewardConfig(
                    initialConfig.copy(
                        birthdayMonth = 9,
                        birthdayDay = 10,
                        adminPin = "7890",
                        unlockedMilestones = updatedUnlocked
                    )
                )
            }

            // Seed initial memories if empty
            val initialMemories = rewardRepo.allMemories.first()
            if (initialMemories.isEmpty()) {
                val memories = listOf(
                    BirthdayMemoryEntity(
                        emoji = "☕🌸",
                        title = "Morning Reflections",
                        caption = "Starting the day with gentle intention and warm tea.",
                        dateTag = "Everyday Joy",
                        orderIndex = 1
                    ),
                    BirthdayMemoryEntity(
                        emoji = "📚✨",
                        title = "A Great Study Milestone",
                        caption = "Focusing on what truly matters and achieving key goals.",
                        dateTag = "Achievement",
                        orderIndex = 2
                    ),
                    BirthdayMemoryEntity(
                        emoji = "🌿🚶‍♀️",
                        title = "Golden Hour Walks",
                        caption = "Taking a peaceful stroll to recharge and enjoy the quiet sunset.",
                        dateTag = "Peaceful Habit",
                        orderIndex = 3
                    ),
                    BirthdayMemoryEntity(
                        emoji = "🎁🎂",
                        title = "Celebrating You",
                        caption = "A little reminder that you are deeply appreciated every day.",
                        dateTag = "Birthday Special",
                        orderIndex = 4
                    )
                )
                rewardRepo.insertAllMemories(memories)
            }

            // Recalculate today's real activity based on actual user completed tasks & habits
            recalculateDailyActivity()

            // Remove all default / preset procedural audios from the library
            musicRepo.deleteDefaultSongs()
            if (audioPlayerManager.currentSong.value?.isPresetSong == true || audioPlayerManager.currentSong.value?.uriString?.startsWith("procedural://") == true) {
                audioPlayerManager.stop()
            }

            // Seed starter custom remix presets if empty
            val initialPresets = musicRepo.allRemixPresets.first()
            if (initialPresets.isEmpty()) {
                val defaultPresets = listOf(
                    RemixPresetEntity(name = "Priyanka's Night Mix 🌙", isCustom = true, bass = 35f, treble = -15f, vocal = 10f, reverb = 50f, echoDelay = 25f, speed = 0.92f, pitch = 0.95f, volume = 95f, balance = 0f),
                    RemixPresetEntity(name = "Deep Lo-Fi Study 📚", isCustom = true, bass = 45f, treble = -25f, vocal = 5f, reverb = 30f, echoDelay = 15f, speed = 0.88f, pitch = 0.92f, volume = 90f, balance = 0f),
                    RemixPresetEntity(name = "Sparkle Acoustic ✨", isCustom = true, bass = 10f, treble = 40f, vocal = 30f, reverb = 60f, echoDelay = 30f, speed = 1.0f, pitch = 1.05f, volume = 100f, balance = 0f)
                )
                defaultPresets.forEach { musicRepo.saveRemixPreset(it) }
            }
        }
    }

    private fun computeCurrentStreak(activities: List<DailyActivityEntity>): Int {
        if (activities.isEmpty()) return 0
        val activityMap = activities.associateBy { it.date }
        val cal = Calendar.getInstance()

        // Check today
        val today = dateFormat.format(cal.time)
        val todayActivity = activityMap[today]
        val isTodayCompleted = todayActivity?.isStreakAchieved == true

        if (!isTodayCompleted) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        var streak = 0
        while (true) {
            val dateStr = dateFormat.format(cal.time)
            val act = activityMap[dateStr]
            if (act != null && act.isStreakAchieved) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    private fun computeLongestStreak(activities: List<DailyActivityEntity>): Int {
        if (activities.isEmpty()) return 0
        val sortedDates = activities.filter { it.isStreakAchieved }
            .mapNotNull {
                try { dateFormat.parse(it.date) } catch (e: Exception) { null }
            }
            .sorted()
        if (sortedDates.isEmpty()) return 0

        var maxStreak = 1
        var current = 1
        for (i in 1 until sortedDates.size) {
            val diff = (sortedDates[i].time - sortedDates[i - 1].time) / (1000 * 60 * 60 * 24)
            if (diff == 1L) {
                current++
                if (current > maxStreak) maxStreak = current
            } else if (diff > 1L) {
                current = 1
            }
        }
        return maxStreak
    }

    private fun checkAndUnlockMilestones(streak: Int) {
        viewModelScope.launch {
            val config = rewardRepo.getRewardConfigOnce() ?: RewardConfigEntity()
            val currentUnlocked = config.unlockedMilestones
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toMutableSet()

            var updated = false
            listOf(7, 30, 50, 100).forEach { milestone ->
                if (streak >= milestone && !currentUnlocked.contains(milestone)) {
                    currentUnlocked.add(milestone)
                    updated = true
                }
            }

            if (updated) {
                val newUnlockedString = currentUnlocked.sorted().joinToString(",")
                rewardRepo.saveRewardConfig(config.copy(unlockedMilestones = newUnlockedString))
            }
        }
    }

    private fun observeDailyActivities() {
        viewModelScope.launch {
            combine(
                taskRepo.allTasks,
                habitRepo.allHabits,
                habitRepo.getLogsForDate(todayDateString),
                rewardRepo.rewardConfig
            ) { tasks, habits, habitLogs, config ->
                val cfg = config ?: RewardConfigEntity()
                val tasksCompleted = tasks.count { it.isCompleted }
                val habitsCompleted = habitLogs.count { it.isCompleted }

                val isStreakAchieved = when (cfg.streakRule) {
                    "AT_LEAST_ONE_TASK" -> tasksCompleted >= 1
                    "ALL_HABITS" -> habits.isNotEmpty() && habitsCompleted >= habits.size
                    "AT_LEAST_ONE_HABIT" -> habitsCompleted >= 1
                    else -> tasksCompleted >= 1 || habitsCompleted >= 1
                }

                DailyActivityEntity(
                    date = todayDateString,
                    tasksCompleted = tasksCompleted,
                    habitsCompleted = habitsCompleted,
                    isStreakAchieved = isStreakAchieved
                )
            }.collect { activity ->
                val currentToday = rewardRepo.getActivityForDate(todayDateString)
                if (currentToday == null ||
                    currentToday.tasksCompleted != activity.tasksCompleted ||
                    currentToday.habitsCompleted != activity.habitsCompleted ||
                    currentToday.isStreakAchieved != activity.isStreakAchieved
                ) {
                    rewardRepo.recordDailyActivity(activity)
                    val updatedActivities = rewardRepo.getAllActivitiesOnce()
                    val newStreak = computeCurrentStreak(updatedActivities)
                    checkAndUnlockMilestones(newStreak)
                }
            }
        }
    }

    fun recalculateDailyActivity() {
        viewModelScope.launch {
            val tasks = taskRepo.allTasks.first()
            val habits = habitRepo.allHabits.first()
            val habitLogs = habitRepo.getLogsForDate(todayDateString).first()
            val config = rewardRepo.getRewardConfigOnce() ?: RewardConfigEntity()

            val tasksCompleted = tasks.count { it.isCompleted }
            val habitsCompleted = habitLogs.count { it.isCompleted }

            val isStreakAchieved = when (config.streakRule) {
                "AT_LEAST_ONE_TASK" -> tasksCompleted >= 1
                "ALL_HABITS" -> habits.isNotEmpty() && habitsCompleted >= habits.size
                "AT_LEAST_ONE_HABIT" -> habitsCompleted >= 1
                else -> tasksCompleted >= 1 || habitsCompleted >= 1
            }

            rewardRepo.recordDailyActivity(
                DailyActivityEntity(
                    date = todayDateString,
                    tasksCompleted = tasksCompleted,
                    habitsCompleted = habitsCompleted,
                    isStreakAchieved = isStreakAchieved
                )
            )

            val updatedActivities = rewardRepo.getAllActivitiesOnce()
            val newStreak = computeCurrentStreak(updatedActivities)
            checkAndUnlockMilestones(newStreak)
        }
    }

    // Onboarding completion
    fun completeOnboarding(
        name: String,
        wakeTime: String,
        sleepTime: String,
        activities: String,
        habits: String,
        reminders: String,
        goals: String,
        theme: String,
        strictness: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = UserProfileEntity(
                id = 1,
                name = name.ifBlank { "Priyanka" },
                wakeTime = wakeTime,
                sleepTime = sleepTime,
                importantActivities = activities,
                targetHabits = habits,
                reminderType = strictness,
                currentGoals = goals,
                favoriteTheme = theme,
                morningGreeting = "Good morning, ${name.ifBlank { "Priyanka" }} 🌷",
                isOnboardingCompleted = true,
                isDarkMode = current.isDarkMode,
                followSystemTheme = current.followSystemTheme
            )
            profileRepo.saveUserProfile(updated)
        }
    }

    // Task actions
    fun seedStarterTasks() {
        viewModelScope.launch {
            val defaultTasks = listOf(
                TaskEntity(
                    title = "Review priority study / work goals",
                    category = "Study",
                    priority = "High",
                    dueDate = todayDateString,
                    dueTime = "10:30"
                ),
                TaskEntity(
                    title = "Afternoon hydration & stretch pause",
                    category = "Personal",
                    priority = "Medium",
                    dueDate = todayDateString,
                    dueTime = "15:00"
                ),
                TaskEntity(
                    title = "Evening walk & listen to favorite playlist",
                    category = "Wellness",
                    priority = "Low",
                    dueDate = todayDateString,
                    dueTime = "18:30"
                ),
                TaskEntity(
                    title = "Read a chapter or wind-down routine",
                    category = "Mind",
                    priority = "Medium",
                    dueDate = todayDateString,
                    dueTime = "21:30"
                )
            )
            defaultTasks.forEach { taskRepo.insertTask(it) }
        }
    }

    fun addTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        dueDate: String,
        dueTime: String,
        recurrence: String = "None"
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                description = description,
                category = category,
                priority = priority,
                dueDate = dueDate,
                dueTime = dueTime,
                recurrence = recurrence,
                isRecurring = recurrence != "None"
            )
            taskRepo.insertTask(task)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepo.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepo.deleteTask(task)
        }
    }

    fun toggleTask(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepo.toggleTaskCompletion(id, isCompleted)
            recalculateDailyActivity()
        }
    }

    // Habit actions
    fun addHabit(title: String, icon: String, category: String, reminderTime: String) {
        viewModelScope.launch {
            val habit = HabitEntity(
                title = title,
                icon = icon.ifBlank { "🌱" },
                category = category,
                reminderTime = reminderTime
            )
            habitRepo.insertHabit(habit)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepo.deleteHabit(habit)
            recalculateDailyActivity()
        }
    }

    fun toggleHabitToday(habit: HabitEntity, isCompleted: Boolean) {
        viewModelScope.launch {
            habitRepo.toggleHabitLog(habit.id, todayDateString, isCompleted, habit)
            recalculateDailyActivity()
        }
    }

    // Mood actions
    fun recordMood(mood: String, emoji: String, note: String = "") {
        viewModelScope.launch {
            val (message, suggestion) = when (mood.lowercase()) {
                "great" -> "Wonderful to hear! Keep this radiant energy going 🌷" to "Share a smile, write down what went right!"
                "good" -> "Glad you're having a pleasant day! Keep at it ✨" to "Enjoy a warm cup of tea and a moment of gratitude."
                "okay" -> "Steady days are good days. Take things at your own comfortable pace." to "Take a 5-minute stretch or walk outside 🌿"
                "not great" -> "It's completely okay to have slower days. Don't push too hard today 🌷" to "Put on your favorite gentle song and hydrate 💧"
                "low" -> "Sending you warmth. Just focus on whatever feels manageable right now." to "Do one tiny relaxing thing: a warm shower or cozy blanket."
                "tired" -> "Your body is asking for a rest. Listen to it and pace yourself tonight 🌙" to "Plan to sleep a bit early and avoid late screens."
                else -> "Take it one moment at a time 🌷" to "A gentle 5-minute breather."
            }

            val entry = MoodEntryEntity(
                mood = mood,
                moodEmoji = emoji,
                supportiveMessage = message,
                activitySuggestion = suggestion,
                note = note,
                dateString = todayDateString
            )
            moodRepo.insertMoodEntry(entry)
        }
    }

    // Notes actions
    fun addNote(title: String, content: String, category: String, colorTag: String, isPinned: Boolean) {
        viewModelScope.launch {
            val note = NoteEntity(
                title = title,
                content = content,
                category = category,
                colorTag = colorTag,
                isPinned = isPinned,
                updatedAt = System.currentTimeMillis()
            )
            noteRepo.insertNote(note)
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepo.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepo.deleteNote(note)
        }
    }

    // Important Dates actions
    fun addDate(title: String, targetDate: String, category: String, icon: String, notes: String) {
        viewModelScope.launch {
            val date = ImportantDateEntity(
                title = title,
                targetDate = targetDate,
                category = category,
                icon = icon.ifBlank { "🎂" },
                notes = notes
            )
            dateRepo.insertDate(date)
        }
    }

    fun deleteDate(date: ImportantDateEntity) {
        viewModelScope.launch {
            dateRepo.deleteDate(date)
        }
    }

    // AI Planner action
    fun generateDailyPlan(userPrompt: String) {
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            _generatedPlan.value = null

            val tasks = allTasks.value.filter { !it.isCompleted }.joinToString("; ") { "${it.title} (${it.priority} priority)" }
            val goals = userProfile.value.currentGoals

            val plan = aiPlannerRepo.planDay(userPrompt, tasks, goals)
            _generatedPlan.value = plan
            _isGeneratingPlan.value = false
        }
    }

    fun clearGeneratedPlan() {
        _generatedPlan.value = null
    }

    // Chat with Pia
    fun sendMessageToPia(messageText: String) {
        if (messageText.isBlank()) return

        viewModelScope.launch {
            // Save user message
            chatRepo.insertMessage(ChatMessageEntity(sender = "user", text = messageText))
            _isPiaThinking.value = true

            val tasks = allTasks.value.filter { !it.isCompleted }.joinToString("; ") { it.title }
            val goals = userProfile.value.currentGoals

            val piaResponse = aiPlannerRepo.chatWithPia(messageText, tasks, goals)
            chatRepo.insertMessage(ChatMessageEntity(sender = "pia", text = piaResponse))
            _isPiaThinking.value = false
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatRepo.clearChat()
            chatRepo.insertMessage(
                ChatMessageEntity(
                    sender = "pia",
                    text = "Chat cleared. How can I help?"
                )
            )
        }
    }

    // Settings actions
    fun updateProfileSettings(
        name: String,
        theme: String,
        greeting: String,
        reminderStrictness: String,
        wakeTime: String,
        sleepTime: String,
        isDark: Boolean,
        followSystem: Boolean
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                name = name.ifBlank { "Priyanka" },
                favoriteTheme = theme,
                morningGreeting = greeting,
                reminderType = reminderStrictness,
                wakeTime = wakeTime,
                sleepTime = sleepTime,
                isDarkMode = isDark,
                followSystemTheme = followSystem
            )
            profileRepo.saveUserProfile(updated)
        }
    }

    fun toggleLittleSurprise(show: Boolean) {
        _showLittleSurprise.value = show
    }

    // Rewards & Birthday actions
    fun openBirthdayExperience(open: Boolean) {
        _isBirthdayExperienceOpen.value = open
    }

    fun openAdminConfig(open: Boolean) {
        _isAdminConfigOpen.value = open
    }

    fun dismissCelebration() {
        _celebrationReward.value = null
    }

    fun claimReward(milestone: Int) {
        viewModelScope.launch {
            val config = rewardRepo.getRewardConfigOnce() ?: RewardConfigEntity()
            val (updatedConfig, title) = when (milestone) {
                7 -> config.copy(reward7Claimed = true) to config.reward7Title
                30 -> config.copy(reward30Claimed = true) to config.reward30Title
                50 -> config.copy(reward50Claimed = true) to config.reward50Title
                100 -> config.copy(reward100Claimed = true) to config.reward100Title
                else -> config to "Special Milestone"
            }
            rewardRepo.saveRewardConfig(updatedConfig)
            _celebrationReward.value = milestone to title
        }
    }

    fun updateRewardConfig(config: RewardConfigEntity) {
        viewModelScope.launch {
            rewardRepo.saveRewardConfig(config)
        }
    }

    fun setBirthdayTestMode(enabled: Boolean) {
        viewModelScope.launch {
            val config = rewardRepo.getRewardConfigOnce() ?: RewardConfigEntity()
            rewardRepo.saveRewardConfig(config.copy(isBirthdayTestMode = enabled))
        }
    }

    fun addBirthdayMemory(
        emoji: String,
        title: String,
        caption: String,
        dateTag: String,
        imageUrl: String
    ) {
        viewModelScope.launch {
            val current = rewardRepo.allMemories.first()
            val newMemory = BirthdayMemoryEntity(
                emoji = emoji.ifBlank { "🌸" },
                title = title.ifBlank { "Memory" },
                caption = caption,
                dateTag = dateTag.ifBlank { "Special" },
                imageUrl = imageUrl,
                orderIndex = current.size + 1
            )
            rewardRepo.insertMemory(newMemory)
        }
    }

    fun updateBirthdayMemory(memory: BirthdayMemoryEntity) {
        viewModelScope.launch {
            rewardRepo.updateMemory(memory)
        }
    }

    fun deleteBirthdayMemory(memory: BirthdayMemoryEntity) {
        viewModelScope.launch {
            rewardRepo.deleteMemory(memory)
        }
    }

    fun setManualStreak(days: Int) {
        viewModelScope.launch {
            rewardRepo.clearAllActivities()
            if (days > 0) {
                val cal = Calendar.getInstance()
                for (i in 0 until days) {
                    val dateCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                    val dStr = dateFormat.format(dateCal.time)
                    rewardRepo.recordDailyActivity(
                        DailyActivityEntity(
                            date = dStr,
                            tasksCompleted = 2,
                            habitsCompleted = 2,
                            isStreakAchieved = true
                        )
                    )
                }
            }
            checkAndUnlockMilestones(days)
        }
    }

    // ==================== MUSIC PLAYER & LIBRARY ====================

    fun searchOnlineMusic(
        query: String,
        filter: com.example.data.provider.MusicSearchFilter = com.example.data.provider.MusicSearchFilter.ALL
    ) {
        val cleanQuery = query.trim()
        _currentOnlineQuery.value = query
        _onlineSearchFilter.value = filter

        if (cleanQuery.isBlank()) {
            loadOnlineDiscover(_selectedOnlineGenre.value)
            return
        }

        _isOnlineSearching.value = true
        _onlineSearchError.value = null

        viewModelScope.launch {
            try {
                val result = onlineMusicProvider.searchTracks(cleanQuery, filter, page = 0, pageSize = 40)
                result.onSuccess { tracks ->
                    val favs = favoriteSongs.value.associateBy { it.externalId ?: it.uriString }
                    val mappedTracks = tracks.map { track ->
                        val match = favs[track.externalId] ?: favs[track.uriString]
                        if (match != null) track.copy(id = match.id, isFavorite = match.isFavorite) else track
                    }
                    _onlineSearchResults.value = mappedTracks
                    _isOnlineSearching.value = false
                    _onlineSearchError.value = null
                }.onFailure { err ->
                    _isOnlineSearching.value = false
                    _onlineSearchError.value = err.message ?: "Failed to find songs. Please check your internet connection."
                }
            } catch (e: Exception) {
                _isOnlineSearching.value = false
                _onlineSearchError.value = e.message ?: "Network error"
            }
        }
    }

    fun loadOnlineDiscover(genreOrCategory: String = "Top Hits") {
        _selectedOnlineGenre.value = genreOrCategory
        _currentOnlineQuery.value = ""
        _isOnlineSearching.value = true
        _onlineSearchError.value = null

        viewModelScope.launch {
            try {
                val result = onlineMusicProvider.getFeaturedTracks(genreOrCategory, page = 0, pageSize = 40)
                result.onSuccess { tracks ->
                    val favs = favoriteSongs.value.associateBy { it.externalId ?: it.uriString }
                    val mappedTracks = tracks.map { track ->
                        val match = favs[track.externalId] ?: favs[track.uriString]
                        if (match != null) track.copy(id = match.id, isFavorite = match.isFavorite) else track
                    }
                    _onlineSearchResults.value = mappedTracks
                    _isOnlineSearching.value = false
                    _onlineSearchError.value = null
                }.onFailure { err ->
                    _isOnlineSearching.value = false
                    _onlineSearchError.value = err.message ?: "Failed to load online music. Tap retry to reload."
                }
            } catch (e: Exception) {
                _isOnlineSearching.value = false
                _onlineSearchError.value = e.message ?: "Network error"
            }
        }
    }

    fun playSong(song: SongEntity, queue: List<SongEntity> = listOf(song)) {
        viewModelScope.launch {
            val playableSong = if (song.isOnline) {
                // Ensure online track is registered in local Room DB for history & playlist support
                val existing = if (song.externalId != null) {
                    musicRepo.getSongByExternalId(song.externalId)
                } else {
                    musicRepo.getSongByUri(song.uriString)
                }
                if (existing != null) {
                    val now = System.currentTimeMillis()
                    musicRepo.updateLastPlayed(existing.id, now)
                    existing.copy(lastPlayedTimestamp = now)
                } else {
                    val now = System.currentTimeMillis()
                    val newId = musicRepo.insertSong(song.copy(lastPlayedTimestamp = now))
                    song.copy(id = newId, lastPlayedTimestamp = now)
                }
            } else {
                musicRepo.updateLastPlayed(song.id, System.currentTimeMillis())
                song
            }
            audioPlayerManager.playSong(playableSong, queue)
        }
    }

    fun togglePlayPause() {
        audioPlayerManager.togglePlayPause()
    }

    fun playNextSong() {
        audioPlayerManager.playNext()
    }

    fun playPreviousSong() {
        audioPlayerManager.playPrevious()
    }

    fun seekAudioTo(positionMs: Long) {
        audioPlayerManager.seekTo(positionMs)
    }

    fun seekAudioRelative(seconds: Int) {
        audioPlayerManager.seekRelative(seconds)
    }

    fun toggleShuffle() {
        audioPlayerManager.toggleShuffle()
    }

    fun cycleRepeatMode() {
        audioPlayerManager.cycleRepeatMode()
    }

    fun openFullScreenPlayer(open: Boolean) {
        audioPlayerManager.openFullScreenPlayer(open)
    }

    fun openRemixStudio(open: Boolean) {
        audioPlayerManager.openRemixStudio(open)
    }

    fun setVisualizerType(type: com.example.ui.audio.VisualizerType) {
        audioPlayerManager.setVisualizerType(type)
    }

    fun toggleSongFavorite(song: SongEntity) {
        viewModelScope.launch {
            if (song.isOnline) {
                val existing = if (song.externalId != null) {
                    musicRepo.getSongByExternalId(song.externalId)
                } else {
                    musicRepo.getSongByUri(song.uriString)
                }
                if (existing != null) {
                    val newFav = !existing.isFavorite
                    musicRepo.toggleFavorite(existing.id, newFav)
                    _onlineSearchResults.value = _onlineSearchResults.value.map {
                        if ((song.externalId != null && it.externalId == song.externalId) || it.uriString == song.uriString) {
                            it.copy(id = existing.id, isFavorite = newFav)
                        } else it
                    }
                } else {
                    val newId = musicRepo.insertSong(song.copy(isFavorite = true))
                    _onlineSearchResults.value = _onlineSearchResults.value.map {
                        if ((song.externalId != null && it.externalId == song.externalId) || it.uriString == song.uriString) {
                            it.copy(id = newId, isFavorite = true)
                        } else it
                    }
                }
            } else {
                val newFav = !song.isFavorite
                musicRepo.toggleFavorite(song.id, newFav)
                _onlineSearchResults.value = _onlineSearchResults.value.map {
                    if (it.id == song.id || (song.externalId != null && it.externalId == song.externalId)) {
                        it.copy(isFavorite = newFav)
                    } else it
                }
            }
        }
    }

    fun deleteSong(song: SongEntity) {
        viewModelScope.launch {
            musicRepo.deleteSong(song)
        }
    }

    fun importLocalAudioFiles(uris: List<android.net.Uri>) {
        viewModelScope.launch {
            val songsToInsert = mutableListOf<SongEntity>()
            for (uri in uris) {
                val existing = musicRepo.getSongByUri(uri.toString())
                if (existing == null) {
                    val parsed = audioPlayerManager.parseAudioFile(uri)
                    if (parsed != null) {
                        songsToInsert.add(parsed)
                    }
                }
            }
            if (songsToInsert.isNotEmpty()) {
                musicRepo.insertSongs(songsToInsert)
            }
        }
    }

    // Remix Studio controls
    fun updateBass(bass: Float) = audioPlayerManager.updateBass(bass)
    fun updateTreble(treble: Float) = audioPlayerManager.updateTreble(treble)
    fun updateVocal(vocal: Float) = audioPlayerManager.updateVocal(vocal)
    fun updateReverb(reverb: Float) = audioPlayerManager.updateReverb(reverb)
    fun updateEchoDelay(echo: Float) = audioPlayerManager.updateEchoDelay(echo)
    fun updateSpeed(speed: Float) = audioPlayerManager.updateSpeed(speed)
    fun updatePitch(pitch: Float) = audioPlayerManager.updatePitch(pitch)
    fun updateVolume(vol: Float) = audioPlayerManager.updateVolume(vol)
    fun updateBalance(balance: Float) = audioPlayerManager.updateBalance(balance)
    fun applyPreset(presetName: String) = audioPlayerManager.applyPreset(presetName)
    fun applyCustomPreset(preset: RemixPresetEntity) = audioPlayerManager.applyCustomPreset(preset)
    fun resetRemixEffects() = audioPlayerManager.resetEffects()

    fun saveCustomRemixPreset(name: String) {
        viewModelScope.launch {
            val state = remixState.value
            val preset = RemixPresetEntity(
                name = name.ifBlank { "Custom Remix" },
                isCustom = true,
                bass = state.bass,
                treble = state.treble,
                vocal = state.vocal,
                reverb = state.reverb,
                echoDelay = state.echoDelay,
                speed = state.speed,
                pitch = state.pitch,
                volume = state.volume,
                balance = state.balance
            )
            musicRepo.saveRemixPreset(preset)
        }
    }

    fun deleteRemixPreset(preset: RemixPresetEntity) {
        viewModelScope.launch {
            musicRepo.deleteRemixPreset(preset)
        }
    }

    // Playlists
    fun createPlaylist(name: String, icon: String = "🎵", description: String = "") {
        viewModelScope.launch {
            musicRepo.createPlaylist(name, icon, description)
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            musicRepo.deletePlaylist(playlist)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepo.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepo.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>> {
        return musicRepo.getSongsForPlaylist(playlistId)
    }

    // Gallery Actions
    fun saveGalleryCreation(creation: GalleryCreationEntity) {
        viewModelScope.launch {
            galleryRepo.saveCreation(creation)
        }
    }

    fun deleteGalleryCreation(creation: GalleryCreationEntity) {
        viewModelScope.launch {
            galleryRepo.deleteCreation(creation)
        }
    }

    fun toggleGalleryFavorite(id: Long, isFav: Boolean) {
        viewModelScope.launch {
            galleryRepo.toggleFavorite(id, isFav)
        }
    }

    // Push Notification Actions
    fun sendTestPushNotification(slotTime: String = "00:00", onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            com.example.notification.PushNotificationManager(getApplication()).sendTestPushNotification(slotTime) { success ->
                onResult(success)
            }
        }
    }

    fun sendTestCountdownPushNotification(daysRemaining: Int = 8, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            com.example.notification.PushNotificationManager(getApplication()).sendTestCountdownPushNotification(daysRemaining) { success ->
                onResult(success)
            }
        }
    }

    fun syncPushRegistration() {
        viewModelScope.launch {
            com.example.notification.PushNotificationManager(getApplication()).syncTokenAndRegistration()
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
    }
}
