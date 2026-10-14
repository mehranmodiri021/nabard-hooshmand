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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Tournament
import com.example.data.model.TournamentCategory
import com.example.ui.components.GlowCard
import com.example.ui.components.GradientButton
import com.example.ui.components.formatPersianInt
import com.example.ui.components.formatPersianNumber
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

@Composable
fun TournamentsScreen(
    tournaments: List<Tournament>,
    selectedCategory: TournamentCategory,
    userBalanceToman: Long,
    onCategorySelected: (TournamentCategory) -> Unit,
    onJoinTournament: (Tournament) -> Unit,
    onNavigateToWallet: () -> Unit
) {
    var tournamentToConfirm by remember { mutableStateOf<Tournament?>(null) }
    var showInsufficientBalanceDialog by remember { mutableStateOf(false) }

    val filteredList = remember(tournaments, selectedCategory) {
        if (selectedCategory == TournamentCategory.ALL) {
            tournaments
        } else {
            tournaments.filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Title
        Text(
            text = "مسابقات و چالش‌های فعال",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Categories Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TournamentCategory.values()) { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) GoldPrimary else CosmicCard)
                        .border(
                            1.dp,
                            if (isSelected) GoldLight else CosmicBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onCategorySelected(cat) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat.titleFa,
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tournament Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredList) { tour ->
                TournamentCard(
                    tournament = tour,
                    onStartClick = {
                        if (tour.entryFeeToman > userBalanceToman) {
                            showInsufficientBalanceDialog = true
                        } else {
                            tournamentToConfirm = tour
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Join Confirmation Dialog
    tournamentToConfirm?.let { tour ->
        AlertDialog(
            onDismissRequest = { tournamentToConfirm = null },
            title = {
                Text(
                    text = "شرکت در ${tour.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = tour.description,
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "هزینه ورودی:")
                        Text(
                            text = if (tour.isFree) "رایگان" else "${formatPersianNumber(tour.entryFeeToman)} تومان",
                            color = if (tour.isFree) EmeraldSuccess else GoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "مجموع جایزه:")
                        Text(
                            text = "${formatPersianNumber(tour.prizePoolToman)} تومان",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val selected = tour
                        tournamentToConfirm = null
                        onJoinTournament(selected)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(text = "تایید و ورود به بازی", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { tournamentToConfirm = null }) {
                    Text(text = "انصراف")
                }
            }
        )
    }

    // Insufficient Balance Dialog
    if (showInsufficientBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showInsufficientBalanceDialog = false },
            title = { Text(text = "موجودی ناکافی کیف پول", fontWeight = FontWeight.Bold) },
            text = {
                Text(text = "موجودی فعلی شما کمتر از مبلغ ورودی این مسابقه است. برای شرکت، لطفاً ابتدا کیف پول خود را شارژ کنید.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInsufficientBalanceDialog = false
                        onNavigateToWallet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(text = "شارژ کیف پول", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showInsufficientBalanceDialog = false }) {
                    Text(text = "انصراف")
                }
            }
        )
    }
}

@Composable
fun TournamentCard(
    tournament: Tournament,
    onStartClick: () -> Unit
) {
    val progress = tournament.currentParticipants.toFloat() / tournament.maxParticipants.coerceAtLeast(1)

    GlowCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (tournament.isHotOrSpecial) GoldPrimary.copy(alpha = 0.5f) else CosmicBorder
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Badge + Difficulty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tournament.category.titleFa,
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    if (tournament.isHotOrSpecial) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDC2626).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ویژه 🔥",
                                color = Color(0xFFF87171),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Text(
                    text = "سختی: ${tournament.difficulty.labelFa}",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Description
            Text(
                text = tournament.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tournament.description,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stats Bar (Prize Pool + Entry Fee)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CosmicCard)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = GoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مجموع جایزه نقدی:",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatPersianNumber(tournament.prizePoolToman)} تومان",
                        color = GoldLight,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Paid,
                            contentDescription = null,
                            tint = if (tournament.isFree) EmeraldSuccess else CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مبلغ ورود:",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (tournament.isFree) "رایگان" else "${formatPersianNumber(tournament.entryFeeToman)} تومان",
                        color = if (tournament.isFree) EmeraldSuccess else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Participants Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ظرفیت: ${formatPersianInt(tournament.currentParticipants)} از ${formatPersianInt(tournament.maxParticipants)} نفر",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "آماده شروع",
                        color = CyberCyan,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = GoldPrimary,
                trackColor = Color(0xFF334155),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            GradientButton(
                text = "ثبت‌نام و شرکت در مسابقه",
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("join_tournament_btn_${tournament.id}")
            )
        }
    }
}
