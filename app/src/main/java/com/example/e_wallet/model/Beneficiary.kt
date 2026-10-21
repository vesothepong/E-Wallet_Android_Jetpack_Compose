package com.example.e_wallet.model

data class Beneficiary(
    val beneficiaryId: String,
    val ownerUserId: String,
    val receiverUserId: String,
    val displayName: String,
    val walletNumber: String,
    val avatarInitials: String,
    val isFavorite: Boolean = true
)
