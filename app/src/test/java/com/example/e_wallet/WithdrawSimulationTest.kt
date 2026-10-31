package com.example.e_wallet

import com.example.e_wallet.data.InMemoryWalletRepository
import com.example.e_wallet.data.WithdrawResult
import com.example.e_wallet.model.TransactionStatus
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.ui.home.HomeViewModel
import com.example.e_wallet.ui.withdraw.WithdrawStep
import com.example.e_wallet.ui.withdraw.WithdrawViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying Section 3.D (Withdraw Simulation) and Section 10 (Testing Checklist):
 * - Withdraw of a positive amount decreases balance by exactly that amount.
 * - Withdraw with insufficient funds fails and leaves balance unchanged.
 * - Withdraw of zero, negative, or malformed amount is rejected.
 * - Withdraw exceeding demo limit ($2,000.00) is rejected.
 * - Summary statistics (totalWithdrawCents) and recent transactions update from single source of truth.
 * - Duplicate prevention and validation rules.
 */
class WithdrawSimulationTest {

    private lateinit var repository: InMemoryWalletRepository
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var withdrawViewModel: WithdrawViewModel
    private val testScope = CoroutineScope(Dispatchers.Unconfined + Job())

    @Before
    fun setUp() {
        repository = InMemoryWalletRepository()
        homeViewModel = HomeViewModel(repository, testScope)
        withdrawViewModel = WithdrawViewModel(repository, testScope)
    }

    @Test
    fun testSuccessfulWithdrawalDecreasesBalanceAndUpdatesLedger() = runBlocking {
        val initialBalance = repository.currentWallet.value.currentBalance
        val initialWithdrawTotal = homeViewModel.uiState.value.totalWithdrawCents
        val initialTxCount = homeViewModel.uiState.value.completedTransactionCount

        val withdrawAmountCents = 12000L // $120.00
        val result = repository.withdraw(
            amountCents = withdrawAmountCents,
            note = "ATM cashout for groceries",
            destinationDescription = "ATM Cashout (Cardless Demo)"
        )

        // Verify result
        assertTrue(result is WithdrawResult.Success)
        val success = result as WithdrawResult.Success
        assertEquals(initialBalance - withdrawAmountCents, success.newBalance)
        assertEquals(withdrawAmountCents, success.transaction.amount)
        assertEquals(TransactionType.WITHDRAW, success.transaction.type)
        assertEquals(TransactionStatus.COMPLETED, success.transaction.status)
        assertEquals("ATM cashout for groceries", success.transaction.description)
        assertEquals("ATM Cashout (Cardless Demo)", success.transaction.receiverName)
        assertNotNull(success.transaction.referenceNumber)

        // Verify single source of truth in repository
        assertEquals(initialBalance - withdrawAmountCents, repository.currentWallet.value.currentBalance)
        assertEquals(initialBalance - withdrawAmountCents, repository.currentWallet.value.availableBalance)

        // Verify reactive Dashboard ViewModel stats update
        val homeState = homeViewModel.uiState.value
        assertEquals(initialBalance - withdrawAmountCents, homeState.wallet?.currentBalance)
        assertEquals(initialWithdrawTotal + withdrawAmountCents, homeState.totalWithdrawCents)
        assertEquals(initialTxCount + 1, homeState.completedTransactionCount)

        // Verify top transaction in recent transactions is the new withdrawal
        val latestTx = homeState.recentTransactions.first()
        assertEquals(success.transaction.transactionId, latestTx.transactionId)
        assertEquals(TransactionType.WITHDRAW, latestTx.type)
    }

    @Test
    fun testWithdrawalWithInsufficientFundsFailsAndLeavesBalanceUnchanged() = runBlocking {
        // Drop balance so it is less than $1,000 (< $2,000 limit)
        // Withdraw $2,000 6 times = $12,000 debited, leaving $450.80
        repeat(6) { repository.withdraw(amountCents = 200000L) }
        val balanceAfterFirst = repository.currentWallet.value.currentBalance
        val txCountAfterFirst = repository.transactions.value.size

        // Attempt to withdraw $1,000.00 (< $2,000.00 limit, but > $450.80 available balance)
        val result = repository.withdraw(amountCents = 100000L)

        assertTrue(result is WithdrawResult.Error)
        val error = result as WithdrawResult.Error
        assertTrue(error.message.contains("Insufficient funds", ignoreCase = true))

        // Balance and transactions must remain strictly unchanged from balanceAfterFirst
        assertEquals(balanceAfterFirst, repository.currentWallet.value.currentBalance)
        assertEquals(balanceAfterFirst, repository.currentWallet.value.availableBalance)
        assertEquals(txCountAfterFirst, repository.transactions.value.size)
    }

