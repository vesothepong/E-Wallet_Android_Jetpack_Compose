package com.example.e_wallet

import com.example.e_wallet.data.InMemoryWalletRepository
import com.example.e_wallet.model.AnalyticsTimePeriod
import com.example.e_wallet.model.CategoryBudget
import com.example.e_wallet.model.FinancialHealthScore
import com.example.e_wallet.model.SpendingCategory
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.model.TransactionClassifier
import com.example.e_wallet.model.TransactionStatus
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.ui.analytics.AnalyticsViewModel
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
 * Comprehensive business logic and unit tests for Spending Analytics, Financial Insights, and Category Budgets.
 */
class AnalyticsBusinessLogicTest {

    private lateinit var repository: InMemoryWalletRepository
    private lateinit var viewModel: AnalyticsViewModel
    private val testScope = CoroutineScope(Dispatchers.Unconfined + Job())

    @Before
    fun setUp() {
        repository = InMemoryWalletRepository()
        viewModel = AnalyticsViewModel(repository = repository, scope = testScope)
    }

    @Test
    fun testTransactionClassifierFoodAndDining() {
        val tx1 = Transaction(
            transactionId = "t1",
            type = TransactionType.QR_PAYMENT,
            amount = 850L,
            timestamp = System.currentTimeMillis(),
            description = "Morning Starbucks caramel latte ☕"
        )
        assertEquals(SpendingCategory.FOOD_DINING, TransactionClassifier.categorize(tx1))

        val tx2 = Transaction(
            transactionId = "t2",
            type = TransactionType.SEND,
            amount = 3500L,
            timestamp = System.currentTimeMillis(),
            description = "Italian pizza dinner share 🍕"
        )
        assertEquals(SpendingCategory.FOOD_DINING, TransactionClassifier.categorize(tx2))
    }

    @Test
    fun testTransactionClassifierBillsAndUtilities() {
        val billTx = Transaction(
            transactionId = "t3",
            type = TransactionType.BILL_PAYMENT,
            amount = 8500L,
            timestamp = System.currentTimeMillis(),
            description = "Power bill"
        )
        assertEquals(SpendingCategory.BILLS_UTILITIES, TransactionClassifier.categorize(billTx))

        val utilityTx = Transaction(
            transactionId = "t4",
            type = TransactionType.SEND,
            amount = 5000L,
            timestamp = System.currentTimeMillis(),
            description = "High-speed broadband internet"
        )
        assertEquals(SpendingCategory.BILLS_UTILITIES, TransactionClassifier.categorize(utilityTx))
    }

    @Test
    fun testTransactionClassifierTransport() {
        val tx = Transaction(
            transactionId = "t5",
            type = TransactionType.SEND,
            amount = 2200L,
            timestamp = System.currentTimeMillis(),
            description = "Airport uber ride 🚖"
        )
        assertEquals(SpendingCategory.TRANSPORT, TransactionClassifier.categorize(tx))
    }

    @Test
    fun testTransactionClassifierEntertainment() {
        val movieTx = Transaction(
            transactionId = "t6",
            type = TransactionType.SEND,
            amount = 4500L,
            timestamp = System.currentTimeMillis(),
            description = "Movie tickets 🎬"
        )
        assertEquals(SpendingCategory.ENTERTAINMENT, TransactionClassifier.categorize(movieTx))

        val bookTx = Transaction(
            transactionId = "t7",
            type = TransactionType.SEND,
            amount = 2500L,
            timestamp = System.currentTimeMillis(),
            description = "Book club subscription 📚"
        )
        assertEquals(SpendingCategory.ENTERTAINMENT, TransactionClassifier.categorize(bookTx))
    }

    @Test
    fun testTransactionClassifierSavingsAndVaults() {
        val savingsTx = Transaction(
            transactionId = "t8",
            type = TransactionType.SAVINGS_DEPOSIT,
            amount = 15000L,
            timestamp = System.currentTimeMillis(),
            description = "Emergency fund allocation"
        )
        assertEquals(SpendingCategory.SAVINGS, TransactionClassifier.categorize(savingsTx))
    }

    @Test
    fun testTransactionClassifierGeneralTransfers() {
        val genericSend = Transaction(
            transactionId = "t9",
            type = TransactionType.SEND,
            amount = 10000L,
            timestamp = System.currentTimeMillis(),
            description = "Payment for friend"
        )
        assertEquals(SpendingCategory.TRANSFERS, TransactionClassifier.categorize(genericSend))
    }

    @Test
    fun testDefaultCategoryBudgetsLoaded() {
        val budgets = repository.categoryBudgets.value
        assertTrue(budgets.isNotEmpty())
        assertTrue(budgets.any { it.category == SpendingCategory.FOOD_DINING })
        assertTrue(budgets.any { it.category == SpendingCategory.BILLS_UTILITIES })
        assertTrue(budgets.any { it.category == SpendingCategory.SHOPPING })
    }

    @Test
    fun testSetCategoryBudgetUpdateAndAdd() {
        // Update existing Food & Dining budget to $350.00 (35,000 cents)
        repository.setCategoryBudget(SpendingCategory.FOOD_DINING, 35000L)
        val updated = repository.categoryBudgets.value.first { it.category == SpendingCategory.FOOD_DINING }
        assertEquals(35000L, updated.monthlyLimitCents)

        // Delete budget
        repository.deleteCategoryBudget(SpendingCategory.FOOD_DINING)
        assertFalse(repository.categoryBudgets.value.any { it.category == SpendingCategory.FOOD_DINING })

        // Re-add budget
        repository.setCategoryBudget(SpendingCategory.FOOD_DINING, 40000L)
        assertTrue(repository.categoryBudgets.value.any { it.category == SpendingCategory.FOOD_DINING && it.monthlyLimitCents == 40000L })
    }

