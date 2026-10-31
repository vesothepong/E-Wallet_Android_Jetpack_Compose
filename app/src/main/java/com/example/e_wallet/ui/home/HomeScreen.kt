package com.example.e_wallet.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.model.Beneficiary
import com.example.e_wallet.model.BillCategory
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.ui.bills.BillPaymentBottomSheet
import com.example.e_wallet.ui.components.BottomNavTab
import com.example.e_wallet.ui.components.HomeTopBar
import com.example.e_wallet.ui.components.PocketPayBottomBar
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.components.QuickActionsSection
import com.example.e_wallet.ui.components.TransactionItemRow
import com.example.e_wallet.ui.components.WalletCard
import com.example.e_wallet.ui.deposit.DepositBottomSheet
import com.example.e_wallet.ui.history.TransactionDetailSheet
import com.example.e_wallet.ui.receive.ReceiveBottomSheet
import com.example.e_wallet.ui.scan.ScanQrBottomSheet
import com.example.e_wallet.ui.send.SendMoneyBottomSheet
import com.example.e_wallet.ui.theme.EWalletTheme
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.LightTextPrimary
import com.example.e_wallet.ui.theme.LightTextSecondary
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.ui.withdraw.WithdrawBottomSheet

/**
 * Screen 4: Home Dashboard
 * Top header, gradient hero balance card ($12,450.80), 4 squircle actions,
 * Monthly Budget progress widget, recent transactions feed, feature shortcuts,
 * and 4-item bottom navigation.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = remember { HomeViewModel() },
    onNavigateToHistory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToBills: () -> Unit = {},
    onNavigateToSavings: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToSplitBill: () -> Unit = {},
    onNavigateToBudgets: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentTab by remember { mutableStateOf(BottomNavTab.HOME) }

    var showDepositSheet by remember { mutableStateOf(false) }
    var showWithdrawSheet by remember { mutableStateOf(false) }
    var showSendSheet by remember { mutableStateOf(false) }
    var showReceiveSheet by remember { mutableStateOf(false) }
    var showScanSheet by remember { mutableStateOf(false) }
    var showBillPaymentSheet by remember { mutableStateOf(false) }
    var selectedBillCategory by remember { mutableStateOf<BillCategory?>(null) }
    var selectedBeneficiaryForSend by remember { mutableStateOf<Beneficiary?>(null) }
    var selectedTransactionForDetail by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            PocketPayBottomBar(
                currentTab = currentTab,
                onTabSelected = { selected ->
                    currentTab = selected
                    when (selected) {
                        BottomNavTab.HOME -> { /* already on home */ }
                        BottomNavTab.TRANSFER -> {
                            currentTab = BottomNavTab.HOME
                            onNavigateToHistory()
                        }
                        BottomNavTab.CARDS -> {
                            currentTab = BottomNavTab.HOME
                            onNavigateToWallet()
                        }
                        BottomNavTab.PROFILE -> {
                            currentTab = BottomNavTab.HOME
                            onNavigateToProfile()
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Top Bar: Greeting, Avatar, Notification & Theme Toggle
            HomeTopBar(
                user = uiState.currentUser,
                unreadNotificationsCount = uiState.unreadNotificationsCount,
                onNotificationClick = onNavigateToNotifications,
                onThemeToggleClick = { viewModel.repository.toggleDarkTheme() },
                onProfileClick = onNavigateToProfile
            )

            // 2. Total Balance Hero Card ($12,450.80)
            WalletCard(
                wallet = uiState.wallet,
                isBalanceVisible = uiState.isBalanceVisible,
                totalReceivedCents = uiState.totalReceivedCents,
                totalSentCents = uiState.totalSentCents,
                onToggleBalanceVisibility = viewModel::toggleBalanceVisibility,
                modifier = Modifier.clickable(onClick = onNavigateToWallet)
            )

            // 3. 4 Action Squircles: Send, Receive, Scan & Pay, Top Up
            QuickActionsSection(
                onActionClick = { action ->
                    when (action) {
                        HomeQuickAction.SEND -> {
                            selectedBeneficiaryForSend = null
                            showSendSheet = true
                        }
                        HomeQuickAction.RECEIVE -> showReceiveSheet = true
                        HomeQuickAction.SCAN_QR -> showScanSheet = true
                        HomeQuickAction.DEPOSIT -> showDepositSheet = true
                        HomeQuickAction.WITHDRAW -> showWithdrawSheet = true
                    }
                }
            )

            // 4. Monthly Budget Card (62% Used, Spent $1,240.00, Limit $2,000.00)
            MonthlyBudgetWidget(
                spentCents = 124000L,
                limitCents = 200000L,
                onClick = onNavigateToBudgets
            )

            // 5. Recent Transactions Section (Apple Store -$799.00, Salary Deposit +$4,250.00)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onNavigateToHistory) {
                        Text(
                            text = "See All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val recentItems = uiState.recentTransactions.take(2)
                    if (recentItems.isNotEmpty()) {
                        recentItems.forEach { tx ->
                            TransactionItemRow(
                                transaction = tx,
                                onClick = { selectedTransactionForDetail = tx }
                            )
                        }
                    } else {
                        // Fallback sample items if empty
                        TransactionItemRow(
                            transaction = Transaction(
                                transactionId = "tx_992140188",
                                type = com.example.e_wallet.model.TransactionType.SEND,
                                senderId = "usr_demo_001",
                                senderName = "Kantumruy Dev",
                                receiverId = "mch_apple",
                                receiverName = "Apple Store",
                                amount = 79900L,
                                fee = 0L,
                                timestamp = System.currentTimeMillis() - 15 * 60 * 1000L,
                                description = "MacBook accessories & adapter",
                                status = com.example.e_wallet.model.TransactionStatus.COMPLETED
                            ),
                            onClick = {
                                selectedTransactionForDetail = Transaction(
                                    transactionId = "tx_992140188",
                                    type = com.example.e_wallet.model.TransactionType.SEND,
                                    senderId = "usr_demo_001",
                                    senderName = "Kantumruy Dev",
                                    receiverId = "mch_apple",
                                    receiverName = "Apple Store",
                                    amount = 79900L,
                                    fee = 0L,
                                    timestamp = System.currentTimeMillis() - 15 * 60 * 1000L,
                                    description = "MacBook accessories & adapter",
                                    status = com.example.e_wallet.model.TransactionStatus.COMPLETED,
                                    referenceNumber = "REF-APL-88219"
                                )
                            }
                        )
                    }
                }
            }

            // 6. Quick Hub Shortcuts (Split Bills, Bill Payments, Savings Goals, Spending Analytics)
            QuickHubShortcuts(
                onSplitBillClick = onNavigateToSplitBill,
                onBillsClick = onNavigateToBills,
                onSavingsClick = onNavigateToSavings,
                onAnalyticsClick = onNavigateToAnalytics
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Modals & Bottom Sheets
        if (showSendSheet) {
            SendMoneyBottomSheet(
                initialBeneficiary = selectedBeneficiaryForSend,
                onDismiss = {
                    showSendSheet = false
                    selectedBeneficiaryForSend = null
                },
                repository = viewModel.repository
            )
        }

        if (showReceiveSheet) {
            ReceiveBottomSheet(
                onDismiss = { showReceiveSheet = false }
            )
        }

        if (showDepositSheet) {
            DepositBottomSheet(
                onDismiss = { showDepositSheet = false },
                repository = viewModel.repository
            )
        }

        if (showWithdrawSheet) {
            WithdrawBottomSheet(
                onDismiss = { showWithdrawSheet = false },
                repository = viewModel.repository
            )
        }

        if (showScanSheet) {
            ScanQrBottomSheet(
                onDismiss = { showScanSheet = false },
                repository = viewModel.repository
            )
        }

        if (showBillPaymentSheet) {
            BillPaymentBottomSheet(
                initialCategory = selectedBillCategory,
                onDismiss = {
                    showBillPaymentSheet = false
                    selectedBillCategory = null
                },
                repository = viewModel.repository
            )
        }

        if (selectedTransactionForDetail != null) {
            TransactionDetailSheet(
                transaction = selectedTransactionForDetail!!,
                onDismiss = { selectedTransactionForDetail = null }
            )
        }
    }
}

