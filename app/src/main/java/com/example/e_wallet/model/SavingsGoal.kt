package com.example.e_wallet.model

/**
 * Supported categories for savings goals and vaults in PocketPay.
 */
enum class SavingsCategory(
    val displayName: String,
    val iconName: String,
    val colorHex: Long
) {
    EMERGENCY(
        displayName = "Emergency Fund",
        iconName = "Shield",
        colorHex = 0xFF0D9488 // Teal
    ),
    TRAVEL(
        displayName = "Vacation & Travel",
        iconName = "Flight",
        colorHex = 0xFF2563EB // Blue
    ),
    TECH(
        displayName = "Tech & Gadgets",
        iconName = "Smartphone",
        colorHex = 0xFF8B5CF6 // Purple
    ),
    VEHICLE(
        displayName = "Car & Transport",
        iconName = "DirectionsCar",
        colorHex = 0xFFEA580C // Orange
    ),
    HOME(
        displayName = "Home & Living",
        iconName = "Home",
        colorHex = 0xFF16A34A // Green
    ),
    EDUCATION(
        displayName = "Education & Self",
        iconName = "School",
        colorHex = 0xFFD97706 // Amber
    ),
    GENERAL(
        displayName = "Rainy Day & General",
        iconName = "Savings",
        colorHex = 0xFF6366F1 // Indigo
    )
}

/**
 * Domain entity representing an individual savings vault / goal.
 */
data class SavingsGoal(
    val id: String,
    val name: String,
    val targetAmountCents: Long,
    val currentAmountCents: Long,
    val category: SavingsCategory,
    val targetDateMillis: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isLocked: Boolean = false,
    val apyRatePercent: Double = 4.50, // 4.5% annual yield simulation
    val note: String = ""
) {
    val progressFraction: Float
        get() = if (targetAmountCents > 0) {
            (currentAmountCents.toFloat() / targetAmountCents.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()

    val isCompleted: Boolean
        get() = currentAmountCents >= targetAmountCents

    val remainingCents: Long
        get() = (targetAmountCents - currentAmountCents).coerceAtLeast(0L)

    // Projected monthly interest earned based on current saved balance at given APY
    val projectedMonthlyInterestCents: Long
        get() = ((currentAmountCents * (apyRatePercent / 100.0)) / 12.0).toLong()
}

/**
 * Sealed results returned by savings goal operations.
 */
sealed class SavingsGoalResult {
    data class Success(
        val goal: SavingsGoal,
        val updatedWalletBalance: Long,
        val message: String
    ) : SavingsGoalResult()

    data class Error(val message: String) : SavingsGoalResult()
}
