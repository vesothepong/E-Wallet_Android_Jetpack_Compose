package com.example.e_wallet.model

enum class TransactionType {
    SEND,
    RECEIVE,
    DEPOSIT,
    WITHDRAW,
    QR_PAYMENT,
    BILL_PAYMENT
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REVERSED
}

data class Transaction(
    val transactionId: String,
    val type: TransactionType,
    val senderId: String? = null,
    val senderName: String? = null,
    val receiverId: String? = null,
    val receiverName: String? = null,
    val amount: Long, // in cents
    val fee: Long = 0L, // in cents
    val timestamp: Long, // epoch millis
    val description: String = "",
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val referenceNumber: String = ""
)
