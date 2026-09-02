package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reward_config")
data class RewardConfigEntity(
    @PrimaryKey val id: Int = 1,
    // Birthday Settings
    val birthdayMonth: Int = 9, // September (1-12)
    val birthdayDay: Int = 10,  // 10th September
    val isBirthdayTestMode: Boolean = false, // When true, lets admin preview birthday surprise anytime
    val birthdayTitle: String = "Happy Birthday, Priyanka! 🎂✨",
    val birthdayMessage: String = "Wishing you the happiest of birthdays, Priyanka! 🌷✨ It's so wonderful seeing how much thought, kindness, and dedication you bring into everything you do. May this year ahead be filled with gentle peace, exciting adventures, and big milestones reached effortlessly.",
    val birthdayWish: String = "Here's to a year filled with cozy mornings, laughter with loved ones, zero stress on deadlines, and celebrating every small victory along the way. Thank you for making everyday brighter!",
    val birthdayFinalSurpriseNote: String = "One last thing... 💌 Never forget to celebrate how far you've come! Whenever things feel overwhelming, take a quiet breath and remember you've got this.",
    
    // Streak Rules: "TASK_OR_HABIT", "AT_LEAST_ONE_TASK", "ALL_HABITS", "AT_LEAST_ONE_HABIT"
    val streakRule: String = "TASK_OR_HABIT",
    
    // Milestones definition & gifts
    // 7 Days
    val reward7Title: String = "7-Day Starter 🌱",
    val reward7Desc: String = "You completed a full week of consistent daily focus!",
    val reward7Type: String = "DIGITAL", // "DIGITAL" or "PHYSICAL"
    val reward7Gift: String = "Unlocked 'Golden Sunrise' theme & a special encouragement audio note! 🌅",
    val reward7Link: String = "",
    val reward7Claimed: Boolean = false,

    // 30 Days
    val reward30Title: String = "30-Day Achiever ✨",
    val reward30Desc: String = "A whole month of dedication and steady progress!",
    val reward30Type: String = "DIGITAL",
    val reward30Gift: String = "Unlocked the 'Tulip Garden' custom badge & curated study playlist 🎧",
    val reward30Link: String = "https://open.spotify.com",
    val reward30Claimed: Boolean = false,

    // 50 Days
    val reward50Title: String = "50-Day Champion 🎁",
    val reward50Desc: String = "You made it to 50 days! Halfway to a hundred days of brilliance.",
    val reward50Type: String = "PHYSICAL",
    val reward50Gift: String = "Special boba / coffee treat delivery or a favorite sweet treat from the app creator! 🧋🍰",
    val reward50Link: String = "",
    val reward50Claimed: Boolean = false,

    // 100 Days
    val reward100Title: String = "100-Day Legend 🏆",
    val reward100Desc: String = "100 DAYS! You actually did it, Priyanka! 🥹🎉 An extraordinary achievement.",
    val reward100Type: String = "PHYSICAL",
    val reward100Gift: String = "Grand milestone celebration gift box & a celebratory dinner treat! 🎁🌟",
    val reward100Link: String = "",
    val reward100Claimed: Boolean = false,

    // Unlocked milestones history (e.g. "7,30") - once reached, permanently retained
    val unlockedMilestones: String = "",

    // Admin security PIN
    val adminPin: String = "7890",

    // Background Birthday Push Notification Configuration
    val isBirthdayNotificationEnabled: Boolean = true,
    val notificationHour: Int = 0, // 12:00 AM (0)
    val notificationMinute: Int = 0,
    val notificationFrequency: String = "2_HOURS", // "1_HOUR", "2_HOURS", "CUSTOM"
    val allowFinalNotification2355: Boolean = true,
    val lastBirthdayNotificationYear: Int = 0,
    val lastBirthdayNotificationTimestamp: Long = 0L,
    val lastBirthdayNotificationSlot: String = "",
    val sentBirthdaySlots: String = "", // Comma-separated sent slots e.g. "00:00,02:00" for the current year
    val lastNotificationStatus: String = "IDLE", // "IDLE", "SUCCESS", "FAILED"
    val lastNotificationError: String = "",
    val fcmToken: String = "",
    val fcmServerRegistered: Boolean = false,
    val deviceTimezone: String = ""
)
