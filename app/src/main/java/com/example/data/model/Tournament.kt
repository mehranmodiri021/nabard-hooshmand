package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

enum class TournamentCategory(val titleFa: String) {
    ALL("همه"),
    FREE("رایگان"),
    VIP("ویژه"),
    DAILY("روزانه"),
    WEEKLY("هفتگی"),
    GRAND("مسابقات بزرگ")
}

data class Tournament(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: TournamentCategory = TournamentCategory.DAILY,
    val entryFeeToman: Long = 0L,
    val prizePoolToman: Long = 0L,
    val maxParticipants: Int = 100,
    val currentParticipants: Int = 0,
    val startsInSeconds: Long = 0L,
    val questions: List<QuizQuestion> = emptyList(),
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val isHotOrSpecial: Boolean = false,
    val winnerCount: Int = 3,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val isFree: Boolean get() = entryFeeToman == 0L
    val isFull: Boolean get() = currentParticipants >= maxParticipants
}

data class SubmittedAnswer(
    val questionId: String = "",
    val selectedOptionIndex: Int = -1,
    val remainingSeconds: Int = 0,
    val responseTimeMs: Long = 0L,
    val sequenceIndex: Int = 0
)

data class ServerValidationResult(
    val isSuccess: Boolean = false,
    val scoreEarned: Long = 0L,
    val correctCount: Int = 0,
    val totalQuestions: Int = 0,
    val rank: Int = 4,
    val prizeWonToman: Long = 0L,
    val message: String = "",
    val isServerEnforced: Boolean = true
)