    @Test
    fun testCategoryBudgetCalculations() {
        val budget = CategoryBudget(
            category = SpendingCategory.FOOD_DINING,
            monthlyLimitCents = 20000L // $200.00
        )

        // Case 1: Under budget ($120.00 spent)
        assertFalse(budget.isOverBudget(12000L))
        assertEquals(0.6f, budget.progressFraction(12000L), 0.001f)
        assertEquals(8000L, budget.remainingCents(12000L))
        assertEquals(0L, budget.overBudgetCents(12000L))

        // Case 2: Over budget ($250.00 spent)
        assertTrue(budget.isOverBudget(25000L))
        assertEquals(1.25f, budget.progressFraction(25000L), 0.001f)
        assertEquals(0L, budget.remainingCents(25000L))
        assertEquals(5000L, budget.overBudgetCents(25000L))
    }

    @Test
    fun testAnalyticsSummaryCalculation() {
        val summary = repository.getAnalyticsSummary(AnalyticsTimePeriod.ALL_TIME)

        // Income should include tx_1001 ($150) + tx_1002 ($500) + tx_1007 ($80) = $730.00 (73,000 cents)
        assertEquals(73000L, summary.totalIncomeCents)

        // Outflows should include tx_1003 ($45) + tx_1004 ($8.50) + tx_1005 ($100) + tx_1006 ($25) = $178.50 (17,850 cents)
        assertEquals(17850L, summary.totalExpenseCents)

        // Net cashflow: 73000 - 17850 = 55150 cents
        assertEquals(55150L, summary.netCashFlowCents)
        assertTrue(summary.netCashFlowCents > 0L)

        // Savings rate should be around 75%
        assertTrue(summary.savingsRatePercent > 50)

        // Largest single expense is tx_1005 ($100 = 10,000 cents)
        assertEquals(10000L, summary.largestExpenseCents)

        // Slices should sum up to totalExpenseCents
        val slicesSum = summary.spendingSlices.sumOf { it.amountCents }
        assertEquals(summary.totalExpenseCents, slicesSum)

        // Daily spend bars should exist
        assertEquals(7, summary.dailySpendBars.size)
    }

    @Test
    fun testFinancialHealthScoreGrading() {
        // High savings rate and surplus -> Excellent / Healthy
        val excellent = FinancialHealthScore.calculate(
            incomeCents = 500000L,
            expenseCents = 200000L,
            savingsRatePercent = 60,
            overBudgetCategories = 0
        )
        assertTrue(excellent.score >= 85)
        assertEquals("Excellent", excellent.rating)

        // Deficit spending with exceeded budgets -> Needs Attention
        val poor = FinancialHealthScore.calculate(
            incomeCents = 100000L,
            expenseCents = 250000L,
            savingsRatePercent = 0,
            overBudgetCategories = 3
        )
        assertTrue(poor.score < 50)
        assertEquals("Needs Attention", poor.rating)
    }

    @Test
    fun testAnalyticsViewModelFilteringAndCategorySelection() {
        // Initially no category selected
        assertNull(viewModel.uiState.value.selectedCategory)
        val initialTxCount = viewModel.uiState.value.categoryTransactions.size

        // Select FOOD_DINING
        viewModel.selectCategory(SpendingCategory.FOOD_DINING)
        assertEquals(SpendingCategory.FOOD_DINING, viewModel.uiState.value.selectedCategory)
        assertTrue(viewModel.uiState.value.categoryTransactions.all {
            TransactionClassifier.categorize(it) == SpendingCategory.FOOD_DINING
        })

        // Toggle again unselects and restores full list
        viewModel.selectCategory(SpendingCategory.FOOD_DINING)
        assertNull(viewModel.uiState.value.selectedCategory)
        assertEquals(initialTxCount, viewModel.uiState.value.categoryTransactions.size)
    }

    @Test
    fun testAnalyticsViewModelBudgetValidationAndSave() {
        viewModel.openEditBudget(SpendingCategory.SHOPPING)
        assertTrue(viewModel.uiState.value.showEditBudgetDialog)
        assertEquals(SpendingCategory.SHOPPING, viewModel.uiState.value.editingBudgetCategory)

        // Test invalid negative or non-numeric input
        viewModel.onLimitInputChange("-50")
        viewModel.saveBudget()
        assertNotNull(viewModel.uiState.value.editingLimitError)
        assertTrue(viewModel.uiState.value.showEditBudgetDialog)

        // Test valid input $320.50
        viewModel.onLimitInputChange("320.50")
        viewModel.saveBudget()
        assertNull(viewModel.uiState.value.editingLimitError)
        assertFalse(viewModel.uiState.value.showEditBudgetDialog)

        val saved = repository.categoryBudgets.value.first { it.category == SpendingCategory.SHOPPING }
        assertEquals(32050L, saved.monthlyLimitCents)
    }

    @Test
    fun testResetDemoDataRestoresBudgets() {
        // Change budget
        repository.setCategoryBudget(SpendingCategory.FOOD_DINING, 99999L)
        assertEquals(99999L, repository.categoryBudgets.value.first { it.category == SpendingCategory.FOOD_DINING }.monthlyLimitCents)

        // Reset demo
        repository.resetDemoData()
        assertEquals(20000L, repository.categoryBudgets.value.first { it.category == SpendingCategory.FOOD_DINING }.monthlyLimitCents)
    }
}
