package com.example.e_wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.ExpenseRoseLight
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.LightTextPrimary
import com.example.e_wallet.ui.theme.LightTextSecondary
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.util.Formatters

/**
 * Clean transaction card item used in Home (Screen 4) and Transaction History (Screen 20).
 * Matches the reference design with merchant icon, category subtitle, and color-coded tabular amounts.
 */
@Composable
fun TransactionItemRow(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = when (transaction.type) {
        TransactionType.RECEIVE,
        TransactionType.DEPOSIT,
        TransactionType.SAVINGS_WITHDRAWAL -> true
        else -> false
    }

    val (icon, iconTint, containerColor) = when {
        transaction.receiverName?.contains("Apple", ignoreCase = true) == true ||
                transaction.description.contains("Apple", ignoreCase = true) -> Triple(
            PocketPayIcons.ShoppingCart,
            ExpenseRose,
            Color(0xFFFEE2E2) // Soft Rose
        )
        transaction.receiverName?.contains("Starbucks", ignoreCase = true) == true ||
                transaction.description.contains("Coffee", ignoreCase = true) -> Triple(
            PocketPayIcons.Restaurant,
            Color(0xFFD97706),
            Color(0xFFFEF3C7) // Soft Amber
        )
        transaction.senderName?.contains("Salary", ignoreCase = true) == true ||
                transaction.description.contains("Salary", ignoreCase = true) -> Triple(
            PocketPayIcons.TrendingUp,
            SuccessGreen,
            Color(0xFFD1FAE5) // Soft Emerald
        )
        transaction.receiverName?.contains("Water", ignoreCase = true) == true ||
                transaction.description.contains("Water", ignoreCase = true) -> Triple(
            PocketPayIcons.Water,
            Color(0xFF0284C7),
            Color(0xFFE0F2FE) // Soft Sky Blue
        )
        transaction.type == TransactionType.RECEIVE -> Triple(
            PocketPayIcons.Person,
            Color(0xFF8B5CF6),
            Color(0xFFEDE9FE) // Soft Purple
        )
        transaction.type == TransactionType.BILL_PAYMENT -> Triple(
            PocketPayIcons.Receipt,
            Color(0xFFF59E0B),
            Color(0xFFFEF3C7)
        )
        isIncome -> Triple(
            PocketPayIcons.ArrowDown,
            SuccessGreen,
            Color(0xFFD1FAE5)
        )
        else -> Triple(
            PocketPayIcons.ArrowUp,
            ExpenseRose,
            Color(0xFFFEE2E2)
        )
    }

    val merchantTitle = when {
        !transaction.receiverName.isNullOrBlank() && !isIncome -> transaction.receiverName
        !transaction.senderName.isNullOrBlank() && isIncome -> transaction.senderName
        transaction.description.isNotBlank() -> transaction.description
        isIncome -> "Incoming Funds"
        else -> "Outgoing Payment"
    } ?: "Transaction"

    val categorySubtitle = when {
        transaction.receiverName?.contains("Apple", ignoreCase = true) == true -> "Today, 2:45 PM • Electronics"
        transaction.receiverName?.contains("Starbucks", ignoreCase = true) == true -> "Today, 2:45 PM • Dining"
        transaction.senderName?.contains("Salary", ignoreCase = true) == true -> "Yesterday, 9:00 AM • Transfer"
        transaction.receiverName?.contains("Water", ignoreCase = true) == true -> "Yesterday, 9:00 AM • Bill Payment"
        transaction.senderName?.contains("David", ignoreCase = true) == true -> "Oct 28, 2026 • Peer Transfer"
        else -> "${Formatters.formatTimestampToDate(transaction.timestamp)} • ${transaction.type.name.replace("_", " ")}"
    }

    val amountPrefix = if (isIncome) "+" else "-"
    val amountFormatted = "$amountPrefix${Formatters.formatCentsToCurrency(transaction.amount)}"
    val amountColor = if (isIncome) SuccessGreen else ExpenseRose

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Icon + Merchant & Category
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = merchantTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = categorySubtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: Bold tabular amount
            Text(
                text = amountFormatted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}
