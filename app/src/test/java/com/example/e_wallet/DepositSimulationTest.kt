package com.example.e_wallet

import com.example.e_wallet.data.DepositResult
import com.example.e_wallet.data.InMemoryWalletRepository
import com.example.e_wallet.model.TransactionStatus
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.ui.deposit.DepositStep
import com.example.e_wallet.ui.deposit.DepositViewModel
import com.example.e_wallet.ui.home.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying Section 3.C (Deposit Simulation) and Section 10 (Testing Checklist):
 * - Deposit of a positive amount increases balance by exactly that amount.
 * - Deposit of zero, negative, or malformed amount is rejected.
 * - Summary statistics and recent transactions update from single source of truth.
 * - Duplicate prevention and validation rules.
 */
class DepositSimulationTest {

    private lateinit var repository: InMemoryWalletRepository
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var depositViewModel: DepositViewModel
    private val testScope = CoroutineScope(Dispatchers.Unconfined + Job())

    @Before
    fun setUp() {
        repository = InMemoryWalletRepository()
        homeViewModel = HomeViewModel(repository, testScope)
        depositViewModel = DepositViewModel(repository, testScope)
    }

    @Test
    fun testSuccessfulDepositIncreasesBalanceAndUpdatesLedger() = runBlocking {
        val initialBalance = repository.currentWallet.value.currentBalance
        val initialDepositTotal = homeViewModel.uiState.value.totalDepositCents
        val initialTxCount = homeViewModel.uiState.value.completedTransactionCount

        val depositAmountCents = 7500L // $75.00
        val result = repository.deposit(
            amountCents = depositAmountCents,
            note = "Freelance bonus",
            sourceDescription = "Demo Checking Account"
        )

        // Verify result
        assertTrue(result is DepositResult.Success)
        val success = result as DepositResult.Success
        assertEquals(initialBalance + depositAmountCents, success.newBalance)
        assertEquals(depositAmountCents, success.transaction.amount)
        assertEquals(TransactionType.DEPOSIT, success.transaction.type)
        assertEquals(TransactionStatus.COMPLETED, success.transaction.status)
        assertEquals("Freelance bonus", success.transaction.description)
        assertNotNull(success.transaction.referenceNumber)

        // Verify single source of truth in repository
        assertEquals(initialBalance + depositAmountCents, repository.currentWallet.value.currentBalance)
        assertEquals(initialBalance + depositAmountCents, repository.currentWallet.value.availableBalance)

        // Verify reactive Dashboard ViewModel stats update
        val homeState = homeViewModel.uiState.value
        assertEquals(initialBalance + depositAmountCents, homeState.wallet?.currentBalance)
        assertEquals(initialDepositTotal + depositAmountCents, homeState.totalDepositCents)
        assertEquals(initialTxCount + 1, homeState.completedTransactionCount)

        // Verify top transaction in recent transactions is the new deposit
        val latestTx = homeState.recentTransactions.first()
        assertEquals(success.transaction.transactionId, latestTx.transactionId)
        assertEquals(TransactionType.DEPOSIT, latestTx.type)
    }

    @Test
    fun testDepositWithZeroOrNegativeAmountIsRejectedWithoutChangingBalance() = runBlocking {
        val initialBalance = repository.currentWallet.value.currentBalance
        val initialTxCount = repository.transactions.value.size

        // Zero amount deposit
        val zeroResult = repository.deposit(amountCents = 0L)
        assertTrue(zeroResult is DepositResult.Error)
        assertEquals(initialBalance, repository.currentWallet.value.currentBalance)
        assertEquals(initialTxCount, repository.transactions.value.size)

        // Negative amount deposit
        val negativeResult = repository.deposit(amountCents = -5000L)
        assertTrue(negativeResult is DepositResult.Error)
        assertEquals(initialBalance, repository.currentWallet.value.currentBalance)
        assertEquals(initialTxCount, repository.transactions.value.size)
    }

