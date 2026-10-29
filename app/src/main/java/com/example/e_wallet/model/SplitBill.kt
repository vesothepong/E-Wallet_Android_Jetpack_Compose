package com.example.e_wallet.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.e_wallet.ui.components.PocketPayIcons

/**
 * Categories for split bill and group expenses.
 */
enum class SplitBillCategory(
    val displayName: String,
    val colorHex: Long
) {
    DINING(
        displayName = "Dining & Drinks",
        colorHex = 0xFFF97316 // Orange
    ),
    TRIP(
        displayName = "Travel & Trips",
        colorHex = 0xFF2563EB // Blue
    ),
    HOUSEHOLD(
        displayName = "Rent & Household",
        colorHex = 0xFF10B981 // Emerald
    ),
    ENTERTAINMENT(
        displayName = "Entertainment & Events",
        colorHex = 0xFFEAB308 // Amber
    ),
    SHOPPING(
        displayName = "Groceries & Shopping",
        colorHex = 0xFFEC4899 // Pink
    ),
    OTHER(
        displayName = "General Shared Cost",
        colorHex = 0xFF6366F1 // Indigo
    );

    val icon: ImageVector
        get() = when (this) {
            DINING -> PocketPayIcons.Restaurant
            TRIP -> PocketPayIcons.Flight
            HOUSEHOLD -> PocketPayIcons.Home
            ENTERTAINMENT -> PocketPayIcons.Tv
            SHOPPING -> PocketPayIcons.ShoppingCart
            OTHER -> PocketPayIcons.ReceiptLong
        }
}

/**
 * Lifecycle status of a split bill.
 */
enum class SplitBillStatus {
    ACTIVE,
    SETTLED,
    CANCELLED
}

/**
 * Individual participant in a group bill split.
 */
data class SplitParticipant(
    val participantId: String,
    val name: String,
    val phoneNumber: String,
    val avatarInitials: String,
    val amountCents: Long,
    val isPaid: Boolean = false,
    val paidAtMillis: Long? = null,
    val reminderCount: Int = 0,
    val isHost: Boolean = false
)

/**
 * Domain entity representing a shared group expense / split bill.
 */
data class SplitBill(
    val id: String,
    val title: String,
    val category: SplitBillCategory,
    val totalAmountCents: Long,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val dueDateMillis: Long? = null,
    val note: String = "",
    val participants: List<SplitParticipant>,
    val status: SplitBillStatus = SplitBillStatus.ACTIVE
) {
    val collectedAmountCents: Long
        get() = participants.filter { it.isPaid }.sumOf { it.amountCents }

    val pendingAmountCents: Long
        get() = participants.filter { !it.isPaid }.sumOf { it.amountCents }

    val paidParticipantsCount: Int
        get() = participants.count { it.isPaid }

    val totalParticipantsCount: Int
        get() = participants.size

    val progressFraction: Float
        get() = if (totalAmountCents > 0L) {
            (collectedAmountCents.toFloat() / totalAmountCents.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()

    val isFullySettled: Boolean
        get() = participants.isNotEmpty() && participants.all { it.isPaid }
}
