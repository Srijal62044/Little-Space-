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

/**
 * Birthday Notification Messages for Countdown (Days Remaining)
 */
const COUNTDOWN_MESSAGES = {
  1: {
    title: "🚨 1 DAY REMAINING!",
    body: "Tomorrow is Priyanka's birthday! 🎂🎉"
  },
  2: {
    title: "🎁 2 Days Remaining!",
    body: "Just 2 more sleeps until the big day! ✨"
  },
  3: {
    title: "🎂 3 Days Remaining!",
    body: "Only 3 days left! The birthday countdown is on! 🎉"
  },
  4: {
    title: "💫 4 Days Remaining!",
    body: "The special day is getting closer! ✨"
  },
  5: {
    title: "🎉 5 Days Remaining!",
    body: "Just 5 more days until the celebration! 🎁"
  },
  6: {
    title: "✨ 6 Days Remaining!",
    body: "The countdown continues... 🎂"
  },
  7: {
    title: "🎁 7 Days Remaining!",
    body: "Only 7 days until Priyanka's special day! 🌸"
  },
  8: {
    title: "🎂 8 Days Remaining!",
    body: "Priyanka's birthday is getting closer! ✨"
  },
  0: {
    title: "🎂 HAPPY BIRTHDAY, PRIYANKA!",
    body: "Today is the day! Tap to open your birthday surprise 🎁✨"
  }
};

function getCountdownMessage(daysRemaining) {
  if (COUNTDOWN_MESSAGES[daysRemaining]) {
    return COUNTDOWN_MESSAGES[daysRemaining];
  }
  return {
    title: `🎂 ${daysRemaining} Days Remaining!`,
    body: `The countdown to Priyanka's special day is on! ✨`
  };
}

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

  // Also check daily countdown notifications for users leading up to their birthday
  const countdownResult = await processDailyCountdownNotifications(db, messaging);

  return {
    birthdaySeries: { sent: sentCount, checked: checkedCount },
    countdown: countdownResult
  };
}

/**
 * Checks all registered users and sends exactly ONE daily birthday countdown notification
 * at the user's configured notification time (default 10:00 AM local time).
 */
