package com.example.e_wallet

import com.example.e_wallet.data.InMemoryWalletRepository
import com.example.e_wallet.model.SavingsCategory
import com.example.e_wallet.model.SavingsGoalResult
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.ui.savings.SavingsGoalFilter
import com.example.e_wallet.ui.savings.SavingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive business logic and unit tests for Savings Goals & Vaults / Piggy Bank module:
 * - Default savings vaults loading & APY interest calculations.
 * - Single source of truth repository consistency:
 *   - Creating a goal with initial deposit debits wallet atomically.
 *   - Depositing to a goal debits wallet and records TransactionType.SAVINGS_DEPOSIT.
 *   - Withdrawing from a goal requires 4-digit PIN, enforces locked vault constraints,
 *     credits wallet, and records TransactionType.SAVINGS_WITHDRAWAL.
 *   - Closing/deleting a goal requires PIN and refunds any remaining funds to wallet balance.
 * - ViewModel filtering (All, In Progress, Completed, Locked).
 */
class SavingsBusinessLogicTest {

    private lateinit var repository: InMemoryWalletRepository
    private lateinit var viewModel: SavingsViewModel
    private val testScope = CoroutineScope(Dispatchers.Unconfined + Job())

    @Before
    fun setUp() {
        repository = InMemoryWalletRepository()
        viewModel = SavingsViewModel(repository, testScope)
    }

    @Test
    fun testInitialSavingsGoalsLoaded() {
        val goals = repository.savingsGoals.value
        assertEquals(3, goals.size)

        val travelGoal = goals.first { it.id == "goal_001" }
        assertEquals("Summer Vacation to Bali", travelGoal.name)
        assertEquals(300000L, travelGoal.targetAmountCents)
        assertEquals(180000L, travelGoal.currentAmountCents)
        assertEquals(SavingsCategory.TRAVEL, travelGoal.category)
        assertFalse(travelGoal.isLocked)
        assertEquals(60, travelGoal.progressPercent)
        assertFalse(travelGoal.isCompleted)
    }

    @Test
    fun testTotalSavingsBalanceCalculation() {
        val totalExpected = 180000L + 240000L + 225000L // $6,450.00
        assertEquals(totalExpected, repository.totalSavingsCents.value)
        assertEquals(totalExpected, viewModel.uiState.value.totalSavedCents)
    }

    @Test
    fun testCreateSavingsGoalWithoutInitialDeposit() = runBlocking {
        val initialWalletBalance = repository.currentWallet.value.currentBalance
        val result = repository.createSavingsGoal(
            name = "Wedding Fund",
            targetAmountCents = 1000000L, // $10,000
            initialDepositCents = 0L,
            category = SavingsCategory.GENERAL,
            isLocked = false
        )

        assertTrue(result is SavingsGoalResult.Success)
        val success = result as SavingsGoalResult.Success
        assertEquals("Wedding Fund", success.goal.name)
        assertEquals(0L, success.goal.currentAmountCents)
        assertEquals(initialWalletBalance, success.updatedWalletBalance)
        assertEquals(initialWalletBalance, repository.currentWallet.value.currentBalance)
        assertEquals(4, repository.savingsGoals.value.size)
    }

    @Test
    fun testCreateSavingsGoalWithInitialDepositDebitsWallet() = runBlocking {
        val initialWalletBalance = repository.currentWallet.value.currentBalance
        val depositAmount = 20000L // $200.00

        val result = repository.createSavingsGoal(
            name = "Paris Trip",
            targetAmountCents = 300000L,
            initialDepositCents = depositAmount,
            category = SavingsCategory.TRAVEL,
            isLocked = false
        )

        assertTrue(result is SavingsGoalResult.Success)
        val success = result as SavingsGoalResult.Success
        assertEquals(depositAmount, success.goal.currentAmountCents)
        assertEquals(initialWalletBalance - depositAmount, repository.currentWallet.value.currentBalance)
        assertEquals(initialWalletBalance - depositAmount, repository.currentWallet.value.availableBalance)

        // Verify transaction recorded
        val latestTx = repository.transactions.value.first()
        assertEquals(TransactionType.SAVINGS_DEPOSIT, latestTx.type)
        assertEquals(depositAmount, latestTx.amount)
        assertEquals("Paris Trip", latestTx.receiverName)
    }

