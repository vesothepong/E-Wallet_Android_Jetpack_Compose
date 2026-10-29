package com.example.e_wallet.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.e_wallet.model.Beneficiary
import com.example.e_wallet.model.BillCategory
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.ui.bills.BillPaymentBottomSheet
import com.example.e_wallet.ui.components.BottomNavTab
import com.example.e_wallet.ui.components.DashboardStatsSection
import com.example.e_wallet.ui.components.DemoWarningBanner
import com.example.e_wallet.ui.components.HomeTopBar
import com.example.e_wallet.ui.components.PocketPayBottomBar
import com.example.e_wallet.ui.components.QuickActionsSection
import com.example.e_wallet.ui.components.RecentBeneficiariesRow
import com.example.e_wallet.ui.components.RecentTransactionsSection
import com.example.e_wallet.ui.components.SavingsGoalsSection
import com.example.e_wallet.ui.components.ServicesSection
import com.example.e_wallet.ui.components.WalletCard
import com.example.e_wallet.ui.deposit.DepositBottomSheet
import com.example.e_wallet.ui.receive.ReceiveBottomSheet
import com.example.e_wallet.ui.scan.ScanQrBottomSheet
import com.example.e_wallet.ui.withdraw.WithdrawBottomSheet
import com.example.e_wallet.ui.send.SendMoneyBottomSheet
import com.example.e_wallet.ui.theme.EWalletTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = remember { HomeViewModel() },
    onNavigateToHistory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToBills: () -> Unit = {},
    onNavigateToSavings: () -> Unit = {}
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

    // Show snackbar feedback when triggered
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            PocketPayBottomBar(
                currentTab = currentTab,
                onTabSelected = { selected ->
                    currentTab = selected
                    when (selected) {
                        BottomNavTab.HOME -> { /* already on home */ }
                        BottomNavTab.WALLET -> {
                            currentTab = BottomNavTab.HOME
                            onNavigateToWallet()
                        }
                        BottomNavTab.SCAN -> {
                            showScanSheet = true
                        }
                        BottomNavTab.HISTORY -> {
                            currentTab = BottomNavTab.HOME
                            onNavigateToHistory()
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
        HomeScreenContent(
            uiState = uiState,
            onToggleBalance = viewModel::toggleBalanceVisibility,
            onQuickAction = { action ->
                when (action) {
                    HomeQuickAction.SEND -> {
                        selectedBeneficiaryForSend = null
                        showSendSheet = true
                    }
                    HomeQuickAction.RECEIVE -> showReceiveSheet = true
                    HomeQuickAction.DEPOSIT -> showDepositSheet = true
                    HomeQuickAction.WITHDRAW -> showWithdrawSheet = true
                    HomeQuickAction.SCAN_QR -> showScanSheet = true
                }
            },
            onFilterSelect = viewModel::setTransactionFilter,
            onBeneficiaryClick = { beneficiary ->
                selectedBeneficiaryForSend = beneficiary
                showSendSheet = true
            },
            onAddBeneficiaryClick = {
                // Open send sheet ready to add custom receiver
                selectedBeneficiaryForSend = null
                showSendSheet = true
            },
            onNotificationClick = onNavigateToNotifications,
            onResetDemoClick = viewModel::resetDemoData,
            onProfileClick = onNavigateToProfile,
            onWalletClick = onNavigateToWallet,
            onCategoryClick = { category ->
                selectedBillCategory = category
                showBillPaymentSheet = true
            },
            onServicesSeeAllClick = onNavigateToBills,
            onNavigateToSavings = onNavigateToSavings,
            onTransactionClick = viewModel::onTransactionClicked,
            onSeeAllClick = onNavigateToHistory,
            modifier = Modifier.padding(innerPadding)
        )

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

        if (showDepositSheet) {
            DepositBottomSheet(
                onDismiss = {
                    showDepositSheet = false
                    if (currentTab == BottomNavTab.WALLET) {
                        currentTab = BottomNavTab.HOME
                    }
                },
                repository = viewModel.repository
            )
        }

        if (showWithdrawSheet) {
            WithdrawBottomSheet(
                onDismiss = {
                    showWithdrawSheet = false
                },
                repository = viewModel.repository
            )
        }

        if (showReceiveSheet) {
            ReceiveBottomSheet(
                onDismiss = { showReceiveSheet = false }
            )
        }

        if (showScanSheet) {
            ScanQrBottomSheet(
                onDismiss = {
                    showScanSheet = false
                    if (currentTab == BottomNavTab.SCAN) {
                        currentTab = BottomNavTab.HOME
                    }
                },
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
    }
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onToggleBalance: () -> Unit,
    onQuickAction: (HomeQuickAction) -> Unit,
    onFilterSelect: (TransactionFilter) -> Unit,
    onBeneficiaryClick: (Beneficiary) -> Unit,
    onAddBeneficiaryClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onResetDemoClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCategoryClick: (BillCategory?) -> Unit = {},
    onServicesSeeAllClick: () -> Unit = {},
    onNavigateToSavings: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onWalletClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Top Bar: Greeting, Avatar, Notification & Reset
        HomeTopBar(
            user = uiState.currentUser,
            unreadNotificationsCount = uiState.unreadNotificationsCount,
            onNotificationClick = onNotificationClick,
            onResetDemoClick = onResetDemoClick,
            onProfileClick = onProfileClick
        )

        // 2. Educational Demo Simulator Banner
        DemoWarningBanner()

        // 3. Wallet Card with Balance Hide/Show and In/Out mini stats
        WalletCard(
            wallet = uiState.wallet,
            isBalanceVisible = uiState.isBalanceVisible,
            totalReceivedCents = uiState.totalReceivedCents,
            totalSentCents = uiState.totalSentCents,
            onToggleBalanceVisibility = onToggleBalance,
            modifier = Modifier.clickable(onClick = onWalletClick)
        )

        // 4. Quick Actions: Send, Receive, Deposit, Withdraw
        QuickActionsSection(
            onActionClick = onQuickAction
        )

        // 5. Pay Bills & Utilities Section
        ServicesSection(
            onCategoryClick = onCategoryClick,
            onSeeAllClick = onServicesSeeAllClick
        )

        // 6. Savings Goals & Vaults Carousel
        SavingsGoalsSection(
            goals = uiState.savingsGoals,
            onSeeAllClick = onNavigateToSavings,
            onGoalClick = { onNavigateToSavings() }
        )

        // 7. Financial Overview: Derived Dashboard KPIs
        DashboardStatsSection(
            totalReceivedCents = uiState.totalReceivedCents,
            totalSentCents = uiState.totalSentCents,
            totalDepositCents = uiState.totalDepositCents,
            totalWithdrawCents = uiState.totalWithdrawCents,
            completedTransactionCount = uiState.completedTransactionCount,
            isBalanceVisible = uiState.isBalanceVisible
        )

        // 6. Quick Send: Favorite Beneficiaries
        RecentBeneficiariesRow(
            beneficiaries = uiState.beneficiaries,
            onBeneficiaryClick = onBeneficiaryClick,
            onAddBeneficiaryClick = onAddBeneficiaryClick
        )

        // 7. Recent Transactions with Filtering (All, Sent, Received, Deposits, Withdrawals)
        RecentTransactionsSection(
            transactions = uiState.filteredTransactions,
            selectedFilter = uiState.selectedFilter,
            onFilterSelect = onFilterSelect,
            onTransactionClick = onTransactionClick,
            onSeeAllClick = onSeeAllClick
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    EWalletTheme {
        HomeScreenContent(
            uiState = HomeUiState(),
            onToggleBalance = {},
            onQuickAction = {},
            onFilterSelect = {},
            onBeneficiaryClick = {},
            onAddBeneficiaryClick = {},
            onNotificationClick = {},
            onResetDemoClick = {},
            onTransactionClick = {},
            onSeeAllClick = {}
        )
    }
}
