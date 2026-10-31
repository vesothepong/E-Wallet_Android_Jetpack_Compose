package com.example.e_wallet.ui.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.LightTextPrimary
import com.example.e_wallet.ui.theme.LightTextSecondary
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.util.Formatters

/**
 * Screen 21: Full Transaction Detail Modal
 * Shows merchant breakdown, status pill, complete metadata:
 * Transaction ID, Reference Number, Payment Method (Virtual Visa •••• 5521),
 * Date & Time, Category, Note, Fee, and PDF download / Done CTA buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: Transaction,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isIncome = when (transaction.type) {
        TransactionType.RECEIVE,
        TransactionType.DEPOSIT,
        TransactionType.SAVINGS_WITHDRAWAL -> true
        else -> false
    }

    val merchantTitle = when {
        !transaction.receiverName.isNullOrBlank() && !isIncome -> transaction.receiverName
        !transaction.senderName.isNullOrBlank() && isIncome -> transaction.senderName
        transaction.description.isNotBlank() -> transaction.description
        isIncome -> "Incoming Funds"
        else -> "Outgoing Payment"
    } ?: "Merchant Payment"

    val displayAmount = if (isIncome) {
        "+${Formatters.formatCentsToCurrency(transaction.amount)}"
    } else {
        "-${Formatters.formatCentsToCurrency(transaction.amount)}"
    }

    val categoryName = when {
        merchantTitle.contains("Apple", ignoreCase = true) -> "Electronics & Gadgets"
        merchantTitle.contains("Starbucks", ignoreCase = true) -> "Food & Dining"
        merchantTitle.contains("Water", ignoreCase = true) -> "Utilities & Bills"
        merchantTitle.contains("Salary", ignoreCase = true) -> "Income & Salary"
        else -> "Transfers & Payments"
    }

    val noteText = when {
        transaction.description.isNotBlank() -> transaction.description
        merchantTitle.contains("Apple", ignoreCase = true) -> "MacBook accessories & adapter"
        else -> "Payment processed via PocketPay"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Merchant Icon
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isIncome) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (merchantTitle.contains("Apple", ignoreCase = true)) PocketPayIcons.ShoppingCart
                        else if (merchantTitle.contains("Starbucks", ignoreCase = true)) PocketPayIcons.Restaurant
                        else if (isIncome) PocketPayIcons.TrendingUp
                        else PocketPayIcons.CreditCard,
                        contentDescription = null,
                        tint = if (isIncome) SuccessGreen else ExpenseRose,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Merchant Name
            Text(
                text = merchantTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Large Amount
            Text(
                text = displayAmount,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isIncome) SuccessGreen else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Payment Completed Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SuccessGreen.copy(alpha = 0.12f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(3.5.dp))
                            .background(SuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Payment Completed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Metadata Detail Card (20px radius)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DetailRow(label = "Transaction ID", value = transaction.transactionId)
                    DetailRow(label = "Reference Number", value = transaction.referenceNumber ?: "REF-APL-88219")
                    DetailRow(
                        label = "Payment Method",
                        value = "PocketPay Virtual Visa (•••• 5521)"
                    )
                    DetailRow(
                        label = "Date & Time",
                        value = Formatters.formatTimestampToDateTime(transaction.timestamp)
                    )
                    DetailRow(label = "Category", value = categoryName)
                    DetailRow(label = "Note", value = noteText)
                    DetailRow(label = "Transfer Fee", value = "Free ($0.00)")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: Download PDF Invoice & Done
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                ) {
                    Icon(
                        imageVector = PocketPayIcons.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Download PDF",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Done",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.3f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
