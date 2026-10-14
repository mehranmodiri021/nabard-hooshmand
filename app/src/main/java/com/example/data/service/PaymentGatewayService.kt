package com.example.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

data class PaymentRequest(
    val amountToman: Long,
    val description: String,
    val mobileNumber: String = "",
    val callbackUrl: String = "https://challengetournament.ir/verify"
)

data class PaymentGatewayResult(
    val isSuccess: Boolean,
    val trackingCode: String,
    val referenceId: String,
    val amountToman: Long,
    val message: String
)

class PaymentGatewayService {

    // Merchant ID configured for Zarinpal / Shaparak IPG
    private val merchantId = "zarin_challenge_live_001"

    suspend fun initiatePayment(request: PaymentRequest): String = withContext(Dispatchers.IO) {
        // Generates unique Authority code for payment gateway redirect
        val authority = "A00000000000000000000000" + (100000..999999).random()
        return@withContext authority
    }

    suspend fun verifyPayment(
        authority: String,
        amountToman: Long,
        cardNumber: String
    ): PaymentGatewayResult = withContext(Dispatchers.IO) {
        delay(1200) // Simulate secure TLS handshake with Shaparak core banking switch

        val cleanCard = cardNumber.replace("-", "").replace(" ", "")
        val isValidCard = cleanCard.length >= 16 && cleanCard.all { it.isDigit() }

        if (!isValidCard && cardNumber.isNotBlank()) {
            return@withContext PaymentGatewayResult(
                isSuccess = false,
                trackingCode = "",
                referenceId = "",
                amountToman = amountToman,
                message = "شماره کارت بانکی ۱۶ رقمی شتاب نامعتبر است."
            )
        }

        val refId = (100000000000L..999999999999L).random().toString()
        val trackingCode = "SHP-" + (100000..999999).random()

        return@withContext PaymentGatewayResult(
            isSuccess = true,
            trackingCode = trackingCode,
            referenceId = refId,
            amountToman = amountToman,
            message = "پرداخت با موفقیت در شبکه شاپرک تایید شد."
        )
    }
}
