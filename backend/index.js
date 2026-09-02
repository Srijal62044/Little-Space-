require('dotenv').config();
const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');
const { BIRTHDAY_SCHEDULE, processBirthdayNotifications, sendTestNotification } = require('./scheduler');

// Initialize Firebase Admin SDK
// Uses GOOGLE_APPLICATION_CREDENTIALS or Firebase default credentials in Cloud Functions / Cloud Run
if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const messaging = admin.messaging();

const app = express();
app.use(cors());
app.use(express.json());

// Health Check
app.get('/api/health', (req, res) => {
  res.json({
    status: 'online',
    service: 'Priyanka Birthday Push Notification Multi-Series Backend',
    timestamp: new Date().toISOString()
  });
});

/**
 * Get Full Birthday Notification Schedule
 * GET /api/schedule
 */
app.get('/api/schedule', (req, res) => {
  res.json({
    birthday: "September 10",
    schedule: BIRTHDAY_SCHEDULE
  });
});

/**
 * Register or update device FCM token and user timezone
 * POST /api/registerDevice
 * Body: { userId, token, timezone, birthdayNotificationEnabled, notificationFrequency, allowFinalNotification2355 }
 */
app.post('/api/registerDevice', async (req, res) => {
  try {
    const {
      userId = 'priyanka_default',
      token,
      timezone = 'Asia/Kolkata',
      birthdayNotificationEnabled = true,
      notificationFrequency = '2_HOURS',
      allowFinalNotification2355 = true
    } = req.body;

    if (!token) {
      return res.status(400).json({ error: 'FCM Token is required' });
    }

    const userRef = db.collection('users').doc(userId);
    const doc = await userRef.get();

    if (!doc.exists) {
      await userRef.set({
        userId,
        timezone,
        birthdayMonth: 9,
        birthdayDay: 10,
        birthdayNotificationEnabled: birthdayNotificationEnabled ?? true,
        notificationFrequency,
        allowFinalNotification2355,
        birthdayNotificationHour: 0,
        birthdayNotificationMinute: 0,
        fcmTokens: [token],
        birthdayNotificationState: {
          year: 0,
          sentSlots: []
        },
        lastBirthdayNotificationYear: 0,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });
    } else {
      await userRef.update({
        timezone,
        birthdayNotificationEnabled: birthdayNotificationEnabled ?? true,
        notificationFrequency,
        allowFinalNotification2355,
        fcmTokens: admin.firestore.FieldValue.arrayUnion(token),
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });
    }

    res.json({ success: true, message: 'Device registered successfully', userId, timezone });
  } catch (error) {
    console.error('Device registration failed:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * Send Test Push Notification
 * POST /api/sendTestNotification
 * Body: { userId, token, slotTime }
 */
app.post('/api/sendTestNotification', async (req, res) => {
  try {
    const { userId = 'priyanka_default', token, slotTime = '00:00' } = req.body;
    const result = await sendTestNotification(db, messaging, userId, slotTime, token);
    res.json(result);
  } catch (error) {
    console.error('Send test notification failed:', error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * Cron trigger endpoint (called by Google Cloud Scheduler or external cron)
 * POST /api/cron/checkBirthdayTrigger
 */
app.post('/api/cron/checkBirthdayTrigger', async (req, res) => {
  try {
    const result = await processBirthdayNotifications(db, messaging);
    res.json({ success: true, ...result });
  } catch (error) {
    console.error('Birthday cron execution failed:', error);
    res.status(500).json({ error: error.message });
  }
});

// Firebase Cloud Functions v2 Scheduler Export (Optional if using Cloud Functions)
let functions;
try {
  functions = require('firebase-functions');
} catch (e) {
  // functions module not in local runtime
}

let scheduledBirthdayJob = null;
if (functions && functions.pubsub) {
  // Recommended every 10 or 15 minutes to guarantee timely slot triggers
  scheduledBirthdayJob = functions.pubsub
    .schedule('every 15 minutes')
    .onRun(async (context) => {
      console.log('Running scheduled birthday check via Firebase Cloud Functions...');
      return await processBirthdayNotifications(db, messaging);
    });
}

const PORT = process.env.PORT || 8080;
if (process.env.NODE_ENV !== 'test' && !process.env.FUNCTION_NAME) {
  app.listen(PORT, () => {
    console.log(`Birthday Push Notification server running on port ${PORT}`);
  });
}

module.exports = {
  app,
  scheduledBirthdayJob
};
