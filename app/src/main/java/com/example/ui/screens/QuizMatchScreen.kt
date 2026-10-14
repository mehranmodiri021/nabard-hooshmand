package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowCard
import com.example.ui.components.formatPersianInt
import com.example.ui.components.formatPersianNumber
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicCardElevated
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.MatchState

@Composable
fun QuizMatchScreen(
    matchState: MatchState,
    onAnswerSelected: (Int) -> Unit,
    onQuitMatch: () -> Unit
) {
    var showQuitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showQuitDialog = true
    }

    val tournament = matchState.tournament ?: return
    val totalQuestions = tournament.questions.size
    val currentQIndex = matchState.currentQuestionIndex
    val question = tournament.questions.getOrNull(currentQIndex) ?: return

    val timerProgress = matchState.remainingSeconds.toFloat() / question.timeLimitSeconds.coerceAtLeast(1)
    val timerColor = when {
        matchState.remainingSeconds <= 4 -> CrimsonDanger
        matchState.remainingSeconds <= 8 -> GoldPrimary
        else -> CyberCyan
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CosmicDark, Color(0xFF0F172A), Color(0xFF131D31))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Quit, Question progress, Score, Timer
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showQuitDialog = true },
                        modifier = Modifier.testTag("match_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "خروج از مسابقه",
                            tint = Color.White
                        )
                    }

                    // Question counter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CosmicCard)
                            .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "سوال ${formatPersianInt(currentQIndex + 1)} از ${formatPersianInt(totalQuestions)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Score Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldPrimary.copy(alpha = 0.2f))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = GoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${formatPersianNumber(matchState.scoreEarned)}",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Linear overall quiz progress
                LinearProgressIndicator(
                    progress = { (currentQIndex + 1).toFloat() / totalQuestions },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CyberCyan,
                    trackColor = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Timer & Streak Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Countdown Timer
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(46.dp)) {
                            CircularProgressIndicator(
                                progress = { timerProgress },
                                color = timerColor,
                                trackColor = Color(0xFF334155),
                                strokeWidth = 4.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = formatPersianInt(matchState.remainingSeconds),
                                color = timerColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ثانیه باقی‌مانده",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    // Streak Multiplier
                    if (matchState.comboStreak > 1) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(GoldPrimary, CyberPurple)))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "ضریب سرعت: ${formatPersianInt(matchState.comboStreak)}x 🔥",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Question Card
            GlowCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                backgroundColor = CosmicCardElevated,
                borderColor = timerColor.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "دسته‌بندی: ${question.category}",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = question.question,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 26.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 4 Options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val optionDigits = listOf("۱", "۲", "۳", "۴")
                question.options.forEachIndexed { index, optionText ->
                    val isSelected = matchState.selectedOptionIndex == index
                    val isCorrectAnswer = index == question.correctIndex
                    val isConfirmed = matchState.isAnswerConfirmed

                    val cardBorderColor = when {
                        !isConfirmed -> if (isSelected) GoldPrimary else CosmicBorder
                        isCorrectAnswer -> EmeraldSuccess
                        isSelected && !isCorrectAnswer -> CrimsonDanger
                        else -> CosmicBorder
                    }

                    val cardBgColor = when {
                        !isConfirmed -> if (isSelected) CosmicCardElevated else CosmicCard
                        isCorrectAnswer -> EmeraldSuccess.copy(alpha = 0.18f)
                        isSelected && !isCorrectAnswer -> CrimsonDanger.copy(alpha = 0.18f)
                        else -> CosmicCard.copy(alpha = 0.6f)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardBgColor)
                            .border(1.5.dp, cardBorderColor, RoundedCornerShape(16.dp))
                            .clickable(enabled = !isConfirmed) {
                                onAnswerSelected(index)
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("option_button_$index")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF334155)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optionDigits.getOrElse(index) { "${index + 1}" },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = optionText,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }

                            if (isConfirmed) {
                                if (isCorrectAnswer) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "صحیح",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "غلط",
                                        tint = CrimsonDanger,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Explanation Section upon answer confirmation
            AnimatedVisibility(
                visible = matchState.isAnswerConfirmed,
                enter = fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "💡 ${question.explanation}",
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    // Quit Match Confirmation Dialog
    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text(text = "خروج از مسابقه؟", fontWeight = FontWeight.Bold) },
            text = {
                Text(text = "در صورت خروج از مسابقه، امتیازات فعلی محاسبه نخواهد شد و شانس دریافت جایزه نقدی از دست می‌رود.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQuitDialog = false
                        onQuitMatch()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger)
                ) {
                    Text(text = "خروج قطعی", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showQuitDialog = false }) {
                    Text(text = "ادامه مسابقه")
                }
            }
        )
    }
}
