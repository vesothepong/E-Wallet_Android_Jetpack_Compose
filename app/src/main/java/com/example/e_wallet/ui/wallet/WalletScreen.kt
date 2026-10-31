package com.example.e_wallet.ui.wallet

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.model.BankAccount
import com.example.e_wallet.model.PaymentCard
import com.example.e_wallet.ui.components.BottomNavTab
import com.example.e_wallet.ui.components.PocketPayBottomBar
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.HolographicCardEnd
import com.example.e_wallet.ui.theme.HolographicCardStart
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.ui.theme.WarningAmber
import com.example.e_wallet.util.Formatters

/**
 * Screen 16: Cards & Accounts Hub (Tab 3)
 * Matches Image 17/19 and specifications:
 * - Header: "My Cards & Accounts" with "+ Add" icon button
 * - Virtual Debit Card (320x190px) featuring:
 *   - Holographic dark indigo gradient (#1E1B4B to #312E81)
 *   - Silver EMV chip, contactless payment icon, PocketPay logo in white
 *   - Masked card number "•••• •••• •••• 5521", expiry "09/29", Visa Platinum badge
 * - Card Action Controls (Row of 3 pill buttons under card):
 *   1. "❄️ Freeze Card" (Interactive toggle)
 *   2. "🔒 Online Payments" (Active green badge)
 *   3. "👁️ Show Details"
 * - Linked Bank Accounts Section:
 *   - Chase Bank Checking (•••• 4412) with bank icon and "$8,320.00" balance
 *   - Bank of America Savings (•••• 9011) with bank icon and "$14,500.00" balance
 * - Bottom navigation bar with "Cards" tab active in indigo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBackClick: () -> Unit = {},
    onTabSelected: (BottomNavTab) -> Unit = {}
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
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            PocketPayBottomBar(
                currentTab = BottomNavTab.CARDS,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Header: My Cards & Accounts + "+ Add" Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = PocketPayIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "My Cards & Accounts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    modifier = Modifier.clickable { viewModel.openAddCardDialog() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = PocketPayIcons.Add,
                            contentDescription = "Add Card",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Premium Virtual Debit Card Carousel
            val card = uiState.selectedCard ?: uiState.cards.firstOrNull()
            if (card != null) {
                PremiumVirtualDebitCard(
                    card = card,
                    isRevealed = uiState.isDetailsRevealed
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Row of 3 Pill Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Freeze Card
                val isFrozen = card?.isFrozen == true
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isFrozen) PrimaryBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isFrozen) PrimaryBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleFreezeCard() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = PocketPayIcons.Snowflake,
                            contentDescription = "Freeze",
                            tint = if (isFrozen) PrimaryBlue else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFrozen) "Frozen" else "Freeze",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isFrozen) PrimaryBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 2. Online Payments
                val isOnline = card?.isOnlinePaymentEnabled != false
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            card?.let { viewModel.toggleOnlinePayments(it.cardId) }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isOnline) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Online Pay",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 3. Show Details
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleRevealDetails() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isDetailsRevealed) PocketPayIcons.VisibilityOff else PocketPayIcons.Visibility,
                            contentDescription = "Details",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isDetailsRevealed) "Hide" else "Details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Linked Bank Accounts Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Linked Bank Accounts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "2 Accounts",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bank Account 1: Chase Bank Checking ($8,320.00)
            BankAccountRow(
                bankName = "Chase Bank",
                accountType = "Checking",
                last4 = "4412",
                balance = "$8,320.00",
                iconColor = Color(0xFF1E3A8A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bank Account 2: Bank of America Savings ($14,500.00)
            BankAccountRow(
                bankName = "Bank of America",
                accountType = "Savings",
                last4 = "9011",
                balance = "$14,500.00",
                iconColor = Color(0xFFDC2626)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // "+ Link New Bank Account" Button
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { viewModel.openAddBankDialog() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = PocketPayIcons.Add,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Link New Bank Account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Add Card Dialog
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

        // Add Bank Dialog
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
    }
}

/**
 * Premium Holographic Virtual Debit Card (320x190px)
 */
@Composable
private fun PremiumVirtualDebitCard(
    card: PaymentCard,
    isRevealed: Boolean
) {
    val cardBackgroundBrush = Brush.linearGradient(
        colors = listOf(
            HolographicCardStart, // #1E1B4B
            HolographicCardEnd    // #312E81
        ),
        start = Offset(0f, 0f),
        end = Offset(700f, 400f)
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp), spotColor = HolographicCardStart),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBackgroundBrush)
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: PocketPay Logo + Contactless Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "P",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PocketPay",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Contactless waves & EMV chip
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PocketPayIcons.Wifi,
                            contentDescription = "Contactless",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFD4AF37), // Metallic gold EMV chip
                            modifier = Modifier
                                .width(34.dp)
                                .height(26.dp)
                        ) {}
                    }
                }

                // Middle: Masked Card Number
                val cardNumberDisplay = if (isRevealed) {
                    "4421  8829  9011  5521"
                } else {
                    "••••  ••••  ••••  5521"
                }

                Text(
                    text = cardNumberDisplay,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = Color.White
                )

                // Bottom Row: Cardholder Name, Expiry, VISA Platinum
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "CARD HOLDER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "KANTUMRUY DEV",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column {
                        Text(
                            text = "EXPIRES",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "09/29",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "VISA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Linked Bank Account Item Row
 */
@Composable
private fun BankAccountRow(
    bankName: String,
    accountType: String,
    last4: String,
    balance: String,
    iconColor: Color
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = iconColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = PocketPayIcons.AccountBalance,
                            contentDescription = bankName,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "$bankName $accountType",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "•••• $last4",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = balance,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
private fun AddCardBottomSheet(
    holderName: String,
    network: com.example.e_wallet.model.CardNetwork,
    type: com.example.e_wallet.model.CardType,
    onHolderNameChanged: (String) -> Unit,
    onNetworkChanged: (com.example.e_wallet.model.CardNetwork) -> Unit,
    onTypeChanged: (com.example.e_wallet.model.CardType) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
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
                        com.example.e_wallet.model.CardNetwork.values().forEach { net ->
                            val isSelected = net == network
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { onNetworkChanged(net) }
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
                        listOf(com.example.e_wallet.model.CardType.VIRTUAL_DEBIT, com.example.e_wallet.model.CardType.PHYSICAL_DEBIT).forEach { t ->
                            val isSelected = t == type
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { onTypeChanged(t) }
                            ) {
                                Text(
                                    text = t.displayName,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
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
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

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
    androidx.compose.material3.AlertDialog(
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
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
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
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
