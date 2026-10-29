package com.example.e_wallet.ui.withdraw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.data.WithdrawResult
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.util.Formatters
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class WithdrawStep {
    INPUT,
    CONFIRMATION,
    PROCESSING,
    SUCCESS,
    ERROR
}

data class WithdrawUiState(
    val step: WithdrawStep = WithdrawStep.INPUT,
    val amountInput: String = "50",
    val noteInput: String = "Simulated ATM Cashout",
    val destinationName: String = "ATM Cashout (Cardless Demo)",
    val destinations: List<String> = listOf(
        "ATM Cashout (Cardless Demo)",
        "Demo Checking Account (•••• 4812)",
        "Debit Card (•••• 9931)"
    ),
    val feeCents: Long = 0L,
    val availableBalanceCents: Long = 0L,
    val errorMessage: String? = null,
    val isProcessing: Boolean = false,
    val completedTransaction: Transaction? = null,
    val newBalanceCents: Long = 0L,
    val quickAmounts: List<Int> = listOf(20, 50, 100, 200, 500)
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

    val totalDebitCents: Long
        get() = amountCents + feeCents

    val remainingBalanceCents: Long
        get() = (availableBalanceCents - totalDebitCents).coerceAtLeast(0L)

    val isValidAmount: Boolean
        get() = amountCents > 0L && totalDebitCents <= availableBalanceCents && amountCents <= 200000L
}

class WithdrawViewModel(
    private val repository: WalletRepository = WalletRepositoryProvider.repository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val _uiState = MutableStateFlow(
        WithdrawUiState(
            availableBalanceCents = repository.currentWallet.value.availableBalance
        )
    )
    val uiState: StateFlow<WithdrawUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            repository.currentWallet.collect { wallet ->
                _uiState.update { current ->
                    current.copy(availableBalanceCents = wallet.availableBalance)
                }
            }
        }

        scope.launch {
            repository.bankAccounts.collect { accounts ->
                _uiState.update { current ->
                    val dynamicDestinations = listOf("ATM Cashout (Cardless Demo)") +
                        accounts.map { "${it.bankName} (${it.accountNumberMasked})" }
                    current.copy(
                        destinations = dynamicDestinations,
                        destinationName = if (current.destinationName in dynamicDestinations) current.destinationName else dynamicDestinations.first()
                    )
                }
            }
        }
    }

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

        val limitError = when {
            cents > 200000L -> "Withdrawal limit is $2,000.00 per demo transaction"
            cents > _uiState.value.availableBalanceCents -> "Amount exceeds available balance (${Formatters.formatCentsToCurrency(_uiState.value.availableBalanceCents)})"
            else -> null
        }

        _uiState.update {
            it.copy(
                amountInput = sanitized,
                errorMessage = limitError
            )
        }
    }

    fun onQuickAmountSelected(amount: Int) {
        val cents = amount * 100L
        val limitError = when {
            cents > 200000L -> "Withdrawal limit is $2,000.00 per demo transaction"
            cents > _uiState.value.availableBalanceCents -> "Amount exceeds available balance (${Formatters.formatCentsToCurrency(_uiState.value.availableBalanceCents)})"
            else -> null
        }

        _uiState.update {
            it.copy(
                amountInput = amount.toString(),
                errorMessage = limitError
            )
        }
    }

    fun onUseMaxAmount() {
        val maxAllowed = minOf(_uiState.value.availableBalanceCents, 200000L)
        val formattedAmount = if (maxAllowed <= 0L) {
            "0"
        } else {
            val bd = BigDecimal(maxAllowed).movePointLeft(2)
            if (maxAllowed % 100L == 0L) {
                bd.toInt().toString()
            } else {
                bd.setScale(2, RoundingMode.HALF_UP).toPlainString()
            }
        }

        _uiState.update {
            it.copy(
                amountInput = formattedAmount,
                errorMessage = if (maxAllowed == 0L) "Available balance is $0.00" else null
            )
        }
    }

    fun onDestinationSelected(destination: String) {
        _uiState.update { it.copy(destinationName = destination) }
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
        if (cents > 200000L) {
            _uiState.update { it.copy(errorMessage = "Withdrawal limit is $2,000.00 per demo transaction") }
            return
        }
        if (state.totalDebitCents > state.availableBalanceCents) {
            _uiState.update {
                it.copy(
                    errorMessage = "Insufficient funds. Available balance is ${Formatters.formatCentsToCurrency(state.availableBalanceCents)}"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                step = WithdrawStep.CONFIRMATION,
                errorMessage = null
            )
        }
    }

    fun backToInput() {
        if (_uiState.value.isProcessing) return
        _uiState.update {
            it.copy(
                step = WithdrawStep.INPUT,
                errorMessage = null
            )
        }
    }

    fun confirmWithdraw() {
        val currentState = _uiState.value
        if (currentState.isProcessing) return

        val cents = currentState.amountCents
        if (cents <= 0L || cents > 200000L || currentState.totalDebitCents > currentState.availableBalanceCents) {
            _uiState.update {
                it.copy(
                    errorMessage = "Invalid withdrawal request",
                    step = WithdrawStep.INPUT
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isProcessing = true,
                step = WithdrawStep.PROCESSING,
                errorMessage = null
            )
        }

        scope.launch {
            // Simulated network latency
            delay(500)

            when (val result = repository.withdraw(
                amountCents = cents,
                note = currentState.noteInput.ifBlank { "Simulated ATM Cashout" },
                destinationDescription = currentState.destinationName,
                feeCents = currentState.feeCents
            )) {
                is WithdrawResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            step = WithdrawStep.SUCCESS,
                            completedTransaction = result.transaction,
                            newBalanceCents = result.newBalance,
                            errorMessage = null
                        )
                    }
                }
                is WithdrawResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            step = WithdrawStep.ERROR,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun reset() {
        val accounts = repository.bankAccounts.value
        val dynamicDestinations = listOf("ATM Cashout (Cardless Demo)") +
            accounts.map { "${it.bankName} (${it.accountNumberMasked})" }
        _uiState.value = WithdrawUiState(
            availableBalanceCents = repository.currentWallet.value.availableBalance,
            destinations = dynamicDestinations,
            destinationName = dynamicDestinations.first()
        )
    }
}
