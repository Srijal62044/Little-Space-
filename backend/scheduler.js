/**
 * Birthday Push Notification Multi-Series Scheduler & FCM Dispatcher
 * 
 * Features:
 * - Multi-slot notification schedule on September 10 throughout the entire day:
 *   (00:00, 02:00, 04:00, 06:00, 08:00, 10:00, 12:00, 14:00, 16:00, 18:00, 20:00, 22:00, 23:55)
 * - Unique, wholesome, and friendly message for each notification slot
 * - Timezone-aware date and time evaluation using Luxon
 * - Server-side duplicate prevention per slot (stored in birthdayNotificationState.sentSlots)
 * - Anti-flood protection: only currently relevant slots are sent, preventing backlogged spam
 * - Automatic pruning of expired/invalid FCM tokens
 * - Support for multiple devices per user
 */

const admin = require('firebase-admin');
const { DateTime } = require('luxon');

const BIRTHDAY_SCHEDULE = [
  {
    slot: "00:00",
    hour: 0,
    minute: 0,
    title: "🎂 Happy Birthday, Priyanka! ✨",
    body: "Your special day has officially begun! Hope this year brings you countless beautiful moments. 🎁"
  },
  {
    slot: "02:00",
    hour: 2,
    minute: 0,
    title: "🌙 Birthday Night ✨",
    body: "A tiny birthday wish just for you. Sleep peacefully knowing today is your special day! 💫"
  },
  {
    slot: "04:00",
    hour: 4,
    minute: 0,
    title: "✨ Another Birthday Wish",
    body: "Just another little reminder that today belongs to you. Have a beautiful birthday! 🎂"
  },
  {
    slot: "06:00",
    hour: 6,
    minute: 0,
    title: "🌅 Good Morning, Birthday Girl!",
    body: "Rise and shine! It's September 10 — your special day has arrived! 🎉"
  },
  {
    slot: "08:00",
    hour: 8,
    minute: 0,
    title: "☀️ Birthday Morning!",
    body: "A fresh morning, a fresh year and plenty of reasons to smile. Have an amazing day! 🎁"
  },
  {
    slot: "10:00",
    hour: 10,
    minute: 0,
    title: "💐 Birthday Wish #6",
    body: "Sending another little birthday wish your way. Hope your day is going wonderfully! ✨"
  },
  {
    slot: "12:00",
    hour: 12,
    minute: 0,
    title: "🎉 It's Your Birthday!",
    body: "Half the day is here, but the celebrations aren't over. Keep enjoying your special day! 🎂"
  },
  {
    slot: "14:00",
    hour: 14,
    minute: 0,
    title: "🎁 A Little Birthday Surprise",
    body: "Another tiny notification just to make your birthday a little more special. ✨"
  },
  {
    slot: "16:00",
    hour: 16,
    minute: 0,
    title: "🌸 Birthday Vibes",
    body: "Hope your birthday is filled with happy moments, laughter and lots of smiles! 🎉"
  },
  {
    slot: "18:00",
    hour: 18,
    minute: 0,
    title: "🌇 Evening Birthday Wish",
    body: "The day is slowly turning into evening, but the birthday vibes continue! 🎂✨"
  },
  {
    slot: "20:00",
    hour: 20,
    minute: 0,
    title: "🎂 Birthday Celebration Continues!",
    body: "Your special day isn't over yet. Hope you're having an unforgettable birthday! 🎁"
  },
  {
    slot: "22:00",
    hour: 22,
    minute: 0,
    title: "🌙 Almost Midnight...",
    body: "Before your birthday day ends, here's another little wish: keep smiling and enjoy every moment! ✨"
  },
  {
    slot: "23:55",
    hour: 23,
    minute: 55,
    title: "✨ One Last Birthday Wish",
    body: "One last birthday wish before this special day comes to an end. Hope it was amazing! 🎂🎁"
  }
];

/**
 * Checks all registered users and triggers the appropriate birthday push notification slot
 * if today is September 10 in the user's local timezone.
 */
