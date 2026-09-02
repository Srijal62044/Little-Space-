# Birthday Day Notification Series - Backend & Cloud Deployment

Production-ready backend architecture for **Priyanka's Birthday Day Notification Series** using Firebase Cloud Messaging (FCM), Google Cloud Scheduler, and Firestore.

---

## 🏗️ Architecture Overview

```
Mobile App (Android)
        ↓  (Registers FCM Token, Timezone & Frequency Preference)
Firestore Database (`users` collection)
        ↓
Google Cloud Scheduler / Cloud Functions (Cron every 15 minutes)
        ↓  (Evaluates: Date == Sep 10 in User's Local Timezone && Slot not in sentSlots)
Firebase Cloud Messaging (FCM API v1)
        ↓
Android System Notification (High Priority, Sound & Vibration)
        ↓  (User taps notification)
App Launches / Deep Link directly to Birthday Surprise Screen 🎂
```

---

## ⏰ Notification Schedule Series (September 10)

| Time Slot | Message Theme | Title | Body |
| :--- | :--- | :--- | :--- |
| **00:00** | Birthday Midnight Kickoff | 🎂 Happy Birthday, Priyanka! ✨ | Your special day has officially begun! Hope this year brings you countless beautiful moments. 🎁 |
| **02:00** | Birthday Night Wish | 🌙 Birthday Night ✨ | A tiny birthday wish just for you. Sleep peacefully knowing today is your special day! 💫 |
| **04:00** | Early Morning Reminder | ✨ Another Birthday Wish | Just another little reminder that today belongs to you. Have a beautiful birthday! 🎂 |
| **06:00** | Morning Wakeup Wish | 🌅 Good Morning, Birthday Girl! | Rise and shine! It's September 10 — your special day has arrived! 🎉 |
| **08:00** | Morning Celebration | ☀️ Birthday Morning! | A fresh morning, a fresh year and plenty of reasons to smile. Have an amazing day! 🎁 |
| **10:00** | Mid-Morning Smile | 💐 Birthday Wish #6 | Sending another little birthday wish your way. Hope your day is going wonderfully! ✨ |
| **12:00** | Midday Celebration | 🎉 It's Your Birthday! | Half the day is here, but the celebrations aren't over. Keep enjoying your special day! 🎂 |
| **14:00** | Afternoon Surprise | 🎁 A Little Birthday Surprise | Another tiny notification just to make your birthday a little more special. ✨ |
| **16:00** | Birthday Vibes | 🌸 Birthday Vibes | Hope your birthday is filled with happy moments, laughter and lots of smiles! 🎉 |
| **18:00** | Evening Kickoff | 🌇 Evening Birthday Wish | The day is slowly turning into evening, but the birthday vibes continue! 🎂✨ |
| **20:00** | Prime Celebration | 🎂 Birthday Celebration Continues! | Your special day isn't over yet. Hope you're having an unforgettable birthday! 🎁 |
| **22:00** | Late Night Reminder | 🌙 Almost Midnight... | Before your birthday day ends, here's another little wish: keep smiling and enjoy every moment! ✨ |
| **23:55** | Final Midnight Farewell | ✨ One Last Birthday Wish | One last birthday wish before this special day comes to an end. Hope it was amazing! 🎂🎁 |

---

## 🔑 Firebase & Firestore Setup

1. **Create Firestore Document** in `users` collection:
   ```json
   {
     "userId": "priyanka_default",
     "timezone": "Asia/Kolkata",
     "birthdayMonth": 9,
     "birthdayDay": 10,
     "birthdayNotificationEnabled": true,
     "notificationFrequency": "2_HOURS",
     "allowFinalNotification2355": true,
     "fcmTokens": ["fcm_token_string_here"],
     "birthdayNotificationState": {
       "year": 2026,
       "sentSlots": ["00:00", "02:00"]
     },
     "lastBirthdayNotificationYear": 2026
   }
   ```

2. **Obtain Service Account Key**:
   - Go to **Project Settings > Service accounts** in Firebase Console.
   - Click **Generate new private key**.
   - Export: `export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account-key.json"`

---

## 🚀 Deployment Options

### Option A: Firebase Cloud Functions (Serverless Cron)
```bash
cd backend
npm install
firebase deploy --only functions
```

### Option B: Cloud Run / Container + Cloud Scheduler
1. Deploy `backend` to Google Cloud Run.
2. Set up **Cloud Scheduler** job:
   - Target: `HTTP`
   - URL: `https://<YOUR_CLOUD_RUN_URL>/api/cron/checkBirthdayTrigger`
   - Method: `POST`
   - Frequency: `*/15 * * * *` (Every 15 minutes)

---

## 🧪 Testing in Admin Settings

1. Open app → Navigate to **Settings > Rewards Admin (PIN 7890)**.
2. Under **Birthday Notification Schedule**:
   - Pick any slot (e.g. `00:00`, `12:00`, `23:55`).
   - Tap **"Send Test Push"**.
   - Tap **"Preview Today's Notification Schedule"** to view all messages.
