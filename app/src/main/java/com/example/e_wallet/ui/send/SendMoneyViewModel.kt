package com.example.e_wallet.ui.send

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.SendResult
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.Beneficiary
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

enum class SendMoneyStep {
    SELECT_RECEIVER,
    INPUT_AMOUNT,
    CONFIRMATION,
    PIN_VERIFICATION,
    PROCESSING,
    SUCCESS,
    ERROR
}

data class SendMoneyUiState(
    val step: SendMoneyStep = SendMoneyStep.SELECT_RECEIVER,
    val searchQuery: String = "",
    val allBeneficiaries: List<Beneficiary> = emptyList(),
    val filteredBeneficiaries: List<Beneficiary> = emptyList(),
    val selectedReceiver: Beneficiary? = null,
    val customReceiverInput: String = "",
    val amountInput: String = "25",
    val noteInput: String = "",
    val feeCents: Long = 0L, // Free demo transfer
    val availableBalanceCents: Long = 0L,
    val pinInput: String = "",
    val pinError: String? = null,
    val pinAttemptsLeft: Int = 3,
    val demoPin: String = "1234",
    val errorMessage: String? = null,
    val isProcessing: Boolean = false,
    val completedTransaction: Transaction? = null,
    val newBalanceCents: Long = 0L,
    val quickAmounts: List<Int> = listOf(10, 25, 50, 100, 250),
    val isInitialBeneficiaryPreselected: Boolean = false
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
        get() = amountCents > 0L && totalDebitCents <= availableBalanceCents && amountCents <= 250000L

    val receiverDisplayName: String
        get() = selectedReceiver?.displayName ?: customReceiverInput.ifBlank { "Unknown Receiver" }

    val receiverWalletNumber: String
        get() = selectedReceiver?.walletNumber ?: customReceiverInput

    val receiverId: String
        get() = selectedReceiver?.receiverUserId ?: "usr_custom_${customReceiverInput.hashCode().coerceAtLeast(0)}"
}

