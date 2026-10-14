package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Tournament
import com.example.data.model.User
import com.example.ui.components.GlowCard
import com.example.ui.components.GradientButton
import com.example.ui.components.TopGameHeader
import com.example.ui.components.formatPersianInt
import com.example.ui.components.formatPersianNumber
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicCardElevated
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

@Composable
fun HomeScreen(
    user: User,
    balanceToman: Long,
    tournaments: List<Tournament>,
    onSelectTournament: (Tournament) -> Unit,
    onNavigateToTournaments: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val todayTournament = tournaments.find { it.id == "tour_today_01" } ?: tournaments.firstOrNull()
    val activeTournaments = tournaments.filter { it.currentParticipants > 0 }
    val specialTournaments = tournaments.filter { it.isHotOrSpecial }

    Column(modifier = Modifier.fillMaxSize()) {
        TopGameHeader(
            points = user.totalPoints,
            balanceToman = balanceToman,
            level = user.level,
            onWalletClick = onNavigateToWallet,
            onSettingsClick = onNavigateToSettings
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Banner Card
            item {
                HeroBannerSection(
                    todayTournament = todayTournament,
                    onJoin = { todayTournament?.let { onSelectTournament(it) } }
                )
            }

            // Quick Stats Bar (User Rank, Points, Wallet)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Rank Card
                    QuickStatCard(
                        title = "رتبه شما",
                        value = "#${formatPersianInt(user.globalRank)} کشوری",
                        icon = Icons.Default.Leaderboard,
                        accentColor = GoldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLeaderboard
                    )
                    // Points Card
                    QuickStatCard(
                        title = "امتیاز کل",
                        value = formatPersianNumber(user.totalPoints),
                        icon = Icons.Default.EmojiEvents,
                        accentColor = CyberCyan,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLeaderboard
                    )
                    // Wallet Card
                    QuickStatCard(
                        title = "کیف پول",
                        value = "${formatPersianNumber(balanceToman / 1000)}k ت",
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWallet
                    )
                }
            }

            // AI Smart Challenge Card
            item {
                AiBannerCard(onOpenAi = onNavigateToAi)
            }

            // Active Tournaments Carousel
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مسابقات فعال",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "مشاهده همه ➔",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onNavigateToTournaments() }
                            .padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(activeTournaments) { tour ->
                        CompactTournamentCard(
                            tournament = tour,
                            onJoin = { onSelectTournament(tour) }
                        )
                    }
                }
            }

            // Special & VIP Tournaments
            item {
                Text(
                    text = "مسابقات ویژه با جوایز میلیونی",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    specialTournaments.forEach { tour ->
                        SpecialTournamentRow(
                            tournament = tour,
                            onJoin = { onSelectTournament(tour) }
                        )
                    }
                }
            }

            // Top Cash Prizes Showcase
            item {
                PrizesShowcaseCard()
            }

            // Bottom Spacing for bottom bar
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun HeroBannerSection(
    todayTournament: Tournament?,
    onJoin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .shadow(12.dp, RoundedCornerShape(24.dp))
    ) {
        // Background Hero Image
        Image(
            painter = painterResource(id = R.drawable.hero_tournament_banner),
            contentDescription = "بنر مسابقات",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark Gradient Overlay for high readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0xFF090D16).copy(alpha = 0.7f),
                            Color(0xFF090D16).copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldPrimary)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "مسابقه بزرگ امروز 🔥",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = GoldLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "شروع تا ۳۰ دقیقه",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }

            // Bottom Title & Action
            Column {
                Text(
                    text = todayTournament?.title ?: "جام طلایی نبرد نخبگان",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "مجموع جایزه نقدی:",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${formatPersianNumber(todayTournament?.prizePoolToman ?: 5000000)} تومان",
                            color = GoldLight,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                    }

                    GradientButton(
                        text = "شرکت فوری",
                        onClick = onJoin,
                        icon = Icons.Default.PlayArrow,
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("hero_join_button")
                    )
                }
            }
        }
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CosmicCard)
            .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun AiBannerCard(onOpenAi: () -> Unit) {
    GlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenAi() },
        backgroundColor = CosmicCardElevated,
        borderColor = CyberPurple.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(CyberPurple, CyberCyan))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "هوش مصنوعی",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "طراح چالش با هوش مصنوعی",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "تولید مسابقه اختصاصی در زمینه دلخواه شما و تحلیل شانس برد",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberCyan.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "شروع ➔",
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CompactTournamentCard(
    tournament: Tournament,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, CosmicBorder, RoundedCornerShape(18.dp))
            .clickable { onJoin() },
        colors = CardDefaults.cardColors(containerColor = CosmicCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (tournament.isFree) EmeraldSuccess.copy(alpha = 0.2f) else GoldPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (tournament.isFree) "رایگان" else "${formatPersianNumber(tournament.entryFeeToman)} تومان",
                        color = if (tournament.isFree) EmeraldSuccess else GoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${formatPersianInt(tournament.currentParticipants)} نفر",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = tournament.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "جایزه: ${formatPersianNumber(tournament.prizePoolToman)} تومان",
                color = GoldLight,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(GoldPrimary, GoldDark)))
                    .clickable { onJoin() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ورود به مسابقه",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun SpecialTournamentRow(
    tournament: Tournament,
    onJoin: () -> Unit
) {
    GlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onJoin() },
        borderColor = GoldPrimary.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = tournament.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "جایزه: ",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${formatPersianNumber(tournament.prizePoolToman)} تومان",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ورودی: ${if (tournament.isFree) "رایگان" else formatPersianNumber(tournament.entryFeeToman) + " ت"}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldPrimary)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "شرکت",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PrizesShowcaseCard() {
    GlowCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = CosmicCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "جوایز نقدی فعال این ماه",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "بیش از ۳۵۰ میلیون تومان جوایز نقدی واریز مستقیم به حساب برندگان مسابقات هفتگی و ماهانه. تسویه فوری پس از اتمام مسابقه از طریق شماره شبا.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}
