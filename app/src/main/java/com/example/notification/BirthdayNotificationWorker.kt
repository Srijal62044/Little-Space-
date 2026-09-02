package com.example.notification

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.data.local.AppDatabase
import java.util.*
import java.util.concurrent.TimeUnit

class BirthdayNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "BirthdayNotificationWorker executing background multi-slot check...")

        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val config = db.rewardConfigDao().getRewardConfigSync() ?: return Result.success()

            if (!config.isBirthdayNotificationEnabled) {
                Log.d(TAG, "Birthday notification series is disabled in settings.")
                return Result.success()
            }

            val now = Calendar.getInstance()
            val currentYear = now.get(Calendar.YEAR)
            val currentMonth = now.get(Calendar.MONTH) + 1 // 1-12
            val currentDay = now.get(Calendar.DAY_OF_MONTH)
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(Calendar.MINUTE)

            val targetMonth = config.birthdayMonth
            val targetDay = config.birthdayDay

            val isBirthdayToday = (currentMonth == targetMonth && currentDay == targetDay)

            if (isBirthdayToday) {
                // Determine current active slots according to schedule frequency
                val candidateSlots = BirthdayNotificationMessages.SCHEDULE_SERIES.filter { item ->
                    if (item.slotTime == "23:55" && !config.allowFinalNotification2355) {
                        return@filter false
                    }
                    if (config.notificationFrequency == "1_HOUR") {
                        true // All slots
                    } else {
                        // 2_HOURS (0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22) + 23:55
                        item.hour % 2 == 0 || item.slotTime == "23:55"
                    }
                }

                // Find the latest eligible slot for the current time
                // Anti-flood: Only consider a slot eligible if current time is within [slotTime, slotTime + 1h 45m]
                val currentTotalMinutes = currentHour * 60 + currentMinute
                val eligibleSlot = candidateSlots.filter { item ->
                    val slotTotalMinutes = item.hour * 60 + item.minute
                    val diff = currentTotalMinutes - slotTotalMinutes
                    diff in 0..105 // Valid window: within 1 hour 45 minutes of the scheduled time
                }.maxByOrNull { it.hour * 60 + it.minute }

                if (eligibleSlot != null) {
                    val alreadySentSlots = if (config.lastBirthdayNotificationYear == currentYear) {
                        config.sentBirthdaySlots.split(",").filter { it.isNotBlank() }.toSet()
                    } else {
                        emptySet()
                    }

                    if (!alreadySentSlots.contains(eligibleSlot.slotTime)) {
                        Log.d(TAG, "Triggering birthday slot notification for ${eligibleSlot.slotTime} ($currentYear)...")
                        NotificationHelper.showBirthdayNotification(
                            context = applicationContext,
                            title = eligibleSlot.title,
                            message = eligibleSlot.message,
                            slotTime = eligibleSlot.slotTime
                        )

                        val updatedSlots = (alreadySentSlots + eligibleSlot.slotTime).joinToString(",")
                        db.rewardConfigDao().insertOrUpdate(
                            config.copy(
                                lastBirthdayNotificationYear = currentYear,
                                lastBirthdayNotificationTimestamp = System.currentTimeMillis(),
                                lastBirthdayNotificationSlot = eligibleSlot.slotTime,
                                sentBirthdaySlots = updatedSlots,
                                lastNotificationStatus = "SUCCESS",
                                lastNotificationError = ""
                            )
                        )
                    } else {
                        Log.d(TAG, "Slot ${eligibleSlot.slotTime} was already delivered.")
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing BirthdayNotificationWorker", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "BirthdayWorker"
        const val WORK_NAME = "BirthdayNotificationPeriodicCheck"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            // Run periodic check every 30 minutes in background
            val periodicWork = PeriodicWorkRequestBuilder<BirthdayNotificationWorker>(
                30, TimeUnit.MINUTES,
                10, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
            )
        }
    }
}
