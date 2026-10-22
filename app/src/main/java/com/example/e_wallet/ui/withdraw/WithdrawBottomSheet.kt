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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.NeutralDark
import com.example.e_wallet.ui.theme.NeutralLight
import com.example.e_wallet.ui.theme.NeutralMuted
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SecondaryTeal
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.ui.theme.SurfaceCard
import com.example.e_wallet.ui.theme.WarningAmber
import com.example.e_wallet.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    repository: WalletRepository = WalletRepositoryProvider.repository,
    viewModel: WithdrawViewModel = remember(repository) { WithdrawViewModel(repository) }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiState by viewModel.uiState.collectAsState()

    val handleDismiss = {
        if (!uiState.isProcessing) {
            viewModel.reset()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = handleDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState.step) {
                WithdrawStep.INPUT -> {
                    WithdrawInputStep(
                        uiState = uiState,
                        onAmountChanged = viewModel::onAmountChanged,
                        onQuickAmountSelected = viewModel::onQuickAmountSelected,
                        onUseMaxAmount = viewModel::onUseMaxAmount,
                        onDestinationSelected = viewModel::onDestinationSelected,
                        onNoteChanged = viewModel::onNoteChanged,
                        onProceed = viewModel::proceedToConfirmation,
                        onClose = handleDismiss
                    )
                }

                WithdrawStep.CONFIRMATION, WithdrawStep.PROCESSING -> {
                    WithdrawConfirmationStep(
                        uiState = uiState,
                        onConfirm = viewModel::confirmWithdraw,
                        onBack = viewModel::backToInput
                    )
                }

                WithdrawStep.SUCCESS -> {
                    WithdrawSuccessStep(
                        uiState = uiState,
                        onDone = {
                            viewModel.reset()
                            onDismiss()
                        }
                    )
                }

                WithdrawStep.ERROR -> {
                    WithdrawErrorStep(
                        errorMessage = uiState.errorMessage ?: "An unexpected error occurred",
                        onRetry = viewModel::backToInput,
                        onClose = handleDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun WithdrawInputStep(
    uiState: WithdrawUiState,
    onAmountChanged: (String) -> Unit,
    onQuickAmountSelected: (Int) -> Unit,
    onUseMaxAmount: () -> Unit,
    onDestinationSelected: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onProceed: () -> Unit,
    onClose: () -> Unit
) {
    // Top Bar
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Withdraw Simulation",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = NeutralDark
            )
            Text(
                text = "Cash out simulated funds from your demo wallet",
                fontSize = 13.sp,
                color = NeutralMuted
            )
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = PocketPayIcons.Close,
                contentDescription = "Close",
                tint = NeutralMuted
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Simulation Disclaimer Banner
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ExpenseRose.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRose.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = PocketPayIcons.Info,
                contentDescription = null,
                tint = ExpenseRose,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Demo Simulation: Funds withdrawn here are virtual demo debits for learning e-wallet mechanics.",
                fontSize = 12.sp,
                color = NeutralDark,
                lineHeight = 16.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Available Balance & Max Button Card
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PocketPayIcons.Wallet,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Available Balance",
                        fontSize = 11.sp,
                        color = NeutralMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = Formatters.formatCentsToCurrency(uiState.availableBalanceCents),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PrimaryBlue.copy(alpha = 0.12f),
                modifier = Modifier.clickable(onClick = onUseMaxAmount)
            ) {
                Text(
                    text = "Use Max",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Destination Selector
    Text(
        text = "Withdraw Destination",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = NeutralDark,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uiState.destinations.forEach { destination ->
            val isSelected = uiState.destinationName == destination
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) PrimaryBlue.copy(alpha = 0.08f) else SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) PrimaryBlue else Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDestinationSelected(destination) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = destination,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) PrimaryBlue else NeutralDark
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = PocketPayIcons.CheckCircle,
                            contentDescription = "Selected",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Amount Input
    Text(
        text = "Enter Amount (USD)",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = NeutralDark,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = uiState.amountInput,
        onValueChange = onAmountChanged,
        modifier = Modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ExpenseRose
        ),
        prefix = {
            Text(
                text = "$ ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ExpenseRose
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = uiState.errorMessage != null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ExpenseRose,
            unfocusedBorderColor = NeutralLight
        ),
        shape = RoundedCornerShape(14.dp)
    )

    if (uiState.errorMessage != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = uiState.errorMessage,
            color = ExpenseRose,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Quick Amount Chips
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uiState.quickAmounts.forEach { amount ->
            val isSelected = uiState.amountInput == amount.toString()
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) ExpenseRose else NeutralLight,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onQuickAmountSelected(amount) }
            ) {
                Text(
                    text = "$$amount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else NeutralDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Note Input (Optional)
    OutlinedTextField(
        value = uiState.noteInput,
        onValueChange = onNoteChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Note / Description") },
        placeholder = { Text("e.g. Weekend cashout, Groceries") },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ExpenseRose,
            unfocusedBorderColor = NeutralLight
        ),
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Proceed Button
    Button(
        onClick = onProceed,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRose),
        enabled = uiState.isValidAmount
    ) {
        Text(
            text = "Review Withdrawal",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun WithdrawConfirmationStep(
    uiState: WithdrawUiState,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    // Top Bar
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            enabled = !uiState.isProcessing,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = PocketPayIcons.ArrowBack,
                contentDescription = "Back",
                tint = NeutralDark
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Confirm Withdrawal",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NeutralDark
        )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Simulated Banner Notice
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = WarningAmber.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = PocketPayIcons.Info,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Simulation: This will immediately deduct simulated funds from your demo wallet. No real bank or ATM is connected.",
                fontSize = 12.sp,
                color = NeutralDark,
                lineHeight = 16.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Amount Summary Card
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Withdrawal Amount",
                fontSize = 13.sp,
                color = NeutralMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "-${Formatters.formatCentsToCurrency(uiState.amountCents)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ExpenseRose
            )

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = NeutralLight)
            Spacer(modifier = Modifier.height(16.dp))

            // Details list
            DetailRow(label = "From", value = "PocketPay Demo Wallet")
            Spacer(modifier = Modifier.height(10.dp))
            DetailRow(label = "Destination", value = uiState.destinationName)
            Spacer(modifier = Modifier.height(10.dp))
            DetailRow(label = "Simulated Fee", value = "$0.00 (Free)")
            Spacer(modifier = Modifier.height(10.dp))
            DetailRow(
                label = "Total Deduction",
                value = Formatters.formatCentsToCurrency(uiState.totalDebitCents),
                isBold = true,
                boldColor = ExpenseRose
            )
            Spacer(modifier = Modifier.height(10.dp))
            DetailRow(
                label = "Remaining Balance",
                value = Formatters.formatCentsToCurrency(uiState.remainingBalanceCents)
            )
            Spacer(modifier = Modifier.height(10.dp))
            DetailRow(label = "Note", value = uiState.noteInput.ifBlank { "Simulated ATM Cashout" })
        }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Action Buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            enabled = !uiState.isProcessing,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text = "Back", fontWeight = FontWeight.SemiBold, color = NeutralDark)
        }

        Button(
            onClick = onConfirm,
            enabled = !uiState.isProcessing,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRose)
        ) {
            if (uiState.isProcessing) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = "Confirm Withdraw",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun WithdrawSuccessStep(
    uiState: WithdrawUiState,
    onDone: () -> Unit
) {
    Spacer(modifier = Modifier.height(12.dp))

    // Success Icon
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(SuccessGreen.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = PocketPayIcons.CheckCircle,
            contentDescription = "Success",
            tint = SuccessGreen,
            modifier = Modifier.size(44.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Withdrawal Successful!",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = NeutralDark
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Simulated funds have been deducted from your demo wallet",
        fontSize = 13.sp,
        color = NeutralMuted
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "-${Formatters.formatCentsToCurrency(uiState.amountCents)}",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = ExpenseRose
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Receipt Card
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = PocketPayIcons.Receipt,
                    contentDescription = null,
                    tint = ExpenseRose,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Simulated Receipt",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralDark
                )
            }

            HorizontalDivider(color = NeutralLight)

            uiState.completedTransaction?.let { tx ->
                DetailRow(label = "Reference Number", value = tx.referenceNumber)
                DetailRow(label = "Destination", value = uiState.destinationName)
                DetailRow(label = "Date & Time", value = Formatters.formatTransactionTime(tx.timestamp))
                DetailRow(label = "Status", value = "COMPLETED", isHighlighted = true)
            }

            DetailRow(
                label = "Remaining Wallet Balance",
                value = Formatters.formatCentsToCurrency(uiState.newBalanceCents),
                isBold = true,
                boldColor = PrimaryBlue
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Done Button
    Button(
        onClick = onDone,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
    ) {
        Text(
            text = "Done",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun WithdrawErrorStep(
    errorMessage: String,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(ExpenseRose.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = PocketPayIcons.Close,
            contentDescription = "Error",
            tint = ExpenseRose,
            modifier = Modifier.size(36.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Withdrawal Failed",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = NeutralDark
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = errorMessage,
        fontSize = 14.sp,
        color = NeutralMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cancel", fontWeight = FontWeight.SemiBold, color = NeutralDark)
        }

        Button(
            onClick = onRetry,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRose)
        ) {
            Text("Try Again", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isHighlighted: Boolean = false,
    boldColor: Color = PrimaryBlue
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = NeutralMuted
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlighted) SuccessGreen else if (isBold) boldColor else NeutralDark
        )
    }
}
