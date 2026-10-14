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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardTimeframe
import com.example.data.model.Tournament
import com.example.data.model.TournamentCategory
import com.example.data.model.User
import com.example.data.model.WalletTransaction
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.MainTab

@Composable
fun MainScaffold(
    currentTab: MainTab,
    user: User,
    balanceToman: Long,
    tournaments: List<Tournament>,
    transactions: List<WalletTransaction>,
    selectedCategory: TournamentCategory,
    selectedTimeframe: LeaderboardTimeframe,
    onTabSelected: (MainTab) -> Unit,
    onCategorySelected: (TournamentCategory) -> Unit,
    onTimeframeSelected: (LeaderboardTimeframe) -> Unit,
    onSelectTournament: (Tournament) -> Unit,
    onDepositRequest: (Long) -> Unit,
    onSubmitWithdrawal: (Long, String, String, String) -> Boolean,
    onUpdateProfile: (String, String, String) -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            bottomBar = {
                BottomNavGameBar(
                    currentTab = currentTab,
                    onTabSelected = onTabSelected
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    MainTab.HOME -> {
                        HomeScreen(
                            user = user,
                            balanceToman = balanceToman,
                            tournaments = tournaments,
                            onSelectTournament = onSelectTournament,
                            onNavigateToTournaments = { onTabSelected(MainTab.TOURNAMENTS) },
                            onNavigateToWallet = { onTabSelected(MainTab.WALLET) },
                            onNavigateToLeaderboard = { onTabSelected(MainTab.LEADERBOARD) },
                            onNavigateToAi = onNavigateToAi,
                            onNavigateToSettings = onNavigateToSettings
                        )
                    }
                    MainTab.TOURNAMENTS -> {
                        TournamentsScreen(
                            tournaments = tournaments,
                            selectedCategory = selectedCategory,
                            userBalanceToman = balanceToman,
                            onCategorySelected = onCategorySelected,
                            onJoinTournament = onSelectTournament,
                            onNavigateToWallet = { onTabSelected(MainTab.WALLET) }
                        )
                    }
                    MainTab.LEADERBOARD -> {
                        LeaderboardScreen(
                            entries = com.example.data.repository.UserRepository().getLeaderboard(selectedTimeframe),
                            selectedTimeframe = selectedTimeframe,
                            onTimeframeSelected = onTimeframeSelected
                        )
                    }
                    MainTab.WALLET -> {
                        WalletScreen(
                            balanceToman = balanceToman,
                            totalWonToman = user.totalCashWonToman,
                            transactions = transactions,
                            onDepositRequest = onDepositRequest,
                            onSubmitWithdrawal = onSubmitWithdrawal
                        )
                    }
                    MainTab.PROFILE -> {
                        ProfileScreen(
                            user = user,
                            onUpdateProfile = onUpdateProfile,
                            onNavigateToSettings = onNavigateToSettings
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavGameBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .shadow(16.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(CosmicCard)
            .border(1.dp, CosmicBorder, RoundedCornerShape(22.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTabItem(
                title = "خانه",
                selected = currentTab == MainTab.HOME,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                onClick = { onTabSelected(MainTab.HOME) },
                testTag = "nav_tab_home"
            )
            NavTabItem(
                title = "مسابقات",
                selected = currentTab == MainTab.TOURNAMENTS,
                selectedIcon = Icons.Filled.EmojiEvents,
                unselectedIcon = Icons.Outlined.EmojiEvents,
                onClick = { onTabSelected(MainTab.TOURNAMENTS) },
                testTag = "nav_tab_tournaments"
            )
            NavTabItem(
                title = "رتبه‌بندی",
                selected = currentTab == MainTab.LEADERBOARD,
                selectedIcon = Icons.Filled.Leaderboard,
                unselectedIcon = Icons.Outlined.Leaderboard,
                onClick = { onTabSelected(MainTab.LEADERBOARD) },
                testTag = "nav_tab_leaderboard"
            )
            NavTabItem(
                title = "کیف پول",
                selected = currentTab == MainTab.WALLET,
                selectedIcon = Icons.Filled.AccountBalanceWallet,
                unselectedIcon = Icons.Outlined.AccountBalanceWallet,
                onClick = { onTabSelected(MainTab.WALLET) },
                testTag = "nav_tab_wallet"
            )
            NavTabItem(
                title = "پروفایل",
                selected = currentTab == MainTab.PROFILE,
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                onClick = { onTabSelected(MainTab.PROFILE) },
                testTag = "nav_tab_profile"
            )
        }
    }
}

@Composable
fun NavTabItem(
    title: String,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = GoldPrimary
    val inactiveColor = Color(0xFF94A3B8)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (selected) activeColor.copy(alpha = 0.15f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = title,
                tint = if (selected) activeColor else inactiveColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            color = if (selected) activeColor else inactiveColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp
        )
    }
}
