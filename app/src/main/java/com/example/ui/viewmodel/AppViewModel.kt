package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppSettings
import com.example.data.model.LeaderboardTimeframe
import com.example.data.model.QuizQuestion
import com.example.data.model.SubmittedAnswer
import com.example.data.model.Tournament
import com.example.data.model.TournamentCategory
import com.example.data.model.TransactionType
import com.example.data.model.User
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.TournamentRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.WalletRepository
import com.example.data.security.AntiCheatEngine
import com.example.data.service.GeminiAiService
import com.example.data.service.PaymentGatewayService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    AUTH,
    MAIN,
    TOURNAMENT_MATCH,
    MATCH_RESULT,
    AI_CHALLENGE,
    SETTINGS,
    ADMIN_PANEL
}

enum class MainTab {
    HOME,
    TOURNAMENTS,
    LEADERBOARD,
    WALLET,
    PROFILE
}

data class MatchState(
    val tournament: Tournament? = null,
    val currentQuestionIndex: Int = 0,
    val remainingSeconds: Int = 15,
    val selectedOptionIndex: Int? = null,
    val isAnswerConfirmed: Boolean = false,
    val isAnswerCorrect: Boolean = false,
    val scoreEarned: Long = 0,
    val correctAnswersCount: Int = 0,
    val comboStreak: Int = 0,
    val isFinished: Boolean = false,
    val rankInRoom: Int = 1,
    val prizeWonToman: Long = 0,
    val submittedAnswers: List<SubmittedAnswer> = emptyList(),
    val isSubmittingToServer: Boolean = false,
    val serverStatusMessage: String = "",
    val isServerVerified: Boolean = false
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val firestoreRepo = FirestoreRepository(application)
    val tournamentRepo = TournamentRepository()
    val walletRepo = WalletRepository()
    val userRepo = UserRepository()
    val aiService = GeminiAiService()
    val paymentGatewayService = PaymentGatewayService()

    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow(TournamentCategory.ALL)
    val selectedCategory: StateFlow<TournamentCategory> = _selectedCategory.asStateFlow()

    private val _leaderboardTimeframe = MutableStateFlow(LeaderboardTimeframe.WEEKLY)
    val leaderboardTimeframe: StateFlow<LeaderboardTimeframe> = _leaderboardTimeframe.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _matchState = MutableStateFlow(MatchState())
    val matchState: StateFlow<MatchState> = _matchState.asStateFlow()

    private val _depositAmount = MutableStateFlow(50000L)
    val depositAmount: StateFlow<Long> = _depositAmount.asStateFlow()

    private val _aiGenerating = MutableStateFlow(false)
    val aiGenerating: StateFlow<Boolean> = _aiGenerating.asStateFlow()

    private val _aiPerformanceText = MutableStateFlow("")
    val aiPerformanceText: StateFlow<String> = _aiPerformanceText.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Splash auto transition after 3 seconds
        viewModelScope.launch {
            delay(3000)
            if (_currentScreen.value == AppScreen.SPLASH) {
                if (firestoreRepo.auth.currentUser == null) {
                    _currentScreen.value = AppScreen.AUTH
                } else {
                    _currentScreen.value = AppScreen.MAIN
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        if (_currentScreen.value != AppScreen.MAIN) {
            _currentScreen.value = AppScreen.MAIN
        }
    }

    fun selectTournamentCategory(category: TournamentCategory) {
        _selectedCategory.value = category
    }

    fun selectLeaderboardTimeframe(timeframe: LeaderboardTimeframe) {
        _leaderboardTimeframe.value = timeframe
    }

    // Active User from Firestore or local fallback
    fun getEffectiveUser(): User {
        return firestoreRepo.currentUser.value ?: userRepo.currentUser.value
    }

    // --- Tournament Gameplay Engine & Anti-Cheat ---
    fun startTournament(tournament: Tournament): Boolean {
        val user = getEffectiveUser()
        if (tournament.entryFeeToman > 0) {
            if (user.balanceToman < tournament.entryFeeToman) return false
            viewModelScope.launch {
                firestoreRepo.recordTransaction(
                    userId = user.id,
                    title = "ورودی مسابقه: ${tournament.title}",
                    amountToman = -tournament.entryFeeToman,
                    type = TransactionType.ENTRY_FEE,
                    trackingCode = "ENT-" + (100000..999999).random()
                )
            }
            walletRepo.payEntryFee(tournament.entryFeeToman, tournament.title)
        }

        _matchState.value = MatchState(
            tournament = tournament,
            currentQuestionIndex = 0,
            remainingSeconds = tournament.questions.firstOrNull()?.timeLimitSeconds ?: 15,
            scoreEarned = 0,
            correctAnswersCount = 0,
            comboStreak = 0,
            isFinished = false,
            submittedAnswers = emptyList(),
            isSubmittingToServer = false,
            serverStatusMessage = "",
            isServerVerified = false
        )
        _currentScreen.value = AppScreen.TOURNAMENT_MATCH
        AntiCheatEngine.onQuestionDisplayed()
        startQuestionTimer()
        return true
    }

    private fun startQuestionTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_matchState.value.remainingSeconds > 0 && !_matchState.value.isAnswerConfirmed) {
                delay(1000)
                val current = _matchState.value
                if (current.remainingSeconds > 0) {
                    _matchState.value = current.copy(remainingSeconds = current.remainingSeconds - 1)
                }
            }
            if (!_matchState.value.isAnswerConfirmed) {
                onAnswerSelected(-1)
            }
        }
    }

    fun onAnswerSelected(optionIndex: Int) {
        timerJob?.cancel()
        val current = _matchState.value
        val tournament = current.tournament ?: return
        val question = tournament.questions.getOrNull(current.currentQuestionIndex) ?: return

        val elapsedMs = AntiCheatEngine.getElapsedResponseTimeMs()
        val submittedAnswer = SubmittedAnswer(
            questionId = question.id,
            selectedOptionIndex = optionIndex,
            remainingSeconds = current.remainingSeconds,
            responseTimeMs = elapsedMs,
            sequenceIndex = current.currentQuestionIndex
        )
        val updatedAnswers = current.submittedAnswers + submittedAnswer

        val isCorrect = optionIndex == question.correctIndex
        val validation = AntiCheatEngine.validateAnswerSubmission(
            basePoints = question.basePoints,
            difficultyMultiplier = question.difficulty.multiplier,
            remainingSeconds = current.remainingSeconds,
            timeLimitSeconds = question.timeLimitSeconds,
            streak = current.comboStreak,
            isCorrect = isCorrect
        )

        val addedScore = if (validation.isValid) validation.calculatedScore else 0L
        val streak = if (isCorrect && validation.isValid) current.comboStreak + 1 else 0

        _matchState.value = current.copy(
            selectedOptionIndex = optionIndex,
            isAnswerConfirmed = true,
            isAnswerCorrect = isCorrect && validation.isValid,
            scoreEarned = current.scoreEarned + addedScore,
            correctAnswersCount = if (isCorrect && validation.isValid) current.correctAnswersCount + 1 else current.correctAnswersCount,
            comboStreak = streak,
            submittedAnswers = updatedAnswers
        )

        viewModelScope.launch {
            delay(1600)
            val nextIndex = current.currentQuestionIndex + 1
            if (nextIndex < tournament.questions.size) {
                val nextQ = tournament.questions[nextIndex]
                _matchState.value = _matchState.value.copy(
                    currentQuestionIndex = nextIndex,
                    remainingSeconds = nextQ.timeLimitSeconds,
                    selectedOptionIndex = null,
                    isAnswerConfirmed = false,
                    isAnswerCorrect = false
                )
                AntiCheatEngine.onQuestionDisplayed()
                startQuestionTimer()
            } else {
                finishMatch()
            }
        }
    }

    private fun finishMatch() {
        val state = _matchState.value
        val tournament = state.tournament ?: return

        _matchState.value = state.copy(
            isSubmittingToServer = true,
            serverStatusMessage = "در حال ارسال و اعتبارسنجی امن در سرور Cloud Functions..."
        )
        _currentScreen.value = AppScreen.MATCH_RESULT

        viewModelScope.launch {
            val serverResult = firestoreRepo.submitMatchAnswersSecurely(
                tournamentId = tournament.id,
                answers = state.submittedAnswers
            )

            if (serverResult.isSuccess && serverResult.scoreEarned > 0) {
                // Server calculated everything and updated Firestore ledger atomically
                _matchState.value = _matchState.value.copy(
                    isFinished = true,
                    isSubmittingToServer = false,
                    isServerVerified = true,
                    scoreEarned = serverResult.scoreEarned,
                    correctAnswersCount = serverResult.correctCount,
                    rankInRoom = serverResult.rank,
                    prizeWonToman = serverResult.prizeWonToman,
                    serverStatusMessage = serverResult.message
                )

                if (serverResult.prizeWonToman > 0) {
                    walletRepo.awardPrize(serverResult.prizeWonToman, tournament.title)
                }
                userRepo.addMatchResults(
                    pointsEarned = serverResult.scoreEarned,
                    isWin = serverResult.rank <= 3,
                    cashEarned = serverResult.prizeWonToman
                )
            } else {
                // Server rejected or reported error / duplicate / function not active yet
                val accuracy = if (tournament.questions.isNotEmpty()) (state.correctAnswersCount.toFloat() / tournament.questions.size) else 0f
                val fallbackRank = when {
                    accuracy >= 0.85f -> 1
                    accuracy >= 0.70f -> 2
                    accuracy >= 0.50f -> 3
                    else -> 4
                }
                _matchState.value = _matchState.value.copy(
                    isFinished = true,
                    isSubmittingToServer = false,
                    isServerVerified = false,
                    rankInRoom = fallbackRank,
                    serverStatusMessage = serverResult.message
                )
            }
        }
    }

    // --- Wallet & Real Payment Gateway ---
    fun setDepositAmount(amount: Long) {
        _depositAmount.value = amount
    }

    fun completeDeposit(): Boolean {
        val user = getEffectiveUser()
        val amount = _depositAmount.value
        viewModelScope.launch {
            val authority = paymentGatewayService.initiatePayment(
                com.example.data.service.PaymentRequest(
                    amountToman = amount,
                    description = "شارژ کیف پول کاربری چالش و مسابقه"
                )
            )
            val result = paymentGatewayService.verifyPayment(authority, amount, "6037-9975-1234-5678")
            if (result.isSuccess) {
                firestoreRepo.recordTransaction(
                    userId = user.id,
                    title = "شارژ از درگاه شاپرک (${result.referenceId.take(6)})",
                    amountToman = amount,
                    type = TransactionType.DEPOSIT,
                    trackingCode = result.trackingCode
                )
            }
        }
        walletRepo.deposit(amount)
        _currentScreen.value = AppScreen.MAIN
        _currentTab.value = MainTab.WALLET
        return true
    }

    fun submitWithdrawal(amount: Long, iban: String, card: String, name: String): Boolean {
        val user = getEffectiveUser()
        viewModelScope.launch {
            firestoreRepo.submitWithdrawalRequest(
                userId = user.id,
                amountToman = amount,
                shabaIban = iban,
                cardNumber = card,
                accountHolder = name
            )
        }
        val success = walletRepo.requestWithdrawal(amount, iban, card, name)
        if (success) {
            _currentScreen.value = AppScreen.MAIN
            _currentTab.value = MainTab.WALLET
        }
        return success
    }

    // --- AI Generator ---
    fun generateAiChallenge(topic: String, onFinished: (Tournament) -> Unit) {
        viewModelScope.launch {
            _aiGenerating.value = true
            val questions = aiService.generateQuizQuestions(topic, count = 4)
            val newTournament = tournamentRepo.addCustomAiTournament(
                title = "چالش هوش مصنوعی: $topic",
                category = topic,
                questions = questions
            )
            firestoreRepo.createTournament(newTournament)
            _aiGenerating.value = false
            onFinished(newTournament)
        }
    }

    fun analyzePerformance() {
        val user = getEffectiveUser()
        viewModelScope.launch {
            _aiGenerating.value = true
            val analysis = aiService.analyzeUserPerformance(
                wins = user.wins,
                totalPlayed = user.tournamentsPlayed,
                totalPoints = user.totalPoints,
                winRate = user.winRatePercent
            )
            _aiPerformanceText.value = analysis
            _aiGenerating.value = false
        }
    }

    // --- Settings & Profile ---
    fun toggleDarkMode() {
        _settings.value = _settings.value.copy(isDarkMode = !_settings.value.isDarkMode)
    }

    fun toggleNotifications() {
        _settings.value = _settings.value.copy(notificationsEnabled = !_settings.value.notificationsEnabled)
    }

    fun toggleSound() {
        _settings.value = _settings.value.copy(soundEffectsEnabled = !_settings.value.soundEffectsEnabled)
    }

    fun updateProfile(name: String, bio: String, phone: String) {
        val user = getEffectiveUser()
        viewModelScope.launch {
            firestoreRepo.updateUserProfile(user.id, name, bio, phone)
        }
        userRepo.updateProfile(name, bio, phone)
    }

    fun signOut() {
        firestoreRepo.auth.signOut()
        _currentScreen.value = AppScreen.AUTH
    }
}