    @Test
    fun testDepositExceedingLimitIsRejected() = runBlocking {
        val initialBalance = repository.currentWallet.value.currentBalance
        val limitCents = 500000L // $5,000.00
        val overLimitCents = limitCents + 100L

        val result = repository.deposit(amountCents = overLimitCents)
        assertTrue(result is DepositResult.Error)
        assertEquals(initialBalance, repository.currentWallet.value.currentBalance)
    }

    @Test
    fun testDepositViewModelLifecycleAndValidation() {
        // Initial state
        assertEquals(DepositStep.INPUT, depositViewModel.uiState.value.step)

        // Quick amount selection
        depositViewModel.onQuickAmountSelected(100)
        assertEquals("100", depositViewModel.uiState.value.amountInput)
        assertEquals(10000L, depositViewModel.uiState.value.amountCents)
        assertTrue(depositViewModel.uiState.value.isValidAmount)

        // Input sanitization: non-numeric ignored
        depositViewModel.onAmountChanged("150.50")
        assertEquals(15050L, depositViewModel.uiState.value.amountCents)

        // Advance to confirmation
        depositViewModel.proceedToConfirmation()
        assertEquals(DepositStep.CONFIRMATION, depositViewModel.uiState.value.step)

        // Back to input
        depositViewModel.backToInput()
        assertEquals(DepositStep.INPUT, depositViewModel.uiState.value.step)

        // Invalid amount input rejects proceed
        depositViewModel.onAmountChanged("0")
        depositViewModel.proceedToConfirmation()
        assertEquals(DepositStep.INPUT, depositViewModel.uiState.value.step)
        assertNotNull(depositViewModel.uiState.value.errorMessage)
    }

    @Test
    fun testDepositViewModelCancellationDoesNotChangeBalance() {
        val initialBalance = repository.currentWallet.value.currentBalance
        val initialTxCount = repository.transactions.value.size

        depositViewModel.onAmountChanged("200")
        depositViewModel.proceedToConfirmation()
        // User cancels / resets
        depositViewModel.reset()

        assertEquals(DepositStep.INPUT, depositViewModel.uiState.value.step)
        assertEquals(initialBalance, repository.currentWallet.value.currentBalance)
        assertEquals(initialTxCount, repository.transactions.value.size)
    }

    @Test
    fun testDepositCurrencyPrecisionAndCommaNormalization() {
        // Floating point truncation protection ($29.99 must be 2999L, not 2998L)
        depositViewModel.onAmountChanged("29.99")
        assertEquals(2999L, depositViewModel.uiState.value.amountCents)

        depositViewModel.onAmountChanged("1.14")
        assertEquals(114L, depositViewModel.uiState.value.amountCents)

        // Comma as decimal separator support ($45,50 -> 4550L)
        depositViewModel.onAmountChanged("45,50")
        assertEquals(4550L, depositViewModel.uiState.value.amountCents)

        // Decimal places restricted to 2: typing 3rd decimal place is ignored
        depositViewModel.onAmountChanged("10.999")
        assertEquals(4550L, depositViewModel.uiState.value.amountCents) // ignored change from 45.50
    }

    @Test
    fun testDepositViewModelExecutionCompletesSuccessfully() = runBlocking {
        val initialBalance = repository.currentWallet.value.currentBalance
        val initialTxCount = repository.transactions.value.size

        depositViewModel.onAmountChanged("65.50")
        depositViewModel.onNoteChanged("Consulting dividend")
        depositViewModel.proceedToConfirmation()
        assertEquals(DepositStep.CONFIRMATION, depositViewModel.uiState.value.step)

        depositViewModel.confirmDeposit()
        // Wait for simulated network latency
        kotlinx.coroutines.delay(600)

        val state = depositViewModel.uiState.value
        assertEquals(DepositStep.SUCCESS, state.step)
        assertEquals(initialBalance + 6550L, state.newBalanceCents)
        assertEquals(initialBalance + 6550L, repository.currentWallet.value.currentBalance)
        assertEquals(initialTxCount + 1, repository.transactions.value.size)
        assertEquals("Consulting dividend", state.completedTransaction?.description)
    }
}
