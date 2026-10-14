const test = require("node:test");
const assert = require("node:assert/strict");
const { validateAndCalculateScore } = require("../index.js");

// Sample mock tournament data configured as official server ground-truth
const mockTournament = {
  id: "tour_test_001",
  title: "جام قهرمانان هوش و اطلاعات عمومی",
  prizePoolToman: 500000,
  category: "GENERAL_KNOWLEDGE"
};

const mockQuestions = [
  {
    id: "q1",
    text: "پایتخت تاریخی دوره صفویه در دوران اوج شکوه کدام شهر بود؟",
    options: ["شیراز", "اصفهان", "تبریز", "قزوین"],
    correctIndex: 1, // اصفهان
    difficulty: "NORMAL",
    basePoints: 100,
    timeLimitSeconds: 15
  },
  {
    id: "q2",
    text: "کدام عنصر بیشترین درصد جو کره زمین را تشکیل می‌دهد؟",
    options: ["اکسیژن", "نیتروژن", "هیدروژن", "دی‌اکسید کربن"],
    correctIndex: 1, // نیتروژن
    difficulty: "HARD",
    basePoints: 100,
    timeLimitSeconds: 15
  },
  {
    id: "q3",
    text: "بزرگ‌ترین اقیانوس کره زمین کدام است؟",
    options: ["آرام", "اطلس", "هند", "منجمد شمالی"],
    correctIndex: 0, // آرام
    difficulty: "NORMAL",
    basePoints: 100,
    timeLimitSeconds: 15
  }
];

test("Real Scenario 1: Honest player with 100% correct answers receives valid score and 1st rank prize", () => {
  const userAnswers = [
    {
      questionId: "q1",
      sequenceIndex: 0,
      selectedOptionIndex: 1, // Correct
      remainingSeconds: 10,
      responseTimeMs: 3200
    },
    {
      questionId: "q2",
      sequenceIndex: 1,
      selectedOptionIndex: 1, // Correct
      remainingSeconds: 8,
      responseTimeMs: 4500
    },
    {
      questionId: "q3",
      sequenceIndex: 2,
      selectedOptionIndex: 0, // Correct
      remainingSeconds: 12,
      responseTimeMs: 2100
    }
  ];

  const result = validateAndCalculateScore(mockTournament, mockQuestions, userAnswers);

  assert.equal(result.correctCount, 3);
  assert.equal(result.totalQuestions, 3);
  assert.equal(result.accuracy, 1.0);
  assert.equal(result.rank, 1);
  // 50% of 500,000 = 250,000
  assert.equal(result.prizeWonToman, 250000);
  assert.ok(result.totalScore > 0, "Total score should be calculated and greater than 0");
});

test("Real Scenario 2: Anti-Cheat blocks automated bot responding unnaturally fast (< 400ms)", () => {
  const botAnswers = [
    {
      questionId: "q1",
      sequenceIndex: 0,
      selectedOptionIndex: 1,
      remainingSeconds: 15,
      responseTimeMs: 80 // Unhuman speed (< 400ms)
    },
    {
      questionId: "q2",
      sequenceIndex: 1,
      selectedOptionIndex: 1,
      remainingSeconds: 15,
      responseTimeMs: 50 // Unhuman speed (< 400ms)
    },
    {
      questionId: "q3",
      sequenceIndex: 2,
      selectedOptionIndex: 0,
      remainingSeconds: 15,
      responseTimeMs: 90 // Unhuman speed (< 400ms)
    }
  ];

  const result = validateAndCalculateScore(mockTournament, mockQuestions, botAnswers);

  // Even though options matched, bot timing invalidates all answers
  assert.equal(result.correctCount, 0);
  assert.equal(result.totalScore, 0);
  assert.equal(result.rank, 4);
  assert.equal(result.prizeWonToman, 0);
});

test("Real Scenario 3: Anti-Cheat rejects manipulation of question order / sequence", () => {
  const scrambledAnswers = [
    {
      questionId: "q2", // Out of order!
      sequenceIndex: 1,
      selectedOptionIndex: 1,
      remainingSeconds: 10,
      responseTimeMs: 2500
    },
    {
      questionId: "q1",
      sequenceIndex: 0,
      selectedOptionIndex: 1,
      remainingSeconds: 10,
      responseTimeMs: 2500
    }
  ];

  assert.throws(
    () => {
      validateAndCalculateScore(mockTournament, mockQuestions, scrambledAnswers);
    },
    (err) => {
      return err.message.includes("ترتیب ارسال") || err.message.includes("تطابق شناسه");
    }
  );
});

test("Real Scenario 4: Answers submitted after question time limit are rejected", () => {
  const expiredAnswers = [
    {
      questionId: "q1",
      sequenceIndex: 0,
      selectedOptionIndex: 1,
      remainingSeconds: 0,
      responseTimeMs: 25000 // Time limit is 15s + 3.5s buffer = 18.5s max
    },
    {
      questionId: "q2",
      sequenceIndex: 1,
      selectedOptionIndex: 1,
      remainingSeconds: 5,
      responseTimeMs: 4000
    },
    {
      questionId: "q3",
      sequenceIndex: 2,
      selectedOptionIndex: 0,
      remainingSeconds: 6,
      responseTimeMs: 3000
    }
  ];

  const result = validateAndCalculateScore(mockTournament, mockQuestions, expiredAnswers);

  // q1 expired -> only q2 and q3 correct
  assert.equal(result.correctCount, 2);
  assert.equal(result.totalQuestions, 3);
  // Accuracy = 2/3 = 0.666 -> Rank 3 (>= 50%) -> 15% prize
  assert.equal(result.rank, 3);
  assert.equal(result.prizeWonToman, 75000); // 15% of 500,000
});
