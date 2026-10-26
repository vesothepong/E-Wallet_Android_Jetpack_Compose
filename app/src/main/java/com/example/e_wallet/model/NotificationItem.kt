package com.example.e_wallet.model

enum class NotificationType(val displayName: String) {
    TRANSACTION("Transactions"),
    SECURITY("Security"),
    PROMOTION("Promotions"),
    SYSTEM("System")
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType,
    val isRead: Boolean = false,
    val amountCents: Long? = null,
    val transactionId: String? = null
)
