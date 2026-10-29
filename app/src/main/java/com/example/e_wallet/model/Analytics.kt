package com.example.e_wallet.model

import com.example.e_wallet.ui.components.PocketPayIcons
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Calendar

/**
 * High-level categories for spending analytics and budget tracking in PocketPay.
 */
enum class SpendingCategory(
    val displayName: String,
    val colorHex: Long,
    val description: String
) {
    FOOD_DINING(
        displayName = "Food & Dining",
        colorHex = 0xFFF97316, // Orange
        description = "Restaurants, groceries, cafe & takeout"
    ),
    BILLS_UTILITIES(
        displayName = "Bills & Utilities",
        colorHex = 0xFF0EA5E9, // Sky Blue
        description = "Electricity, water, internet & mobile top-up"
    ),
    SHOPPING(
        displayName = "Shopping & Retail",
        colorHex = 0xFFEC4899, // Pink
        description = "Supermarkets, clothes, gadgets & ecommerce"
    ),
    TRANSPORT(
        displayName = "Travel & Transport",
        colorHex = 0xFF8B5CF6, // Purple
        description = "Rides, transit, flights & fuel"
    ),
    ENTERTAINMENT(
        displayName = "Entertainment",
        colorHex = 0xFFEAB308, // Amber / Yellow
        description = "Movies, events, games & streaming"
    ),
    TRANSFERS(
        displayName = "Transfers & P2P",
        colorHex = 0xFF6366F1, // Indigo
        description = "Direct money sent to friends & family"
    ),
    SAVINGS(
        displayName = "Savings & Vaults",
        colorHex = 0xFF10B981, // Emerald Green
        description = "Deposits into locked goals & high-yield vaults"
    ),
    OTHER(
        displayName = "General & Other",
        colorHex = 0xFF64748B, // Slate Gray
        description = "Cashouts, fees & miscellaneous"
    );

    val icon: ImageVector
        get() = when (this) {
            FOOD_DINING -> PocketPayIcons.Restaurant
            BILLS_UTILITIES -> PocketPayIcons.FlashOn
            SHOPPING -> PocketPayIcons.ShoppingCart
            TRANSPORT -> PocketPayIcons.DirectionsCar
            ENTERTAINMENT -> PocketPayIcons.Tv
            TRANSFERS -> PocketPayIcons.Send
            SAVINGS -> PocketPayIcons.Savings
            OTHER -> PocketPayIcons.ReceiptLong
        }
}

/**
 * Intelligent classifier that inspects a [Transaction] and maps it to a [SpendingCategory].
 */
object TransactionClassifier {

    fun categorize(transaction: Transaction): SpendingCategory {
        // Savings deposit
        if (transaction.type == TransactionType.SAVINGS_DEPOSIT) {
            return SpendingCategory.SAVINGS
        }

        // Bill payment
        if (transaction.type == TransactionType.BILL_PAYMENT) {
            return SpendingCategory.BILLS_UTILITIES
        }

        val text = "${transaction.description} ${transaction.receiverName ?: ""} ${transaction.senderName ?: ""}".lowercase()

        // Food & Dining keywords
        if (containsAny(text, "starbucks", "coffee", "cafe", "dinner", "lunch", "food", "pizza", "burger", "restaurant", "bakery", "kitchen")) {
            return SpendingCategory.FOOD_DINING
        }

        // Bills & Utilities keywords
        if (containsAny(text, "electricity", "water", "broadband", "internet", "power", "utility", "top-up", "telecom", "mobile bill")) {
            return SpendingCategory.BILLS_UTILITIES
        }

        // Transport keywords
        if (containsAny(text, "uber", "grab", "taxi", "flight", "airline", "transit", "metro", "bus", "fuel", "gas station", "airport")) {
            return SpendingCategory.TRANSPORT
        }

        // Entertainment keywords
        if (containsAny(text, "movie", "cinema", "netflix", "spotify", "ticket", "book club", "game", "concert", "theater", "gaming")) {
            return SpendingCategory.ENTERTAINMENT
        }

        // Shopping keywords
        if (containsAny(text, "market", "groceries", "supermarket", "amazon", "apple store", "clothing", "shopping", "walmart", "whole foods")) {
            return SpendingCategory.SHOPPING
        }

        // Transfers / P2P
        if (transaction.type == TransactionType.SEND) {
            return SpendingCategory.TRANSFERS
        }

        if (transaction.type == TransactionType.QR_PAYMENT) {
            return SpendingCategory.SHOPPING
        }

        return SpendingCategory.OTHER
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { keyword -> text.contains(keyword, ignoreCase = true) }
    }
}

