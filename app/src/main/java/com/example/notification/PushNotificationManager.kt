package com.example.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.local.AppDatabase
import com.example.data.local.entity.RewardConfigEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.TimeZone

class PushNotificationManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val db = AppDatabase.getDatabase(context)

    fun initialize() {
        NotificationHelper.createNotificationChannels(context)
        BirthdayNotificationWorker.schedule(context)
        syncTokenAndRegistration()
    }

    fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun syncTokenAndRegistration() {
        val currentTimezone = TimeZone.getDefault().id
        try {
            val isFirebaseAvailable = try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context) != null
                } else {
                    true
                }
            } catch (t: Throwable) {
                false
            }

            if (isFirebaseAvailable) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("PushManager", "Fetching FCM registration token failed", task.exception)
                        return@addOnCompleteListener
                    }

                    val token = task.result
                    Log.d("PushManager", "Current FCM Token: $token, Timezone: $currentTimezone")

                    scope.launch {
                        try {
                            val config = db.rewardConfigDao().getRewardConfigSync() ?: RewardConfigEntity()
                            db.rewardConfigDao().insertOrUpdate(
                                config.copy(
                                    fcmToken = token,
                                    deviceTimezone = currentTimezone,
                                    fcmServerRegistered = true
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("PushManager", "Failed to persist token", e)
                        }
                    }
                }
            } else {
                Log.i("PushManager", "Running in offline mode: local notification scheduler & WorkManager active.")
                scope.launch {
                    val config = db.rewardConfigDao().getRewardConfigSync() ?: RewardConfigEntity()
                    db.rewardConfigDao().insertOrUpdate(
                        config.copy(
                            deviceTimezone = currentTimezone
                        )
                    )
                }
            }
        } catch (e: Throwable) {
            Log.i("PushManager", "Local WorkManager notification scheduler active.")
            scope.launch {
                val config = db.rewardConfigDao().getRewardConfigSync() ?: RewardConfigEntity()
                db.rewardConfigDao().insertOrUpdate(
                    config.copy(
                        deviceTimezone = currentTimezone
                    )
                )
            }
        }
    }

    fun sendTestPushNotification(slotTime: String = "00:00", onComplete: (Boolean) -> Unit = {}) {
        try {
            val item = BirthdayNotificationMessages.getMessageForSlot(slotTime)
            NotificationHelper.showTestNotification(
                context = context,
                title = item.title,
                message = item.message
            )
            onComplete(true)
        } catch (e: Exception) {
            Log.e("PushManager", "Error posting test push", e)
            onComplete(false)
        }
    }

    fun sendTestCountdownPushNotification(daysRemaining: Int = 8, onComplete: (Boolean) -> Unit = {}) {
        try {
            val item = BirthdayNotificationMessages.getCountdownMessage(daysRemaining)
            NotificationHelper.showCountdownNotification(
                context = context,
                title = item.title,
                message = item.message,
                daysRemaining = daysRemaining,
                isTest = true
            )
            onComplete(true)
        } catch (e: Exception) {
            Log.e("PushManager", "Error posting test countdown push", e)
            onComplete(false)
        }
    }
}
