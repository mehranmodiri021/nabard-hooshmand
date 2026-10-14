package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowCard
import com.example.ui.components.GradientButton
import com.example.ui.components.formatPersianInt
import com.example.ui.components.formatPersianNumber
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.MatchState

@Composable
fun MatchResultScreen(
    matchState: MatchState,
    onGoToWallet: () -> Unit,
    onGoToHome: () -> Unit,
    onPlayAnother: () -> Unit
) {
    BackHandler {
        onGoToHome()
    }

    val tournament = matchState.tournament
    val totalQuestions = tournament?.questions?.size ?: 5
    val isPodium = matchState.rankInRoom <= 3
    val prize = matchState.prizeWonToman

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CosmicDark, Color(0xFF0F172A), Color(0xFF1E1B4B))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(20.dp))

                // Trophy / Medal Badge
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(24.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                if (isPodium) listOf(GoldPrimary, GoldDark) else listOf(CyberPurple, CyberCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (matchState.rankInRoom) {
                            1 -> "🥇"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> "🎖️"
                        },
                        fontSize = 54.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isPodium) "تبریک! عملکرد درخشان و قهرمانی" else "مسابقه به پایان رسید!",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = tournament?.title ?: "مسابقه مهارتی و فکری",
                    color = GoldLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Results Card
            GlowCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = CosmicCard,
                borderColor = if (isPodium) GoldPrimary else CosmicBorder
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Rank achieved
                    Text(
                        text = "رتبه شما در مسابقه",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "رتبه ${formatPersianInt(matchState.rankInRoom)}",
                        color = if (isPodium) GoldLight else Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "امتیاز کسب شده",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${formatPersianNumber(matchState.scoreEarned)}",
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(CosmicBorder)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "پاسخ‌های صحیح",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${formatPersianInt(matchState.correctAnswersCount)} از ${formatPersianInt(totalQuestions)}",
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Cash Prize Box (if won)
                    if (prize > 0) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(EmeraldSuccess.copy(alpha = 0.15f))
                                .border(1.dp, EmeraldSuccess, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "جایزه نقدی کسب شده (واریز آنی به کیف پول):",
                                    color = EmeraldLight,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "+${formatPersianNumber(prize)} تومان",
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp
                                )
                            }
                        }
                    }

                    // Cloud Functions Anti-Cheat Verification Badge
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (matchState.isServerVerified) EmeraldSuccess.copy(alpha = 0.12f)
                                else Color(0xFF1E293B)
                            )
                            .border(
                                1.dp,
                                if (matchState.isServerVerified) EmeraldSuccess.copy(alpha = 0.5f)
                                else CosmicBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (matchState.isSubmittingToServer) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = CyberCyan
                                )
                            } else {
                                Icon(
                                    imageVector = if (matchState.isServerVerified) Icons.Default.CheckCircle else Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = if (matchState.isServerVerified) EmeraldSuccess else GoldLight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (matchState.serverStatusMessage.isNotBlank())
                                    matchState.serverStatusMessage
                                else if (matchState.isServerVerified)
                                    "امتیاز و رتبه توسط سرور امن Cloud Functions اعتبارسنجی و ثبت شد."
                                else
                                    "اعتبارسنجی امن نتایج",
                                color = if (matchState.isServerVerified) EmeraldLight else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (prize > 0) {
                    GradientButton(
                        text = "مشاهده کیف پول و درخواست تسویه",
                        onClick = onGoToWallet,
                        icon = Icons.Default.AccountBalanceWallet,
                        modifier = Modifier.fillMaxWidth().testTag("result_wallet_btn"),
                        gradient = Brush.horizontalGradient(listOf(EmeraldSuccess, Color(0xFF059669)))
                    )
                }

                GradientButton(
                    text = "شرکت در مسابقه دیگر",
                    onClick = onPlayAnother,
                    icon = Icons.Default.Replay,
                    modifier = Modifier.fillMaxWidth().testTag("result_play_again_btn")
                )

                GradientButton(
                    text = "بازگشت به صفحه اصلی",
                    onClick = onGoToHome,
                    icon = Icons.Default.Home,
                    gradient = Brush.horizontalGradient(listOf(CosmicCard, CosmicCard)),
                    textColor = Color.White,
                    modifier = Modifier.fillMaxWidth().testTag("result_home_btn")
                )
            }
        }
    }
}
