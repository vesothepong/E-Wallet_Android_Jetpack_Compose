package com.example.e_wallet.model

data class Wallet(
    val walletId: String,
    val walletNumber: String,
    val userId: String,
    val currentBalance: Long, // represented in cents (e.g. 245000 = $2,450.00)
    val availableBalance: Long,
    val currency: String = "USD"
)
