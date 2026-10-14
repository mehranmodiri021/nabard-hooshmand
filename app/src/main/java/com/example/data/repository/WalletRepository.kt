package com.example.data.repository

import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.WalletTransaction
import com.example.data.model.WithdrawalRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class WalletRepository {

    private val _balanceToman = MutableStateFlow(150000L) // Initial demo balance
    val balanceToman: StateFlow<Long> = _balanceToman.asStateFlow()

    private val _totalWonToman = MutableStateFlow(37500000L)
    val totalWonToman: StateFlow<Long> = _totalWonToman.asStateFlow()

    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    private val _withdrawals = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
    val withdrawals: StateFlow<List<WithdrawalRequest>> = _withdrawals.asStateFlow()

    init {
        _transactions.value = listOf(
            WalletTransaction(
                id = "tx_1",
                title = "پاداش قهرمانی مسابقه روزانه",
                amountToman = 1200000,
                type = TransactionType.PRIZE_WON,
                status = TransactionStatus.SUCCESS,
                datePersian = "۱۴۰۳/۰۷/۲۱ - ۱۶:۳۰",
                trackingCode = "TRX-849201"
            ),
            WalletTransaction(
                id = "tx_2",
                title = "هزینه ثبت‌نام جام ویژه سلاطین",
                amountToman = -30000,
                type = TransactionType.ENTRY_FEE,
                status = TransactionStatus.SUCCESS,
                datePersian = "۱۴۰۳/۰۷/۲۰ - ۲۱:۱۵",
                trackingCode = "TRX-721094"
            ),
            WalletTransaction(
                id = "tx_3",
                title = "افزایش اعتبار از درگاه شاپرک",
                amountToman = 100000,
                type = TransactionType.DEPOSIT,
                status = TransactionStatus.SUCCESS,
                datePersian = "۱۴۰۳/۰۷/۱۸ - ۱۱:۰۰",
                trackingCode = "SHP-394812"
            ),
            WalletTransaction(
                id = "tx_4",
                title = "برداشت وجه به شماره شبا IR82...",
                amountToman = -5000000,
                type = TransactionType.WITHDRAWAL,
                status = TransactionStatus.SUCCESS,
                datePersian = "۱۴۰۳/۰۷/۱۵ - ۰۹:۴۵",
                trackingCode = "PAY-194038"
            )
        )
    }

    fun deposit(amountToman: Long, trackingCode: String = "SHP-" + (100000..999999).random()): Boolean {
        if (amountToman <= 0) return false
        _balanceToman.value += amountToman

        val newTx = WalletTransaction(
            id = "tx_" + UUID.randomUUID().toString().take(6),
            title = "شارژ کیف پول از درگاه بانکی",
            amountToman = amountToman,
            type = TransactionType.DEPOSIT,
            status = TransactionStatus.SUCCESS,
            datePersian = getFormattedPersianDate(),
            trackingCode = trackingCode
        )
        _transactions.value = listOf(newTx) + _transactions.value
        return true
    }

    fun payEntryFee(amountToman: Long, tournamentTitle: String): Boolean {
        if (amountToman == 0L) return true
        if (_balanceToman.value < amountToman) return false

        _balanceToman.value -= amountToman
        val newTx = WalletTransaction(
            id = "tx_" + UUID.randomUUID().toString().take(6),
            title = "ورودی مسابقه: $tournamentTitle",
            amountToman = -amountToman,
            type = TransactionType.ENTRY_FEE,
            status = TransactionStatus.SUCCESS,
            datePersian = getFormattedPersianDate(),
            trackingCode = "ENT-" + (100000..999999).random()
        )
        _transactions.value = listOf(newTx) + _transactions.value
        return true
    }

    fun awardPrize(amountToman: Long, tournamentTitle: String) {
        if (amountToman <= 0) return
        _balanceToman.value += amountToman
        _totalWonToman.value += amountToman

        val newTx = WalletTransaction(
            id = "tx_" + UUID.randomUUID().toString().take(6),
            title = "جایزه پیروزی: $tournamentTitle",
            amountToman = amountToman,
            type = TransactionType.PRIZE_WON,
            status = TransactionStatus.SUCCESS,
            datePersian = getFormattedPersianDate(),
            trackingCode = "PRZ-" + (100000..999999).random()
        )
        _transactions.value = listOf(newTx) + _transactions.value
    }

    fun requestWithdrawal(
        amountToman: Long,
        shabaIban: String,
        cardNumber: String,
        accountHolder: String
    ): Boolean {
        if (amountToman <= 0 || _balanceToman.value < amountToman) return false

        _balanceToman.value -= amountToman

        val req = WithdrawalRequest(
            id = "wth_" + UUID.randomUUID().toString().take(6),
            amountToman = amountToman,
            shabaIban = shabaIban,
            cardNumber = cardNumber,
            accountHolder = accountHolder,
            status = TransactionStatus.PENDING,
            createdAtPersian = getFormattedPersianDate()
        )
        _withdrawals.value = listOf(req) + _withdrawals.value

        val newTx = WalletTransaction(
            id = "tx_" + req.id,
            title = "درخواست تسویه به شبا ($accountHolder)",
            amountToman = -amountToman,
            type = TransactionType.WITHDRAWAL,
            status = TransactionStatus.PENDING,
            datePersian = getFormattedPersianDate(),
            trackingCode = "WTH-" + (100000..999999).random()
        )
        _transactions.value = listOf(newTx) + _transactions.value
        return true
    }

    private fun getFormattedPersianDate(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "امروز - " + sdf.format(Date())
    }
}
