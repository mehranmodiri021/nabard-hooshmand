package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

enum class TransactionType(val labelFa: String) {
    DEPOSIT("شارژ کیف پول"),
    ENTRY_FEE("هزینه ثبت‌نام مسابقه"),
    PRIZE_WON("جایزه نقدی مسابقه"),
    WITHDRAWAL("تسویه و برداشت وجه")
}

enum class TransactionStatus(val labelFa: String) {
    SUCCESS("موفق"),
    PENDING("در حال پردازش"),
    FAILED("ناموفق")
}

data class WalletTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amountToman: Long = 0L,
    val type: TransactionType = TransactionType.DEPOSIT,
    val status: TransactionStatus = TransactionStatus.SUCCESS,
    val datePersian: String = "",
    val trackingCode: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class WithdrawalRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val amountToman: Long = 0L,
    val shabaIban: String = "",
    val cardNumber: String = "",
    val accountHolder: String = "",
    val status: TransactionStatus = TransactionStatus.PENDING,
    val createdAtPersian: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