/**
 * Monthly budget target set for a specific [SpendingCategory].
 */
data class CategoryBudget(
    val category: SpendingCategory,
    val monthlyLimitCents: Long
) {
    fun isOverBudget(spentCents: Long): Boolean = spentCents > monthlyLimitCents

    fun progressFraction(spentCents: Long): Float {
        if (monthlyLimitCents <= 0) return 0f
        return (spentCents.toFloat() / monthlyLimitCents.toFloat()).coerceAtLeast(0f)
    }

    fun remainingCents(spentCents: Long): Long {
        return (monthlyLimitCents - spentCents).coerceAtLeast(0L)
    }

    fun overBudgetCents(spentCents: Long): Long {
        return (spentCents - monthlyLimitCents).coerceAtLeast(0L)
    }
}

/**
 * Filter time period for analytics reporting.
 */
enum class AnalyticsTimePeriod(val displayName: String) {
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_30_DAYS("Last 30 Days"),
    ALL_TIME("All Time");

    fun getStartTimestampMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        return when (this) {
            THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            LAST_30_DAYS -> nowMillis - (30L * 24 * 60 * 60 * 1000L)
            ALL_TIME -> 0L
        }
    }

    fun getEndTimestampMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        if (this == LAST_MONTH) {
            val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis - 1
        }
        return nowMillis
    }
}

/**
 * Visual slice representation for the spending donut chart.
 */
data class SpendingSlice(
    val category: SpendingCategory,
    val amountCents: Long,
    val percentage: Float,
    val transactionCount: Int,
    val sweepAngle: Float,
    val startAngle: Float
)

/**
 * Data item for daily bar chart visualization.
 */
data class DailySpendBar(
    val label: String,
    val dateMillis: Long,
    val amountCents: Long,
    val normalizedHeight: Float // 0f to 1f relative to peak daily spend
)

/**
 * High-level financial wellness score calculation.
 */
data class FinancialHealthScore(
    val score: Int, // 0 to 100
    val rating: String, // "Excellent", "Healthy", "Fair", "Needs Attention"
    val advice: String
) {
    companion object {
        fun calculate(
            incomeCents: Long,
            expenseCents: Long,
            savingsRatePercent: Int,
            overBudgetCategories: Int
        ): FinancialHealthScore {
            var calculated = 60 // Baseline

            // Savings rate reward (+ up to 30 points)
            calculated += (savingsRatePercent * 0.6).toInt().coerceIn(0, 30)

            // Positive cashflow bonus
            if (incomeCents > expenseCents) {
                calculated += 10
            } else {
                calculated -= 20
            }

            // Budget penalty (-10 per exceeded category)
            calculated -= (overBudgetCategories * 10)

            val finalScore = calculated.coerceIn(10, 100)
            val (rating, advice) = when {
                finalScore >= 85 -> Pair(
                    "Excellent",
                    "Outstanding financial discipline! You have strong savings and spend within your target budgets."
                )
                finalScore >= 70 -> Pair(
                    "Healthy",
                    "Great work! Your income comfortably exceeds spending with steady savings progress."
                )
                finalScore >= 50 -> Pair(
                    "Fair",
                    "Stable, but keep an eye on discretionary spending to boost your monthly savings rate."
                )
                else -> Pair(
                    "Needs Attention",
                    "Spending is currently exceeding or near your income. Review your category budgets below."
                )
            }

            return FinancialHealthScore(score = finalScore, rating = rating, advice = advice)
        }
    }
}

/**
 * Complete consolidated analytics summary for PocketPay.
 */
data class AnalyticsSummary(
    val period: AnalyticsTimePeriod = AnalyticsTimePeriod.THIS_MONTH,
    val totalIncomeCents: Long = 0L,
    val totalExpenseCents: Long = 0L,
    val netCashFlowCents: Long = 0L,
    val savingsRatePercent: Int = 0,
    val averageDailySpendCents: Long = 0L,
    val largestExpenseCents: Long = 0L,
    val spendingSlices: List<SpendingSlice> = emptyList(),
    val dailySpendBars: List<DailySpendBar> = emptyList(),
    val healthScore: FinancialHealthScore = FinancialHealthScore(75, "Healthy", "Keep up your current savings rhythm!"),
    val overBudgetCategoriesCount: Int = 0
)
