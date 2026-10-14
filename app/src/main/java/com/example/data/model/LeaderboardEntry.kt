package com.example.data.model

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val points: Long,
    val wins: Int,
    val winRatePercent: Int,
    val totalPrizeToman: Long,
    val avatarEmoji: String,
    val isCurrentUser: Boolean = false
)

enum class LeaderboardTimeframe(val labelFa: String) {
    WEEKLY("هفتگی"),
    MONTHLY("ماهانه"),
    ALL_TIME("کل دوران")
}

data class AppSettings(
    val isDarkMode: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val language: String = "فارسی"
)
