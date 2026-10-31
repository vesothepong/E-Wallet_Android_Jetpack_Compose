package com.example.e_wallet.ui.deposit

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
 * Screen 7: Top-Up / Deposit Modal
 * Matches Image 7 with:
 * - Current Balance: $12,450.80
 * - Funding source radio list (Chase, PocketPay Visa Debit, Link new bank)
 * - Enter Amount with quick chips ($20, $50, $100, $500)
 * - Instant deposit • Zero transaction fee badge
 * - "Deposit $100.00 Now" CTA
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    repository: WalletRepository = WalletRepositoryProvider.repository
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val wallet by repository.currentWallet.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedSource by remember { mutableStateOf(0) } // 0: Chase, 1: PocketPay Visa, 2: New Bank
    var amountText by remember { mutableStateOf("100.00") }
    var isProcessing by remember { mutableStateOf(false) }

    val amountDouble = amountText.toDoubleOrNull() ?: 0.0
    val amountCents = (amountDouble * 100).toLong()

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
            // Header: Close 'X' and Title + Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = PocketPayIcons.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Top Up Wallet Balance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Current Balance: ${Formatters.formatCentsToCurrency(wallet.currentBalance)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Select Funding Source
            Text(
                text = "Select Funding Source",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Source 1: Chase
                FundingSourceItem(
                    title = "Chase Checking Account",
                    subtitle = "•••• 4412",
                    iconColor = Color(0xFF117ACA),
                    iconContent = {
                        Text(text = "C", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    },
                    isSelected = selectedSource == 0,
                    onClick = { selectedSource = 0 }
                )

                // Source 2: PocketPay Visa Debit Card
                FundingSourceItem(
                    title = "PocketPay Visa Debit Card",
                    subtitle = "•••• 5521",
                    iconColor = Color(0xFF0F766E),
                    iconContent = {
                        Text(text = "P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    },
                    isSelected = selectedSource == 1,
                    onClick = { selectedSource = 1 }
                )

                // Source 3: + Link New Bank Account
                FundingSourceItem(
                    title = "+ Link New Bank Account",
                    subtitle = "Instant Plaid Verification",
                    iconColor = Color.Transparent,
                    iconContent = null,
                    isSelected = selectedSource == 2,
                    onClick = { selectedSource = 2 }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Enter Amount
            Text(
                text = "Enter Amount",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        BasicTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Start
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Chips: $20, $50, $100, $500
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val chips = listOf("20", "50", "100", "500")
                for (chip in chips) {
                    val isSelected = amountText == chip || amountText == "$chip.00"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryBlue else LightBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { amountText = "$chip.00" }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$$chip",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Badge: Instant deposit • Zero transaction fee
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryBlue.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = PocketPayIcons.Info,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Instant deposit • Zero transaction fee",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CTA Button: Deposit $100.00 Now
            Button(
                onClick = {
                    if (amountCents > 0) {
                        isProcessing = true
                        scope.launch {
                            repository.deposit(
                                amountCents = amountCents,
                                note = "Deposit via Chase Checking •••• 4412",
                                sourceDescription = "Chase Checking Account"
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
                enabled = !isProcessing && amountCents > 0
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Deposit $${String.format("%.2f", amountDouble)} Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FundingSourceItem(
    title: String,
    subtitle: String,
    iconColor: Color,
    iconContent: (@Composable () -> Unit)?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (iconContent != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    iconContent()
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