/**
 * Monthly Budget Progress Card Widget
 */
@Composable
fun MonthlyBudgetWidget(
    spentCents: Long = 124000L,
    limitCents: Long = 200000L,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = ((spentCents.toDouble() / limitCents.toDouble()) * 100).toInt()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEDE9FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PocketPayIcons.PieChart,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Monthly Budget",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "$percentage% Used",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE2E8F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (spentCents.toFloat() / limitCents.toFloat()).coerceIn(0f, 1f))
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PrimaryBlue)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: $1,240.00",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Limit: $2,000.00",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Convenient Hub Shortcuts row to easily access the other core screens
 */
@Composable
private fun QuickHubShortcuts(
    onSplitBillClick: () -> Unit,
    onBillsClick: () -> Unit,
    onSavingsClick: () -> Unit,
    onAnalyticsClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HubCard(
                title = "Split Bills",
                subtitle = "Share with friends",
                icon = PocketPayIcons.Group,
                color = Color(0xFF6366F1),
                onClick = onSplitBillClick,
                modifier = Modifier.weight(1f)
            )

            HubCard(
                title = "Pay Utilities",
                subtitle = "Electric, water & wifi",
                icon = PocketPayIcons.Receipt,
                color = Color(0xFF0D9488),
                onClick = onBillsClick,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HubCard(
                title = "Savings Goals",
                subtitle = "$6,450.00 saved",
                icon = PocketPayIcons.Savings,
                color = Color(0xFF10B981),
                onClick = onSavingsClick,
                modifier = Modifier.weight(1f)
            )

            HubCard(
                title = "Spending Analytics",
                subtitle = "Health score 86/100",
                icon = PocketPayIcons.BarChart,
                color = Color(0xFF8B5CF6),
                onClick = onAnalyticsClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HubCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    EWalletTheme {
        HomeScreen()
    }
}
