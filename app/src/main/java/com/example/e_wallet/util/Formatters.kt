package com.example.e_wallet.util

import com.example.e_wallet.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatCentsToCurrency(amountInCents: Long, currency: String = "USD"): String {
        val dollars = amountInCents / 100.0
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        val formatted = formatter.format(dollars)
        return if (currency == "USD") {
            formatted
        } else {
            "$formatted $currency"
        }
    }

    fun formatSignedAmount(amountInCents: Long, type: TransactionType): String {
        val formatted = formatCentsToCurrency(amountInCents)
        return when (type) {
            TransactionType.RECEIVE, TransactionType.DEPOSIT -> "+$formatted"
            TransactionType.SEND, TransactionType.WITHDRAW, TransactionType.QR_PAYMENT -> "-$formatted"
        }
    }

    fun formatTransactionTime(timestamp: Long): String {
        val now = Calendar.getInstance()
        val timeCal = Calendar.getInstance().apply { timeInMillis = timestamp }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(Date(timestamp))

        return if (now.get(Calendar.YEAR) == timeCal.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == timeCal.get(Calendar.DAY_OF_YEAR)
        ) {
            "Today, $formattedTime"
        } else if (now.get(Calendar.YEAR) == timeCal.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) - timeCal.get(Calendar.DAY_OF_YEAR) == 1
        ) {
            "Yesterday, $formattedTime"
        } else {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }

    fun maskWalletNumber(walletNumber: String): String {
        // e.g. PKP-8829-4102 -> •••• 4102 or PKP-••••-4102
        if (walletNumber.length <= 4) return walletNumber
        val last4 = walletNumber.takeLast(4)
        return "•••• •••• $last4"
    }
}
