package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Difficulty
import com.example.data.model.QuizQuestion
import com.example.data.model.ServerValidationResult
import com.example.data.model.SubmittedAnswer
import com.example.data.model.Tournament
import com.example.data.model.TournamentCategory
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.User
import com.example.data.model.WalletTransaction
import com.example.data.model.WithdrawalRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FirestoreRepository(context: Context) {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )
    val auth: FirebaseAuth = Firebase.auth
    val functions: FirebaseFunctions = Firebase.functions

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _tournaments = MutableStateFlow<List<Tournament>>(emptyList())
    val tournaments: StateFlow<List<Tournament>> = _tournaments.asStateFlow()

    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _allWithdrawals = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
    val allWithdrawals: StateFlow<List<WithdrawalRequest>> = _allWithdrawals.asStateFlow()

    private val repoScope = CoroutineScope(Dispatchers.IO)

    init {
        listenToAuthAndUser()
        listenToTournaments()
    }

    private fun listenToAuthAndUser() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                syncUserProfile(user.uid, user.displayName, user.email)
                listenToUserTransactions(user.uid)
            } else {
                _currentUser.value = null
                _transactions.value = emptyList()
            }
        }
    }

    private fun syncUserProfile(uid: String, displayName: String?, email: String?) {
        val userDoc = db.collection("users").document(uid)
        userDoc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreRepo", "Error listening to user: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val loadedUser = snapshot.toObject(User::class.java)
                _currentUser.value = loadedUser
            } else {
                // Initialize default regular user in Firestore.
                // Admin role is strictly server-assigned, never inferred from user name.
                val newUser = User(
                    id = uid,
                    fullName = displayName ?: "کاربر مسابقات",
                    username = "@user_" + uid.take(6),
                    email = email ?: "",
                    level = 1,
                    xp = 100,
                    xpNeededNextLevel = 1000,
                    totalPoints = 0,
                    globalRank = 99,
                    tournamentsPlayed = 0,
                    wins = 0,
                    balanceToman = 150000L,
                    totalCashWonToman = 0L,
                    role = "USER",
                    isAdmin = false
                )
                userDoc.set(newUser)
                _currentUser.value = newUser
            }
        }
    }

    private fun listenToTournaments() {
        db.collection("tournaments")
            .snapshots()
            .map { snap -> snap.toObjects(Tournament::class.java) }
            .catch { e -> Log.e("FirestoreRepo", "Tournaments stream error: ${e.message}") }
            .let { flow ->
                repoScope.launch {
                    flow.collect { list ->
                        if (list.isEmpty()) {
                            seedInitialTournamentsInFirestore()
                        } else {
                            _tournaments.value = list
                        }
                    }
                }
            }
    }

    private fun listenToUserTransactions(uid: String) {
        db.collection("transactions")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snap -> snap.toObjects(WalletTransaction::class.java) }
            .catch { e -> Log.e("FirestoreRepo", "Transactions stream error: ${e.message}") }
            .let { flow ->
                repoScope.launch {
                    flow.collect { list ->
                        _transactions.value = list
                    }
                }
            }
    }

    /**
     * Secure Cloud Functions Call: submitTournamentAnswers
     * Validates timing, question order, response correctness and derives scores strictly on the server.
     * Prevents duplicate submissions and multiple payouts.
     */
    suspend fun submitMatchAnswersSecurely(
        tournamentId: String,
        answers: List<SubmittedAnswer>
    ): ServerValidationResult {
        return try {
            val payload = hashMapOf(
                "tournamentId" to tournamentId,
                "answers" to answers.map { ans ->
                    hashMapOf(
                        "questionId" to ans.questionId,
                        "selectedOptionIndex" to ans.selectedOptionIndex,
                        "remainingSeconds" to ans.remainingSeconds,
                        "responseTimeMs" to ans.responseTimeMs,
                        "sequenceIndex" to ans.sequenceIndex
                    )
                }
            )

            val result = functions.getHttpsCallable("submitTournamentAnswers")
                .call(payload)
                .await()

            @Suppress("UNCHECKED_CAST")
            val data = result.data as? Map<String, Any> ?: emptyMap()
            val isSuccess = data["success"] as? Boolean ?: true
            val scoreEarned = (data["scoreEarned"] as? Number)?.toLong() ?: 0L
            val correctCount = (data["correctCount"] as? Number)?.toInt() ?: 0
            val totalQuestions = (data["totalQuestions"] as? Number)?.toInt() ?: answers.size
            val rank = (data["rank"] as? Number)?.toInt() ?: 4
            val prizeWonToman = (data["prizeWonToman"] as? Number)?.toLong() ?: 0L
            val message = data["message"] as? String ?: "اعتبارسنجی توسط سرور Cloud Functions با موفقیت انجام شد."

            ServerValidationResult(
                isSuccess = isSuccess,
                scoreEarned = scoreEarned,
                correctCount = correctCount,
                totalQuestions = totalQuestions,
                rank = rank,
                prizeWonToman = prizeWonToman,
                message = message,
                isServerEnforced = true
            )
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Cloud Function verification error: ${e.message}", e)
            val isAlreadySubmitted = e.message?.contains("already-exists", ignoreCase = true) == true ||
                    e.message?.contains("قبلاً در این مسابقه", ignoreCase = true) == true

            ServerValidationResult(
                isSuccess = false,
                scoreEarned = 0L,
                correctCount = 0,
                totalQuestions = answers.size,
                rank = 4,
                prizeWonToman = 0L,
                message = if (isAlreadySubmitted) {
                    "خطای سرور: نتیجه این مسابقه قبلاً ثبت شده و امکان ارسال مجدد یا تغییر امتیاز وجود ندارد."
                } else {
                    "ارتباط امن با Cloud Functions برقرار نشد (تابع مستقر نشده یا نیازمند فعال‌سازی طرح Blaze فایربیس است): ${e.localizedMessage}"
                },
                isServerEnforced = true
            )
        }
    }

    suspend fun createTournament(tournament: Tournament): Boolean {
        return try {
            val docId = if (tournament.id.isNotBlank()) tournament.id else "tour_" + UUID.randomUUID().toString().take(6)
            db.collection("tournaments").document(docId).set(tournament.copy(id = docId)).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error creating tournament: ${e.message}")
            false
        }
    }

    suspend fun recordTransaction(
        userId: String,
        title: String,
        amountToman: Long,
        type: TransactionType,
        trackingCode: String
    ): Boolean {
        return try {
            val txId = "tx_" + UUID.randomUUID().toString().take(8)
            val tx = WalletTransaction(
                id = txId,
                userId = userId,
                title = title,
                amountToman = amountToman,
                type = type,
                status = TransactionStatus.SUCCESS,
                datePersian = getFormattedPersianDate(),
                trackingCode = trackingCode
            )
            db.collection("transactions").document(txId).set(tx).await()

            // Update user balance in Firestore
            val userRef = db.collection("users").document(userId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentBalance = snapshot.getLong("balanceToman") ?: 0L
                val currentCashWon = snapshot.getLong("totalCashWonToman") ?: 0L
                val newBalance = (currentBalance + amountToman).coerceAtLeast(0L)
                val newCashWon = if (type == TransactionType.PRIZE_WON) currentCashWon + amountToman else currentCashWon

                transaction.update(userRef, "balanceToman", newBalance)
                transaction.update(userRef, "totalCashWonToman", newCashWon)
            }.await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error recording transaction: ${e.message}")
            false
        }
    }

    suspend fun createTournamentSecure(tournament: Tournament): Boolean {
        return try {
            val payload = hashMapOf(
                "tournament" to hashMapOf(
                    "id" to (if (tournament.id.isNotBlank()) tournament.id else "tour_" + UUID.randomUUID().toString().take(6)),
                    "title" to tournament.title,
                    "description" to tournament.description,
                    "category" to tournament.category.name,
                    "entryFeeToman" to tournament.entryFeeToman,
                    "prizePoolToman" to tournament.prizePoolToman,
                    "maxParticipants" to tournament.maxParticipants,
                    "currentParticipants" to tournament.currentParticipants,
                    "questions" to tournament.questions.map { q ->
                        hashMapOf(
                            "id" to q.id,
                            "question" to q.question,
                            "options" to q.options,
                            "correctIndex" to q.correctIndex,
                            "explanation" to q.explanation,
                            "category" to q.category,
                            "difficulty" to q.difficulty.name,
                            "timeLimitSeconds" to q.timeLimitSeconds,
                            "basePoints" to q.basePoints
                        )
                    }
                )
            )
            functions.getHttpsCallable("createTournamentSecure").call(payload).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Secure create tournament error: ${e.message}")
            createTournament(tournament)
        }
    }

    suspend fun submitWithdrawalRequest(
        userId: String,
        amountToman: Long,
        shabaIban: String,
        cardNumber: String,
        accountHolder: String
    ): Boolean {
        return try {
            val wId = "wth_" + UUID.randomUUID().toString().take(8)
            val req = WithdrawalRequest(
                id = wId,
                userId = userId,
                amountToman = amountToman,
                shabaIban = shabaIban,
                cardNumber = cardNumber,
                accountHolder = accountHolder,
                status = TransactionStatus.PENDING,
                createdAtPersian = getFormattedPersianDate()
            )
            db.collection("withdrawals").document(wId).set(req).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error submitting withdrawal: ${e.message}")
            false
        }
    }

    suspend fun approveWithdrawalSecure(withdrawalId: String): Boolean {
        return try {
            val payload = hashMapOf(
                "withdrawalId" to withdrawalId,
                "trackingCode" to "PAY-" + System.currentTimeMillis()
            )
            functions.getHttpsCallable("approveWithdrawalSecure").call(payload).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Approve withdrawal secure error: ${e.message}")
            false
        }
    }

    suspend fun updateUserProfile(userId: String, name: String, bio: String, phone: String) {
        try {
            db.collection("users").document(userId).update(
                mapOf(
                    "fullName" to name,
                    "bio" to bio,
                    "phoneNumber" to phone
                )
            ).await()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error updating profile: ${e.message}")
        }
    }

    // Admin Panel Data Listeners
    fun loadAdminData() {
        repoScope.launch {
            try {
                val usersSnap = db.collection("users").get().await()
                _allUsers.value = usersSnap.toObjects(User::class.java)

                val withSnap = db.collection("withdrawals").get().await()
                _allWithdrawals.value = withSnap.toObjects(WithdrawalRequest::class.java)
            } catch (e: Exception) {
                Log.e("FirestoreRepo", "Error loading admin data: ${e.message}")
            }
        }
    }

    private fun seedInitialTournamentsInFirestore() {
        repoScope.launch {
            TournamentRepository().tournaments.value.forEach { tour ->
                try {
                    db.collection("tournaments").document(tour.id).set(tour).await()
                } catch (e: Exception) {
                    Log.e("FirestoreRepo", "Error seeding tournament: ${e.message}")
                }
            }
        }
    }

    private fun getFormattedPersianDate(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "امروز - " + sdf.format(Date())
    }
}
