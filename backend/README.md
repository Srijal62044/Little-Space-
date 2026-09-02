# Birthday Day & Daily Countdown Push Notification Backend

Production-ready backend architecture for **Priyanka's Birthday Day Notification Series** and **Daily Birthday Countdown Push Notification System** using Firebase Cloud Messaging (FCM), Google Cloud Scheduler, and Firestore.

---

## 🏗️ Architecture Overview

```
Mobile App (Android)
        ↓  (Registers FCM Token, Timezone, Countdown Preferences)
Firestore Database (`users` collection)
        ↓
Google Cloud Scheduler / Cloud Functions (Cron every 15 minutes)
        ├─▶ Daily Countdown: Leading up to Sep 10 (1 push/day at 10:00 AM local time)
        └─▶ Birthday Day Series: On Sep 10 (Multi-slot wishes from 00:00 to 23:55)
Firebase Cloud Messaging (FCM API v1)
        ↓
Android System Push Notification (High Priority, Sound & Vibration)
        ├─▶ Countdown Notification: Deep links to Home & highlights Birthday Countdown Card ✨
        └─▶ Birthday Wish Notification: Deep links directly to Birthday Experience / Surprise Screen 🎂
```

---

## 📅 Daily Birthday Countdown Schedule (Leading up to Sept 10)

| Days Remaining | Example Date (2026) | Title | Body | Action on Tap |
| :--- | :--- | :--- | :--- | :--- |
| **8 Days** | September 2 | 🎂 8 Days Remaining! | Priyanka's birthday is getting closer! ✨ | Highlights Home Countdown Card |
| **7 Days** | September 3 | 🎁 7 Days Remaining! | Only 7 days until Priyanka's special day! 🌸 | Highlights Home Countdown Card |
| **6 Days** | September 4 | ✨ 6 Days Remaining! | The countdown continues... 🎂 | Highlights Home Countdown Card |
| **5 Days** | September 5 | 🎉 5 Days Remaining! | Just 5 more days until the celebration! 🎁 | Highlights Home Countdown Card |
| **4 Days** | September 6 | 💫 4 Days Remaining! | The special day is getting closer! ✨ | Highlights Home Countdown Card |
| **3 Days** | September 7 | 🎂 3 Days Remaining! | Only 3 days left! The birthday countdown is on! 🎉 | Highlights Home Countdown Card |
| **2 Days** | September 8 | 🎁 2 Days Remaining! | Just 2 more sleeps until the big day! ✨ | Highlights Home Countdown Card |
| **1 Day** | September 9 | 🚨 1 DAY REMAINING! | Tomorrow is Priyanka's birthday! 🎂🎉 | Shows "🎉 TOMORROW IS THE BIG DAY!" |
| **0 Days (Sep 10)**| September 10 | 🎂 HAPPY BIRTHDAY, PRIYANKA! | Today is the day! Tap to open your birthday surprise 🎁✨ | Opens Birthday Surprise Directly 🎂 |

---

## ⏰ Birthday Day Notification Series (September 10)

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
     "countdownNotificationEnabled": true,
     "countdownNotificationHour": 10,
     "countdownNotificationMinute": 0,
     "birthdayNotificationEnabled": true,
     "notificationFrequency": "2_HOURS",
     "allowFinalNotification2355": true,
     "fcmTokens": ["fcm_token_string_here"],
     "birthdayCountdownState": {
       "year": 2026,
       "lastSentDate": "2026-09-02",
       "lastSentDaysRemaining": 8
     },
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
2. Under **Daily Birthday Countdown Push Notifications**:
   - Choose days remaining (e.g. `8 Days`, `5 Days`, `1 Day`, `Birthday Today`).
   - Tap **"Send Test Countdown Push"**.
   - Tap the notification on the device to see the app highlight the Home Countdown Card!
3. Under **Birthday Notification Schedule**:
   - Pick any slot (e.g. `00:00`, `12:00`, `23:55`).
   - Tap **"Send Test Push"**.
