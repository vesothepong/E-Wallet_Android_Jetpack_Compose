package com.example.e_wallet.ui.bills

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.BillCategory
import com.example.e_wallet.model.Biller
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SecondaryTeal
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.ui.theme.WarningAmber
import com.example.e_wallet.util.Formatters

/**
 * Top-level screen for paying utility bills, telecom mobile top-ups, internet, and school fees.
 * Supports a step-by-step verified workflow with 4-digit PIN verification and receipt generation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillPaymentScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialCategory: BillCategory? = null,
    initialBiller: Biller? = null,
    repository: WalletRepository = WalletRepositoryProvider.repository,
    viewModel: BillPaymentViewModel = remember(repository) {
        BillPaymentViewModel(
            repository = repository,
            initialCategory = initialCategory,
            initialBiller = initialBiller
        )
    }
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val handleBack: () -> Unit = {
        if (!viewModel.onStepBack()) {
            onBackClick()
        }
    }

    val topBarTitle = when (uiState.currentStep) {
        BillPaymentStep.SELECT_BILLER -> "Bills & Utilities"
        BillPaymentStep.ENTER_DETAILS -> uiState.selectedBiller?.name ?: "Payment Details"
        BillPaymentStep.CONFIRM_REVIEW -> "Review Payment"
        BillPaymentStep.PIN_VERIFICATION -> "Security Verification"
        BillPaymentStep.SUCCESS -> "Payment Receipt"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = topBarTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (uiState.currentStep != BillPaymentStep.SUCCESS) {
                        IconButton(onClick = handleBack) {
                            Icon(
                                imageVector = PocketPayIcons.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.currentStep != BillPaymentStep.SELECT_BILLER &&
                        uiState.currentStep != BillPaymentStep.SUCCESS
                    ) {
                        IconButton(onClick = { viewModel.reset() }) {
                            Icon(
                                imageVector = PocketPayIcons.Close,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (uiState.currentStep) {
                BillPaymentStep.SELECT_BILLER -> {
                    BillerCatalogContent(
                        uiState = uiState,
                        onCategorySelected = viewModel::selectCategory,
                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                        onBillerSelected = viewModel::selectBiller
                    )
                }
                BillPaymentStep.ENTER_DETAILS -> {
                    BillerDetailsContent(
                        uiState = uiState,
                        onAccountNumberChanged = viewModel::onAccountNumberChanged,
                        onAmountChanged = viewModel::onAmountChanged,
                        onPresetSelected = viewModel::selectPresetAmount,
                        onNoteChanged = viewModel::onNoteChanged,
                        onContinue = { viewModel.proceedToReview() },
                        onBack = handleBack
                    )
                }
                BillPaymentStep.CONFIRM_REVIEW -> {
                    BillReviewContent(
                        uiState = uiState,
                        onConfirm = { viewModel.proceedToPin() },
                        onBack = handleBack
                    )
                }
                BillPaymentStep.PIN_VERIFICATION -> {
                    BillPinVerificationContent(
                        uiState = uiState,
                        onDigit = viewModel::onPinDigit,
                        onBackspace = viewModel::onPinBackspace,
                        onCancel = handleBack
                    )
                }
                BillPaymentStep.SUCCESS -> {
                    BillReceiptContent(
                        uiState = uiState,
                        onDone = {
                            viewModel.reset()
                            onBackClick()
                        },
                        onPayAnother = {
                            viewModel.reset()
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Step 1: Biller Catalog Content
// ---------------------------------------------------------------------------
@Composable
private fun BillerCatalogContent(
    uiState: BillPaymentUiState,
    onCategorySelected: (BillCategory?) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onBillerSelected: (Biller) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Search provider, utility, or telecom...") },
            leadingIcon = {
                Icon(
                    imageVector = PocketPayIcons.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = PocketPayIcons.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("All Services") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            items(BillCategory.entries) { category ->
                val isSelected = uiState.selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Biller List
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (uiState.selectedCategory == null) "All Providers (${uiState.filteredBillers.size})"
                else "${uiState.selectedCategory.title} Providers (${uiState.filteredBillers.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (uiState.filteredBillers.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = PocketPayIcons.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No billers found",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Try searching with a different provider or service name.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                uiState.filteredBillers.forEach { biller ->
                    BillerCardItem(
                        biller = biller,
                        onClick = { onBillerSelected(biller) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun BillerCardItem(
    biller: Biller,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            val (icon, tint) = getCategoryIconAndColor(biller.category)
            Surface(
                shape = CircleShape,
                color = tint.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = biller.name,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = biller.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (biller.isPopular) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "POPULAR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${biller.category.title} • ${if (biller.feeCents == 0L) "Free Payment" else "Fee: ${Formatters.formatCentsToCurrency(biller.feeCents)}"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = PocketPayIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Step 2: Biller Details & Amount Input Content
// ---------------------------------------------------------------------------
@Composable
private fun BillerDetailsContent(
    uiState: BillPaymentUiState,
    onAccountNumberChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onPresetSelected: (Long) -> Unit,
    onNoteChanged: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val biller = uiState.selectedBiller ?: return
    val (icon, tint) = getCategoryIconAndColor(biller.category)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Provider Header Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = tint.copy(alpha = 0.12f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = biller.name,
                            tint = tint,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = biller.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${biller.category.title} • Instant Processing",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Account Number Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = biller.category.prompt,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedTextField(
                value = uiState.accountNumber,
                onValueChange = onAccountNumberChanged,
                placeholder = { Text(biller.accountHint) },
                singleLine = true,
                isError = uiState.accountNumberError != null,
                supportingText = {
                    uiState.accountNumberError?.let {
                        Text(text = it, color = ExpenseRose, fontSize = 12.sp)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Bill Amount Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment Amount",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Balance: ${Formatters.formatCentsToCurrency(uiState.walletBalanceCents)}",
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
            }

            OutlinedTextField(
                value = uiState.amountInput,
                onValueChange = onAmountChanged,
                placeholder = { Text("0.00") },
                prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = uiState.amountError != null,
                supportingText = {
                    uiState.amountError?.let {
                        Text(text = it, color = ExpenseRose, fontSize = 12.sp)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Quick Preset Amounts
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Preset Amounts",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                biller.standardAmountsCents.forEach { presetCents ->
                    val isSelected = uiState.selectedPresetCents == presetCents
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surface,
                        border = if (!isSelected) borderStroke() else null,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPresetSelected(presetCents) }
                    ) {
                        Text(
                            text = Formatters.formatCentsToCurrency(presetCents),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // Memo / Note Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Payment Memo (Optional)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedTextField(
                value = uiState.note,
                onValueChange = onNoteChanged,
                placeholder = { Text("e.g. October Apartment Electric") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Continue Button
        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Review Payment",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Step 3: Review Payment Content
// ---------------------------------------------------------------------------
@Composable
private fun BillReviewContent(
    uiState: BillPaymentUiState,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val biller = uiState.selectedBiller ?: return
    val (icon, tint) = getCategoryIconAndColor(biller.category)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Summary Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = tint.copy(alpha = 0.12f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = biller.name,
                            tint = tint,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = biller.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = biller.category.title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = Formatters.formatCentsToCurrency(uiState.totalDebitCents),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                ReviewRow(label = biller.category.prompt, value = uiState.accountNumber)
                ReviewRow(label = "Bill Amount", value = Formatters.formatCentsToCurrency(uiState.amountCents))
                ReviewRow(
                    label = "Service Fee",
                    value = if (uiState.feeCents == 0L) "Free" else Formatters.formatCentsToCurrency(uiState.feeCents)
                )
                ReviewRow(label = "Payment Source", value = "PocketPay Wallet Balance")
                if (uiState.note.isNotBlank()) {
                    ReviewRow(label = "Note", value = uiState.note)
                }
            }
        }

        // Security Assurance Pill
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PrimaryBlue.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = PocketPayIcons.Shield,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Requires 4-digit security PIN verification on the next step.",
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Confirm & Pay Button
        Button(
            onClick = onConfirm,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Authenticate & Pay",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = "Modify Details")
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------------------------------------------------------------------------
// Step 4: PIN Security Verification Content
// ---------------------------------------------------------------------------
@Composable
private fun BillPinVerificationContent(
    uiState: BillPaymentUiState,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.12f),
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = PocketPayIcons.Lock,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter Security PIN",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Confirm payment of ${Formatters.formatCentsToCurrency(uiState.totalDebitCents)} to ${uiState.selectedBiller?.name}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 4 PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isFilled = index < uiState.pinInput.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) PrimaryBlue
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                    )
                }
            }

            // Error or Loading Indicator
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .padding(top = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = PrimaryBlue
                    )
                } else if (uiState.pinError != null) {
                    Text(
                        text = uiState.pinError,
                        color = ExpenseRose,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = "Demo PIN: 1234",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // Numeric Keypad
        NumericKeypad(
            onDigit = onDigit,
            onBackspace = onBackspace,
            isEnabled = !uiState.isProcessing
        )
    }
}

@Composable
private fun NumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    isEnabled: Boolean
) {
    val keypadLayout = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        keypadLayout.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (key) {
                            "" -> { /* Empty spacer */ }
                            "DEL" -> {
                                IconButton(
                                    onClick = onBackspace,
                                    enabled = isEnabled,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Icon(
                                        imageVector = PocketPayIcons.Backspace,
                                        contentDescription = "Backspace",
                                        tint = if (isEnabled) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                            else -> {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .clickable(enabled = isEnabled) { onDigit(key) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = key,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isEnabled) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
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
}

// ---------------------------------------------------------------------------
// Step 5: Success Receipt Content
// ---------------------------------------------------------------------------
@Composable
private fun BillReceiptContent(
    uiState: BillPaymentUiState,
    onDone: () -> Unit,
    onPayAnother: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val biller = uiState.selectedBiller
    val ref = uiState.receiptReference ?: uiState.completedTransaction?.referenceNumber ?: "N/A"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Success Icon
        Surface(
            shape = CircleShape,
            color = SuccessGreen.copy(alpha = 0.15f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = PocketPayIcons.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Text(
            text = "Payment Successful!",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = Formatters.formatCentsToCurrency(uiState.totalDebitCents),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = SuccessGreen
        )

        // Receipt Details Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReceiptItem(label = "Biller / Provider", value = biller?.name ?: "Utility Provider")
                ReceiptItem(label = "Category", value = biller?.category?.title ?: "Services")
                ReceiptItem(label = "Account / Meter", value = uiState.accountNumber)
                ReceiptItem(label = "Amount", value = Formatters.formatCentsToCurrency(uiState.amountCents))
                if (uiState.feeCents > 0L) {
                    ReceiptItem(label = "Service Fee", value = Formatters.formatCentsToCurrency(uiState.feeCents))
                }
                ReceiptItem(label = "Reference No.", value = ref, isCopiable = true, onCopy = {
                    clipboardManager.setText(AnnotatedString(ref))
                })
                ReceiptItem(
                    label = "Remaining Balance",
                    value = Formatters.formatCentsToCurrency(uiState.walletBalanceCents)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Done Button
        Button(
            onClick = onDone,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Return to Home",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        OutlinedButton(
            onClick = onPayAnother,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = "Pay Another Bill")
        }
    }
}

@Composable
private fun ReceiptItem(
    label: String,
    value: String,
    isCopiable: Boolean = false,
    onCopy: () -> Unit = {}
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isCopiable) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = PocketPayIcons.ContentCopy,
                    contentDescription = "Copy",
                    tint = PrimaryBlue,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onCopy)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
private fun getCategoryIconAndColor(category: BillCategory): Pair<ImageVector, Color> {
    return when (category) {
        BillCategory.ELECTRICITY -> PocketPayIcons.FlashOn to WarningAmber
        BillCategory.WATER -> PocketPayIcons.Water to SecondaryTeal
        BillCategory.INTERNET -> PocketPayIcons.Wifi to PrimaryBlue
        BillCategory.MOBILE_TOPUP -> PocketPayIcons.Smartphone to SuccessGreen
        BillCategory.EDUCATION -> PocketPayIcons.School to PrimaryBlue
        BillCategory.GOVERNMENT -> PocketPayIcons.AccountBalance to WarningAmber
    }
}

@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(
    1.dp,
    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
)
