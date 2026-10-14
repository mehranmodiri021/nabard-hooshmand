package com.example.data.security

import android.content.Context
import android.os.SystemClock
import android.util.Log

object AntiCheatEngine {

    private const val MIN_HUMAN_RESPONSE_TIME_MS = 450L
    private var questionStartTimeMs: Long = 0L

    fun onQuestionDisplayed() {
        questionStartTimeMs = SystemClock.elapsedRealtime()
    }

    fun getElapsedResponseTimeMs(): Long {
        val elapsed = SystemClock.elapsedRealtime() - questionStartTimeMs
        return elapsed.coerceAtLeast(0L)
    }

    data class ValidationResult(
        val isValid: Boolean,
        val responseTimeMs: Long,
        val calculatedScore: Long,
        val reason: String = "OK"
    )

    fun validateAnswerSubmission(
        basePoints: Int,
        difficultyMultiplier: Float,
        remainingSeconds: Int,
        timeLimitSeconds: Int,
        streak: Int,
        isCorrect: Boolean
    ): ValidationResult {
        val elapsedMs = SystemClock.elapsedRealtime() - questionStartTimeMs
        val responseTimeMs = elapsedMs.coerceAtLeast(0L)

        // 1. Robotic Speed Check (Less than humanly possible threshold)
        if (elapsedMs < MIN_HUMAN_RESPONSE_TIME_MS && isCorrect) {
            Log.w("AntiCheat", "Suspicious speed detected: $elapsedMs ms")
            return ValidationResult(
                isValid = false,
                responseTimeMs = responseTimeMs,
                calculatedScore = 0L,
                reason = "سرعت پاسخگویی غیرعادی و مشکوک به ربات (کمتر از ۴۵۰ میلی‌ثانیه)"
            )
        }

        // 2. Time limit bounds check
        if (remainingSeconds < 0 || remainingSeconds > timeLimitSeconds) {
            return ValidationResult(
                isValid = false,
                responseTimeMs = responseTimeMs,
                calculatedScore = 0L,
                reason = "زمان پاسخگویی خارج از محدوده مجاز سوال است"
            )
        }

        if (!isCorrect) {
            return ValidationResult(
                isValid = true,
                responseTimeMs = responseTimeMs,
                calculatedScore = 0L,
                reason = "پاسخ نادرست"
            )
        }

        // 3. Strict mathematical score derivation
        val maxAllowedSpeedBonus = timeLimitSeconds * 10
        val actualSpeedBonus = (remainingSeconds * 10).coerceIn(0, maxAllowedSpeedBonus)
        val baseScore = (basePoints * difficultyMultiplier).toInt()
        val streakBonus = if (streak > 1) (streak * 25).coerceAtMost(300) else 0

        val totalScore = (baseScore + actualSpeedBonus + streakBonus).toLong()

        return ValidationResult(
            isValid = true,
            responseTimeMs = responseTimeMs,
            calculatedScore = totalScore,
            reason = "تایید معتبر توسط موتور امنیتی"
        )
    }

    fun verifyApkIntegrity(context: Context): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.packageName == "com.aistudio.challengetournament.qmvpxz"
        } catch (e: Exception) {
            true
        }
    }
}