async function processDailyCountdownNotifications(db, messaging) {
  const usersRef = db.collection('users');
  const snapshot = await usersRef.where('countdownNotificationEnabled', '!=', false).get();

  if (snapshot.empty) {
    console.log('No users with birthday countdown notifications enabled.');
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
      continue;
    }

    // Evaluate date and time in user's local timezone (preventing UTC mismatch)
    const nowInUserTz = DateTime.now().setZone(userTimezone);
    const todayDateStr = nowInUserTz.toISODate(); // "YYYY-MM-DD"
    const currentYear = nowInUserTz.year;

    const targetMonth = user.birthdayMonth || 9; // September
    const targetDay = user.birthdayDay || 10;     // 10th

    // Target birthday date for current calendar year
    const targetBdayThisYear = DateTime.fromObject({
      year: currentYear,
      month: targetMonth,
      day: targetDay,
      hour: 0,
      minute: 0,
      second: 0
    }, { zone: userTimezone });

    // Target date start of day vs current day start of day
    const startOfToday = nowInUserTz.startOf('day');
    const startOfTarget = targetBdayThisYear.startOf('day');

    let daysRemaining = 0;
    let targetYear = currentYear;

    if (startOfToday < startOfTarget) {
      // Before September 10 of current year -> Target is September 10 of current year
      daysRemaining = Math.round(startOfTarget.diff(startOfToday, 'days').days);
      targetYear = currentYear;
    } else if (startOfToday.hasSame(startOfTarget, 'day')) {
      // Exactly September 10 -> Today is birthday (days remaining = 0)
      // Note: Full day multi-series scheduler handles slot wishes on September 10.
      // We skip countdown "days remaining" notification on birthday day itself as per requirement.
      console.log(`User ${userId} is celebrating Birthday Today (${todayDateStr}). Countdown skipped.`);
      continue;
    } else {
      // After September 10 of current year: Stop countdown notifications for this year.
      // Countdown for next year's cycle will begin next year.
      console.log(`User ${userId}: Birthday for year ${currentYear} has already passed. Countdown stopped for this year.`);
      continue;
    }

    // Check configured notification time (Default: 10:00 AM)
    const scheduledHour = user.countdownNotificationHour ?? 10;
    const scheduledMinute = user.countdownNotificationMinute ?? 0;

    const currentTotalMinutes = nowInUserTz.hour * 60 + nowInUserTz.minute;
    const scheduledTotalMinutes = scheduledHour * 60 + scheduledMinute;
    const diffMinutes = currentTotalMinutes - scheduledTotalMinutes;

    // Delivery Window: Within [scheduledTime, scheduledTime + 120 minutes]
    // Anti-flood: If device/server was offline, only the current day's single notification is sent once.
    if (diffMinutes < 0 || diffMinutes > 120) {
      continue;
    }

    // Duplicate Protection: Check if today's countdown notification has already been sent
    const countdownState = user.birthdayCountdownState || {};
    if (countdownState.lastSentDate === todayDateStr && countdownState.year === currentYear) {
      console.log(`User ${userId} already received countdown notification for date ${todayDateStr}. Skipping.`);
      continue;
    }

    const messageData = getCountdownMessage(daysRemaining);

    console.log(`Sending Countdown Notification (${daysRemaining} Days Left) to User ${userId} [${messageData.title}]...`);

    const payload = {
      notification: {
        title: messageData.title,
        body: messageData.body
      },
      data: {
        type: "birthday_countdown",
        action: "birthday_countdown",
        open_birthday_countdown: "true",
        days_remaining: String(daysRemaining),
        target_year: String(targetYear),
        target_date: targetBdayThisYear.toISODate(),
        timestamp: String(Date.now())
      },
      android: {
        priority: "high",
        notification: {
          channelId: "birthday_surprise_channel",
          priority: "high",
          defaultSound: true,
          defaultVibrateTimings: true,
          color: "#E11D48",
          clickAction: "ACTION_OPEN_BIRTHDAY_COUNTDOWN"
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
        const response = await messaging.send({ token, ...payload });
        console.log(`Successfully sent countdown push (${daysRemaining} days) to token ${token.substring(0, 10)}... MsgId: ${response}`);
        sentCount++;
      } catch (error) {
        console.error(`FCM countdown send error for token ${token.substring(0, 10)}:`, error.code, error.message);
        if (
          error.code === 'messaging/invalid-registration-token' ||
          error.code === 'messaging/registration-token-not-registered'
        ) {
          invalidTokens.push(token);
        }
      }
    }

    // Record server-side duplicate prevention state
    const updateData = {
      birthdayCountdownState: {
        year: currentYear,
        lastSentDate: todayDateStr,
        lastSentDaysRemaining: daysRemaining
      },
      lastCountdownNotificationTimestamp: admin.firestore.FieldValue.serverTimestamp(),
      lastCountdownNotificationStatus: 'SUCCESS'
    };

    if (invalidTokens.length > 0) {
      updateData.fcmTokens = admin.firestore.FieldValue.arrayRemove(...invalidTokens);
    }

    await usersRef.doc(userId).update(updateData);
  }

  return { sent: sentCount, checked: checkedCount };
}

/**
 * Sends a single test birthday countdown notification without modifying real countdown state.
 */
async function sendTestCountdownNotification(db, messaging, userId, daysRemaining = 8, customToken = null) {
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
    throw new Error('No FCM tokens available to send test countdown push.');
  }

  const messageData = getCountdownMessage(daysRemaining);

  const payload = {
    notification: {
      title: messageData.title,
      body: messageData.body
    },
    data: {
      type: "test_countdown_notification",
      action: "birthday_countdown",
      open_birthday_countdown: "true",
      days_remaining: String(daysRemaining),
      is_test: "true",
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

  return {
    success: successCount > 0,
    delivered: successCount,
    total: tokens.length,
    daysRemaining,
    title: messageData.title,
    body: messageData.body,
    errors
  };
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
  COUNTDOWN_MESSAGES,
  getCountdownMessage,
  processBirthdayNotifications,
  processDailyCountdownNotifications,
  sendTestNotification,
  sendTestCountdownNotification
};
