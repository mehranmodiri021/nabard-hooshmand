package com.example

import com.example.data.model.ServerValidationResult
import com.example.data.model.SubmittedAnswer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizSecurityTest {

    @Test
    fun testSubmittedAnswerIntegrity() {
        val answer = SubmittedAnswer(
            questionId = "q_101",
            selectedOptionIndex = 2,
            remainingSeconds = 11,
            responseTimeMs = 2850L,
            sequenceIndex = 0
        )

        assertEquals("q_101", answer.questionId)
        assertEquals(2, answer.selectedOptionIndex)
        assertEquals(11, answer.remainingSeconds)
        assertEquals(2850L, answer.responseTimeMs)
        assertEquals(0, answer.sequenceIndex)
    }

    @Test
    fun testServerValidationResultSuccessModeling() {
        val verifiedResult = ServerValidationResult(
            isSuccess = true,
            scoreEarned = 1450L,
            correctCount = 5,
            totalQuestions = 5,
            rank = 1,
            prizeWonToman = 250000L,
            message = "نتایج توسط سرور امن Cloud Functions تایید شد.",
            isServerEnforced = true
        )

        assertTrue(verifiedResult.isSuccess)
        assertTrue(verifiedResult.isServerEnforced)
        assertEquals(1, verifiedResult.rank)
        assertEquals(250000L, verifiedResult.prizeWonToman)
        assertEquals(1450L, verifiedResult.scoreEarned)
    }

    @Test
    fun testServerValidationResultRejectedModeling() {
        val rejectedResult = ServerValidationResult(
            isSuccess = false,
            scoreEarned = 0L,
            correctCount = 0,
            totalQuestions = 5,
            rank = 4,
            prizeWonToman = 0L,
            message = "نتیجه این مسابقه قبلاً ثبت شده و امکان ارسال مجدد وجود ندارد.",
            isServerEnforced = true
        )

        assertFalse(rejectedResult.isSuccess)
        assertTrue(rejectedResult.isServerEnforced)
        assertEquals(0L, rejectedResult.prizeWonToman)
        assertEquals(4, rejectedResult.rank)
    }
}
