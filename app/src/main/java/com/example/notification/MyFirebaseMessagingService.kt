package com.example.notification

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.RewardConfigEntity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed FCM Token: $token")
        
        scope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val config = db.rewardConfigDao().getRewardConfigSync() ?: RewardConfigEntity()
                db.rewardConfigDao().insertOrUpdate(
                    config.copy(
                        fcmToken = token,
                        fcmServerRegistered = true
                    )
                )
            } catch (e: Exception) {
                Log.e("FCM", "Failed to store refreshed token", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "From: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val type = data["type"] ?: "birthday_surprise"
        val action = data["action"] ?: ""
        val slotTime = data["slot_time"] ?: "00:00"
        val daysRemainingStr = data["days_remaining"] ?: "0"
        val daysRemaining = daysRemainingStr.toIntOrNull() ?: 0
        val title = data["title"] ?: notification?.title ?: "🎂 Happy Birthday, Priyanka! ✨"
        val message = data["message"] ?: data["body"] ?: notification?.body ?: "Today is your special day! 🎁 Tap to open your birthday surprise."

        // Test countdown notification
        if (type == "test_countdown_notification") {
            NotificationHelper.showCountdownNotification(
                context = applicationContext,
                title = title,
                message = message,
                daysRemaining = daysRemaining,
                isTest = true
            )
            return
        }

        // Standard test notification
        if (type == "test_notification") {
            NotificationHelper.showTestNotification(
                context = applicationContext,
                title = title.ifBlank { "🧪 Test Birthday Notification" },
                message = message.ifBlank { "Your birthday push notification system is working! 🎂" }
            )
            return
        }

        // Daily Birthday Countdown Notification
        if (type == "birthday_countdown" || action == "birthday_countdown") {
            NotificationHelper.showCountdownNotification(
                context = applicationContext,
                title = title,
                message = message,
                daysRemaining = daysRemaining,
                isTest = false
            )

            // Record local countdown state
            scope.launch {
                try {
                    val db = AppDatabase.getDatabase(applicationContext)
                    val config = db.rewardConfigDao().getRewardConfigSync() ?: RewardConfigEntity()
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

                    db.rewardConfigDao().insertOrUpdate(
                        config.copy(
                            lastCountdownNotificationYear = currentYear,
                            lastCountdownNotificationDate = todayDateStr,
                            lastCountdownDaysRemaining = daysRemaining,
                            lastNotificationStatus = "SUCCESS"
                        )
                    )
                } catch (e: Exception) {
                    Log.e("FCM", "Error updating local countdown notification log", e)
                }
            }
            return
        }

        // Multi-series Birthday Surprise Push Notification
        NotificationHelper.showBirthdayNotification(
            context = applicationContext,
            title = title,
            message = message,
            slotTime = slotTime
        )

        // Record slot and update local notification telemetry
        scope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val config = db.rewardConfigDao().getRewardConfigSync() ?: RewardConfigEntity()
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                
                val currentSentSlots = if (config.lastBirthdayNotificationYear == currentYear) {
                    config.sentBirthdaySlots.split(",").filter { it.isNotBlank() }.toMutableSet()
                } else {
                    mutableSetOf()
                }
                currentSentSlots.add(slotTime)

                db.rewardConfigDao().insertOrUpdate(
                    config.copy(
                        lastBirthdayNotificationYear = currentYear,
                        lastBirthdayNotificationTimestamp = System.currentTimeMillis(),
                        lastBirthdayNotificationSlot = slotTime,
                        sentBirthdaySlots = currentSentSlots.joinToString(","),
                        lastNotificationStatus = "SUCCESS",
                        lastNotificationError = ""
                    )
                )
            } catch (e: Exception) {
                Log.e("FCM", "Error updating local notification log", e)
            }
        }
    }
}
