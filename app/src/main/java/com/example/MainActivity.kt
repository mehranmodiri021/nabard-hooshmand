package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AiChallengeScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainScaffold
import com.example.ui.screens.MatchResultScreen
import com.example.ui.screens.QuizMatchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.MainTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val settings by appViewModel.settings.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = settings.isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigationRoot(viewModel = appViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigationRoot(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val localUser by viewModel.userRepo.currentUser.collectAsStateWithLifecycle()
    val firestoreUser by viewModel.firestoreRepo.currentUser.collectAsStateWithLifecycle()
    val effectiveUser = firestoreUser ?: localUser

    val balanceToman by viewModel.walletRepo.balanceToman.collectAsStateWithLifecycle()
    val firestoreTournaments by viewModel.firestoreRepo.tournaments.collectAsStateWithLifecycle()
    val localTournaments by viewModel.tournamentRepo.tournaments.collectAsStateWithLifecycle()
    val effectiveTournaments = if (firestoreTournaments.isNotEmpty()) firestoreTournaments else localTournaments

    val firestoreTx by viewModel.firestoreRepo.transactions.collectAsStateWithLifecycle()
    val localTx by viewModel.walletRepo.transactions.collectAsStateWithLifecycle()
    val effectiveTx = if (firestoreTx.isNotEmpty()) firestoreTx else localTx

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedTimeframe by viewModel.leaderboardTimeframe.collectAsStateWithLifecycle()
    val matchState by viewModel.matchState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isAiGenerating by viewModel.aiGenerating.collectAsStateWithLifecycle()
    val aiAnalysisText by viewModel.aiPerformanceText.collectAsStateWithLifecycle()

    val adminUsers by viewModel.firestoreRepo.allUsers.collectAsStateWithLifecycle()
    val adminWithdrawals by viewModel.firestoreRepo.allWithdrawals.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        when (currentScreen) {
            AppScreen.SPLASH -> {
                SplashScreen(
                    onTimeout = {
                        if (viewModel.firestoreRepo.auth.currentUser == null) {
                            viewModel.navigateTo(AppScreen.AUTH)
                        } else {
                            viewModel.navigateTo(AppScreen.MAIN)
                        }
                    }
                )
            }

            AppScreen.AUTH -> {
                AuthScreen(
                    onAuthSuccess = { viewModel.navigateTo(AppScreen.MAIN) },
                    onSkipGuest = { viewModel.navigateTo(AppScreen.MAIN) }
                )
            }

            AppScreen.MAIN -> {
                MainScaffold(
                    currentTab = currentTab,
                    user = effectiveUser,
                    balanceToman = effectiveUser.balanceToman,
                    tournaments = effectiveTournaments,
                    transactions = effectiveTx,
                    selectedCategory = selectedCategory,
                    selectedTimeframe = selectedTimeframe,
                    onTabSelected = { viewModel.selectTab(it) },
                    onCategorySelected = { viewModel.selectTournamentCategory(it) },
                    onTimeframeSelected = { viewModel.selectLeaderboardTimeframe(it) },
                    onSelectTournament = { viewModel.startTournament(it) },
                    onDepositRequest = { amount ->
                        viewModel.setDepositAmount(amount)
                        viewModel.completeDeposit()
                    },
                    onSubmitWithdrawal = { amount, iban, card, name ->
                        viewModel.submitWithdrawal(amount, iban, card, name)
                    },
                    onUpdateProfile = { name, bio, phone ->
                        viewModel.updateProfile(name, bio, phone)
                    },
                    onNavigateToAi = { viewModel.navigateTo(AppScreen.AI_CHALLENGE) },
                    onNavigateToSettings = { viewModel.navigateTo(AppScreen.SETTINGS) }
                )
            }

            AppScreen.TOURNAMENT_MATCH -> {
                QuizMatchScreen(
                    matchState = matchState,
                    onAnswerSelected = { viewModel.onAnswerSelected(it) },
                    onQuitMatch = { viewModel.navigateTo(AppScreen.MAIN) }
                )
            }

            AppScreen.MATCH_RESULT -> {
                MatchResultScreen(
                    matchState = matchState,
                    onGoToWallet = {
                        viewModel.selectTab(MainTab.WALLET)
                        viewModel.navigateTo(AppScreen.MAIN)
                    },
                    onGoToHome = {
                        viewModel.selectTab(MainTab.HOME)
                        viewModel.navigateTo(AppScreen.MAIN)
                    },
                    onPlayAnother = {
                        viewModel.selectTab(MainTab.TOURNAMENTS)
                        viewModel.navigateTo(AppScreen.MAIN)
                    }
                )
            }

            AppScreen.AI_CHALLENGE -> {
                BackHandler {
                    viewModel.navigateTo(AppScreen.MAIN)
                }
                AiChallengeScreen(
                    isGenerating = isAiGenerating,
                    aiAnalysisText = aiAnalysisText,
                    onGenerateChallenge = { topic, onFinished ->
                        viewModel.generateAiChallenge(topic, onFinished)
                    },
                    onAnalyzePerformance = { viewModel.analyzePerformance() },
                    onPlayGeneratedTournament = { tournament ->
                        viewModel.startTournament(tournament)
                    },
                    onBack = { viewModel.navigateTo(AppScreen.MAIN) }
                )
            }

            AppScreen.SETTINGS -> {
                BackHandler {
                    viewModel.navigateTo(AppScreen.MAIN)
                }
                SettingsScreen(
                    settings = settings,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onToggleNotifications = { viewModel.toggleNotifications() },
                    onToggleSound = { viewModel.toggleSound() },
                    onBack = { viewModel.navigateTo(AppScreen.MAIN) },
                    onOpenAdminPanel = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) }
                )
            }

            AppScreen.ADMIN_PANEL -> {
                BackHandler {
                    viewModel.navigateTo(AppScreen.MAIN)
                }
                AdminPanelScreen(
                    currentUser = effectiveUser,
                    firestoreRepo = viewModel.firestoreRepo,
                    tournaments = effectiveTournaments,
                    users = adminUsers,
                    withdrawals = adminWithdrawals,
                    onBack = { viewModel.navigateTo(AppScreen.MAIN) }
                )
            }
        }
    }
}
