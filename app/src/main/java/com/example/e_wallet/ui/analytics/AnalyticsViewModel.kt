package com.example.e_wallet.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.AnalyticsSummary
import com.example.e_wallet.model.AnalyticsTimePeriod
import com.example.e_wallet.model.CategoryBudget
import com.example.e_wallet.model.SpendingCategory
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.model.TransactionClassifier
import com.example.e_wallet.model.TransactionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val selectedPeriod: AnalyticsTimePeriod = AnalyticsTimePeriod.THIS_MONTH,
    val selectedCategory: SpendingCategory? = null,
    val summary: AnalyticsSummary = AnalyticsSummary(),
    val categoryBudgets: List<CategoryBudget> = emptyList(),
    val categoryTransactions: List<Transaction> = emptyList(),
    val showEditBudgetDialog: Boolean = false,
    val editingBudgetCategory: SpendingCategory? = null,
    val editingLimitInput: String = "",
    val editingLimitError: String? = null,
    val isExporting: Boolean = false,
    val snackbarMessage: String? = null
)

class AnalyticsViewModel(
    val repository: WalletRepository = WalletRepositoryProvider.repository,
    private val scope: CoroutineScope? = null
) : ViewModel() {

    private val coroutineScope: CoroutineScope
        get() = scope ?: viewModelScope

    private val _selectedPeriod = MutableStateFlow(AnalyticsTimePeriod.THIS_MONTH)
    private val _selectedCategory = MutableStateFlow<SpendingCategory?>(null)
    private val _showEditBudgetDialog = MutableStateFlow(false)
    private val _editingBudgetCategory = MutableStateFlow<SpendingCategory?>(null)
    private val _editingLimitInput = MutableStateFlow("")
    private val _editingLimitError = MutableStateFlow<String?>(null)
    private val _isExporting = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<AnalyticsUiState> = combine(
        repository.transactions,
        repository.categoryBudgets,
        _selectedPeriod,
        _selectedCategory,
        _showEditBudgetDialog,
        _editingBudgetCategory,
        _editingLimitInput,
        _editingLimitError,
        _isExporting,
        _snackbarMessage
    ) { args: Array<Any?> ->
        val txs = args[0] as List<Transaction>
        val budgets = args[1] as List<CategoryBudget>
        val period = args[2] as AnalyticsTimePeriod
        val selectedCat = args[3] as SpendingCategory?
        val showDialog = args[4] as Boolean
        val editCat = args[5] as SpendingCategory?
        val limitInput = args[6] as String
        val limitError = args[7] as String?
        val exporting = args[8] as Boolean
        val snackbar = args[9] as String?

        // Recompute summary dynamically for selected period
        val summary = repository.getAnalyticsSummary(period)

        // Filter transactions for period and selected category
        val now = System.currentTimeMillis()
        val startTs = period.getStartTimestampMillis(now)
        val endTs = period.getEndTimestampMillis(now)

        val periodCompletedTxs = txs.filter {
            it.status == TransactionStatus.COMPLETED && it.timestamp in startTs..endTs
        }

        val filteredTxs = if (selectedCat != null) {
            periodCompletedTxs.filter { TransactionClassifier.categorize(it) == selectedCat }
        } else {
            periodCompletedTxs
        }

        AnalyticsUiState(
            isLoading = false,
            selectedPeriod = period,
            selectedCategory = selectedCat,
            summary = summary,
            categoryBudgets = budgets,
            categoryTransactions = filteredTxs,
            showEditBudgetDialog = showDialog,
            editingBudgetCategory = editCat,
            editingLimitInput = limitInput,
            editingLimitError = limitError,
            isExporting = exporting,
            snackbarMessage = snackbar
        )
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = AnalyticsUiState()
    )

    fun setPeriod(period: AnalyticsTimePeriod) {
        _selectedPeriod.value = period
    }

    fun selectCategory(category: SpendingCategory?) {
        // Toggle selection: if already selected, unselect to show all
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun openEditBudget(category: SpendingCategory? = null) {
        val targetCategory = category ?: SpendingCategory.FOOD_DINING
        val existingBudget = repository.categoryBudgets.value.firstOrNull { it.category == targetCategory }
        val initialInput = if (existingBudget != null) {
            (existingBudget.monthlyLimitCents / 100.0).let {
                if (it % 1.0 == 0.0) it.toLong().toString() else String.format("%.2f", it)
            }
        } else {
            "200"
        }
        _editingBudgetCategory.value = targetCategory
        _editingLimitInput.value = initialInput
        _editingLimitError.value = null
        _showEditBudgetDialog.value = true
    }

    fun dismissEditBudget() {
        _showEditBudgetDialog.value = false
        _editingBudgetCategory.value = null
        _editingLimitInput.value = ""
        _editingLimitError.value = null
    }

    fun onLimitInputChange(text: String) {
        _editingLimitInput.value = text
        _editingLimitError.value = null
    }

    fun setEditingCategory(category: SpendingCategory) {
        _editingBudgetCategory.value = category
        val existing = repository.categoryBudgets.value.firstOrNull { it.category == category }
        if (existing != null) {
            val dollars = existing.monthlyLimitCents / 100.0
            _editingLimitInput.value = if (dollars % 1.0 == 0.0) dollars.toLong().toString() else String.format("%.2f", dollars)
        }
        _editingLimitError.value = null
    }

    fun saveBudget() {
        val category = _editingBudgetCategory.value ?: return
        val rawInput = _editingLimitInput.value.trim().replace("$", "").replace(",", "")
        val amountDouble = rawInput.toDoubleOrNull()

        if (amountDouble == null || amountDouble <= 0.0) {
            _editingLimitError.value = "Enter a valid monthly budget amount (e.g. 250.00)"
            return
        }

        if (amountDouble > 50000.0) {
            _editingLimitError.value = "Maximum monthly limit is $50,000.00"
            return
        }

        val limitCents = (amountDouble * 100).toLong()
        repository.setCategoryBudget(category, limitCents)
        _snackbarMessage.value = "Updated budget for ${category.displayName} to $${String.format("%.2f", amountDouble)}"
        dismissEditBudget()
    }

    fun deleteBudget(category: SpendingCategory) {
        repository.deleteCategoryBudget(category)
        _snackbarMessage.value = "Removed budget for ${category.displayName}"
        dismissEditBudget()
    }

    fun exportStatement() {
        coroutineScope.launch {
            _isExporting.value = true
            delay(800) // Simulated processing
            _isExporting.value = false
            _snackbarMessage.value = "Financial Statement for ${_selectedPeriod.value.displayName} exported to PDF (Simulated)"
        }
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }
}
