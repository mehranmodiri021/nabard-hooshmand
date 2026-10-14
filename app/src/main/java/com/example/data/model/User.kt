package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Medal(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconEmoji: String = "",
    val isUnlocked: Boolean = false
)

data class User(
    @DocumentId val id: String = "u_001",
    val fullName: String = "سیدحمید موسوی زاده",
    val username: String = "@seyed_hamid",
    val email: String = "",
    val level: Int = 14,
    val xp: Int = 4320,
    val xpNeededNextLevel: Int = 5000,
    val totalPoints: Long = 18450,
    val globalRank: Int = 3,
    val tournamentsPlayed: Int = 42,
    val wins: Int = 29,
    val balanceToman: Long = 150000L,
    val totalCashWonToman: Long = 37500000L,
    val bio: String = "علاقه‌مند به چالش‌های فکری، ریاضی و فناوری. هدف: قهرمانی در تورنمنت بزرگ کشوری!",
    val phoneNumber: String = "۰۹۱۲۳۴۵۶۷۸۹",
    val role: String = "USER", // Role is verified securely from server (e.g. ADMIN or USER)
    val isAdmin: Boolean = false, // Server-enforced claim, creator name is NOT solely an admin permit
    val medals: List<Medal> = listOf(
        Medal("m1", "جام قهرمانی طلایی", "کسب رتبه ۱ در مسابقه بزرگ کشوری", "🏆", true),
        Medal("m2", "سرعت نور", "پاسخ صحیح به ۵ سوال در کمتر از ۳ ثانیه", "⚡", true),
        Medal("m3", "ذهن برتر", "پاسخ صحیح به تمام سوالات بدون اشتباه", "🧠", true),
        Medal("m4", "میلیونر چالش", "کسب بیش از ۲۵ میلیون تومان جایزه", "💰", true),
        Medal("m5", "استاد روزانه", "پیروزی در ۷ مسابقه روزانه متوالی", "🎯", true),
        Medal("m6", "اسطوره بی‌پایان", "شرکت در بیش از ۱۰۰ مسابقه", "👑", false)
    ),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val winRatePercent: Int get() = if (tournamentsPlayed > 0) ((wins.toFloat() / tournamentsPlayed) * 100).toInt() else 0
    val isSystemAdmin: Boolean get() = isAdmin || role.equals("ADMIN", ignoreCase = true)
}
