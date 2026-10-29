package com.example.e_wallet.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.model.TransactionStatus
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SecondaryTeal
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.ui.theme.WarningAmber
import com.example.e_wallet.util.Formatters

@Composable
fun TransactionItemRow(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconTint, containerColor) = when (transaction.type) {
        TransactionType.SEND -> Triple(
            PocketPayIcons.ArrowUp,
            ExpenseRose,
            ExpenseRose.copy(alpha = 0.12f)
        )
        TransactionType.RECEIVE -> Triple(
            PocketPayIcons.ArrowDown,
            SuccessGreen,
            SuccessGreen.copy(alpha = 0.12f)
        )
        TransactionType.DEPOSIT -> Triple(
            PocketPayIcons.Add,
            SecondaryTeal,
            SecondaryTeal.copy(alpha = 0.12f)
        )
        TransactionType.WITHDRAW -> Triple(
            PocketPayIcons.Send,
            WarningAmber,
            WarningAmber.copy(alpha = 0.12f)
        )
        TransactionType.QR_PAYMENT -> Triple(
            PocketPayIcons.QrCode,
            PrimaryBlue,
            PrimaryBlue.copy(alpha = 0.12f)
        )
        TransactionType.BILL_PAYMENT -> Triple(
            PocketPayIcons.Receipt,
            WarningAmber,
            WarningAmber.copy(alpha = 0.12f)
        )
        TransactionType.SAVINGS_DEPOSIT -> Triple(
            PocketPayIcons.Savings,
            PrimaryBlue,
            PrimaryBlue.copy(alpha = 0.12f)
        )
        TransactionType.SAVINGS_WITHDRAWAL -> Triple(
            PocketPayIcons.Savings,
            SuccessGreen,
            SuccessGreen.copy(alpha = 0.12f)
        )
    }

    val displayTitle = when {
        transaction.description.isNotBlank() -> transaction.description
        transaction.type == TransactionType.SEND -> "Sent to ${transaction.receiverName ?: "Beneficiary"}"
        transaction.type == TransactionType.RECEIVE -> "Received from ${transaction.senderName ?: "Contact"}"
        transaction.type == TransactionType.DEPOSIT -> "Simulated Bank Deposit"
        transaction.type == TransactionType.WITHDRAW -> "ATM Cash Withdrawal"
        transaction.type == TransactionType.QR_PAYMENT -> "QR Merchant Payment"
        transaction.type == TransactionType.BILL_PAYMENT -> "Bill Payment to ${transaction.receiverName ?: "Biller"}"
        transaction.type == TransactionType.SAVINGS_DEPOSIT -> "Saved to ${transaction.receiverName ?: "Goal Vault"}"
        transaction.type == TransactionType.SAVINGS_WITHDRAWAL -> "Transferred from ${transaction.senderName ?: "Goal Vault"}"
        else -> "Transaction"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Icon
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = containerColor,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Middle Column: Title & Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Formatters.formatTransactionTime(transaction.timestamp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (transaction.status != TransactionStatus.COMPLETED) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = WarningAmber.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = transaction.status.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Column: Signed Amount
            val isPositive = transaction.type == TransactionType.RECEIVE || transaction.type == TransactionType.DEPOSIT
            val amountColor = if (isPositive) SuccessGreen else MaterialTheme.colorScheme.onSurface

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatSignedAmount(transaction.amount, transaction.type),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                if (transaction.fee > 0L) {
                    Text(
                        text = "Fee: ${Formatters.formatCentsToCurrency(transaction.fee)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
