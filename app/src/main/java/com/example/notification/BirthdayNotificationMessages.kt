package com.example.notification

data class BirthdayNotificationScheduleItem(
    val slotTime: String, // "00:00", "02:00", "04:00", ... "23:55"
    val hour: Int,
    val minute: Int,
    val title: String,
    val message: String
)

object BirthdayNotificationMessages {

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
