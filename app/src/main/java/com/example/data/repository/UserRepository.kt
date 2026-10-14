package com.example.data.repository

import com.example.data.model.LeaderboardEntry
import com.example.data.model.LeaderboardTimeframe
import com.example.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserRepository {

    private val _currentUser = MutableStateFlow(User())
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    fun updateProfile(name: String, bio: String, phone: String) {
        _currentUser.value = _currentUser.value.copy(
            fullName = name,
            bio = bio,
            phoneNumber = phone
        )
    }

    fun addMatchResults(pointsEarned: Long, isWin: Boolean, cashEarned: Long = 0) {
        val user = _currentUser.value
        val newTournamentsCount = user.tournamentsPlayed + 1
        val newWins = if (isWin) user.wins + 1 else user.wins
        val newPoints = user.totalPoints + pointsEarned
        val newCash = user.totalCashWonToman + cashEarned
        val newXp = user.xp + (pointsEarned / 2).toInt()

        var newLevel = user.level
        var newXpNeeded = user.xpNeededNextLevel
        if (newXp >= newXpNeeded) {
            newLevel += 1
            newXpNeeded += 1000
        }

        _currentUser.value = user.copy(
            tournamentsPlayed = newTournamentsCount,
            wins = newWins,
            totalPoints = newPoints,
            totalCashWonToman = newCash,
            xp = newXp,
            level = newLevel,
            xpNeededNextLevel = newXpNeeded,
            globalRank = if (isWin && user.globalRank > 1) user.globalRank - 1 else user.globalRank
        )
    }

    fun getLeaderboard(timeframe: LeaderboardTimeframe): List<LeaderboardEntry> {
        val currentUserRank = _currentUser.value.globalRank
        val currentUserPoints = _currentUser.value.totalPoints
        val currentUserWins = _currentUser.value.wins
        val currentUserWinRate = _currentUser.value.winRatePercent
        val currentUserCash = _currentUser.value.totalCashWonToman

        return listOf(
            LeaderboardEntry(
                rank = 1,
                name = "امیرحسین رضایی",
                points = when (timeframe) {
                    LeaderboardTimeframe.WEEKLY -> 4200
                    LeaderboardTimeframe.MONTHLY -> 15800
                    LeaderboardTimeframe.ALL_TIME -> 49200
                },
                wins = 54,
                winRatePercent = 88,
                totalPrizeToman = 65000000,
                avatarEmoji = "🦁"
            ),
            LeaderboardEntry(
                rank = 2,
                name = "سارا محمدی",
                points = when (timeframe) {
                    LeaderboardTimeframe.WEEKLY -> 3900
                    LeaderboardTimeframe.MONTHLY -> 14200
                    LeaderboardTimeframe.ALL_TIME -> 41300
                },
                wins = 48,
                winRatePercent = 81,
                totalPrizeToman = 52000000,
                avatarEmoji = "🦊"
            ),
            LeaderboardEntry(
                rank = 3,
                name = _currentUser.value.fullName,
                points = currentUserPoints,
                wins = currentUserWins,
                winRatePercent = currentUserWinRate,
                totalPrizeToman = currentUserCash,
                avatarEmoji = "⭐",
                isCurrentUser = true
            ),
            LeaderboardEntry(
                rank = 4,
                name = "مهدی ناصری",
                points = when (timeframe) {
                    LeaderboardTimeframe.WEEKLY -> 3100
                    LeaderboardTimeframe.MONTHLY -> 11800
                    LeaderboardTimeframe.ALL_TIME -> 34500
                },
                wins = 36,
                winRatePercent = 72,
                totalPrizeToman = 29000000,
                avatarEmoji = "🦅"
            ),
            LeaderboardEntry(
                rank = 5,
                name = "نیلوفر کاظمی",
                points = when (timeframe) {
                    LeaderboardTimeframe.WEEKLY -> 2800
                    LeaderboardTimeframe.MONTHLY -> 9900
                    LeaderboardTimeframe.ALL_TIME -> 30200
                },
                wins = 31,
                winRatePercent = 68,
                totalPrizeToman = 24000000,
                avatarEmoji = "🐯"
            ),
            LeaderboardEntry(
                rank = 6,
                name = "کیانوش افشار",
                points = when (timeframe) {
                    LeaderboardTimeframe.WEEKLY -> 2550
                    LeaderboardTimeframe.MONTHLY -> 8700
                    LeaderboardTimeframe.ALL_TIME -> 26800
                },
                wins = 28,
                winRatePercent = 65,
                totalPrizeToman = 19500000,
                avatarEmoji = "🐺"
            ),
            LeaderboardEntry(
                rank = 7,
                name = "فاطمه حسینی",
                points = when (timeframe) {
                    LeaderboardTimeframe.WEEKLY -> 2200
                    LeaderboardTimeframe.MONTHLY -> 7900
                    LeaderboardTimeframe.ALL_TIME -> 23400
                },
                wins = 24,
                winRatePercent = 61,
                totalPrizeToman = 15000000,
                avatarEmoji = "🦄"
            )
        )
    }
}
