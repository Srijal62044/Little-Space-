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

            if (isBirthdayToday && config.isBirthdayNotificationEnabled) {
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
            } else if (config.isCountdownNotificationEnabled) {
                // Check daily birthday countdown push notification leading up to September 10
                val calToday = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val calTarget = Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentYear)
                    set(Calendar.MONTH, targetMonth - 1)
                    set(Calendar.DAY_OF_MONTH, targetDay)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (calToday.before(calTarget)) {
                    val diffMillis = calTarget.timeInMillis - calToday.timeInMillis
                    val daysRemaining = Math.round(diffMillis.toDouble() / (1000 * 60 * 60 * 24)).toInt()

                    val scheduledHour = config.countdownNotificationHour
                    val scheduledMinute = config.countdownNotificationMinute
                    val currentTotalMinutes = currentHour * 60 + currentMinute
                    val scheduledTotalMinutes = scheduledHour * 60 + scheduledMinute
                    val diffMinutes = currentTotalMinutes - scheduledTotalMinutes

                    // Delivery window: within [scheduledTime, scheduledTime + 120m]
                    if (diffMinutes in 0..120) {
                        val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
                        if (config.lastCountdownNotificationDate != todayDateStr || config.lastCountdownNotificationYear != currentYear) {
                            Log.d(TAG, "Triggering daily countdown notification ($daysRemaining days remaining)...")
                            val countdownMsg = BirthdayNotificationMessages.getCountdownMessage(daysRemaining)

                            NotificationHelper.showCountdownNotification(
                                context = applicationContext,
                                title = countdownMsg.title,
                                message = countdownMsg.message,
                                daysRemaining = daysRemaining,
                                isTest = false
                            )

                            db.rewardConfigDao().insertOrUpdate(
                                config.copy(
                                    lastCountdownNotificationYear = currentYear,
                                    lastCountdownNotificationDate = todayDateStr,
                                    lastCountdownDaysRemaining = daysRemaining,
                                    lastNotificationStatus = "SUCCESS",
                                    lastNotificationError = ""
                                )
                            )
                        } else {
                            Log.d(TAG, "Countdown notification for date $todayDateStr already delivered.")
                        }
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
            try {
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
            } catch (e: Throwable) {
                Log.w(TAG, "WorkManager periodic schedule could not be enqueued: ${e.message}")
            }
        }
    }
}