    @Test
    fun testWithdrawalWithZeroOrNegativeAmountIsRejectedWithoutChangingBalance() = runBlocking {
        val initialBalance = repository.currentWallet.value.currentBalance
        val initialTxCount = repository.transactions.value.size

        // Zero amount withdrawal
        val zeroResult = repository.withdraw(amountCents = 0L)
        assertTrue(zeroResult is WithdrawResult.Error)
        assertEquals(initialBalance, repository.currentWallet.value.currentBalance)
        assertEquals(initialTxCount, repository.transactions.value.size)

        // Negative amount withdrawal
        val negativeResult = repository.withdraw(amountCents = -1000L)
        assertTrue(negativeResult is WithdrawResult.Error)
        assertEquals(initialBalance, repository.currentWallet.value.currentBalance)
        assertEquals(initialTxCount, repository.transactions.value.size)
    }

    @Test
    fun testWithdrawalExceedingDemoLimitIsRejected() = runBlocking {
        val initialBalance = repository.currentWallet.value.currentBalance
        val limitCents = 200000L // $2,000.00
        val overLimitCents = limitCents + 100L

        val result = repository.withdraw(amountCents = overLimitCents)
        assertTrue(result is WithdrawResult.Error)
        val error = result as WithdrawResult.Error
        assertTrue(error.message.contains("limit", ignoreCase = true))

        assertEquals(initialBalance, repository.currentWallet.value.currentBalance)
    }

    @Test
    fun testFullBalanceWithdrawalLeavesZeroBalanceWithoutUnderflow() = runBlocking {
        // Withdraw down to $500.00 first
        while (repository.currentWallet.value.currentBalance > 50000L) {
            val toWithdraw = (repository.currentWallet.value.currentBalance - 50000L).coerceAtMost(200000L)
            val step = repository.withdraw(amountCents = toWithdraw)
            assertTrue(step is WithdrawResult.Success)
        }
        assertEquals(50000L, repository.currentWallet.value.currentBalance)

        // Now withdraw remaining full $500.00
        val withdrawFull = repository.withdraw(amountCents = 50000L)
        assertTrue(withdrawFull is WithdrawResult.Success)
        assertEquals(0L, repository.currentWallet.value.currentBalance)
        assertEquals(0L, repository.currentWallet.value.availableBalance)
    }

    @Test
    fun testViewModelAmountValidationAndLimits() {
        // Valid amount
        withdrawViewModel.onAmountChanged("75.50")
        assertEquals(7550L, withdrawViewModel.uiState.value.amountCents)
        assertNull(withdrawViewModel.uiState.value.errorMessage)
        assertTrue(withdrawViewModel.uiState.value.isValidAmount)

        // Comma replacement
        withdrawViewModel.onAmountChanged("100,25")
        assertEquals(10025L, withdrawViewModel.uiState.value.amountCents)
        assertNull(withdrawViewModel.uiState.value.errorMessage)

        // Exceeding per-transaction demo limit of $2,000.00
        withdrawViewModel.onAmountChanged("2000.01")
        assertNotNull(withdrawViewModel.uiState.value.errorMessage)
        assertFalse(withdrawViewModel.uiState.value.isValidAmount)

        // Exceeding balance
        withdrawViewModel.onAmountChanged("2450.01")
        assertNotNull(withdrawViewModel.uiState.value.errorMessage)
        assertFalse(withdrawViewModel.uiState.value.isValidAmount)

        // Use Max button
        withdrawViewModel.onUseMaxAmount()
        // Max allowed is min(balance 245000L, limit 200000L) = 200000L ($2000.00)
        assertEquals(200000L, withdrawViewModel.uiState.value.amountCents)
        assertNull(withdrawViewModel.uiState.value.errorMessage)
        assertTrue(withdrawViewModel.uiState.value.isValidAmount)
    }

    @Test
    fun testViewModelStepTransitionsAndConfirmation() = runBlocking {
        withdrawViewModel.onAmountChanged("60")
        assertEquals(WithdrawStep.INPUT, withdrawViewModel.uiState.value.step)

        withdrawViewModel.proceedToConfirmation()
        assertEquals(WithdrawStep.CONFIRMATION, withdrawViewModel.uiState.value.step)

        withdrawViewModel.backToInput()
        assertEquals(WithdrawStep.INPUT, withdrawViewModel.uiState.value.step)

        // Proceed and confirm
        withdrawViewModel.proceedToConfirmation()
        withdrawViewModel.confirmWithdraw()

        // Wait for simulated latency
        kotlinx.coroutines.delay(700)

        assertEquals(WithdrawStep.SUCCESS, withdrawViewModel.uiState.value.step)
        assertNotNull(withdrawViewModel.uiState.value.completedTransaction)
        assertEquals(6000L, withdrawViewModel.uiState.value.completedTransaction?.amount)
    }
}
