package com.example.e_wallet

import com.example.e_wallet.data.InMemoryWalletRepository
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.ui.home.HomeViewModel
import com.example.e_wallet.ui.home.TransactionFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying Section 6 (Dashboard Business Logic) and Section 10 (Testing Checklist):
 * - Dashboard balance, totals, transaction count, and recent transactions match the underlying records.
 * - Balance hide/show changes only presentation, not wallet data.
 * - History filter and sort return the expected records.
 */
class DashboardBusinessLogicTest {

    private lateinit var repository: InMemoryWalletRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        repository = InMemoryWalletRepository()
        viewModel = HomeViewModel(repository)
    }

    @Test
    fun testInitialWalletBalanceDerivedFromDataSource() {
        val state = viewModel.uiState.value

        // Check wallet balance matches in-memory source ($2,450.00 = 245,000 cents)
        assertEquals(245000L, state.wallet?.currentBalance)
        assertEquals(245000L, state.wallet?.availableBalance)
        assertEquals("USD", state.wallet?.currency)
        assertEquals("PKP-8829-4102", state.wallet?.walletNumber)
    }

    @Test
    fun testDerivedTotalsMatchUnderlyingTransactions() {
        val state = viewModel.uiState.value

        // Total received: $150.00 + $80.00 = $230.00 (23,000 cents)
        assertEquals(23000L, state.totalReceivedCents)

        // Total sent (SEND + QR): $45.00 + $8.50 + $25.00 = $78.50 (7,850 cents)
        assertEquals(7850L, state.totalSentCents)

        // Total deposit: $500.00 (50,000 cents)
        assertEquals(50000L, state.totalDepositCents)

        // Total withdraw: $100.00 (10,000 cents)
        assertEquals(10000L, state.totalWithdrawCents)

        // Settled transactions count
        assertEquals(7, state.completedTransactionCount)
    }

    @Test
    fun testBalanceVisibilityToggleDoesNotModifyUnderlyingBalance() {
        val initialBalance = viewModel.uiState.value.wallet?.currentBalance

        assertTrue(viewModel.uiState.value.isBalanceVisible)

        // Toggle to hidden
        viewModel.toggleBalanceVisibility()
        assertFalse(viewModel.uiState.value.isBalanceVisible)
        assertEquals(initialBalance, viewModel.uiState.value.wallet?.currentBalance)

        // Toggle back to visible
        viewModel.toggleBalanceVisibility()
        assertTrue(viewModel.uiState.value.isBalanceVisible)
        assertEquals(initialBalance, viewModel.uiState.value.wallet?.currentBalance)
    }

    @Test
    fun testTransactionFiltering() {
        // Filter by SEND
        viewModel.setTransactionFilter(TransactionFilter.SEND)
        val sendFiltered = viewModel.uiState.value.filteredTransactions
        assertTrue(sendFiltered.all { it.type == TransactionType.SEND || it.type == TransactionType.QR_PAYMENT })
        assertEquals(3, sendFiltered.size)

        // Filter by RECEIVE
        viewModel.setTransactionFilter(TransactionFilter.RECEIVE)
        val receiveFiltered = viewModel.uiState.value.filteredTransactions
        assertTrue(receiveFiltered.all { it.type == TransactionType.RECEIVE })
        assertEquals(2, receiveFiltered.size)

        // Filter by DEPOSIT
        viewModel.setTransactionFilter(TransactionFilter.DEPOSIT)
        val depositFiltered = viewModel.uiState.value.filteredTransactions
        assertTrue(depositFiltered.all { it.type == TransactionType.DEPOSIT })
        assertEquals(1, depositFiltered.size)

        // Filter by WITHDRAW
        viewModel.setTransactionFilter(TransactionFilter.WITHDRAW)
        val withdrawFiltered = viewModel.uiState.value.filteredTransactions
        assertTrue(withdrawFiltered.all { it.type == TransactionType.WITHDRAW })
        assertEquals(1, withdrawFiltered.size)

        // Filter by ALL
        viewModel.setTransactionFilter(TransactionFilter.ALL)
        assertEquals(7, viewModel.uiState.value.filteredTransactions.size)
    }

    @Test
    fun testRecentTransactionsSortedByTimestampDescending() {
        val recentTxs = viewModel.uiState.value.recentTransactions
        for (i in 0 until recentTxs.size - 1) {
            assertTrue(
                "Transaction at index $i should be newer than index ${i + 1}",
                recentTxs[i].timestamp >= recentTxs[i + 1].timestamp
            )
        }
    }
}
