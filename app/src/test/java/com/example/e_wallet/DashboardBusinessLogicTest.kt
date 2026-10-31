package com.example.e_wallet

import com.example.e_wallet.data.InMemoryWalletRepository
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.ui.home.HomeViewModel
import com.example.e_wallet.ui.home.TransactionFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    private val testScope = CoroutineScope(Dispatchers.Unconfined + Job())

    @Before
    fun setUp() {
        repository = InMemoryWalletRepository()
        viewModel = HomeViewModel(repository, testScope)
    }

    @Test
    fun testInitialWalletBalanceDerivedFromDataSource() {
        val state = viewModel.uiState.value

        // Check wallet balance matches in-memory source ($12,450.80 = 1,245,080 cents)
        assertEquals(1245080L, state.wallet?.currentBalance)
        assertEquals(1245080L, state.wallet?.availableBalance)
        assertEquals("USD", state.wallet?.currency)
        assertEquals("PKP-8821-4402", state.wallet?.walletNumber)
    }

    @Test
    fun testDerivedTotalsMatchUnderlyingTransactions() {
        val state = viewModel.uiState.value

        // Total received: $4,250.00 + $20.00 = $4,270.00 (427,000 cents)
        assertEquals(427000L, state.totalReceivedCents)

        // Total sent (SEND + QR + BILL): $799.00 + $8.50 + $42.50 = $850.00 (85,000 cents)
        assertEquals(85000L, state.totalSentCents)

        // Settled transactions count
        assertEquals(5, state.completedTransactionCount)
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
        assertTrue(sendFiltered.all { it.type == TransactionType.SEND || it.type == TransactionType.QR_PAYMENT || it.type == TransactionType.BILL_PAYMENT })
        assertEquals(3, sendFiltered.size)

        // Filter by RECEIVE
        viewModel.setTransactionFilter(TransactionFilter.RECEIVE)
        val receiveFiltered = viewModel.uiState.value.filteredTransactions
        assertTrue(receiveFiltered.all { it.type == TransactionType.RECEIVE })
        assertEquals(2, receiveFiltered.size)

        // Filter by ALL
        viewModel.setTransactionFilter(TransactionFilter.ALL)
        assertEquals(5, viewModel.uiState.value.filteredTransactions.size)
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
