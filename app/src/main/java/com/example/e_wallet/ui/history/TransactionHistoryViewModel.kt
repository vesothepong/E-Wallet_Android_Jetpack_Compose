package com.example.e_wallet.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.model.TransactionStatus
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.model.User
import com.example.e_wallet.ui.home.TransactionFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class TransactionHistoryUiState(
    val allTransactions: List<Transaction> = emptyList(),
    val groupedTransactions: Map<String, List<Transaction>> = emptyMap(),
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val searchQuery: String = "",
    val selectedTransaction: Transaction? = null,
    val totalTransactionCount: Int = 0,
    val totalMoneyIn: Long = 0L,
    val totalMoneyOut: Long = 0L,
    val isLoading: Boolean = false
)

class TransactionHistoryViewModel(
    private val repository: WalletRepository = WalletRepositoryProvider.repository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedTransaction = MutableStateFlow<Transaction?>(null)

    val uiState = combine(
        repository.currentUser,
        repository.transactions,
        _selectedFilter,
        _searchQuery,
        _selectedTransaction
    ) { user, txs, filter, query, selectedTx ->
        buildState(user, txs, filter, query, selectedTx)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = buildState(
            repository.currentUser.value,
            repository.transactions.value,
            _selectedFilter.value,
            _searchQuery.value,
            _selectedTransaction.value
        )
    )

    private fun buildState(
        user: User,
        allTxs: List<Transaction>,
        filter: TransactionFilter,
        query: String,
        selectedTx: Transaction?
    ): TransactionHistoryUiState {
        // Only user-relevant transactions, newest first
        val userTxs = allTxs
            .filter { it.senderId == user.userId || it.receiverId == user.userId }
            .sortedByDescending { it.timestamp }

        // Apply type filter
        val typeFiltered = when (filter) {
            TransactionFilter.ALL -> userTxs
            TransactionFilter.SEND -> userTxs.filter {
                it.type == TransactionType.SEND || it.type == TransactionType.QR_PAYMENT || it.type == TransactionType.BILL_PAYMENT
            }
            TransactionFilter.RECEIVE -> userTxs.filter { it.type == TransactionType.RECEIVE }
            TransactionFilter.DEPOSIT -> userTxs.filter { it.type == TransactionType.DEPOSIT }
            TransactionFilter.WITHDRAW -> userTxs.filter { it.type == TransactionType.WITHDRAW }
        }

        // Apply search query (description, name, ref number, type)
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) typeFiltered else typeFiltered.filter { tx ->
            tx.description.lowercase().contains(q) ||
                (tx.senderName?.lowercase()?.contains(q) == true) ||
                (tx.receiverName?.lowercase()?.contains(q) == true) ||
                tx.referenceNumber.lowercase().contains(q) ||
                tx.type.name.lowercase().contains(q)
        }

        // Group by date label
        val grouped = filtered.groupBy { tx ->
            formatDateGroupLabel(tx.timestamp)
        }

        // Compute summary stats from ALL user txs (regardless of current filter)
        val completedTxs = userTxs.filter { it.status == TransactionStatus.COMPLETED }
        val moneyIn = completedTxs
            .filter { it.type == TransactionType.RECEIVE || it.type == TransactionType.DEPOSIT }
            .sumOf { it.amount }
        val moneyOut = completedTxs
            .filter {
                it.type == TransactionType.SEND ||
                    it.type == TransactionType.QR_PAYMENT ||
                    it.type == TransactionType.WITHDRAW ||
                    it.type == TransactionType.BILL_PAYMENT
            }
            .sumOf { it.amount + it.fee }

        return TransactionHistoryUiState(
            allTransactions = filtered,
            groupedTransactions = grouped,
            selectedFilter = filter,
            searchQuery = query,
            selectedTransaction = selectedTx,
            totalTransactionCount = userTxs.size,
            totalMoneyIn = moneyIn,
            totalMoneyOut = moneyOut
        )
    }

    fun setFilter(filter: TransactionFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun onTransactionClick(tx: Transaction) {
        _selectedTransaction.value = tx
    }

    fun dismissDetail() {
        _selectedTransaction.value = null
    }

    private fun formatDateGroupLabel(timestamp: Long): String {
        val txCal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        val todayCal = java.util.Calendar.getInstance()
        val yesterdayCal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }

        val isSameDay = { a: java.util.Calendar, b: java.util.Calendar ->
            a.get(java.util.Calendar.YEAR) == b.get(java.util.Calendar.YEAR) &&
                a.get(java.util.Calendar.DAY_OF_YEAR) == b.get(java.util.Calendar.DAY_OF_YEAR)
        }

        return when {
            isSameDay(txCal, todayCal) -> "Today"
            isSameDay(txCal, yesterdayCal) -> "Yesterday"
            else -> {
                val sdf = java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            }
        }
    }
}
