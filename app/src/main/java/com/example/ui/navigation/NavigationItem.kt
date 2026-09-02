package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    TASKS("Tasks", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "tab_tasks"),
    HABITS("Habits", Icons.Filled.Spa, Icons.Outlined.Spa, "tab_habits"),
    MUSIC("Music", Icons.Filled.MusicNote, Icons.Outlined.MusicNote, "tab_music"),
    REWARDS("Rewards", Icons.Filled.CardGiftcard, Icons.Outlined.CardGiftcard, "tab_rewards"),
    PIA("Pia ✨", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "tab_pia")
}
