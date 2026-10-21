package com.example.e_wallet.model

enum class AccountStatus {
    ACTIVE,
    PENDING_VERIFICATION,
    SUSPENDED,
    BLOCKED
}

data class User(
    val userId: String,
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    val role: String = "DEMO_USER",
    val profilePhotoUrl: String? = null
)
