package com.example.e_wallet.model

enum class CardType(val displayName: String) {
    VIRTUAL_DEBIT("Virtual Debit"),
    PHYSICAL_DEBIT("Physical Debit"),
    CREDIT("Credit")
}

enum class CardNetwork(val displayName: String) {
    VISA("Visa"),
    MASTERCARD("Mastercard")
}

data class PaymentCard(
    val cardId: String,
    val cardholderName: String,
    val cardNumberMasked: String, // "•••• •••• •••• 9924"
    val fullCardNumber: String,   // "4812 9012 3456 9924"
    val expiryDate: String,       // "08/29"
    val cvv: String,              // "742"
    val cardType: CardType = CardType.VIRTUAL_DEBIT,
    val cardNetwork: CardNetwork = CardNetwork.VISA,
    val isFrozen: Boolean = false,
    val isPrimary: Boolean = true,
    val isOnlinePaymentEnabled: Boolean = true,
    val isInternationalEnabled: Boolean = false,
    val monthlyLimitCents: Long = 300000L, // $3,000.00
    val monthlySpentCents: Long = 85000L,  // $850.00
    val gradientStartColorHex: Long = 0xFF4338CA, // Indigo
    val gradientEndColorHex: Long = 0xFF7E22CE    // Purple
)

data class BankAccount(
    val accountId: String,
    val bankName: String,
    val accountType: String, // "Checking", "Savings"
    val accountNumberMasked: String, // "•••• 4812"
    val routingNumberMasked: String = "•••• 0210",
    val isPrimary: Boolean = false,
    val balanceCents: Long = 1425000L // $14,250.00
)
