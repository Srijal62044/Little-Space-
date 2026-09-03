package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_ID_BIRTHDAY = "birthday_surprise_channel"
    const val CHANNEL_NAME_BIRTHDAY = "Birthday Surprise"
    const val CHANNEL_DESC_BIRTHDAY = "Special birthday notifications, milestone celebrations, and warm surprises for Priyanka"
    
    const val NOTIFICATION_ID_BIRTHDAY_BASE = 100900
    const val NOTIFICATION_ID_COUNTDOWN_BASE = 100800
    const val NOTIFICATION_ID_TEST = 100999

    const val EXTRA_OPEN_BIRTHDAY_SURPRISE = "open_birthday_surprise"
    const val EXTRA_OPEN_BIRTHDAY_COUNTDOWN = "open_birthday_countdown"
    const val EXTRA_DAYS_REMAINING = "days_remaining"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val birthdayChannel = NotificationChannel(
                CHANNEL_ID_BIRTHDAY,
                CHANNEL_NAME_BIRTHDAY,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_BIRTHDAY
                enableLights(true)
                lightColor = 0xFFE11D48.toInt()
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300, 200, 500)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(birthdayChannel)
        }
    }

    fun showBirthdayNotification(
        context: Context,
        title: String = "🎂 Happy Birthday, Priyanka! ✨",
        message: String = "Today is your special day! 🎁 Tap to open your birthday surprise.",
        slotTime: String = "00:00"
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_OPEN_BIRTHDAY_SURPRISE, true)
            putExtra("notification_slot", slotTime)
            action = "ACTION_OPEN_BIRTHDAY_SURPRISE"
        }

        val notificationId = NOTIFICATION_ID_BIRTHDAY_BASE + (slotTime.hashCode() % 50).let { if (it < 0) -it else it }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_BIRTHDAY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setSummaryText("Priyanka's Birthday 🎂 ($slotTime)")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFFE11D48.toInt())
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Throwable) {
            // Permission might have been revoked by user in system settings or Bad notification error
        }
    }

    fun showCountdownNotification(
        context: Context,
        title: String,
        message: String,
        daysRemaining: Int,
        isTest: Boolean = false
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_OPEN_BIRTHDAY_COUNTDOWN, true)
            putExtra(EXTRA_DAYS_REMAINING, daysRemaining)
            if (daysRemaining == 0) {
                putExtra(EXTRA_OPEN_BIRTHDAY_SURPRISE, true)
            }
            action = if (isTest) "ACTION_TEST_COUNTDOWN_NOTIFICATION" else "ACTION_OPEN_BIRTHDAY_COUNTDOWN"
        }

        val notificationId = if (isTest) NOTIFICATION_ID_TEST else NOTIFICATION_ID_COUNTDOWN_BASE + (daysRemaining % 100)

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_BIRTHDAY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setSummaryText(if (daysRemaining == 0) "Priyanka's Birthday 🎂" else "Birthday Countdown 🎂 ($daysRemaining days remaining)")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFFE11D48.toInt())
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Throwable) {
            // Handled gracefully
        }
    }

    fun showTestNotification(
        context: Context,
        title: String = "🧪 Test Birthday Notification",
        message: String = "Your birthday push notification system is working! 🎂"
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_OPEN_BIRTHDAY_SURPRISE, true)
            action = "ACTION_TEST_BIRTHDAY_NOTIFICATION"
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_TEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_BIRTHDAY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setSummaryText("Push Notification Test 🧪")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFFE11D48.toInt())
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID_TEST, builder.build())
        } catch (e: Throwable) {
            // Handled gracefully
        }
    }
}
