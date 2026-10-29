package com.example.e_wallet.ui.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.SavingsCategory
import com.example.e_wallet.model.SavingsGoal
import com.example.e_wallet.model.SavingsGoalResult
import com.example.e_wallet.model.Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SavingsGoalFilter(val label: String) {
    ALL("All Vaults"),
    ACTIVE("In Progress"),
    COMPLETED("Completed"),
    LOCKED("Locked 🔒")
}

data class SavingsUiState(
    val goals: List<SavingsGoal> = emptyList(),
    val filteredGoals: List<SavingsGoal> = emptyList(),
    val totalSavedCents: Long = 0L,
    val totalTargetCents: Long = 0L,
    val overallProgressPercent: Int = 0,
    val projectedMonthlyInterestCents: Long = 0L,
    val walletAvailableBalanceCents: Long = 0L,
    val selectedFilter: SavingsGoalFilter = SavingsGoalFilter.ALL,
    val selectedGoal: SavingsGoal? = null,
    val showCreateDialog: Boolean = false,
    val showDetailSheet: Boolean = false,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
)

class SavingsViewModel(
    val repository: WalletRepository = WalletRepositoryProvider.repository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val _selectedFilter = MutableStateFlow(SavingsGoalFilter.ALL)
    private val _selectedGoal = MutableStateFlow<SavingsGoal?>(null)
    private val _showCreateDialog = MutableStateFlow(false)
    private val _showDetailSheet = MutableStateFlow(false)
    private val _isProcessing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    private fun computeUiState(
        goals: List<SavingsGoal>,
        wallet: Wallet,
        filter: SavingsGoalFilter,
        selectedGoal: SavingsGoal?
    ): SavingsUiState {
        val filtered = when (filter) {
            SavingsGoalFilter.ALL -> goals
            SavingsGoalFilter.ACTIVE -> goals.filter { !it.isCompleted }
            SavingsGoalFilter.COMPLETED -> goals.filter { it.isCompleted }
            SavingsGoalFilter.LOCKED -> goals.filter { it.isLocked }
        }

        val totalSaved = goals.sumOf { it.currentAmountCents }
        val totalTarget = goals.sumOf { it.targetAmountCents }
        val progress = if (totalTarget > 0) {
            ((totalSaved.toFloat() / totalTarget.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else 0

        val totalInterest = goals.sumOf { it.projectedMonthlyInterestCents }

        val updatedSelected = selectedGoal?.let { current ->
            goals.firstOrNull { it.id == current.id } ?: current
        }

        return SavingsUiState(
            goals = goals,
            filteredGoals = filtered,
            totalSavedCents = totalSaved,
            totalTargetCents = totalTarget,
            overallProgressPercent = progress,
            projectedMonthlyInterestCents = totalInterest,
            walletAvailableBalanceCents = wallet.availableBalance,
            selectedFilter = filter,
            selectedGoal = updatedSelected,
            showCreateDialog = _showCreateDialog.value,
            showDetailSheet = _showDetailSheet.value,
            isProcessing = _isProcessing.value,
            errorMessage = _errorMessage.value,
            snackbarMessage = _snackbarMessage.value
        )
    }

    val uiState: StateFlow<SavingsUiState> = combine(
        repository.savingsGoals,
        repository.currentWallet,
        _selectedFilter,
        _selectedGoal
    ) { goals, wallet, filter, selectedGoal ->
        computeUiState(goals, wallet, filter, selectedGoal)
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = computeUiState(
            repository.savingsGoals.value,
            repository.currentWallet.value,
            _selectedFilter.value,
            _selectedGoal.value
        )
    )

    fun setFilter(filter: SavingsGoalFilter) {
        _selectedFilter.value = filter
    }

    fun openCreateGoalDialog() {
        _showCreateDialog.value = true
        _errorMessage.value = null
    }

    fun dismissCreateGoalDialog() {
        _showCreateDialog.value = false
        _errorMessage.value = null
    }

    fun openGoalDetail(goal: SavingsGoal) {
        _selectedGoal.value = goal
        _showDetailSheet.value = true
        _errorMessage.value = null
    }

    fun dismissGoalDetail() {
        _showDetailSheet.value = false
        _selectedGoal.value = null
        _errorMessage.value = null
    }

    fun createGoal(
        name: String,
        targetAmountCents: Long,
        initialDepositCents: Long = 0L,
        category: SavingsCategory = SavingsCategory.GENERAL,
        targetDateMillis: Long? = null,
        isLocked: Boolean = false,
        note: String = ""
    ) {
        scope.launch {
            _isProcessing.value = true
            val result = repository.createSavingsGoal(
                name = name,
                targetAmountCents = targetAmountCents,
                initialDepositCents = initialDepositCents,
                category = category,
                targetDateMillis = targetDateMillis,
                isLocked = isLocked,
                note = note
            )
            _isProcessing.value = false
            when (result) {
                is SavingsGoalResult.Success -> {
                    _showCreateDialog.value = false
                    _snackbarMessage.value = result.message
                }
                is SavingsGoalResult.Error -> {
                    _errorMessage.value = result.message
                    _snackbarMessage.value = result.message
                }
            }
        }
    }

    fun depositToGoal(
        goalId: String,
        amountCents: Long,
        note: String = ""
    ) {
        scope.launch {
            _isProcessing.value = true
            val result = repository.depositToGoal(goalId, amountCents, note)
            _isProcessing.value = false
            when (result) {
                is SavingsGoalResult.Success -> {
                    _selectedGoal.value = result.goal
                    _snackbarMessage.value = result.message
                }
                is SavingsGoalResult.Error -> {
                    _errorMessage.value = result.message
                    _snackbarMessage.value = result.message
                }
            }
        }
    }

    fun withdrawFromGoal(
        goalId: String,
        amountCents: Long,
        pin: String,
        note: String = ""
    ) {
        scope.launch {
            _isProcessing.value = true
            val result = repository.withdrawFromGoal(goalId, amountCents, pin, note)
            _isProcessing.value = false
            when (result) {
                is SavingsGoalResult.Success -> {
                    _selectedGoal.value = result.goal
                    _snackbarMessage.value = result.message
                }
                is SavingsGoalResult.Error -> {
                    _errorMessage.value = result.message
                    _snackbarMessage.value = result.message
                }
            }
        }
    }

    fun deleteGoal(
        goalId: String,
        pin: String
    ) {
        scope.launch {
            _isProcessing.value = true
            val result = repository.deleteSavingsGoal(goalId, pin)
            _isProcessing.value = false
            when (result) {
                is SavingsGoalResult.Success -> {
                    _showDetailSheet.value = false
                    _selectedGoal.value = null
                    _snackbarMessage.value = result.message
                }
                is SavingsGoalResult.Error -> {
                    _errorMessage.value = result.message
                    _snackbarMessage.value = result.message
                }
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
