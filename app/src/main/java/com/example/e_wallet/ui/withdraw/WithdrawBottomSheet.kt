package com.example.e_wallet.ui.withdraw

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.LightTextPrimary
import com.example.e_wallet.ui.theme.LightTextSecondary
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.util.Formatters
import kotlinx.coroutines.launch

/**
 * Screen 8: Withdraw to Bank Modal
 * Matches Image 8 with:
 * - Bank destination card: Bank of America Checking (•••• 9011) with verified green check
 * - Amount with "Max ($12,450.80)" text button
 * - Delivery speed selector (Standard Free vs Instant 1.5% fee)
 * - Breakdown card (Withdrawal Amount, Transfer Fee, Total Debited)
 * - "Confirm Withdrawal" CTA
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    repository: WalletRepository = WalletRepositoryProvider.repository
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val wallet by repository.currentWallet.collectAsState()
    val scope = rememberCoroutineScope()

    var amountText by remember { mutableStateOf("500.00") }
    var isInstantDelivery by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val amountDouble = amountText.toDoubleOrNull() ?: 0.0
    val amountCents = (amountDouble * 100).toLong()
    val maxBalanceCents = wallet.currentBalance

    val feeCents = if (isInstantDelivery) (amountCents * 0.015).toLong() else 0L
    val totalDebitedCents = amountCents + feeCents

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Drag handle space & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Withdraw to Bank Account",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = PocketPayIcons.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Destination Bank Account Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Bank logo
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE11D48).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "BoA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE11D48)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Bank of America Checking",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "•••• 9011",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Green verified checkmark badge
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PocketPayIcons.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount with "Max ($12,450.80)" link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    BasicTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Text(
                    text = "Max (${Formatters.formatCentsToCurrency(maxBalanceCents)})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable {
                        amountText = String.format("%.2f", maxBalanceCents / 100.0)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Delivery speed selector
            Text(
                text = "Delivery speed selector",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Option 1: Standard (1-3 business days) Free
                DeliveryOptionItem(
                    title = "Standard (1-3 business days)",
                    feeDescription = "Free",
                    isSelected = !isInstantDelivery,
                    onClick = { isInstantDelivery = false }
                )

                // Option 2: Instant Transfer (Within minutes) 1.5% Fee
                DeliveryOptionItem(
                    title = "Instant Transfer (Within minutes)",
                    feeDescription = "1.5% Fee ($${String.format("%.2f", amountDouble * 0.015)})",
                    isSelected = isInstantDelivery,
                    onClick = { isInstantDelivery = true }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Breakdown card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Breakdown card",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    BreakdownRow(
                        label = "Withdrawal Amount",
                        value = "$${String.format("%.2f", amountDouble)}"
                    )
                    BreakdownRow(
                        label = "Transfer Fee",
                        value = if (isInstantDelivery) "$${String.format("%.2f", amountDouble * 0.015)}" else "$0.00"
                    )
                    HorizontalDivider(color = LightBorder, thickness = 0.5.dp)
                    BreakdownRow(
                        label = "Total Debited",
                        value = "$${String.format("%.2f", totalDebitedCents / 100.0)}",
                        isBold = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Confirm Withdrawal CTA
            Button(
                onClick = {
                    if (amountCents > 0 && totalDebitedCents <= maxBalanceCents) {
                        isProcessing = true
                        scope.launch {
                            repository.withdraw(
                                amountCents = amountCents,
                                note = "Withdrawal to Bank of America Checking",
                                destinationDescription = "Bank of America Checking (•••• 9011)",
                                feeCents = feeCents
                            )
                            isProcessing = false
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ),
                enabled = !isProcessing && amountCents > 0 && totalDebitedCents <= maxBalanceCents
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Confirm Withdrawal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryOptionItem(
    title: String,
    feeDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) PrimaryBlue else LightBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = feeDescription,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
            )
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
