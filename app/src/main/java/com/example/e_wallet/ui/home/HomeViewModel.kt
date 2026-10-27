package com.example.e_wallet.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.Beneficiary
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.model.TransactionStatus
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.model.User
import com.example.e_wallet.model.Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TransactionFilter(val label: String) {
    ALL("All"),
    SEND("Sent"),
    RECEIVE("Received"),
    DEPOSIT("Deposits"),
    WITHDRAW("Withdrawals")
}

enum class HomeQuickAction {
    SEND,
    RECEIVE,
    DEPOSIT,
    WITHDRAW,
    SCAN_QR
}

data class HomeUiState(
    val currentUser: User? = null,
    val wallet: Wallet? = null,
    val isBalanceVisible: Boolean = true,
    val recentTransactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val beneficiaries: List<Beneficiary> = emptyList(),
    val unreadNotificationsCount: Int = 0,
    val totalSentCents: Long = 0L,
    val totalReceivedCents: Long = 0L,
    val totalDepositCents: Long = 0L,
    val totalWithdrawCents: Long = 0L,
    val completedTransactionCount: Int = 0,
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val isRefreshing: Boolean = false,
    val snackbarMessage: String? = null
)

private data class RepoData(
    val user: User,
    val wallet: Wallet,
    val transactions: List<Transaction>,
    val beneficiaries: List<Beneficiary>,
    val unreadCount: Int
)

private data class UiControls(
    val isBalanceVisible: Boolean,
    val selectedFilter: TransactionFilter,
    val isRefreshing: Boolean,
    val snackbarMessage: String?
)

class HomeViewModel(
    val repository: WalletRepository = WalletRepositoryProvider.repository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val _isBalanceVisible = MutableStateFlow(true)
    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)
    private val _isRefreshing = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    private fun computeState(
        user: User,
        wallet: Wallet,
        txs: List<Transaction>,
        beneficiaries: List<Beneficiary>,
        unreadCount: Int,
        isBalVisible: Boolean,
        filter: TransactionFilter,
        isRefreshing: Boolean,
        snackbarMsg: String?
    ): HomeUiState {
        // Derive statistics strictly from single source of truth (transactions)
        val userTransactions = txs.filter {
            it.senderId == user.userId || it.receiverId == user.userId
        }.sortedByDescending { it.timestamp }

        val completedTxs = userTransactions.filter { it.status == TransactionStatus.COMPLETED }

        val totalSent = completedTxs
            .filter { it.type == TransactionType.SEND || it.type == TransactionType.QR_PAYMENT || it.type == TransactionType.BILL_PAYMENT }
            .sumOf { it.amount + it.fee }

        val totalReceived = completedTxs
            .filter { it.type == TransactionType.RECEIVE }
            .sumOf { it.amount }

        val totalDeposit = completedTxs
            .filter { it.type == TransactionType.DEPOSIT }
            .sumOf { it.amount }

        val totalWithdraw = completedTxs
            .filter { it.type == TransactionType.WITHDRAW }
            .sumOf { it.amount + it.fee }

        val recentTxs = userTransactions.take(10)

        val filtered = when (filter) {
            TransactionFilter.ALL -> userTransactions
            TransactionFilter.SEND -> userTransactions.filter {
                it.type == TransactionType.SEND || it.type == TransactionType.QR_PAYMENT || it.type == TransactionType.BILL_PAYMENT
            }
            TransactionFilter.RECEIVE -> userTransactions.filter { it.type == TransactionType.RECEIVE }
            TransactionFilter.DEPOSIT -> userTransactions.filter { it.type == TransactionType.DEPOSIT }
            TransactionFilter.WITHDRAW -> userTransactions.filter { it.type == TransactionType.WITHDRAW }
        }.take(10)

        return HomeUiState(
            currentUser = user,
            wallet = wallet,
            isBalanceVisible = isBalVisible,
            recentTransactions = recentTxs,
            filteredTransactions = filtered,
            beneficiaries = beneficiaries,
            unreadNotificationsCount = unreadCount,
            totalSentCents = totalSent,
            totalReceivedCents = totalReceived,
            totalDepositCents = totalDeposit,
            totalWithdrawCents = totalWithdraw,
            completedTransactionCount = completedTxs.size,
            selectedFilter = filter,
            isRefreshing = isRefreshing,
            snackbarMessage = snackbarMsg
        )
    }

    private val repoFlow = combine(
        repository.currentUser,
        repository.currentWallet,
        repository.transactions,
        repository.beneficiaries,
        repository.unreadNotificationsCount
    ) { user, wallet, txs, beneficiaries, unreadCount ->
        RepoData(user, wallet, txs, beneficiaries, unreadCount)
    }

    private val uiControlsFlow = combine(
        _isBalanceVisible,
        _selectedFilter,
        _isRefreshing,
        _snackbarMessage
    ) { isBalVisible, filter, isRefreshing, snackbarMsg ->
        UiControls(isBalVisible, filter, isRefreshing, snackbarMsg)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repoFlow,
        uiControlsFlow
    ) { repo, ui ->
        computeState(
            user = repo.user,
            wallet = repo.wallet,
            txs = repo.transactions,
            beneficiaries = repo.beneficiaries,
            unreadCount = repo.unreadCount,
            isBalVisible = ui.isBalanceVisible,
            filter = ui.selectedFilter,
            isRefreshing = ui.isRefreshing,
            snackbarMsg = ui.snackbarMessage
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = computeState(
            repository.currentUser.value,
            repository.currentWallet.value,
            repository.transactions.value,
            repository.beneficiaries.value,
            repository.unreadNotificationsCount.value,
            _isBalanceVisible.value,
            _selectedFilter.value,
            _isRefreshing.value,
            _snackbarMessage.value
        )
    )

    fun toggleBalanceVisibility() {
        _isBalanceVisible.update { !it }
    }

    fun setTransactionFilter(filter: TransactionFilter) {
        _selectedFilter.value = filter
    }

    fun onQuickAction(action: HomeQuickAction) {
        val label = when (action) {
            HomeQuickAction.SEND -> "Send Money flow"
            HomeQuickAction.RECEIVE -> "Receive / QR Code flow"
            HomeQuickAction.DEPOSIT -> "Deposit Simulation flow"
            HomeQuickAction.WITHDRAW -> "Withdraw Simulation flow"
            HomeQuickAction.SCAN_QR -> "Scan QR Code flow"
        }
        _snackbarMessage.value = "Selected $label (Ready for next step)"
    }

    fun onBeneficiaryClicked(beneficiary: Beneficiary) {
        _snackbarMessage.value = "Quick transfer to ${beneficiary.displayName} selected"
    }

    fun onAddBeneficiaryClicked() {
        _snackbarMessage.value = "Add new beneficiary opened"
    }

    fun onNotificationClicked() {
        _snackbarMessage.value = "Notifications opened"
    }

    fun onProfileClicked() {
        _snackbarMessage.value = "User Profile & Settings opened"
    }

    fun onSeeAllTransactionsClicked() {
        _snackbarMessage.value = "All Transactions History opened"
    }

    fun onTransactionClicked(transaction: Transaction) {
        _snackbarMessage.value = "Transaction Details: ${transaction.description.ifEmpty { transaction.type.name }}"
    }

    fun refreshDashboard() {
        scope.launch {
            _isRefreshing.value = true
            delay(600) // Simulated slight network sync
            _isRefreshing.value = false
            _snackbarMessage.value = "Wallet synced with demo ledger"
        }
    }

    fun resetDemoData() {
        repository.resetDemoData()
        _snackbarMessage.value = "Demo wallet and transactions reset to default"
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
