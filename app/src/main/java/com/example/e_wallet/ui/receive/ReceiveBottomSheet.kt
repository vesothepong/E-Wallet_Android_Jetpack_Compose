package com.example.e_wallet.ui.receive

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.EWalletTheme
import com.example.e_wallet.ui.theme.NeutralLight
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveBottomSheet(
    onDismiss: () -> Unit,
    viewModel: ReceiveViewModel = remember { ReceiveViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
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
            // ── Header ────────────────────────────────────────────────────────
            Text(
                text = "Receive Money",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Share your QR code or wallet number",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── QR Code ───────────────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 6.dp,
                color = Color.White,
                modifier = Modifier
                    .border(
                        width = 1.5.dp,
                        color = NeutralLight,
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Logo badge centered on QR
                    Box(contentAlignment = Alignment.Center) {
                        QrCodeCanvas(
                            data = uiState.qrData,
                            qrSize = 200.dp,
                            foregroundColor = Color(0xFF1E1B4B),
                            backgroundColor = Color.White
                        )
                        // Center logo badge overlay
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue)
                        ) {
                            Icon(
                                imageVector = PocketPayIcons.Wallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Name and wallet number
                    Text(
                        text = uiState.user?.fullName ?: "—",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1B4B),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.wallet?.walletNumber ?: "—",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )

                    // If a request amount is set, show it prominently
                    uiState.requestAmountCents?.let { cents ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Requesting ${Formatters.formatCentsToCurrency(cents)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Request Amount Toggle ──────────────────────────────────────────
            TextButton(
                onClick = viewModel::toggleAmountField,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = if (uiState.isAmountFieldVisible) PocketPayIcons.Close else PocketPayIcons.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (uiState.isAmountFieldVisible) "Clear amount" else "Set a request amount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            AnimatedVisibility(
                visible = uiState.isAmountFieldVisible,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.requestAmountInput,
                        onValueChange = viewModel::onAmountInputChange,
                        label = { Text("Amount") },
                        prefix = { Text("$") },
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The payer will see this amount pre-filled when they scan.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Action Buttons ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Copy wallet number button
                ReceiveActionButton(
                    label = "Copy Number",
                    icon = {
                        Icon(
                            imageVector = PocketPayIcons.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        val walletNumber = uiState.wallet?.walletNumber ?: return@ReceiveActionButton
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Wallet Number", walletNumber)
                        clipboard.setPrimaryClip(clip)
                        viewModel.onWalletNumberCopied()
                    },
                    modifier = Modifier.weight(1f)
                )

                // Share button
                ReceiveActionButton(
                    label = "Share",
                    icon = {
                        Icon(
                            imageVector = PocketPayIcons.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    isPrimary = true,
                    onClick = {
                        val walletNumber = uiState.wallet?.walletNumber ?: return@ReceiveActionButton
                        val name = uiState.user?.fullName ?: ""
                        val amountPart = uiState.requestAmountCents
                            ?.let { "\nRequesting: ${Formatters.formatCentsToCurrency(it)}" } ?: ""
                        val shareText = "Send money to $name via PocketPay\nWallet: $walletNumber$amountPart"

                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(
                            android.content.Intent.createChooser(intent, "Share via")
                        )
                        viewModel.onQrShared()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Demo note ─────────────────────────────────────────────────────
            Text(
                text = "This QR pattern is visual only in demo mode.\nA real app would use a scannable QR library.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ReceiveActionButton(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    val containerColor = if (isPrimary) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isPrimary) Color.White else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp)
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalContentColor provides contentColor
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReceiveBottomSheetPreview() {
    EWalletTheme {
        ReceiveBottomSheet(onDismiss = {})
    }
}
