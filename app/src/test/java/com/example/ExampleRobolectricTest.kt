package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.repository.TournamentRepository
import com.example.data.repository.WalletRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("چالش و مسابقه", appName)
    }

    @Test
    fun `test initial tournaments count and wallet balance`() {
        val tournamentRepo = TournamentRepository()
        val walletRepo = WalletRepository()

        assertTrue(tournamentRepo.tournaments.value.isNotEmpty())
        assertEquals(150000L, walletRepo.balanceToman.value)
    }

    @Test
    fun `test wallet deposit increases balance and creates transaction`() {
        val walletRepo = WalletRepository()
        val initialBalance = walletRepo.balanceToman.value
        val initialTxCount = walletRepo.transactions.value.size

        val success = walletRepo.deposit(50000L)
        assertTrue(success)
        assertEquals(initialBalance + 50000L, walletRepo.balanceToman.value)
        assertEquals(initialTxCount + 1, walletRepo.transactions.value.size)

        val latestTx = walletRepo.transactions.value.first()
        assertEquals(TransactionType.DEPOSIT, latestTx.type)
        assertEquals(50000L, latestTx.amountToman)
        assertEquals(TransactionStatus.SUCCESS, latestTx.status)
    }

    @Test
    fun `test wallet withdrawal requests validation and deduction`() {
        val walletRepo = WalletRepository()
        val initialBalance = walletRepo.balanceToman.value

        // Excessive amount should fail
        val excessiveResult = walletRepo.requestWithdrawal(
            amountToman = initialBalance + 100000L,
            shabaIban = "IR820170000000123456789012",
            cardNumber = "6037-9975-1234-5678",
            accountHolder = "سیدحمید موسوی زاده"
        )
        assertFalse(excessiveResult)
        assertEquals(initialBalance, walletRepo.balanceToman.value)

        // Valid withdrawal should succeed
        val validResult = walletRepo.requestWithdrawal(
            amountToman = 50000L,
            shabaIban = "IR820170000000123456789012",
            cardNumber = "6037-9975-1234-5678",
            accountHolder = "سیدحمید موسوی زاده"
        )
        assertTrue(validResult)
        assertEquals(initialBalance - 50000L, walletRepo.balanceToman.value)
        assertEquals(1, walletRepo.withdrawals.value.size)
        assertEquals(TransactionStatus.PENDING, walletRepo.withdrawals.value.first().status)
    }

    @Test
    fun `test prize award credits balance and updates total won`() {
        val walletRepo = WalletRepository()
        val initialBalance = walletRepo.balanceToman.value
        val initialTotalWon = walletRepo.totalWonToman.value

        walletRepo.awardPrize(2500000L, "مسابقه بزرگ کشوری")

        assertEquals(initialBalance + 2500000L, walletRepo.balanceToman.value)
        assertEquals(initialTotalWon + 2500000L, walletRepo.totalWonToman.value)
    }
}
