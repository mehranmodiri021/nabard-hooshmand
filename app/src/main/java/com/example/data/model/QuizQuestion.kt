package com.example.data.model

import com.google.firebase.firestore.DocumentId

data class QuizQuestion(
    @DocumentId val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val explanation: String = "",
    val category: String = "",
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val timeLimitSeconds: Int = 15,
    val basePoints: Int = 100
)

enum class Difficulty(val labelFa: String, val multiplier: Float) {
    EASY("آسان", 1.0f),
    MEDIUM("متوسط", 1.5f),
    HARD("سخت", 2.0f),
    EXPERT("فوق‌العاده", 2.5f)
}
