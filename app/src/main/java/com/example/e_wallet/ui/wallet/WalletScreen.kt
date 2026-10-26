package com.example.e_wallet.ui.wallet

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.model.BankAccount
import com.example.e_wallet.model.CardNetwork
import com.example.e_wallet.model.CardType
import com.example.e_wallet.model.PaymentCard
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.components.TransactionItemRow
import com.example.e_wallet.ui.deposit.DepositBottomSheet
import com.example.e_wallet.ui.send.SendMoneyBottomSheet
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SecondaryTeal
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.ui.theme.WarningAmber
import com.example.e_wallet.ui.withdraw.WithdrawBottomSheet
import com.example.e_wallet.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Wallet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = PocketPayIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::openAddCardDialog) {
                        Icon(
                            imageVector = PocketPayIcons.Add,
                            contentDescription = "Add Card",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Total Balance & Fast Transfer Actions
            WalletBalanceHeader(
                balanceCents = uiState.wallet.currentBalance,
                currency = uiState.wallet.currency,
                onDepositClick = viewModel::openDepositSheet,
                onSendClick = viewModel::openSendSheet,
                onWithdrawClick = viewModel::openWithdrawSheet
            )

            // 2. Cards Carousel (Horizontal Swipeable / Selectable)
            CardsCarouselSection(
                cards = uiState.cards,
                selectedIndex = uiState.selectedCardIndex,
                isRevealed = uiState.isDetailsRevealed,
                onCardSelected = viewModel::selectCard,
                onToggleReveal = viewModel::toggleRevealDetails,
                onAddCardClick = viewModel::openAddCardDialog
            )

            // 3. Selected Card Management Controls
            uiState.selectedCard?.let { card ->
                CardControlsSection(
                    card = card,
                    onToggleFreeze = viewModel::toggleFreezeCard,
                    onToggleOnline = { viewModel.toggleOnlinePayments(card.cardId) },
                    onToggleInternational = { viewModel.toggleInternationalPayments(card.cardId) },
                    onSetPrimary = { viewModel.setPrimaryCard(card.cardId) },
                    onEditLimitClick = viewModel::openEditLimitDialog
                )
            }

            // 4. Linked Bank Accounts
            LinkedBankAccountsSection(
                accounts = uiState.bankAccounts,
                onAddBankClick = viewModel::openAddBankDialog
            )

            // 5. Recent Wallet Transactions
            if (uiState.recentCardTransactions.isNotEmpty()) {
                RecentCardActivitySection(
                    transactions = uiState.recentCardTransactions
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Modals & Sheets
        if (uiState.showAddCardDialog) {
            AddCardBottomSheet(
                holderName = uiState.addCardHolderName,
                network = uiState.addCardNetwork,
                type = uiState.addCardType,
                onHolderNameChanged = viewModel::onAddCardHolderChanged,
                onNetworkChanged = viewModel::onAddCardNetworkChanged,
                onTypeChanged = viewModel::onAddCardTypeChanged,
                onDismiss = viewModel::dismissAddCardDialog,
                onSubmit = viewModel::submitAddCard
            )
        }

        if (uiState.showAddBankDialog) {
            AddBankBottomSheet(
                bankName = uiState.addBankName,
                accountType = uiState.addBankAccountType,
                last4 = uiState.addBankLast4,
                onBankNameChanged = viewModel::onAddBankNameChanged,
                onAccountTypeChanged = viewModel::onAddBankAccountTypeChanged,
                onLast4Changed = viewModel::onAddBankLast4Changed,
                onDismiss = viewModel::dismissAddBankDialog,
                onSubmit = viewModel::submitAddBank
            )
        }

        if (uiState.showEditLimitDialog) {
            EditLimitDialog(
                currentInput = uiState.editLimitInput,
                onInputChanged = viewModel::onLimitInputChanged,
                onDismiss = viewModel::dismissEditLimitDialog,
                onSave = viewModel::saveSpendingLimit
            )
        }

        if (uiState.showDepositSheet) {
            DepositBottomSheet(
                onDismiss = viewModel::dismissDepositSheet,
                repository = viewModel.repository
            )
        }

        if (uiState.showWithdrawSheet) {
            WithdrawBottomSheet(
                onDismiss = viewModel::dismissWithdrawSheet,
                repository = viewModel.repository
            )
        }

        if (uiState.showSendSheet) {
            SendMoneyBottomSheet(
                onDismiss = viewModel::dismissSendSheet,
                repository = viewModel.repository
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 1. Balance Header with Quick Action Buttons
// ---------------------------------------------------------------------------
@Composable
private fun WalletBalanceHeader(
    balanceCents: Long,
    currency: String,
    onDepositClick: () -> Unit,
    onSendClick: () -> Unit,
    onWithdrawClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Balance",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = Formatters.formatCentsToCurrency(balanceCents),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SuccessGreen.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "● Available for Instant Use ($currency)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(14.dp))

            // Action Pills Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WalletQuickButton(
                    label = "Top Up",
                    icon = PocketPayIcons.Add,
                    color = SuccessGreen,
                    onClick = onDepositClick
                )

                WalletQuickButton(
                    label = "Send",
                    icon = PocketPayIcons.Send,
                    color = PrimaryBlue,
                    onClick = onSendClick
                )

                WalletQuickButton(
                    label = "Cash Out",
                    icon = PocketPayIcons.ArrowUp,
                    color = ExpenseRose,
                    onClick = onWithdrawClick
                )
            }
        }
    }
}

@Composable
private fun WalletQuickButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.12f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------------------------------------------------------------------------
// 2. Cards Carousel Section
// ---------------------------------------------------------------------------
@Composable
private fun CardsCarouselSection(
    cards: List<PaymentCard>,
    selectedIndex: Int,
    isRevealed: Boolean,
    onCardSelected: (Int) -> Unit,
    onToggleReveal: () -> Unit,
    onAddCardClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cards (${cards.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            TextButton(onClick = onToggleReveal) {
                Icon(
                    imageVector = if (isRevealed) PocketPayIcons.VisibilityOff else PocketPayIcons.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = PrimaryBlue
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isRevealed) "Hide Details" else "Show Details",
                    fontSize = 12.sp,
                    color = PrimaryBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(cards) { index, card ->
                val isSelected = index == selectedIndex
                SingleCardItem(
                    card = card,
                    isSelected = isSelected,
                    isRevealed = isRevealed,
                    onClick = { onCardSelected(index) }
                )
            }

            item {
                AddCardPlaceholder(onClick = onAddCardClick)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dot indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            cards.forEachIndexed { index, _ ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isSelected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}

@Composable
private fun SingleCardItem(
    card: PaymentCard,
    isSelected: Boolean,
    isRevealed: Boolean,
    onClick: () -> Unit
) {
    val gradientColors = if (card.isFrozen) {
        listOf(Color(0xFF475569), Color(0xFF1E293B))
    } else {
        listOf(Color(card.gradientStartColorHex), Color(card.gradientEndColorHex))
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .width(280.dp)
            .height(175.dp)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) PrimaryBlue else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Type badge & frozen / primary tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PocketPay",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (card.isPrimary) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "PRIMARY",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (card.isFrozen) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ExpenseRose.copy(alpha = 0.8f)
                            ) {
                                Text(
                                    text = "FROZEN",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Middle: Chip + Card Number
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(width = 28.dp, height = 20.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFEAB308))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ")))",
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isRevealed) card.fullCardNumber else card.cardNumberMasked,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                }

                // Bottom: Cardholder, Expiry & Network
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = card.cardholderName.uppercase(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isRevealed) "EXP ${card.expiryDate}  CVV ${card.cvv}" else "EXP ${card.expiryDate}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = card.cardNetwork.displayName.uppercase(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCardPlaceholder(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .width(130.dp)
            .height(175.dp)
            .clickable(onClick = onClick)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = PocketPayIcons.Add,
                        contentDescription = "Add Card",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add Card",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 3. Card Management Controls Section
// ---------------------------------------------------------------------------
@Composable
private fun CardControlsSection(
    card: PaymentCard,
    onToggleFreeze: () -> Unit,
    onToggleOnline: () -> Unit,
    onToggleInternational: () -> Unit,
    onSetPrimary: () -> Unit,
    onEditLimitClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${card.cardType.displayName} Controls",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!card.isPrimary) {
                    OutlinedButton(
                        onClick = onSetPrimary,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Make Primary", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Monthly Limit Progress
            val limitProgress = (card.monthlySpentCents.toFloat() / card.monthlyLimitCents.toFloat()).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Spending Limit",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${Formatters.formatCentsToCurrency(card.monthlySpentCents)} / ${Formatters.formatCentsToCurrency(card.monthlyLimitCents)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(onClick = onEditLimitClick) {
                    Text("Edit Limit", fontSize = 12.sp, color = PrimaryBlue)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { limitProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryBlue,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            // Freeze Card Switch
            ControlSwitchRow(
                title = if (card.isFrozen) "Card is Frozen" else "Freeze Card",
                subtitle = if (card.isFrozen) "Payments currently blocked" else "Instant block on all purchases",
                isChecked = card.isFrozen,
                onCheckedChange = { onToggleFreeze() },
                activeColor = ExpenseRose
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Online Payments Switch
            ControlSwitchRow(
                title = "Online Transactions",
                subtitle = "Allow e-commerce & in-app purchases",
                isChecked = card.isOnlinePaymentEnabled,
                onCheckedChange = { onToggleOnline() }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // International Payments Switch
            ControlSwitchRow(
                title = "International Usage",
                subtitle = "Allow overseas payments and forex conversion",
                isChecked = card.isInternationalEnabled,
                onCheckedChange = { onToggleInternational() }
            )
        }
    }
}

@Composable
private fun ControlSwitchRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeColor: Color = PrimaryBlue
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = activeColor
            )
        )
    }
}

// ---------------------------------------------------------------------------
// 4. Linked Bank Accounts Section
// ---------------------------------------------------------------------------
@Composable
private fun LinkedBankAccountsSection(
    accounts: List<BankAccount>,
    onAddBankClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Linked Bank Accounts",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = onAddBankClick) {
                    Text("+ Link Bank", fontSize = 12.sp, color = PrimaryBlue)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            accounts.forEachIndexed { index, bank ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SecondaryTeal.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = PocketPayIcons.Wallet,
                                    contentDescription = null,
                                    tint = SecondaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = bank.bankName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (bank.isPrimary) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = PrimaryBlue.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "PRIMARY",
                                            color = PrimaryBlue,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${bank.accountType} • ${bank.accountNumberMasked}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = Formatters.formatCentsToCurrency(bank.balanceCents),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (index < accounts.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 5. Recent Activity Section
// ---------------------------------------------------------------------------
@Composable
private fun RecentCardActivitySection(transactions: List<Transaction>) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Recent Wallet Activity",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            transactions.take(4).forEachIndexed { index, tx ->
                TransactionItemRow(
                    transaction = tx,
                    onClick = {}
                )
                if (index < 3 && index < transactions.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Dialog: Add Card
// ---------------------------------------------------------------------------
@Composable
private fun AddCardBottomSheet(
    holderName: String,
    network: CardNetwork,
    type: CardType,
    onHolderNameChanged: (String) -> Unit,
    onNetworkChanged: (CardNetwork) -> Unit,
    onTypeChanged: (CardType) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Issue New Card", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = holderName,
                    onValueChange = onHolderNameChanged,
                    label = { Text("Cardholder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Network Selector
                Column {
                    Text("Payment Network", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CardNetwork.values().forEach { net ->
                            val isSelected = net == network
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { onNetworkChanged(net) }
                            ) {
                                Text(
                                    text = net.displayName,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Type Selector
                Column {
                    Text("Card Format", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(CardType.VIRTUAL_DEBIT, CardType.PHYSICAL_DEBIT).forEach { t ->
                            val isSelected = t == type
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { onTypeChanged(t) }
                            ) {
                                Text(
                                    text = t.displayName,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Issue Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Dialog: Add Bank
// ---------------------------------------------------------------------------
@Composable
private fun AddBankBottomSheet(
    bankName: String,
    accountType: String,
    last4: String,
    onBankNameChanged: (String) -> Unit,
    onAccountTypeChanged: (String) -> Unit,
    onLast4Changed: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Link Bank Account", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = onBankNameChanged,
                    label = { Text("Bank / Institution Name") },
                    placeholder = { Text("e.g. Wells Fargo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Account Type Selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Checking", "Savings").forEach { type ->
                        val isSelected = type == accountType
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onAccountTypeChanged(type) }
                        ) {
                            Text(
                                text = type,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = last4,
                    onValueChange = onLast4Changed,
                    label = { Text("Last 4 Digits of Account") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Link Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Dialog: Edit Monthly Spending Limit
// ---------------------------------------------------------------------------
@Composable
private fun EditLimitDialog(
    currentInput: String,
    onInputChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Set Monthly Limit", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column {
                Text(
                    text = "Specify the maximum amount you can spend with this card in a calendar month.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = currentInput,
                    onValueChange = onInputChanged,
                    label = { Text("Monthly Limit ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