    @Test
    fun testCreateSavingsGoalFailsWhenInitialDepositExceedsBalance() = runBlocking {
        val balance = repository.currentWallet.value.availableBalance
        val excessiveDeposit = balance + 50000L

        val result = repository.createSavingsGoal(
            name = "Supercar Vault",
            targetAmountCents = 20000000L,
            initialDepositCents = excessiveDeposit,
            category = SavingsCategory.VEHICLE
        )

        assertTrue(result is SavingsGoalResult.Error)
        val error = result as SavingsGoalResult.Error
        assertTrue(error.message.contains("Insufficient balance"))
    }

    @Test
    fun testCreateSavingsGoalFailsWithBlankNameOrZeroTarget() = runBlocking {
        val blankResult = repository.createSavingsGoal(
            name = "   ",
            targetAmountCents = 50000L
        )
        assertTrue(blankResult is SavingsGoalResult.Error)

        val zeroTargetResult = repository.createSavingsGoal(
            name = "Valid Name",
            targetAmountCents = 0L
        )
        assertTrue(zeroTargetResult is SavingsGoalResult.Error)
    }

    @Test
    fun testDepositToGoalSuccess() = runBlocking {
        val initialWalletBalance = repository.currentWallet.value.currentBalance
        val targetGoal = repository.savingsGoals.value.first { it.id == "goal_002" } // Tokyo Vacation ($1,750)
        val depositAmount = 50000L // $500.00

        val result = repository.depositToGoal(
            goalId = targetGoal.id,
            amountCents = depositAmount,
            note = "Extra savings from bonus"
        )

        assertTrue(result is SavingsGoalResult.Success)
        val success = result as SavingsGoalResult.Success
        assertEquals(240000L + depositAmount, success.goal.currentAmountCents)
        assertEquals(initialWalletBalance - depositAmount, repository.currentWallet.value.currentBalance)

        // Check transaction recorded
        val latestTx = repository.transactions.value.first()
        assertEquals(TransactionType.SAVINGS_DEPOSIT, latestTx.type)
        assertEquals(depositAmount, latestTx.amount)
        assertEquals("Extra savings from bonus", latestTx.description)
    }

    @Test
    fun testDepositToGoalFailsWhenExceedingWalletBalance() = runBlocking {
        val targetGoal = repository.savingsGoals.value.first { it.id == "goal_002" }
        val excessive = repository.currentWallet.value.availableBalance + 10000L

        val result = repository.depositToGoal(
            goalId = targetGoal.id,
            amountCents = excessive
        )

        assertTrue(result is SavingsGoalResult.Error)
    }

    @Test
    fun testWithdrawFromGoalSuccessWithValidPin() = runBlocking {
        val initialWalletBalance = repository.currentWallet.value.currentBalance
        val targetGoal = repository.savingsGoals.value.first { it.id == "goal_002" } // Unlocked goal ($1,750)
        val withdrawAmount = 25000L // $250.00

        val result = repository.withdrawFromGoal(
            goalId = targetGoal.id,
            amountCents = withdrawAmount,
            pin = "1234",
            note = "Hotel deposit payment"
        )

        assertTrue(result is SavingsGoalResult.Success)
        val success = result as SavingsGoalResult.Success
        assertEquals(240000L - withdrawAmount, success.goal.currentAmountCents)
        assertEquals(initialWalletBalance + withdrawAmount, repository.currentWallet.value.currentBalance)

        // Check transaction recorded
        val latestTx = repository.transactions.value.first()
        assertEquals(TransactionType.SAVINGS_WITHDRAWAL, latestTx.type)
        assertEquals(withdrawAmount, latestTx.amount)
        assertEquals("Hotel deposit payment", latestTx.description)
    }

    @Test
    fun testWithdrawFromGoalFailsWithInvalidPin() = runBlocking {
        val targetGoal = repository.savingsGoals.value.first { it.id == "goal_002" }
        val result = repository.withdrawFromGoal(
            goalId = targetGoal.id,
            amountCents = 10000L,
            pin = "9999" // Wrong PIN
        )

        assertTrue(result is SavingsGoalResult.Error)
        val error = result as SavingsGoalResult.Error
        assertTrue(error.message.contains("Incorrect PIN"))
    }

