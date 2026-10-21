package com.example.e_wallet.ui.deposit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.DepositResult
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.Transaction
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DepositStep {
    INPUT,
    CONFIRMATION,
    PROCESSING,
    SUCCESS,
    ERROR
}

data class DepositUiState(
    val step: DepositStep = DepositStep.INPUT,
    val amountInput: String = "50",
    val noteInput: String = "Demo bank deposit",
    val sourceName: String = "Demo Checking Account (•••• 4812)",
    val errorMessage: String? = null,
    val isProcessing: Boolean = false,
    val completedTransaction: Transaction? = null,
    val newBalanceCents: Long = 0L,
    val quickAmounts: List<Int> = listOf(10, 25, 50, 100, 250)
) {
    val amountCents: Long
        get() {
            val cleaned = amountInput.trim().replace("$", "").replace(",", ".")
            return try {
                if (cleaned.isBlank() || cleaned == ".") 0L
                else {
                    val bd = BigDecimal(cleaned)
                    bd.setScale(2, RoundingMode.HALF_UP).movePointRight(2).toLong()
                }
            } catch (e: Exception) {
                0L
            }
        }

    val isValidAmount: Boolean
        get() = amountCents > 0L && amountCents <= 500000L
}

class DepositViewModel(
    private val repository: WalletRepository = WalletRepositoryProvider.repository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val _uiState = MutableStateFlow(DepositUiState())
    val uiState: StateFlow<DepositUiState> = _uiState.asStateFlow()

    fun onAmountChanged(amount: String) {
        val normalized = amount.replace(',', '.')
        val sanitized = normalized.filter { it.isDigit() || it == '.' }
        val dots = sanitized.count { it == '.' }
        if (dots > 1) return

        val dotIndex = sanitized.indexOf('.')
        if (dotIndex != -1 && sanitized.length - dotIndex - 1 > 2) return

        val cents = try {
            if (sanitized.isBlank() || sanitized == ".") 0L
            else BigDecimal(sanitized).setScale(2, RoundingMode.HALF_UP).movePointRight(2).toLong()
        } catch (e: Exception) {
            0L
        }

        val limitError = if (cents > 500000L) "Deposit limit is $5,000.00 per demo transaction" else null

        _uiState.update {
            it.copy(
                amountInput = sanitized,
                errorMessage = limitError
            )
        }
    }

    fun onQuickAmountSelected(amount: Int) {
        _uiState.update {
            it.copy(
                amountInput = amount.toString(),
                errorMessage = null
            )
        }
    }

    fun onNoteChanged(note: String) {
        _uiState.update { it.copy(noteInput = note) }
    }

    fun proceedToConfirmation() {
        val state = _uiState.value
        val cents = state.amountCents
        if (cents <= 0L) {
            _uiState.update { it.copy(errorMessage = "Please enter an amount greater than $0.00") }
            return
        }
        if (cents > 500000L) {
            _uiState.update { it.copy(errorMessage = "Deposit limit is $5,000.00 per demo transaction") }
            return
        }

        _uiState.update {
            it.copy(
                step = DepositStep.CONFIRMATION,
                errorMessage = null
            )
        }
    }

    fun backToInput() {
        if (_uiState.value.isProcessing) return
        _uiState.update {
            it.copy(
                step = DepositStep.INPUT,
                errorMessage = null
            )
        }
    }

    fun confirmDeposit() {
        val currentState = _uiState.value
        if (currentState.isProcessing) return

        val cents = currentState.amountCents
        if (cents <= 0L || cents > 500000L) {
            _uiState.update { it.copy(errorMessage = "Invalid deposit amount", step = DepositStep.INPUT) }
            return
        }

        _uiState.update {
            it.copy(
                isProcessing = true,
                step = DepositStep.PROCESSING,
                errorMessage = null
            )
        }

        scope.launch {
            // Simulated slight network processing latency
            delay(500)

            when (val result = repository.deposit(
                amountCents = cents,
                note = currentState.noteInput.ifBlank { "Simulated Demo Bank Top-up" },
                sourceDescription = currentState.sourceName
            )) {
                is DepositResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            step = DepositStep.SUCCESS,
                            completedTransaction = result.transaction,
                            newBalanceCents = result.newBalance,
                            errorMessage = null
                        )
                    }
                }
                is DepositResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            step = DepositStep.ERROR,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun reset() {
        _uiState.value = DepositUiState()
    }
}
