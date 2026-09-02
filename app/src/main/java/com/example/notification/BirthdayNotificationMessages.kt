package com.example.notification

data class BirthdayNotificationScheduleItem(
    val slotTime: String, // "00:00", "02:00", "04:00", ... "23:55"
    val hour: Int,
    val minute: Int,
    val title: String,
    val message: String
)

data class CountdownNotificationItem(
    val daysRemaining: Int,
    val title: String,
    val message: String
)

object BirthdayNotificationMessages {

    val COUNTDOWN_SERIES: Map<Int, CountdownNotificationItem> = mapOf(
        1 to CountdownNotificationItem(
            daysRemaining = 1,
            title = "🚨 1 DAY REMAINING!",
            message = "Tomorrow is Priyanka's birthday! 🎂🎉"
        ),
        2 to CountdownNotificationItem(
            daysRemaining = 2,
            title = "🎁 2 Days Remaining!",
            message = "Just 2 more sleeps until the big day! ✨"
        ),
        3 to CountdownNotificationItem(
            daysRemaining = 3,
            title = "🎂 3 Days Remaining!",
            message = "Only 3 days left! The birthday countdown is on! 🎉"
        ),
        4 to CountdownNotificationItem(
            daysRemaining = 4,
            title = "💫 4 Days Remaining!",
            message = "The special day is getting closer! ✨"
        ),
        5 to CountdownNotificationItem(
            daysRemaining = 5,
            title = "🎉 5 Days Remaining!",
            message = "Just 5 more days until the celebration! 🎁"
        ),
        6 to CountdownNotificationItem(
            daysRemaining = 6,
            title = "✨ 6 Days Remaining!",
            message = "The countdown continues... 🎂"
        ),
        7 to CountdownNotificationItem(
            daysRemaining = 7,
            title = "🎁 7 Days Remaining!",
            message = "Only 7 days until Priyanka's special day! 🌸"
        ),
        8 to CountdownNotificationItem(
            daysRemaining = 8,
            title = "🎂 8 Days Remaining!",
            message = "Priyanka's birthday is getting closer! ✨"
        ),
        0 to CountdownNotificationItem(
            daysRemaining = 0,
            title = "🎂 HAPPY BIRTHDAY, PRIYANKA!",
            message = "Today is the day! Tap to open your birthday surprise 🎁✨"
        )
    )

    fun getCountdownMessage(daysRemaining: Int): CountdownNotificationItem {
        return COUNTDOWN_SERIES[daysRemaining] ?: CountdownNotificationItem(
            daysRemaining = daysRemaining,
            title = "🎂 $daysRemaining Days Remaining!",
            message = "The countdown to Priyanka's special day is on! ✨"
        )
    }

    val SCHEDULE_SERIES: List<BirthdayNotificationScheduleItem> = listOf(
        BirthdayNotificationScheduleItem(
            slotTime = "00:00",
            hour = 0,
            minute = 0,
            title = "🎂 Happy Birthday, Priyanka! ✨",
            message = "Your special day has officially begun! Hope this year brings you countless beautiful moments. 🎁"
        ),
        BirthdayNotificationScheduleItem(
            slotTime = "02:00",
            hour = 2,
            minute = 0,
            title = "🌙 Birthday Night ✨",
            message = "A tiny birthday wish just for you. Sleep peacefully knowing today is your special day! 💫"
        ),
        BirthdayNotificationScheduleItem(
            slotTime = "04:00",
            hour = 4,
            minute = 0,
            title = "✨ Another Birthday Wish",
            message = "Just another little reminder that today belongs to you. Have a beautiful birthday! 🎂"
        ),
        BirthdayNotificationScheduleItem(
            slotTime = "06:00",
            hour = 6,
            minute = 0,
            title = "🌅 Good Morning, Birthday Girl!",
            message = "Rise and shine! It's September 10 — your special day has arrived! 🎉"
        ),
        BirthdayNotificationScheduleItem(
            slotTime = "08:00",
            hour = 8,
            minute = 0,
            title = "☀️ Birthday Morning!",
            message = "A fresh morning, a fresh year and plenty of reasons to smile. Have an amazing day! 🎁"
        ),
        BirthdayNotificationScheduleItem(
            slotTime = "10:00",
            hour = 10,
            minute = 0,
            title = "💐 Birthday Wish #6",
            message = "Sending another little birthday wish your way. Hope your day is going wonderfully! ✨"
        ),
        BirthdayNotificationScheduleItem(
            slotTime = "12:00",
            hour = 12,
            minute = 0,
            title = "🎉 It's Your Birthday!",
            message = "Half the day is here, but the celebrations aren't over. Keep enjoying your special day! 🎂"
        ),
        BirthdayNotificationScheduleItem(
            slotTime = "14:00",
            hour = 14,
            minute = 0,
            title = "🎁 A Little Birthday Surprise",
            message = "Another tiny notification just to make your birthday a little more special. ✨"
        ),
        BirthdayNotificationScheduleItem(
            slotTime = "16:00",
            hour = 16,
            minute = 0,
            title = "🌸 Birthday Vibes",
            message = "Hope your birthday is filled with happy moments, laughter and lots of smiles! 🎉"
        ),
        BirthdayNotificationScheduleItem(
            slotTime = "18:00",
            hour = 18,
            minute = 0,
            title = "🌇 Evening Birthday Wish",
            message = "The day is slowly turning into evening, but the birthday vibes continue! 🎂✨"
        ),
        BirthdayNotificationScheduleItem(
            slotTime = "20:00",
            hour = 20,
            minute = 0,
            title = "🎂 Birthday Celebration Continues!",
            message = "Your special day isn't over yet. Hope you're having an unforgettable birthday! 🎁"
        ),
        BirthdayNotificationScheduleItem(
            slotTime = "22:00",
            hour = 22,
            minute = 0,
            title = "🌙 Almost Midnight...",
            message = "Before your birthday day ends, here's another little wish: keep smiling and enjoy every moment! ✨"
        ),
        BirthdayNotificationScheduleItem(
            slotTime = "23:55",
            hour = 23,
            minute = 55,
            title = "✨ One Last Birthday Wish",
            message = "One last birthday wish before this special day comes to an end. Hope it was amazing! 🎂🎁"
        )
    )

    fun getMessageForSlot(slotTime: String): BirthdayNotificationScheduleItem {
        return SCHEDULE_SERIES.find { it.slotTime == slotTime } ?: SCHEDULE_SERIES[0]
    }
}
