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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.model.TransactionStatus
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SecondaryTeal
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.ui.theme.WarningAmber
import com.example.e_wallet.util.Formatters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: Transaction,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Transaction type icon (large)
            val (icon, iconTint, containerColor) = when (transaction.type) {
                TransactionType.SEND -> Triple(
                    PocketPayIcons.ArrowUp, ExpenseRose, ExpenseRose.copy(alpha = 0.12f)
                )
                TransactionType.RECEIVE -> Triple(
                    PocketPayIcons.ArrowDown, SuccessGreen, SuccessGreen.copy(alpha = 0.12f)
                )
                TransactionType.DEPOSIT -> Triple(
                    PocketPayIcons.Add, SecondaryTeal, SecondaryTeal.copy(alpha = 0.12f)
                )
                TransactionType.WITHDRAW -> Triple(
                    PocketPayIcons.Send, WarningAmber, WarningAmber.copy(alpha = 0.12f)
                )
                TransactionType.QR_PAYMENT -> Triple(
                    PocketPayIcons.QrCode, PrimaryBlue, PrimaryBlue.copy(alpha = 0.12f)
                )
                TransactionType.BILL_PAYMENT -> Triple(
                    PocketPayIcons.Receipt, WarningAmber, WarningAmber.copy(alpha = 0.12f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = containerColor,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction type label
            Text(
                text = transaction.type.name
                    .replace("_", " ")
                    .lowercase()
                    .replaceFirstChar { it.uppercase() },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Amount (large)
            val isPositive = transaction.type == TransactionType.RECEIVE ||
                transaction.type == TransactionType.DEPOSIT
            Text(
                text = Formatters.formatSignedAmount(transaction.amount, transaction.type),
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isPositive) SuccessGreen else MaterialTheme.colorScheme.onSurface
            )

            // Fee (if any)
            if (transaction.fee > 0L) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+ ${Formatters.formatCentsToCurrency(transaction.fee)} fee",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Status badge
            StatusBadge(status = transaction.status)

            Spacer(modifier = Modifier.height(24.dp))

            // Detail rows card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // Description / title
                    val title = transaction.description.ifBlank {
                        when (transaction.type) {
                            TransactionType.SEND -> "Sent to ${transaction.receiverName ?: "Beneficiary"}"
                            TransactionType.RECEIVE -> "Received from ${transaction.senderName ?: "Contact"}"
                            TransactionType.DEPOSIT -> "Simulated Bank Deposit"
                            TransactionType.WITHDRAW -> "ATM Cash Withdrawal"
                            TransactionType.QR_PAYMENT -> "QR Merchant Payment"
                            TransactionType.BILL_PAYMENT -> "Bill Payment to ${transaction.receiverName ?: "Biller"}"
                        }
                    }
                    DetailRow(label = "Description", value = title)
                    DetailDivider()

                    // Parties
                    transaction.senderName?.let {
                        DetailRow(label = "From", value = it)
                        DetailDivider()
                    }
                    transaction.receiverName?.let {
                        DetailRow(label = "To", value = it)
                        DetailDivider()
                    }

                    // Date & time
                    val locale = LocalConfiguration.current.locales[0]
                    val fullDateFmt = SimpleDateFormat("MMM dd, yyyy • h:mm a", locale)
                    DetailRow(label = "Date", value = fullDateFmt.format(Date(transaction.timestamp)))
                    DetailDivider()

                    // Amount breakdown
                    DetailRow(
                        label = "Amount",
                        value = Formatters.formatCentsToCurrency(transaction.amount)
                    )
                    if (transaction.fee > 0L) {
                        DetailDivider()
                        DetailRow(
                            label = "Fee",
                            value = Formatters.formatCentsToCurrency(transaction.fee)
                        )
                        DetailDivider()
                        DetailRow(
                            label = "Total",
                            value = Formatters.formatCentsToCurrency(transaction.amount + transaction.fee),
                            valueWeight = FontWeight.Bold
                        )
                    }

                    if (transaction.referenceNumber.isNotBlank()) {
                        DetailDivider()
                        DetailRow(
                            label = "Reference",
                            value = transaction.referenceNumber,
                            valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TransactionStatus) {
    val (bgColor, textColor, label) = when (status) {
        TransactionStatus.COMPLETED -> Triple(SuccessGreen.copy(alpha = 0.12f), SuccessGreen, "Completed")
        TransactionStatus.PENDING -> Triple(WarningAmber.copy(alpha = 0.12f), WarningAmber, "Pending")
        TransactionStatus.FAILED -> Triple(ExpenseRose.copy(alpha = 0.12f), ExpenseRose, "Failed")
        TransactionStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Cancelled"
        )
        TransactionStatus.REVERSED -> Triple(PrimaryBlue.copy(alpha = 0.12f), PrimaryBlue, "Reversed")
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(textColor, shape = RoundedCornerShape(50))
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    valueWeight: FontWeight = FontWeight.Medium
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = valueWeight,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