class SendMoneyViewModel(
    private val repository: WalletRepository = WalletRepositoryProvider.repository,
    coroutineScope: CoroutineScope? = null,
    initialBeneficiary: Beneficiary? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val _uiState = MutableStateFlow(
        SendMoneyUiState(
            availableBalanceCents = repository.currentWallet.value.availableBalance,
            allBeneficiaries = repository.beneficiaries.value,
            filteredBeneficiaries = repository.beneficiaries.value,
            selectedReceiver = initialBeneficiary,
            step = if (initialBeneficiary != null) SendMoneyStep.INPUT_AMOUNT else SendMoneyStep.SELECT_RECEIVER,
            isInitialBeneficiaryPreselected = initialBeneficiary != null
        )
    )
    val uiState: StateFlow<SendMoneyUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            repository.currentWallet.collect { wallet ->
                _uiState.update { current ->
                    current.copy(availableBalanceCents = wallet.availableBalance)
                }
            }
        }

        scope.launch {
            repository.beneficiaries.collect { beneficiaries ->
                _uiState.update { current ->
                    val query = current.searchQuery
                    val filtered = if (query.isBlank()) {
                        beneficiaries
                    } else {
                        repository.searchReceivers(query)
                    }
                    current.copy(
                        allBeneficiaries = beneficiaries,
                        filteredBeneficiaries = filtered
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val filtered = if (query.isBlank()) {
                current.allBeneficiaries
            } else {
                repository.searchReceivers(query)
            }
            current.copy(
                searchQuery = query,
                filteredBeneficiaries = filtered,
                errorMessage = null
            )
        }
    }

    fun onReceiverSelected(beneficiary: Beneficiary) {
        val currentUser = repository.currentUser.value
        val currentWallet = repository.currentWallet.value

        if (beneficiary.receiverUserId == currentUser.userId ||
            beneficiary.walletNumber.equals(currentWallet.walletNumber, ignoreCase = true)
        ) {
            _uiState.update { it.copy(errorMessage = "Self-transfer is not supported. Please select another contact.") }
            return
        }

        _uiState.update { current ->
            current.copy(
                selectedReceiver = beneficiary,
                customReceiverInput = "",
                step = SendMoneyStep.INPUT_AMOUNT,
                errorMessage = null
            )
        }
    }

    fun onSelectCustomReceiver(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid wallet number, phone number, or name.") }
            return
        }

        val currentUser = repository.currentUser.value
        val currentWallet = repository.currentWallet.value

        if (trimmed.equals(currentWallet.walletNumber, ignoreCase = true) ||
            trimmed.equals(currentUser.fullName, ignoreCase = true) ||
            trimmed.equals(currentUser.phoneNumber, ignoreCase = true)
        ) {
            _uiState.update { it.copy(errorMessage = "Self-transfer is not supported.") }
            return
        }

        // Check if matches an existing beneficiary
        val matched = _uiState.value.allBeneficiaries.firstOrNull {
            it.walletNumber.equals(trimmed, ignoreCase = true) ||
            it.displayName.equals(trimmed, ignoreCase = true)
        }

        if (matched != null) {
            onReceiverSelected(matched)
            return
        }

        // Custom receiver
        val initials = trimmed.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
            .ifBlank { "PP" }

        val customBeneficiary = Beneficiary(
            beneficiaryId = "ben_temp_${System.currentTimeMillis()}",
            ownerUserId = currentUser.userId,
            receiverUserId = "usr_${trimmed.replace(Regex("[^a-zA-Z0-9]"), "")}",
            displayName = trimmed,
            walletNumber = if (trimmed.startsWith("PKP-", ignoreCase = true)) trimmed.uppercase() else "PKP-${(1000..9999).random()}-${(1000..9999).random()}",
            avatarInitials = initials,
            isFavorite = false
        )

        _uiState.update { current ->
            current.copy(
                selectedReceiver = customBeneficiary,
                customReceiverInput = trimmed,
                step = SendMoneyStep.INPUT_AMOUNT,
                errorMessage = null
            )
        }
    }

    fun onChangeReceiver() {
        _uiState.update { current ->
            current.copy(
                step = SendMoneyStep.SELECT_RECEIVER,
                errorMessage = null
            )
        }
    }

    fun onAmountChanged(amount: String) {
        val normalized = amount.replace(',', '.')
        val sanitized = normalized.filter { it.isDigit() || it == '.' }
        val dots = sanitized.count { it == '.' }
        if (dots > 1) return

        val dotIndex = sanitized.indexOf('.')
        val formatted = if (dotIndex >= 0 && sanitized.length - dotIndex - 1 > 2) {
            sanitized.substring(0, dotIndex + 3)
        } else {
            sanitized
        }

        _uiState.update { current ->
            current.copy(
                amountInput = formatted,
                errorMessage = null
            )
        }
    }

    fun onQuickAmountSelected(dollars: Int) {
        _uiState.update { current ->
            current.copy(
                amountInput = dollars.toString(),
                errorMessage = null
            )
        }
    }

    fun onUseMaxAmount() {
        val available = _uiState.value.availableBalanceCents
        val maxTransferable = (available - _uiState.value.feeCents).coerceAtLeast(0L).coerceAtMost(250000L)
        val dollars = BigDecimal(maxTransferable).movePointLeft(2).setScale(2, RoundingMode.DOWN).toPlainString()

        _uiState.update { current ->
            current.copy(
                amountInput = dollars,
                errorMessage = null
            )
        }
    }

    fun onNoteChanged(note: String) {
        _uiState.update { current ->
            current.copy(noteInput = note.take(60))
        }
    }

    fun proceedToConfirmation() {
        val state = _uiState.value
        val amount = state.amountCents

        if (state.selectedReceiver == null && state.customReceiverInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please select or enter a recipient first.") }
            return
        }

        if (amount <= 0L) {
            _uiState.update { it.copy(errorMessage = "Please enter an amount greater than $0.00.") }
            return
        }

        if (amount > 250000L) {
            _uiState.update { it.copy(errorMessage = "Amount exceeds demo limit of $2,500.00.") }
            return
        }

        if (state.totalDebitCents > state.availableBalanceCents) {
            _uiState.update {
                it.copy(
                    errorMessage = "Insufficient funds. Available: ${Formatters.formatCentsToCurrency(state.availableBalanceCents)}"
                )
            }
            return
        }

        _uiState.update { current ->
            current.copy(
                step = SendMoneyStep.CONFIRMATION,
                errorMessage = null
            )
        }
    }

    fun proceedToPinVerification() {
        _uiState.update { current ->
            current.copy(
                step = SendMoneyStep.PIN_VERIFICATION,
                pinInput = "",
                pinError = null,
                errorMessage = null
            )
        }
    }

    fun onPinDigit(digit: Char) {
        val currentPin = _uiState.value.pinInput
        if (currentPin.length >= 4) return

        val newPin = currentPin + digit
        _uiState.update { it.copy(pinInput = newPin, pinError = null) }

        if (newPin.length == 4) {
            validatePinAndSubmit(newPin)
        }
    }

    fun onPinBackspace() {
        val currentPin = _uiState.value.pinInput
        if (currentPin.isNotEmpty()) {
            _uiState.update { it.copy(pinInput = currentPin.dropLast(1), pinError = null) }
        }
    }

    private fun validatePinAndSubmit(pin: String) {
        val state = _uiState.value
        val actualPin = repository.currentPin.value
        if (pin != actualPin) {
            val remaining = state.pinAttemptsLeft - 1
            if (remaining <= 0) {
                _uiState.update {
                    it.copy(
                        pinInput = "",
                        pinAttemptsLeft = 0,
                        step = SendMoneyStep.ERROR,
                        errorMessage = "Too many incorrect PIN attempts. Security lock activated for this prototype session."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        pinInput = "",
                        pinAttemptsLeft = remaining,
                        pinError = "Incorrect demo PIN. $remaining attempt${if (remaining == 1) "" else "s"} remaining (Demo PIN: $actualPin)"
                    )
                }
            }
            return
        }

        executeSend()
    }

    fun executeSend() {
        val state = _uiState.value
        if (state.isProcessing) return

        val receiver = state.selectedReceiver
        val receiverName = state.receiverDisplayName
        val receiverWallet = state.receiverWalletNumber
        val receiverId = state.receiverId
        val amount = state.amountCents
        val note = state.noteInput.ifBlank { "Transfer to $receiverName" }
        val fee = state.feeCents

        _uiState.update { current ->
            current.copy(
                isProcessing = true,
                step = SendMoneyStep.PROCESSING,
                errorMessage = null
            )
        }

        scope.launch {
            // Simulated slight network processing delay
            delay(750)

            val result = repository.sendMoney(
                receiverWalletNumber = receiverWallet,
                receiverName = receiverName,
                receiverId = receiverId,
                amountCents = amount,
                note = note,
                feeCents = fee
            )

            when (result) {
                is SendResult.Success -> {
                    _uiState.update { current ->
                        current.copy(
                            isProcessing = false,
                            step = SendMoneyStep.SUCCESS,
                            completedTransaction = result.transaction,
                            newBalanceCents = result.newBalance
                        )
                    }
                }
                is SendResult.Error -> {
                    _uiState.update { current ->
                        current.copy(
                            isProcessing = false,
                            step = SendMoneyStep.ERROR,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun backStep(): Boolean {
        val current = _uiState.value
        return when (current.step) {
            SendMoneyStep.INPUT_AMOUNT -> {
                if (current.isInitialBeneficiaryPreselected) {
                    false // Request close bottom sheet
                } else {
                    _uiState.update { it.copy(step = SendMoneyStep.SELECT_RECEIVER, errorMessage = null) }
                    true
                }
            }
            SendMoneyStep.CONFIRMATION -> {
                _uiState.update { it.copy(step = SendMoneyStep.INPUT_AMOUNT, errorMessage = null) }
                true
            }
            SendMoneyStep.PIN_VERIFICATION -> {
                _uiState.update { it.copy(step = SendMoneyStep.CONFIRMATION, pinInput = "", pinError = null) }
                true
            }
            SendMoneyStep.ERROR -> {
                _uiState.update { it.copy(step = SendMoneyStep.INPUT_AMOUNT, errorMessage = null, pinInput = "") }
                true
            }
            else -> false
        }
    }

    fun reset(initialBeneficiary: Beneficiary? = null) {
        _uiState.value = SendMoneyUiState(
            availableBalanceCents = repository.currentWallet.value.availableBalance,
            allBeneficiaries = repository.beneficiaries.value,
            filteredBeneficiaries = repository.beneficiaries.value,
            selectedReceiver = initialBeneficiary,
            step = if (initialBeneficiary != null) SendMoneyStep.INPUT_AMOUNT else SendMoneyStep.SELECT_RECEIVER,
            isInitialBeneficiaryPreselected = initialBeneficiary != null
        )
    }
}