    @Test
    fun testWithdrawFromLockedGoalBlockedUntilTargetMet() = runBlocking {
        // goal_003 is locked and saved $2,250 / target $5,000 (target not met)
        val lockedGoal = repository.savingsGoals.value.first { it.id == "goal_003" }
        assertTrue(lockedGoal.isLocked)
        assertFalse(lockedGoal.isCompleted)

        val result = repository.withdrawFromGoal(
            goalId = lockedGoal.id,
            amountCents = 10000L,
            pin = "1234"
        )

        assertTrue(result is SavingsGoalResult.Error)
        val error = result as SavingsGoalResult.Error
        assertTrue(error.message.contains("locked until"))
    }

    @Test
    fun testWithdrawFromLockedGoalAllowedWhenTargetMet() = runBlocking {
        // Create a locked goal with small target that fits in wallet balance
        val createResult = repository.createSavingsGoal(
            name = "Locked Mini Vault",
            targetAmountCents = 10000L, // $100.00
            initialDepositCents = 5000L,  // $50.00
            isLocked = true
        )
        assertTrue(createResult is SavingsGoalResult.Success)
        val goalId = (createResult as SavingsGoalResult.Success).goal.id

        // Deposit remaining $50 to reach 100% target
        val depositRes = repository.depositToGoal(goalId, 5000L)
        assertTrue(depositRes is SavingsGoalResult.Success)
        val completedGoal = (depositRes as SavingsGoalResult.Success).goal
        assertTrue(completedGoal.isCompleted)

        // Now withdrawal from completed locked goal should succeed
        val withdrawRes = repository.withdrawFromGoal(
            goalId = goalId,
            amountCents = 2000L,
            pin = "1234"
        )
        assertTrue(withdrawRes is SavingsGoalResult.Success)
    }

    @Test
    fun testDeleteGoalRefundsBalanceToWalletWithValidPin() = runBlocking {
        val initialWalletBalance = repository.currentWallet.value.currentBalance
        val targetGoal = repository.savingsGoals.value.first { it.id == "goal_003" } // $1,600.00 saved
        val goalBalance = targetGoal.currentAmountCents

        val result = repository.deleteSavingsGoal(
            goalId = targetGoal.id,
            pin = "1234"
        )

        assertTrue(result is SavingsGoalResult.Success)
        assertEquals(initialWalletBalance + goalBalance, repository.currentWallet.value.currentBalance)
        assertFalse(repository.savingsGoals.value.any { it.id == "goal_003" })

        // Check refund transaction
        val latestTx = repository.transactions.value.first()
        assertEquals(TransactionType.SAVINGS_WITHDRAWAL, latestTx.type)
        assertEquals(goalBalance, latestTx.amount)
        assertTrue(latestTx.description.contains("Refund"))
    }

    @Test
    fun testDeleteGoalFailsWithInvalidPin() = runBlocking {
        val result = repository.deleteSavingsGoal(
            goalId = "goal_003",
            pin = "0000" // Wrong PIN
        )

        assertTrue(result is SavingsGoalResult.Error)
        assertTrue(repository.savingsGoals.value.any { it.id == "goal_003" })
    }

    @Test
    fun testSavingsViewModelFiltering() {
        // ALL
        viewModel.setFilter(SavingsGoalFilter.ALL)
        assertEquals(3, viewModel.uiState.value.filteredGoals.size)

        // ACTIVE
        viewModel.setFilter(SavingsGoalFilter.ACTIVE)
        assertEquals(3, viewModel.uiState.value.filteredGoals.size)

        // COMPLETED (none initially)
        viewModel.setFilter(SavingsGoalFilter.COMPLETED)
        assertEquals(0, viewModel.uiState.value.filteredGoals.size)

        // LOCKED (goal_003 is locked)
        viewModel.setFilter(SavingsGoalFilter.LOCKED)
        assertEquals(1, viewModel.uiState.value.filteredGoals.size)
        assertEquals("goal_003", viewModel.uiState.value.filteredGoals.first().id)
    }
}
