package com.example.e_wallet.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.BillCategory
import com.example.e_wallet.model.BillPaymentResult
import com.example.e_wallet.model.Biller
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.util.Formatters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Step progression for the Bill Payment & Utility Services user journey.
 */
enum class BillPaymentStep {
    SELECT_BILLER,     // Step 1: Browse by category or search provider
    ENTER_DETAILS,     // Step 2: Input account/meter number and payment amount
    CONFIRM_REVIEW,    // Step 3: Review provider, fees, total debit before authentication
    PIN_VERIFICATION,  // Step 4: Secure 4-digit PIN authentication
    SUCCESS            // Step 5: Official payment receipt with reference code
}

/**
 * UI State for the Bill Payment flow.
 */
data class BillPaymentUiState(
    val currentStep: BillPaymentStep = BillPaymentStep.SELECT_BILLER,
    val selectedCategory: BillCategory? = null,
    val searchQuery: String = "",
    val billers: List<Biller> = emptyList(),
    val filteredBillers: List<Biller> = emptyList(),
    val selectedBiller: Biller? = null,
    val accountNumber: String = "",
    val accountNumberError: String? = null,
    val amountInput: String = "",
    val amountCents: Long = 0L,
    val amountError: String? = null,
    val selectedPresetCents: Long? = null,
    val feeCents: Long = 0L,
    val totalDebitCents: Long = 0L,
    val note: String = "",
    val walletBalanceCents: Long = 0L,
    val pinInput: String = "",
    val pinError: String? = null,
    val isProcessing: Boolean = false,
    val completedTransaction: Transaction? = null,
    val receiptReference: String? = null,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
)

/**
 * ViewModel orchestrating bill payments, catalog filtering, account validation,
 * fee derivation, PIN security verification, and single-source-of-truth balance debits.
 */
