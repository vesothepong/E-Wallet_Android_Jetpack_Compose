package com.example.e_wallet.util

import com.example.e_wallet.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {

    @Volatile
    var defaultCurrency: String = "USD"

    fun formatCentsToCurrency(amountInCents: Long, currency: String = defaultCurrency): String {
        val dollars = amountInCents / 100.0
        return when (currency.uppercase()) {
            "EUR" -> {
                val formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
                formatter.currency = java.util.Currency.getInstance("EUR")
                formatter.format(dollars)
            }
            "GBP" -> {
                val formatter = NumberFormat.getCurrencyInstance(Locale.UK)
                formatter.currency = java.util.Currency.getInstance("GBP")
                formatter.format(dollars)
            }
            "KHR" -> {
                val formatter = NumberFormat.getNumberInstance(Locale.US)
                "៛ ${formatter.format(dollars)}"
            }
            else -> {
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                formatter.format(dollars)
            }
        }
    }

    fun formatSignedAmount(amountInCents: Long, type: TransactionType, currency: String = defaultCurrency): String {
        val absCents = kotlin.math.abs(amountInCents)
        val formatted = formatCentsToCurrency(absCents, currency)
        return when (type) {
            TransactionType.RECEIVE, TransactionType.DEPOSIT, TransactionType.SAVINGS_WITHDRAWAL -> "+$formatted"
            TransactionType.SEND, TransactionType.WITHDRAW, TransactionType.QR_PAYMENT, TransactionType.BILL_PAYMENT, TransactionType.SAVINGS_DEPOSIT -> "-$formatted"
        }
    }

    fun formatTransactionTime(timestamp: Long): String {
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val txDayCal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayDiff = (todayCal.timeInMillis - txDayCal.timeInMillis) / (24 * 60 * 60 * 1000L)

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(Date(timestamp))

        return when (dayDiff) {
            0L -> "Today, $formattedTime"
            1L -> "Yesterday, $formattedTime"
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            }
        }
    }

    fun maskWalletNumber(walletNumber: String): String {
        // e.g. PKP-8829-4102 -> •••• 4102 or PKP-••••-4102
        if (walletNumber.length <= 4) return walletNumber
        val last4 = walletNumber.takeLast(4)
        return "•••• •••• $last4"
    }

    fun formatCentsToAmountOnly(cents: Long): String {
        val dollars = cents / 100.0
        return if (cents % 100 == 0L) {
            (cents / 100).toString()
        } else {
            String.format(Locale.US, "%.2f", dollars)
        }
    }

    fun formatTimestampToFullDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTimestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTimestampToDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy, h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
