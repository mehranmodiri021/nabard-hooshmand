package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LeaderboardTimeframe
import com.example.ui.components.GlowCard
import com.example.ui.components.formatPersianInt
import com.example.ui.components.formatPersianNumber
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicCardElevated
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

@Composable
fun LeaderboardScreen(
    entries: List<LeaderboardEntry>,
    selectedTimeframe: LeaderboardTimeframe,
    onTimeframeSelected: (LeaderboardTimeframe) -> Unit
) {
    val top1 = entries.find { it.rank == 1 }
    val top2 = entries.find { it.rank == 2 }
    val top3 = entries.find { it.rank == 3 }
    val remainingEntries = entries.filter { it.rank > 3 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Title
        Text(
            text = "جدول رتبه‌بندی قهرمانان",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Timeframe Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LeaderboardTimeframe.values().forEach { timeframe ->
                val isSelected = timeframe == selectedTimeframe
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) GoldPrimary else CosmicCard)
                        .border(1.dp, if (isSelected) GoldLight else CosmicBorder, RoundedCornerShape(12.dp))
                        .clickable { onTimeframeSelected(timeframe) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timeframe.labelFa,
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Podium (Top 3)
            item {
                PodiumSection(top1 = top1, top2 = top2, top3 = top3)
            }

            // Current User Highlight Badge
            item {
                val currentUser = entries.find { it.isCurrentUser }
                if (currentUser != null) {
                    GlowCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = CosmicCardElevated,
                        borderColor = GoldPrimary
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${formatPersianInt(currentUser.rank)}",
                                        color = Color.Black,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${currentUser.name} (شما)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${formatPersianInt(currentUser.wins)} پیروزی • برد: ${formatPersianInt(currentUser.winRatePercent)}٪",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Text(
                                text = "${formatPersianNumber(currentUser.points)} امتیاز",
                                color = GoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Other Rankings List
            items(remainingEntries) { entry ->
                LeaderboardRow(entry = entry)
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun PodiumSection(
    top1: LeaderboardEntry?,
    top2: LeaderboardEntry?,
    top3: LeaderboardEntry?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd Place (Silver)
        top2?.let {
            PodiumColumn(
                entry = it,
                rankText = "۲",
                badgeColor = Color(0xFFCBD5E1),
                podiumHeight = 110.dp,
                crown = "🥈"
            )
        }

        // 1st Place (Gold)
        top1?.let {
            PodiumColumn(
                entry = it,
                rankText = "۱",
                badgeColor = GoldPrimary,
                podiumHeight = 140.dp,
                crown = "👑"
            )
        }

        // 3rd Place (Bronze)
        top3?.let {
            PodiumColumn(
                entry = it,
                rankText = "۳",
                badgeColor = Color(0xFFD97706),
                podiumHeight = 90.dp,
                crown = "🥉"
            )
        }
    }
}

@Composable
fun PodiumColumn(
    entry: LeaderboardEntry,
    rankText: String,
    badgeColor: Color,
    podiumHeight: androidx.compose.ui.unit.Dp,
    crown: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        // Crown & Avatar
        Text(text = crown, fontSize = 22.sp)
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(CosmicCard)
                .border(2.dp, badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = entry.avatarEmoji, fontSize = 26.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = entry.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Text(
            text = "${formatPersianNumber(entry.points)}",
            color = GoldLight,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pedestal
        Box(
            modifier = Modifier
                .width(82.dp)
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(badgeColor.copy(alpha = 0.35f), CosmicCardElevated)
                    )
                )
                .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rankText,
                color = badgeColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            )
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    GlowCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (entry.isCurrentUser) CosmicCardElevated else CosmicCard,
        borderColor = if (entry.isCurrentUser) GoldPrimary else CosmicBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank number
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF334155)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatPersianInt(entry.rank),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Avatar
                Text(text = entry.avatarEmoji, fontSize = 22.sp)

                Spacer(modifier = Modifier.width(10.dp))

                // Name & Stats
                Column {
                    Text(
                        text = entry.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatPersianInt(entry.wins)} برد • نرخ پیروزی ${formatPersianInt(entry.winRatePercent)}٪",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatPersianNumber(entry.points)}",
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "امتیاز",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }
        }
    }
}