async function processBirthdayNotifications(db, messaging) {
  const usersRef = db.collection('users');
  const snapshot = await usersRef.where('birthdayNotificationEnabled', '==', true).get();

  if (snapshot.empty) {
    console.log('No users with birthday notifications enabled.');
    return { sent: 0, checked: 0 };
  }

  let sentCount = 0;
  let checkedCount = 0;

  for (const doc of snapshot.docs) {
    checkedCount++;
    const user = doc.data();
    const userId = doc.id;
    const userTimezone = user.timezone || 'Asia/Kolkata';
    const tokens = user.fcmTokens || [];

    if (tokens.length === 0) {
      console.log(`User ${userId} has no registered FCM tokens. Skipping.`);
      continue;
    }

    // Evaluate date and time in user's local timezone
    const nowInUserTz = DateTime.now().setZone(userTimezone);
    const currentYear = nowInUserTz.year;
    const currentMonth = nowInUserTz.month; // 9 = September
    const currentDay = nowInUserTz.day;     // 10 = 10th
    const currentHour = nowInUserTz.hour;
    const currentMinute = nowInUserTz.minute;

    const targetMonth = user.birthdayMonth || 9;
    const targetDay = user.birthdayDay || 10;
    const isBirthdayToday = (currentMonth === targetMonth && currentDay === targetDay);

    if (!isBirthdayToday) {
      continue;
    }

    // Determine user's notification delivery state
    const state = user.birthdayNotificationState || {};
    const stateYear = state.year || 0;
    const sentSlots = (stateYear === currentYear) ? (state.sentSlots || []) : [];

    // Filter active schedule based on user frequency preference
    const frequency = user.notificationFrequency || '2_HOURS'; // '1_HOUR' or '2_HOURS'
    const allowFinal = user.allowFinalNotification2355 !== false;

    const activeSchedule = BIRTHDAY_SCHEDULE.filter(item => {
      if (item.slot === '23:55' && !allowFinal) return false;
      if (frequency === '1_HOUR') return true;
      return (item.hour % 2 === 0) || item.slot === '23:55';
    });

    // Anti-Flood Logic: Find the most relevant current slot
    // A slot is eligible if current time is within [slotTime, slotTime + 1h 45m]
    const currentTotalMinutes = currentHour * 60 + currentMinute;
    const eligibleSlot = activeSchedule.filter(item => {
      const slotTotalMinutes = item.hour * 60 + item.minute;
      const diff = currentTotalMinutes - slotTotalMinutes;
      return diff >= 0 && diff <= 105;
    }).sort((a, b) => (b.hour * 60 + b.minute) - (a.hour * 60 + a.minute))[0];

    if (!eligibleSlot) {
      continue;
    }

    // Check if this specific slot has already been sent for this year
    if (sentSlots.includes(eligibleSlot.slot)) {
      console.log(`User ${userId} already received slot ${eligibleSlot.slot} for ${currentYear}. Skipping.`);
      continue;
    }

    console.log(`Triggering Birthday Notification Slot ${eligibleSlot.slot} for User ${userId} (${userTimezone}, Year ${currentYear})...`);

    const payload = {
      notification: {
        title: eligibleSlot.title,
        body: eligibleSlot.body
      },
      data: {
        type: "birthday_surprise",
        action: "birthday_surprise",
        open_birthday_surprise: "true",
        slot_time: eligibleSlot.slot,
        target_year: String(currentYear),
        timestamp: String(Date.now())
      },
      android: {
        priority: "high",
        notification: {
          channelId: "birthday_surprise_channel",
          priority: "max",
          defaultSound: true,
          defaultVibrateTimings: true,
          color: "#E11D48",
          clickAction: "ACTION_OPEN_BIRTHDAY_SURPRISE"
        }
      },
      apns: {
        payload: {
          aps: {
            sound: "default",
            badge: 1
          }
        }
      }
    };

    const invalidTokens = [];

    for (const token of tokens) {
      try {
        const response = await messaging.send({
          token: token,
          ...payload
        });
        console.log(`Successfully sent FCM slot ${eligibleSlot.slot} to token ${token.substring(0, 10)}... MsgId: ${response}`);
        sentCount++;
      } catch (error) {
        console.error(`FCM send error for token ${token.substring(0, 10)}:`, error.code, error.message);
        if (
          error.code === 'messaging/invalid-registration-token' ||
          error.code === 'messaging/registration-token-not-registered'
        ) {
          invalidTokens.push(token);
        }
      }
    }

    // Update user state with the newly sent slot
    const updatedSentSlots = [...sentSlots, eligibleSlot.slot];
    const updateData = {
      birthdayNotificationState: {
        year: currentYear,
        sentSlots: updatedSentSlots
      },
      lastBirthdayNotificationYear: currentYear,
      lastBirthdayNotificationSlot: eligibleSlot.slot,
      lastBirthdayNotificationTimestamp: admin.firestore.FieldValue.serverTimestamp(),
      lastNotificationStatus: 'SUCCESS',
      lastNotificationError: null
    };

    if (invalidTokens.length > 0) {
      updateData.fcmTokens = admin.firestore.FieldValue.arrayRemove(...invalidTokens);
      console.log(`Pruned ${invalidTokens.length} expired FCM tokens for user ${userId}.`);
    }

    await usersRef.doc(userId).update(updateData);
  }

  return { sent: sentCount, checked: checkedCount };
}

/**
 * Sends a single test push notification without modifying birthdayNotificationState.
 */
async function sendTestNotification(db, messaging, userId, slotTime = "00:00", customToken = null) {
  let tokens = [];

  if (customToken) {
    tokens = [customToken];
  } else if (userId) {
    const userDoc = await db.collection('users').doc(userId).get();
    if (userDoc.exists) {
      tokens = userDoc.data().fcmTokens || [];
    }
  }

  if (tokens.length === 0) {
    throw new Error('No FCM tokens available to send test push.');
  }

  const slotItem = BIRTHDAY_SCHEDULE.find(s => s.slot === slotTime) || BIRTHDAY_SCHEDULE[0];

  const payload = {
    notification: {
      title: slotItem.title,
      body: slotItem.body
    },
    data: {
      type: "test_notification",
      action: "birthday_surprise",
      open_birthday_surprise: "true",
      slot_time: slotItem.slot,
      timestamp: String(Date.now())
    },
    android: {
      priority: "high",
      notification: {
        channelId: "birthday_surprise_channel",
        priority: "high",
        color: "#E11D48"
      }
    }
  };

  let successCount = 0;
  const errors = [];

  for (const token of tokens) {
    try {
      await messaging.send({ token, ...payload });
      successCount++;
    } catch (err) {
      errors.push({ token: token.substring(0, 8) + '...', code: err.code, message: err.message });
    }
  }

  return { success: successCount > 0, delivered: successCount, total: tokens.length, errors, slot: slotItem.slot };
}

module.exports = {
  BIRTHDAY_SCHEDULE,
  processBirthdayNotifications,
  sendTestNotification
};
