package com.example.e_wallet.ui.scan

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

        // Simulated Viewfinder Frame
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            // Viewfinder laser scan animation
            val transition = rememberInfiniteTransition(label = "laser_transition")
            val laserY by transition.animateFloat(
                initialValue = 0f,
                targetValue = 240f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "laser_animation"
            )

            // Neon Corner brackets
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 5.dp.toPx()
                val cornerLength = 32.dp.toPx()
                val color = Color(0xFF38BDF8)

                // Top-Left
                drawLine(color, Offset(24f, 24f), Offset(24f + cornerLength, 24f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(24f, 24f), Offset(24f, 24f + cornerLength), strokeWidth, StrokeCap.Round)

                // Top-Right
                drawLine(color, Offset(size.width - 24f, 24f), Offset(size.width - 24f - cornerLength, 24f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(size.width - 24f, 24f), Offset(size.width - 24f, 24f + cornerLength), strokeWidth, StrokeCap.Round)

                // Bottom-Left
                drawLine(color, Offset(24f, size.height - 24f), Offset(24f + cornerLength, size.height - 24f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(24f, size.height - 24f), Offset(24f, size.height - 24f - cornerLength), strokeWidth, StrokeCap.Round)

                // Bottom-Right
                drawLine(color, Offset(size.width - 24f, size.height - 24f), Offset(size.width - 24f - cornerLength, size.height - 24f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(size.width - 24f, size.height - 24f), Offset(size.width - 24f, size.height - 24f - cornerLength), strokeWidth, StrokeCap.Round)
            }

            // Watermark QR Icon in center
            Icon(
                imageVector = PocketPayIcons.QrCode,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.size(100.dp)
            )

            // Animated Laser Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .offset { IntOffset(x = 0, y = (laserY.dp - 120.dp).roundToPx()) }
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF38BDF8),
                                Color(0xFF00E5FF),
                                Color(0xFF38BDF8),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Camera Overlay Controls (Flashlight & Camera Flip)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Torch Button
                IconButton(
                    onClick = onToggleTorch,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isTorchOn) PocketPayIcons.FlashOn else PocketPayIcons.FlashOff,
                        contentDescription = "Torch",
                        tint = if (isTorchOn) WarningAmber else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Camera Flip Button
                IconButton(
                    onClick = onToggleCamera,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = PocketPayIcons.FlipCamera,
                        contentDescription = "Flip Camera",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

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
// 2. PAYMENT DETAILS VIEW (Amount, Receiver, Balance Breakdown)
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
                text = "Confirm Payment",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(48.dp)) // balance layout
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Receiver / Merchant Badge Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (receiver.isMerchant) SecondaryTeal.copy(alpha = 0.2f) else PrimaryBlue.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = receiver.avatarInitials,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (receiver.isMerchant) SecondaryTeal else PrimaryBlue
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = receiver.receiverName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (receiver.isMerchant) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SecondaryTeal.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Merchant",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SecondaryTeal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = receiver.receiverWalletNumber,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Amount Section
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
                    valueColor = if (uiState.totalDebitCents > uiState.availableBalanceCents) ExpenseRose else SuccessGreen
                )
            }
        }

        // Error message if any
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = uiState.errorMessage,
                color = ExpenseRose,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Continue Button
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
                text = "Continue to PIN",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. PIN VERIFICATION VIEW (Custom Keypad & Secure Indicators)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PinVerificationView(
    uiState: ScanQrUiState,
    onBack: () -> Unit,
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

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Enter 4-Digit Security PIN",
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

        Spacer(modifier = Modifier.height(20.dp))

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
        Spacer(modifier = Modifier.height(14.dp))
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

        Spacer(modifier = Modifier.height(24.dp))

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
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = Formatters.formatCentsToCurrency(tx.amount),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SuccessGreen
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Full Receipt Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReceiptRow(label = "Paid To", value = tx.receiverName ?: "Unknown")
                ReceiptRow(label = "Transaction Type", value = "QR Payment")
                ReceiptRow(label = "Reference No.", value = tx.referenceNumber)
                ReceiptRow(label = "Timestamp", value = Formatters.formatTimestampToFullDate(tx.timestamp))
                ReceiptRow(label = "Remaining Balance", value = Formatters.formatCentsToCurrency(newBalanceCents))
                ReceiptRow(label = "Status", value = "COMPLETED", valueColor = SuccessGreen, isBadge = true)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDone,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
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

// ─────────────────────────────────────────────────────────────────────────────
// 6. ERROR VIEW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorView(
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
            text = "Payment Failed",
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
