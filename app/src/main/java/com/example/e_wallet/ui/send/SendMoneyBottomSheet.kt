package com.example.e_wallet.ui.send

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.Beneficiary
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
fun SendMoneyBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialBeneficiary: Beneficiary? = null,
    repository: WalletRepository = WalletRepositoryProvider.repository,
    viewModel: SendMoneyViewModel = remember(repository, initialBeneficiary) {
        SendMoneyViewModel(repository = repository, initialBeneficiary = initialBeneficiary)
    }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiState by viewModel.uiState.collectAsState()

    val handleDismiss = {
        if (!uiState.isProcessing) {
            viewModel.reset(initialBeneficiary)
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
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState.step) {
                SendMoneyStep.SELECT_RECEIVER -> {
                    SendMoneySelectReceiverStep(
                        uiState = uiState,
                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                        onBeneficiarySelected = viewModel::onReceiverSelected,
                        onSelectCustomReceiver = viewModel::onSelectCustomReceiver,
                        onClose = handleDismiss
                    )
                }

                SendMoneyStep.INPUT_AMOUNT -> {
                    SendMoneyInputAmountStep(
                        uiState = uiState,
                        onChangeReceiver = viewModel::onChangeReceiver,
                        onAmountChanged = viewModel::onAmountChanged,
                        onQuickAmountSelected = viewModel::onQuickAmountSelected,
                        onUseMaxAmount = viewModel::onUseMaxAmount,
                        onNoteChanged = viewModel::onNoteChanged,
                        onProceed = viewModel::proceedToConfirmation,
                        onBack = {
                            if (!viewModel.backStep()) {
                                handleDismiss()
                            }
                        },
                        onClose = handleDismiss
                    )
                }

                SendMoneyStep.CONFIRMATION -> {
                    SendMoneyConfirmationStep(
                        uiState = uiState,
                        onProceedToPin = viewModel::proceedToPinVerification,
                        onBack = { viewModel.backStep() },
                        onClose = handleDismiss
                    )
                }

                SendMoneyStep.PIN_VERIFICATION -> {
                    SendMoneyPinStep(
                        uiState = uiState,
                        onDigitClick = viewModel::onPinDigit,
                        onBackspaceClick = viewModel::onPinBackspace,
                        onBack = { viewModel.backStep() },
                        onClose = handleDismiss
                    )
                }

                SendMoneyStep.PROCESSING -> {
                    SendMoneyProcessingStep()
                }

                SendMoneyStep.SUCCESS -> {
                    SendMoneySuccessStep(
                        uiState = uiState,
                        onDone = {
                            viewModel.reset(initialBeneficiary)
                            onDismiss()
                        }
                    )
                }

                SendMoneyStep.ERROR -> {
                    SendMoneyErrorStep(
                        errorMessage = uiState.errorMessage ?: "Transfer failed. Please try again.",
                        onRetry = { viewModel.backStep() },
                        onClose = handleDismiss
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 1: SELECT RECEIVER
// -------------------------------------------------------------
@Composable
private fun SendMoneySelectReceiverStep(
    uiState: SendMoneyUiState,
    onSearchQueryChanged: (String) -> Unit,
    onBeneficiarySelected: (Beneficiary) -> Unit,
    onSelectCustomReceiver: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Send Money",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = PocketPayIcons.Close,
                    contentDescription = "Close",
                    tint = NeutralMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Demo Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PrimaryBlue.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = PocketPayIcons.Info,
                    contentDescription = "Demo Info",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Select a demo contact or enter any wallet ID to simulate an instant transfer.",
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Input
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Search name, wallet ID or phone", fontSize = 14.sp, color = NeutralMuted) },
            leadingIcon = {
                Icon(
                    imageVector = PocketPayIcons.Search,
                    contentDescription = "Search",
                    tint = NeutralMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = PocketPayIcons.Close,
                            contentDescription = "Clear",
                            tint = NeutralMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                fontSize = 12.sp,
                color = ExpenseRose,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom search action if user typed something not matching an exact beneficiary
        if (uiState.searchQuery.isNotBlank() && uiState.filteredBeneficiaries.none {
                it.displayName.equals(uiState.searchQuery, ignoreCase = true) ||
                it.walletNumber.equals(uiState.searchQuery, ignoreCase = true)
            }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectCustomReceiver(uiState.searchQuery) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SecondaryTeal.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = PocketPayIcons.Person,
                                contentDescription = "Custom Receiver",
                                tint = SecondaryTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Send to \"${uiState.searchQuery}\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap to transfer to this recipient",
                            fontSize = 12.sp,
                            color = NeutralMuted
                        )
                    }

                    Icon(
                        imageVector = PocketPayIcons.ArrowUp,
                        contentDescription = "Select",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = if (uiState.searchQuery.isBlank()) "Contacts & Beneficiaries" else "Matching Recipients",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NeutralMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        // List of beneficiaries
        if (uiState.filteredBeneficiaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No recipients found for \"${uiState.searchQuery}\"",
                        fontSize = 14.sp,
                        color = NeutralMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the suggestion above to send using this wallet or phone.",
                        fontSize = 12.sp,
                        color = PrimaryBlue
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredBeneficiaries, key = { it.beneficiaryId }) { beneficiary ->
                    BeneficiarySelectionItem(
                        beneficiary = beneficiary,
                        onClick = { onBeneficiarySelected(beneficiary) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BeneficiarySelectionItem(
    beneficiary: Beneficiary,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCard,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = beneficiary.avatarInitials,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = beneficiary.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (beneficiary.isFavorite) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = PocketPayIcons.Star,
                            contentDescription = "Favorite",
                            tint = WarningAmber,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = beneficiary.walletNumber,
                    fontSize = 12.sp,
                    color = NeutralMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryBlue.copy(alpha = 0.08f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "Select",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 2: INPUT AMOUNT & NOTE
// -------------------------------------------------------------
@Composable
private fun SendMoneyInputAmountStep(
    uiState: SendMoneyUiState,
    onChangeReceiver: () -> Unit,
    onAmountChanged: (String) -> Unit,
    onQuickAmountSelected: (Int) -> Unit,
    onUseMaxAmount: () -> Unit,
    onNoteChanged: (String) -> Unit,
    onProceed: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = PocketPayIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = NeutralDark
                )
            }

            Text(
                text = "Transfer Amount",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = PocketPayIcons.Close,
                    contentDescription = "Close",
                    tint = NeutralMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Recipient Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.selectedReceiver?.avatarInitials ?: "PP",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.receiverDisplayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = uiState.receiverWalletNumber,
                        fontSize = 12.sp,
                        color = NeutralMuted
                    )
                }

                Text(
                    text = "Change",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onChangeReceiver)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Available Balance Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = PrimaryBlue.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Available Balance",
                        fontSize = 12.sp,
                        color = NeutralMuted
                    )
                    Text(
                        text = Formatters.formatCentsToCurrency(uiState.availableBalanceCents),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PrimaryBlue,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onUseMaxAmount)
                ) {
                    Text(
                        text = "Use Max",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Amount Input Field
        Text(
            text = "Enter Amount (USD)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = NeutralMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.amountInput,
            onValueChange = onAmountChanged,
            prefix = {
                Text(
                    text = "$",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            },
            placeholder = {
                Text(
                    text = "0.00",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralLight
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Amount Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            uiState.quickAmounts.forEach { amount ->
                val isSelected = uiState.amountInput == amount.toString()
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) PrimaryBlue else SurfaceCard,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onQuickAmountSelected(amount) }
                ) {
                    Text(
                        text = "+$$amount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Note Input
        OutlinedTextField(
            value = uiState.noteInput,
            onValueChange = onNoteChanged,
            placeholder = { Text("What's this for? (optional)", fontSize = 14.sp, color = NeutralMuted) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Error message if any
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = error,
                fontSize = 13.sp,
                color = ExpenseRose,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Review Transfer Button
        Button(
            onClick = onProceed,
            enabled = uiState.isValidAmount,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Review Transfer",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// -------------------------------------------------------------
// STEP 3: CONFIRMATION
// -------------------------------------------------------------
@Composable
private fun SendMoneyConfirmationStep(
    uiState: SendMoneyUiState,
    onProceedToPin: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = PocketPayIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = NeutralDark
                )
            }

            Text(
                text = "Confirm Transfer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = PocketPayIcons.Close,
                    contentDescription = "Close",
                    tint = NeutralMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Amount Display
        Text(
            text = Formatters.formatCentsToCurrency(uiState.amountCents),
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryBlue
        )

        Text(
            text = "to ${uiState.receiverDisplayName}",
            fontSize = 15.sp,
            color = NeutralMuted,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Breakdown Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                ConfirmationRow(
                    label = "Recipient",
                    value = uiState.receiverDisplayName,
                    isBold = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                ConfirmationRow(
                    label = "Wallet Number",
                    value = uiState.receiverWalletNumber
                )
                Spacer(modifier = Modifier.height(12.dp))
                ConfirmationRow(
                    label = "Transfer Amount",
                    value = Formatters.formatCentsToCurrency(uiState.amountCents)
                )
                Spacer(modifier = Modifier.height(12.dp))
                ConfirmationRow(
                    label = "Transfer Fee",
                    value = "$0.00 (Demo Free)",
                    valueColor = SuccessGreen
                )

                if (uiState.noteInput.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ConfirmationRow(
                        label = "Note",
                        value = uiState.noteInput
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(14.dp))

                ConfirmationRow(
                    label = "Total Debit",
                    value = Formatters.formatCentsToCurrency(uiState.totalDebitCents),
                    valueColor = ExpenseRose,
                    isBold = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                ConfirmationRow(
                    label = "Remaining Balance",
                    value = Formatters.formatCentsToCurrency(uiState.remainingBalanceCents)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Demo Notice
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = WarningAmber.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = PocketPayIcons.Info,
                    contentDescription = "Notice",
                    tint = WarningAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Demo Verification: You will be asked for your demo PIN (1234) on the next step.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Button(
            onClick = onProceedToPin,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Confirm & Enter PIN",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Edit Amount",
                fontSize = 15.sp,
                color = NeutralDark
            )
        }
    }
}

@Composable
private fun ConfirmationRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = NeutralMuted
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -------------------------------------------------------------
// STEP 4: PIN VERIFICATION
// -------------------------------------------------------------
@Composable
private fun SendMoneyPinStep(
    uiState: SendMoneyUiState,
    onDigitClick: (Char) -> Unit,
    onBackspaceClick: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = PocketPayIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = NeutralDark
                )
            }

            Text(
                text = "Security Verification",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = PocketPayIcons.Close,
                    contentDescription = "Close",
                    tint = NeutralMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = CircleShape,
            color = PrimaryBlue.copy(alpha = 0.12f),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = PocketPayIcons.Lock,
                    contentDescription = "Lock",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Enter Demo PIN",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Default Demo PIN is 1234",
            fontSize = 13.sp,
            color = PrimaryBlue,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // PIN Indicator Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val isFilled = i < uiState.pinInput.length
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 2.dp,
                            color = if (isFilled) PrimaryBlue else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // PIN Error Text
        uiState.pinError?.let { err ->
            Text(
                text = err,
                fontSize = 13.sp,
                color = ExpenseRose,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Numeric Keypad
        PinKeypad(
            onDigitClick = onDigitClick,
            onBackspaceClick = onBackspaceClick
        )
    }
}

@Composable
private fun PinKeypad(
    onDigitClick: (Char) -> Unit,
    onBackspaceClick: () -> Unit
) {
    val keypadRows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf(null, '0', '<')
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        keypadRows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    when (key) {
                        null -> {
                            Spacer(modifier = Modifier.size(64.dp))
                        }
                        '<' -> {
                            Surface(
                                shape = CircleShape,
                                color = SurfaceCard,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onBackspaceClick)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = PocketPayIcons.Backspace,
                                        contentDescription = "Backspace",
                                        tint = NeutralDark,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                        else -> {
                            Surface(
                                shape = CircleShape,
                                color = SurfaceCard,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .clickable { onDigitClick(key) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key.toString(),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 5: PROCESSING
// -------------------------------------------------------------
@Composable
private fun SendMoneyProcessingStep() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = PrimaryBlue,
            strokeWidth = 4.dp,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Processing Transfer...",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Debiting wallet and synchronizing ledger records.",
            fontSize = 13.sp,
            color = NeutralMuted,
            textAlign = TextAlign.Center
        )
    }
}

// -------------------------------------------------------------
// STEP 6: SUCCESS RECEIPT
// -------------------------------------------------------------
@Composable
private fun SendMoneySuccessStep(
    uiState: SendMoneyUiState,
    onDone: () -> Unit
) {
    val tx = uiState.completedTransaction

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Success Icon
        Surface(
            shape = CircleShape,
            color = SuccessGreen.copy(alpha = 0.14f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = PocketPayIcons.CheckCircle,
                    contentDescription = "Success",
                    tint = SuccessGreen,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Transfer Sent Successfully!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "-${Formatters.formatCentsToCurrency(tx?.amount ?: uiState.amountCents)}",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ExpenseRose
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Receipt Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                ReceiptRow(label = "Recipient", value = tx?.receiverName ?: uiState.receiverDisplayName)
                Spacer(modifier = Modifier.height(10.dp))
                ReceiptRow(label = "Wallet Number", value = uiState.receiverWalletNumber)
                Spacer(modifier = Modifier.height(10.dp))
                ReceiptRow(label = "Reference No.", value = tx?.referenceNumber ?: "REF-SND-0000")
                Spacer(modifier = Modifier.height(10.dp))
                ReceiptRow(
                    label = "Date & Time",
                    value = Formatters.formatTransactionTime(tx?.timestamp ?: System.currentTimeMillis())
                )
                Spacer(modifier = Modifier.height(10.dp))
                ReceiptRow(label = "Transfer Fee", value = "$0.00 (Demo Free)")

                if (!tx?.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    ReceiptRow(label = "Note", value = tx?.description ?: "")
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                ReceiptRow(
                    label = "Updated Balance",
                    value = Formatters.formatCentsToCurrency(uiState.newBalanceCents),
                    valueColor = PrimaryBlue,
                    isBold = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Done",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
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
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -------------------------------------------------------------
// STEP 7: ERROR
// -------------------------------------------------------------
@Composable
private fun SendMoneyErrorStep(
    errorMessage: String,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = ExpenseRose.copy(alpha = 0.12f),
            modifier = Modifier.size(68.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = PocketPayIcons.Close,
                    contentDescription = "Error",
                    tint = ExpenseRose,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Transfer Not Completed",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = errorMessage,
            fontSize = 14.sp,
            color = NeutralMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Try Again",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onClose,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Cancel",
                fontSize = 15.sp,
                color = NeutralDark
            )
        }
    }
}
