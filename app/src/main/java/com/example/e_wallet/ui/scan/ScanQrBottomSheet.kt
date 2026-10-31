package com.example.e_wallet.ui.scan

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.Transaction
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
fun ScanQrBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    repository: WalletRepository = WalletRepositoryProvider.repository,
    viewModel: ScanQrViewModel = remember(repository) {
        ScanQrViewModel(repository = repository)
    }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val handleDismiss = {
        if (!uiState.isProcessing) {
            viewModel.reset()
            onDismiss()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    ModalBottomSheet(
        onDismissRequest = handleDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState.step) {
                ScanQrStep.SCANNER -> {
                    ScannerView(
                        isTorchOn = uiState.isTorchOn,
                        isFrontCamera = uiState.isFrontCamera,
                        onToggleTorch = viewModel::toggleTorch,
                        onToggleCamera = viewModel::toggleCamera,
                        onQrScanned = viewModel::onScanResult,
                        onScanError = viewModel::onScanError,
                        onSelectPreset = viewModel::onSelectPreset,
                        onOpenManualInput = { viewModel.toggleManualInputDialog(true) }
                    )
                }
                ScanQrStep.PAYMENT_DETAILS -> {
                    PaymentDetailsView(
                        uiState = uiState,
                        onBack = viewModel::backToScanner,
                        onAmountChanged = viewModel::onAmountChanged,
                        onQuickAmountSelected = viewModel::onQuickAmountSelected,
                        onNoteChanged = viewModel::onNoteChanged,
                        onProceed = viewModel::proceedToPin
                    )
                }
                ScanQrStep.PIN_VERIFICATION -> {
                    PinVerificationView(
                        uiState = uiState,
                        onBack = viewModel::backToPaymentDetails,
                        onBiometricClick = viewModel::authenticateWithBiometrics,
                        onDigitClick = viewModel::onPinDigit,
                        onDeleteClick = viewModel::onPinDelete,
                        onClearClick = viewModel::onPinClear
                    )
                }
                ScanQrStep.PROCESSING -> {
                    ProcessingView()
                }
                ScanQrStep.SUCCESS -> {
                    SuccessReceiptView(
                        transaction = uiState.completedTransaction,
                        newBalanceCents = uiState.newBalanceCents,
                        onDone = {
                            viewModel.reset()
                            onDismiss()
                        }
                    )
                }
                ScanQrStep.ERROR -> {
                    ErrorView(
                        title = uiState.errorTitle ?: "Payment Failed",
                        errorMessage = uiState.errorMessage ?: "Payment could not be completed.",
                        onRetry = viewModel::backToScanner
                    )
                }
            }
        }
    }

    if (uiState.isManualInputVisible) {
        ManualCodeInputDialog(
            text = uiState.manualInputText,
            onTextChanged = viewModel::onManualInputTextChanged,
            onDismiss = { viewModel.toggleManualInputDialog(false) },
            onSubmit = { viewModel.onManualCodeSubmit(uiState.manualInputText) }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. SCANNER VIEW (Simulated Viewfinder with Animated Laser & Flash)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ScannerView(
    isTorchOn: Boolean,
    isFrontCamera: Boolean,
    onToggleTorch: () -> Unit,
    onToggleCamera: () -> Unit,
    onQrScanned: (String) -> Unit,
    onScanError: (String) -> Unit,
    onSelectPreset: (DemoQrPreset) -> Unit,
    onOpenManualInput: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Scan QR Code",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Point camera at PocketPay or Merchant QR",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Manual paste / keyboard button
            IconButton(
                onClick = onOpenManualInput,
                modifier = Modifier
                    .size(40.dp)
                    .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = PocketPayIcons.Receipt,
                    contentDescription = "Manual Code",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware CameraX + Google ML Kit Live Viewfinder + Photo Gallery Fallback
        CameraQrScannerView(
            isTorchOn = isTorchOn,
            isFrontCamera = isFrontCamera,
            onToggleTorch = onToggleTorch,
            onToggleCamera = onToggleCamera,
            onQrScanned = onQrScanned,
            onScanError = onScanError
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Demo QR Simulator Section
        Text(
            text = "Try Demo QRs (One-Tap Test)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QrCodeParser.demoPresets.forEach { preset ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clickable { onSelectPreset(preset) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = preset.iconEmoji, fontSize = 18.sp)
                        Column {
                            Text(
                                text = preset.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = preset.subtitle,
                                fontSize = 10.sp,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. PAYMENT DETAILS VIEW (Amount, Receiver, Balance Breakdown, Pre-Check)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PaymentDetailsView(
    uiState: ScanQrUiState,
    onBack: () -> Unit,
    onAmountChanged: (String) -> Unit,
    onQuickAmountSelected: (Int) -> Unit,
    onNoteChanged: (String) -> Unit,
    onProceed: () -> Unit
) {
    val scrollState = rememberScrollState()
    val receiver = uiState.scannedQr ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Navigation header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = PocketPayIcons.ArrowBack,
                    contentDescription = "Back to Scanner",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Payment Confirmation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Receiver / Merchant Badge Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PrimaryBlue.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(PrimaryBlue.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (receiver.receiverName.contains("Starbucks", ignoreCase = true)) "SB" else receiver.avatarInitials,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = receiver.receiverName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SuccessGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "✓ Verified Merchant",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Wallet ID: ${receiver.receiverWalletNumber}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Amount Section: Dynamic locked invoice vs Editable flexible amount
        if (uiState.isAmountLocked) {
            // Locked by invoice
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PrimaryBlue.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = PocketPayIcons.Lock,
                            contentDescription = "Locked Amount",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "INVOICE AMOUNT FIXED BY QR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = Formatters.formatCentsToCurrency(uiState.amountCents),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            // Flexible Amount Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Payment Amount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = uiState.amountInput,
                    onValueChange = onAmountChanged,
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = {
                        Text(
                            text = "$",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Amount Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.quickAmounts.forEach { amount ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (uiState.amountInput == amount.toString()) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onQuickAmountSelected(amount) }
                        ) {
                            Text(
                                text = "$$amount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.amountInput == amount.toString()) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Note Input
        OutlinedTextField(
            value = uiState.noteInput,
            onValueChange = onNoteChanged,
            label = { Text("Note (Optional)") },
            placeholder = { Text("e.g. Coffee, Lunch split") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Financial Breakdown Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BreakdownRow(label = "Amount", value = Formatters.formatCentsToCurrency(uiState.amountCents))
                BreakdownRow(label = "Transfer Fee", value = "Free ($0.00)", isHighlight = true)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                BreakdownRow(label = "Total Debit", value = Formatters.formatCentsToCurrency(uiState.totalDebitCents), isBold = true)
                BreakdownRow(label = "Wallet Balance", value = Formatters.formatCentsToCurrency(uiState.availableBalanceCents))
                BreakdownRow(
                    label = "Balance After Payment",
                    value = Formatters.formatCentsToCurrency(uiState.remainingBalanceCents),
                    valueColor = if (uiState.isInsufficientBalance) ExpenseRose else SuccessGreen
                )
            }
        }

        // Insufficient Balance Pre-Check Alert Banner
        if (uiState.isInsufficientBalance) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ExpenseRose.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRose.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(ExpenseRose.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PocketPayIcons.Info,
                            contentDescription = null,
                            tint = ExpenseRose,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Insufficient Wallet Balance",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRose
                        )
                        val deficit = uiState.totalDebitCents - uiState.availableBalanceCents
                        Text(
                            text = "Available: ${Formatters.formatCentsToCurrency(uiState.availableBalanceCents)}. You need ${Formatters.formatCentsToCurrency(deficit)} more to complete this payment.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Error message if any
        if (uiState.errorMessage != null && !uiState.isInsufficientBalance) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = uiState.errorMessage,
                color = ExpenseRose,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        val buttonText = when {
            uiState.amountCents == 0L -> "Enter Payment Amount"
            uiState.isInsufficientBalance -> "Insufficient Balance"
            uiState.isOverLimit -> "Exceeds $2,500.00 Limit"
            else -> "Confirm & Authorize (${Formatters.formatCentsToCurrency(uiState.totalDebitCents)})"
        }

        // Confirm / Authorize Button
        Button(
            onClick = onProceed,
            enabled = uiState.isValidAmount,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                disabledContainerColor = PrimaryBlue.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = buttonText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. PIN & BIOMETRIC VERIFICATION VIEW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PinVerificationView(
    uiState: ScanQrUiState,
    onBack: () -> Unit,
    onBiometricClick: () -> Unit,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Navigation header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = PocketPayIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Authorize Payment",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Security Lock Icon
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(PrimaryBlue.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = PocketPayIcons.Lock,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Payment Authorization",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Paying ${Formatters.formatCentsToCurrency(uiState.amountCents)} to ${uiState.scannedQr?.receiverName}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Biometric Option (if enabled)
        if (uiState.isBiometricsEnabled) {
            Button(
                onClick = onBiometricClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryTeal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = PocketPayIcons.Fingerprint,
                    contentDescription = "Biometrics",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Authorize with Biometrics",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Text(
                    text = "  OR ENTER 4-DIGIT PIN  ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4 PIN Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val isFilled = index < uiState.pinInput.length
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            if (isFilled) PrimaryBlue else Color.Transparent,
                            CircleShape
                        )
                        .border(
                            2.dp,
                            if (isFilled) PrimaryBlue else MaterialTheme.colorScheme.outlineVariant,
                            CircleShape
                        )
                )
            }
        }

        // Demo PIN Hint Badge
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = WarningAmber.copy(alpha = 0.12f)
        ) {
            Text(
                text = "Demo PIN: 1234",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = WarningAmber,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        // Error message if wrong PIN
        if (uiState.pinError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.pinError,
                color = ExpenseRose,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Numeric Keypad
        NumericKeypad(
            onDigitClick = onDigitClick,
            onDeleteClick = onDeleteClick,
            onClearClick = onClearClick
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. PROCESSING VIEW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProcessingView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            color = PrimaryBlue,
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Authorizing QR Payment...",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Settling transaction with PocketPay ledger",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. SUCCESS RECEIPT VIEW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SuccessReceiptView(
    transaction: Transaction?,
    newBalanceCents: Long,
    onDone: () -> Unit
) {
    val tx = transaction ?: return
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Green Success Circle
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(SuccessGreen.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = PocketPayIcons.CheckCircle,
                contentDescription = "Success",
                tint = SuccessGreen,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Payment Successful!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "-${Formatters.formatCentsToCurrency(tx.amount)}",
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SuccessGreen
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Full Receipt Card (20px radius)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Receipt: 20px radius",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "20px",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ReceiptRow(label = "Paid To:", value = tx.receiverName ?: "Starbucks Coffee")
                ReceiptRow(label = "Transaction Type:", value = "QR Payment")
                ReceiptRow(label = "Reference No:", value = tx.referenceNumber ?: "REF-QRP-883921")
                ReceiptRow(label = "Timestamp:", value = Formatters.formatTimestampToFullDate(tx.timestamp))
                ReceiptRow(label = "Payment Source:", value = "PocketPay Wallet")
                ReceiptRow(label = "Remaining Balance:", value = Formatters.formatCentsToCurrency(newBalanceCents))
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SuccessGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "COMPLETED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val shareText = "PocketPay Payment Receipt\nPaid To: ${tx.receiverName ?: "Unknown"}\nAmount: ${Formatters.formatCentsToCurrency(tx.amount)}\nReference: ${tx.referenceNumber}\nStatus: Completed"
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Receipt"))
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text(
                    text = "Share Receipt",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onDone,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text(
                    text = "Done",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. ERROR VIEW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorView(
    title: String = "Payment Failed",
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(ExpenseRose.copy(alpha = 0.15f), CircleShape),
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
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Back to Scanner",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 7. KEYPAD & DIALOG HELPERS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "DEL")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { key ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clickable {
                                when (key) {
                                    "DEL" -> onDeleteClick()
                                    "C" -> onClearClick()
                                    else -> onDigitClick(key)
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (key == "DEL") {
                                Icon(
                                    imageVector = PocketPayIcons.Backspace,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = key,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (key == "C") ExpenseRose else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isHighlight: Boolean = false,
    valueColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor ?: if (isHighlight) SuccessGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color? = null,
    isBadge: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (isBadge) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = SuccessGreen.copy(alpha = 0.15f)
            ) {
                Text(
                    text = value,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor ?: MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ManualCodeInputDialog(
    text: String,
    onTextChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Enter QR Code Data", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Paste or enter a PocketPay QR code string (e.g. pocketpay://receive?... or PKP-xxxx-xxxx)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    placeholder = { Text("pocketpay://...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = text.isNotBlank()
            ) {
                Text("Process QR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
