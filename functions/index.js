const { onCall, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

// Initialize Firebase Admin with Firestore
admin.initializeApp();
const db = admin.firestore();

/**
 * Helper to check server-side Admin permission
 * Strictly enforces token custom claims or verified Firestore role.
 * Creator name/display name is NEVER used as authorization!
 */
async function verifyAdminPermission(auth) {
  if (!auth || !auth.uid) {
    throw new HttpsError("unauthenticated", "برای انجام این عملیات باید وارد حساب کاربری شوید.");
  }

  // 1. Check custom claims
  if (auth.token && (auth.token.admin === true || auth.token.role === "ADMIN")) {
    return true;
  }

  // 2. Check secure role in Firestore users collection
  const userDoc = await db.collection("users").doc(auth.uid).get();
  if (userDoc.exists && (userDoc.data().role === "ADMIN" || userDoc.data().isAdmin === true)) {
    return true;
  }

  throw new HttpsError("permission-denied", "دسترسی غیرمجاز: شما دسترسی مدیر سیستم (Admin) را ندارید.");
}

/**
 * Cloud Function: submitTournamentAnswers
 * Securely validates user answers, response timing, question sequence, prevents duplicate submissions and multiple prize payouts.
 */
exports.submitTournamentAnswers = onCall({ cors: true }, async (request) => {
  const auth = request.auth;
  if (!auth || !auth.uid) {
    throw new HttpsError("unauthenticated", "کاربر احراز هویت نشده است.");
  }

  const userId = auth.uid;
  const { tournamentId, answers } = request.data || {};

  if (!tournamentId || !Array.isArray(answers) || answers.length === 0) {
    throw new HttpsError("invalid-argument", "اطلاعات ارسالی مسابقه یا پاسخ‌ها نامعتبر است.");
  }

  const tournamentRef = db.collection("tournaments").doc(tournamentId);
  const userScoreRef = tournamentRef.collection("scores").doc(userId);
  const userRef = db.collection("users").doc(userId);

  // Execute in an atomic transaction to ensure zero race conditions and anti-cheat enforcement
  return await db.runTransaction(async (transaction) => {
    // 1. Anti-Duplicate Check: Prevent re-submitting, duplicate participation or multiple prize claims
    const existingScore = await transaction.get(userScoreRef);
    if (existingScore.exists) {
      throw new HttpsError(
        "already-exists",
        "شما قبلاً در این مسابقه شرکت کرده و نتیجه نهایی خود را ثبت نموده‌اید. امکان ثبت دوباره یا تغییر امتیاز وجود ندارد."
      );
    }

    // 2. Fetch Tournament Details
    const tourDoc = await transaction.get(tournamentRef);
    if (!tourDoc.exists) {
      throw new HttpsError("not-found", "مسابقه مورد نظر یافت نشد.");
    }
    const tournamentData = tourDoc.data();

    // 3. Server-side validation of answers against official questions
    let officialQuestions = tournamentData.questions || [];
    if (officialQuestions.length === 0) {
      const questionsSnap = await tournamentRef.collection("questions").get();
      officialQuestions = questionsSnap.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    }

    if (officialQuestions.length === 0) {
      throw new HttpsError("failed-precondition", "سوالات مسابقه در پایگاه داده سرور یافت نشد.");
    }

    // Call server-side validation engine
    const {
      correctCount,
      totalScore,
      totalQuestions,
      rank,
      prizeWonToman
    } = validateAndCalculateScore(tournamentData, officialQuestions, answers);

    // 5. Atomic State Writes:
    // a. Record verified score record (Client cannot write or modify this directly)
    transaction.set(userScoreRef, {
      userId: userId,
      tournamentId: tournamentId,
      scoreEarned: totalScore,
      correctCount: correctCount,
      totalQuestions: totalQuestions,
      rank: rank,
      prizeWonToman: prizeWonToman,
      verifiedAt: admin.firestore.FieldValue.serverTimestamp(),
      validationStatus: "VERIFIED_SECURE"
    });

    // b. If cash prize won, atomically credit wallet and create official ledger transaction
    if (prizeWonToman > 0) {
      const txId = "tx_prz_" + Date.now() + "_" + Math.floor(1000 + Math.random() * 9000);
      const txRef = db.collection("transactions").doc(txId);

      transaction.set(txRef, {
        id: txId,
        userId: userId,
        title: "جایزه رسمی قهرمانی: " + (tournamentData.title || "مسابقه"),
        amountToman: prizeWonToman,
        type: "PRIZE_WON",
        status: "SUCCESS",
        datePersian: new Date().toLocaleDateString("fa-IR"),
        trackingCode: "PRZ-" + Math.floor(100000 + Math.random() * 900000),
        createdAt: admin.firestore.FieldValue.serverTimestamp()
      });

      transaction.update(userRef, {
        balanceToman: admin.firestore.FieldValue.increment(prizeWonToman),
        totalCashWonToman: admin.firestore.FieldValue.increment(prizeWonToman)
      });
    }

    // c. Update user game statistics atomically
    const isWin = rank <= 3;
    transaction.update(userRef, {
      totalPoints: admin.firestore.FieldValue.increment(totalScore),
      tournamentsPlayed: admin.firestore.FieldValue.increment(1),
      wins: isWin ? admin.firestore.FieldValue.increment(1) : admin.firestore.FieldValue.increment(0),
      xp: admin.firestore.FieldValue.increment(Math.floor(totalScore / 2)),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    return {
      success: true,
      tournamentId: tournamentId,
      scoreEarned: totalScore,
      correctCount: correctCount,
      totalQuestions: totalQuestions,
      rank: rank,
      prizeWonToman: prizeWonToman,
      message: "نتایج و امتیازات با موفقیت توسط سرور امن Cloud Functions اعتبارسنجی و ثبت گردید."
    };
  });
});

/**
 * Cloud Function: createTournamentSecure (Admin Only)
 * Creates new tournament in Firestore with server-enforced role verification.
 */
exports.createTournamentSecure = onCall({ cors: true }, async (request) => {
  await verifyAdminPermission(request.auth);

  const { tournament } = request.data || {};
  if (!tournament || !tournament.title) {
    throw new HttpsError("invalid-argument", "مشخصات مسابقه ناقص است.");
  }

  const tourId = tournament.id || ("tour_" + Date.now());
  const tourRef = db.collection("tournaments").doc(tourId);

  await tourRef.set({
    ...tournament,
    id: tourId,
    createdAt: admin.firestore.FieldValue.serverTimestamp()
  });

  return { success: true, tournamentId: tourId };
});

/**
 * Cloud Function: approveWithdrawalSecure (Admin Only)
 * Approves a cash prize withdrawal claim.
 */
exports.approveWithdrawalSecure = onCall({ cors: true }, async (request) => {
  await verifyAdminPermission(request.auth);

  const { withdrawalId, trackingCode } = request.data || {};
  if (!withdrawalId) {
    throw new HttpsError("invalid-argument", "شناسه درخواست تسویه الزامی است.");
  }

  const wRef = db.collection("withdrawals").doc(withdrawalId);
  await wRef.update({
    status: "PAID",
    trackingCode: trackingCode || ("PAY-" + Date.now()),
    paidAt: admin.firestore.FieldValue.serverTimestamp()
  });

  return { success: true, message: "تسویه با موفقیت تایید و ثبت شد." };
});

/**
 * Cloud Function: setAdminRoleSecure
 * Grants admin role to a target user UID. Requires existing admin authority.
 */
exports.setAdminRoleSecure = onCall({ cors: true }, async (request) => {
  await verifyAdminPermission(request.auth);

  const { targetUserId } = request.data || {};
  if (!targetUserId) {
    throw new HttpsError("invalid-argument", "شناسه کاربر الزامی است.");
  }

  // Set Firebase Custom Claims for token-level security
  await admin.auth().setCustomUserClaims(targetUserId, { admin: true, role: "ADMIN" });

  // Update user document
  await db.collection("users").doc(targetUserId).update({
    role: "ADMIN",
    isAdmin: true,
    updatedAt: admin.firestore.FieldValue.serverTimestamp()
  });

  return { success: true, message: "دسترسی مدیر با موفقیت به کاربر اعطا شد." };
});

/**
 * Server-Side Validation and Anti-Cheat Scoring Engine
 * Pure logic function that validates answer ordering, timings, correctness, and calculates verified scores and ranks.
 */
function validateAndCalculateScore(tournamentData, officialQuestions, answers) {
  if (!answers || !Array.isArray(answers) || answers.length === 0) {
    throw new HttpsError("invalid-argument", "لیست پاسخ‌ها خالی است.");
  }

  if (answers.length > officialQuestions.length) {
    throw new HttpsError("invalid-argument", "تعداد پاسخ‌های ارسالی بیش از تعداد سوالات مسابقه است.");
  }

  let correctCount = 0;
  let totalScore = 0;
  let comboStreak = 0;
  const MIN_HUMAN_TIME_MS = 400; // Human sensory-motor minimum threshold (bot prevention)

  for (let i = 0; i < answers.length; i++) {
    const userAns = answers[i];
    const officialQ = officialQuestions[i];

    // 1. Verify Question Order & Sequence
    if (userAns.sequenceIndex !== undefined && userAns.sequenceIndex !== i) {
      throw new HttpsError("invalid-argument", `ترتیب ارسال سوالات نامعتبر است (سوال شماره ${i + 1}).`);
    }

    if (userAns.questionId && officialQ && userAns.questionId !== officialQ.id) {
      throw new HttpsError("invalid-argument", "تطابق شناسه سوال ارسالی با سوال رسمی سرور تایید نشد.");
    }

    const targetQ = officialQ || officialQuestions.find(q => q.id === userAns.questionId);
    if (!targetQ) continue;

    // 2. Anti-Bot Timing & Time Limit Validation
    const timeLimitSec = targetQ.timeLimitSeconds || 15;
    const maxAllowedDurationMs = (timeLimitSec * 1000) + 3500; // 3.5s network latency buffer
    const responseTimeMs = Number(userAns.responseTimeMs) || 0;
    const remainingSec = Math.max(0, Math.min(timeLimitSec, Number(userAns.remainingSeconds) || 0));

    const isTooFast = responseTimeMs > 0 && responseTimeMs < MIN_HUMAN_TIME_MS;
    const isExpired = responseTimeMs > maxAllowedDurationMs;

    // 3. Validate correctness strictly against server question truth
    const isCorrect = !isTooFast && !isExpired && (userAns.selectedOptionIndex === targetQ.correctIndex);

    if (isCorrect) {
      correctCount++;
      comboStreak++;

      const multiplier = targetQ.difficulty === "HARD" ? 2.0 : (targetQ.difficulty === "EXPERT" ? 2.5 : 1.5);
      const baseScore = Math.floor((targetQ.basePoints || 100) * multiplier);
      const speedBonus = remainingSec * 10;
      const streakBonus = comboStreak > 1 ? Math.min(300, comboStreak * 25) : 0;

      totalScore += (baseScore + speedBonus + streakBonus);
    } else {
      comboStreak = 0;
    }
  }

  // 4. Determine verified Rank and Cash Prize Pool Allocation
  const totalQuestions = officialQuestions.length || answers.length;
  const accuracy = totalQuestions > 0 ? (correctCount / totalQuestions) : 0;

  let rank = 4;
  let prizeWonToman = 0;

  if (accuracy >= 0.85) {
    rank = 1;
    prizeWonToman = Math.floor((tournamentData.prizePoolToman || 0) * 0.50);
  } else if (accuracy >= 0.70) {
    rank = 2;
    prizeWonToman = Math.floor((tournamentData.prizePoolToman || 0) * 0.30);
  } else if (accuracy >= 0.50) {
    rank = 3;
    prizeWonToman = Math.floor((tournamentData.prizePoolToman || 0) * 0.15);
  }

  return {
    correctCount,
    totalScore,
    totalQuestions,
    accuracy,
    rank,
    prizeWonToman
  };
}

exports.validateAndCalculateScore = validateAndCalculateScore;