class BillPaymentViewModel(
    val repository: WalletRepository = WalletRepositoryProvider.repository,
    coroutineScope: CoroutineScope? = null,
    initialCategory: BillCategory? = null,
    initialBiller: Biller? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val _uiState = MutableStateFlow(
        createInitialState(
            initialCategory = initialCategory ?: initialBiller?.category,
            initialBiller = initialBiller
        )
    )
    val uiState: StateFlow<BillPaymentUiState> = _uiState.asStateFlow()

    init {
        // Observe billers from single-source-of-truth repository
        scope.launch {
            repository.billers.collect { allBillers ->
                _uiState.update { current ->
                    val filtered = filterBillers(allBillers, current.selectedCategory, current.searchQuery)
                    current.copy(
                        billers = allBillers,
                        filteredBillers = filtered
                    )
                }
            }
        }

        // Observe current wallet balance
        scope.launch {
            repository.currentWallet.collect { wallet ->
                _uiState.update { current ->
                    current.copy(walletBalanceCents = wallet.availableBalance)
                }
            }
        }
    }

    private fun createInitialState(
        initialCategory: BillCategory?,
        initialBiller: Biller?
    ): BillPaymentUiState {
        val allBillers = repository.billers.value
        val initialFiltered = filterBillers(allBillers, initialCategory, "")
        val balance = repository.currentWallet.value.availableBalance
        val fee = initialBiller?.feeCents ?: 0L

        return BillPaymentUiState(
            currentStep = if (initialBiller != null) BillPaymentStep.ENTER_DETAILS else BillPaymentStep.SELECT_BILLER,
            selectedCategory = initialCategory,
            billers = allBillers,
            filteredBillers = initialFiltered,
            selectedBiller = initialBiller,
            feeCents = fee,
            walletBalanceCents = balance
        )
    }

    private fun filterBillers(
        allBillers: List<Biller>,
        category: BillCategory?,
        searchQuery: String
    ): List<Biller> {
        val q = searchQuery.trim().lowercase()
        return allBillers.filter { b ->
            val matchesCategory = category == null || b.category == category
            val matchesQuery = q.isEmpty() ||
                b.name.lowercase().contains(q) ||
                b.category.title.lowercase().contains(q) ||
                b.providerCode.lowercase().contains(q)
            matchesCategory && matchesQuery
        }
    }

    fun selectCategory(category: BillCategory?) {
        _uiState.update { current ->
            val filtered = filterBillers(current.billers, category, current.searchQuery)
            current.copy(
                selectedCategory = category,
                filteredBillers = filtered
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val filtered = filterBillers(current.billers, current.selectedCategory, query)
            current.copy(
                searchQuery = query,
                filteredBillers = filtered
            )
        }
    }

    fun selectBiller(biller: Biller) {
        _uiState.update { current ->
            current.copy(
                selectedBiller = biller,
                selectedCategory = biller.category,
                accountNumber = "",
                accountNumberError = null,
                amountInput = "",
                amountCents = 0L,
                amountError = null,
                selectedPresetCents = null,
                feeCents = biller.feeCents,
                totalDebitCents = 0L,
                currentStep = BillPaymentStep.ENTER_DETAILS
            )
        }
    }

    fun onAccountNumberChanged(account: String) {
        _uiState.update { current ->
            current.copy(
                accountNumber = account,
                accountNumberError = if (account.isNotBlank()) null else current.accountNumberError
            )
        }
    }

    fun onAmountChanged(input: String) {
        // Sanitize numeric input (allow only digits and at most one decimal separator)
        val filtered = input.filter { it.isDigit() || it == '.' }
        val parts = filtered.split('.')
        val sanitized = if (parts.size > 2) {
            parts[0] + "." + parts[1]
        } else if (parts.size == 2 && parts[1].length > 2) {
            parts[0] + "." + parts[1].take(2)
        } else {
            filtered
        }

        val parsedCents = parseAmountCents(sanitized)
        _uiState.update { current ->
            val fee = current.selectedBiller?.feeCents ?: 0L
            val totalDebit = if (parsedCents > 0L) parsedCents + fee else 0L
            current.copy(
                amountInput = sanitized,
                amountCents = parsedCents,
                selectedPresetCents = null,
                totalDebitCents = totalDebit,
                amountError = null
            )
        }
    }

    fun selectPresetAmount(cents: Long) {
        val sanitized = String.format(java.util.Locale.US, "%.2f", cents / 100.0)
        _uiState.update { current ->
            val fee = current.selectedBiller?.feeCents ?: 0L
            current.copy(
                amountInput = sanitized,
                amountCents = cents,
                selectedPresetCents = cents,
                totalDebitCents = cents + fee,
                amountError = null
            )
        }
    }

    fun onNoteChanged(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    fun proceedToReview(): Boolean {
        val state = _uiState.value
        val biller = state.selectedBiller ?: return false
        val account = state.accountNumber.trim()
        val amountCents = state.amountCents
        val balance = repository.currentWallet.value.availableBalance
        val totalDebit = amountCents + biller.feeCents

        var hasError = false
        var accError: String? = null
        var amtError: String? = null

        if (account.length < 3) {
            accError = "Please enter a valid account or meter number (min 3 chars)."
            hasError = true
        }

        if (amountCents <= 0L) {
            amtError = "Please enter a bill amount greater than $0.00."
            hasError = true
        } else if (totalDebit > balance) {
            amtError = "Insufficient balance. Available: ${Formatters.formatCentsToCurrency(balance)}"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    accountNumberError = accError,
                    amountError = amtError
                )
            }
            return false
        }

        _uiState.update {
            it.copy(
                accountNumberError = null,
                amountError = null,
                totalDebitCents = totalDebit,
                currentStep = BillPaymentStep.CONFIRM_REVIEW
            )
        }
        return true
    }

    fun proceedToPin() {
        _uiState.update {
            it.copy(
                pinInput = "",
                pinError = null,
                currentStep = BillPaymentStep.PIN_VERIFICATION
            )
        }
    }

    fun onPinDigit(digit: String) {
        val currentPin = _uiState.value.pinInput
        if (currentPin.length < 4 && !_uiState.value.isProcessing) {
            val updated = currentPin + digit
            _uiState.update { it.copy(pinInput = updated, pinError = null) }

            // Automatically execute verification when full 4 digits entered
            if (updated.length == 4) {
                executePaymentWithPin(updated)
            }
        }
    }

    fun onPinBackspace() {
        val currentPin = _uiState.value.pinInput
        if (currentPin.isNotEmpty() && !_uiState.value.isProcessing) {
            _uiState.update { it.copy(pinInput = currentPin.dropLast(1), pinError = null) }
        }
    }

    private fun executePaymentWithPin(pin: String) {
        val isValidPin = repository.verifyPin(pin)
        if (!isValidPin) {
            _uiState.update {
                it.copy(
                    pinError = "Incorrect security PIN. Please try again.",
                    pinInput = ""
                )
            }
            return
        }

        val state = _uiState.value
        val biller = state.selectedBiller ?: return
        val account = state.accountNumber.trim()
        val amountCents = state.amountCents

        _uiState.update { it.copy(isProcessing = true) }

        scope.launch {
            val result = repository.payBill(
                billerId = biller.billerId,
                billerName = biller.name,
                category = biller.category,
                accountNumber = account,
                amountCents = amountCents,
                feeCents = biller.feeCents,
                note = state.note
            )

            when (result) {
                is BillPaymentResult.Success -> {
                    _uiState.update { current ->
                        current.copy(
                            isProcessing = false,
                            completedTransaction = result.transaction,
                            receiptReference = result.referenceNumber,
                            currentStep = BillPaymentStep.SUCCESS
                        )
                    }
                }
                is BillPaymentResult.Error -> {
                    _uiState.update { current ->
                        current.copy(
                            isProcessing = false,
                            errorMessage = result.message,
                            snackbarMessage = result.message,
                            currentStep = BillPaymentStep.CONFIRM_REVIEW
                        )
                    }
                }
            }
        }
    }

    fun onStepBack(): Boolean {
        when (_uiState.value.currentStep) {
            BillPaymentStep.SUCCESS -> {
                reset()
                return false
            }
            BillPaymentStep.PIN_VERIFICATION -> {
                _uiState.update { it.copy(currentStep = BillPaymentStep.CONFIRM_REVIEW) }
                return true
            }
            BillPaymentStep.CONFIRM_REVIEW -> {
                _uiState.update { it.copy(currentStep = BillPaymentStep.ENTER_DETAILS) }
                return true
            }
            BillPaymentStep.ENTER_DETAILS -> {
                _uiState.update { it.copy(currentStep = BillPaymentStep.SELECT_BILLER) }
                return true
            }
            BillPaymentStep.SELECT_BILLER -> {
                return false
            }
        }
    }

    fun reset(keepBiller: Boolean = false) {
        val current = _uiState.value
        val resetBiller = if (keepBiller) current.selectedBiller else null
        val resetCategory = if (keepBiller) current.selectedCategory else null

        _uiState.update {
            it.copy(
                currentStep = BillPaymentStep.SELECT_BILLER,
                selectedBiller = resetBiller,
                selectedCategory = resetCategory,
                accountNumber = "",
                accountNumberError = null,
                amountInput = "",
                amountCents = 0L,
                selectedPresetCents = null,
                amountError = null,
                note = "",
                pinInput = "",
                pinError = null,
                isProcessing = false,
                completedTransaction = null,
                receiptReference = null,
                errorMessage = null,
                snackbarMessage = null
            )
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun parseAmountCents(input: String): Long {
        if (input.isBlank()) return 0L
        return try {
            val dollars = input.toDouble()
            (dollars * 100).toLong()
        } catch (_: Exception) {
            0L
        }
    }
}
