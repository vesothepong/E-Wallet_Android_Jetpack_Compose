package com.example.e_wallet.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.QrPaymentResult
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
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

/**
 * Discrete state machine steps representing the end-to-end QR payment lifecycle.
 */
enum class ScanQrStep {
    /** 1. Viewfinder actively scanning for barcodes or receiving photo inputs */
    SCANNER,
    /** 2. Amount entry (if static QR), recipient verification, and financial balance breakdown */
    PAYMENT_DETAILS,
    /** 3. Step-up security verification via Biometrics (Fingerprint/FaceID) or 4-digit PIN */
    PIN_VERIFICATION,
    /** 4. In-flight transaction dispatch and ledger synchronization */
    PROCESSING,
    /** 5. Digital receipt generated and displayed upon successful settlement */
    SUCCESS,
    /** 6. Terminal or recoverable error state (e.g. invalid QR, lockout, network drop) */
    ERROR
}

/**
 * Immutable UI state model for the QR scanning and payment flow.
 */
data class ScanQrUiState(
    /** Current stage in the payment lifecycle state machine */
    val step: ScanQrStep = ScanQrStep.SCANNER,
    /** Parsed recipient and invoice details extracted from the QR code */
    val scannedQr: ParsedQrPayment? = null,
    /** User input amount string in dollars (for static/open QR codes) */
    val amountInput: String = "",
    /** Optional transfer note/memo (e.g., "Dinner split") */
    val noteInput: String = "",
    /** Transaction fee in cents (demo environment provides free transfers) */
    val feeCents: Long = 0L,
    /** Current available balance in cents snapshot from the wallet repository */
    val availableBalanceCents: Long = 0L,
    /** Hardware flashlight / torch state */
    val isTorchOn: Boolean = false,
    /** Front vs rear camera selector */
    val isFrontCamera: Boolean = false,
    /** Visibility toggle for manual QR payload entry dialog */
    val isManualInputVisible: Boolean = false,
    /** Scratch buffer for manual code entry dialog */
    val manualInputText: String = "",
    /** PIN digits currently entered by the user (max 4 digits) */
    val pinInput: String = "",
    /** Inline validation error message for incorrect PIN attempts */
    val pinError: String? = null,
    /** Remaining PIN retry attempts before triggering security lockout */
    val pinAttemptsLeft: Int = 3,
    /** Whether biometric authentication (Fingerprint / Face ID) is enabled on this account */
    val isBiometricsEnabled: Boolean = true,
    /** True when maximum PIN failures have occurred, locking out further authorization */
    val isLockedOut: Boolean = false,
    /** Default demo PIN used for simulation */
    val demoPin: String = "1234",
    /** Contextual title displayed on the ErrorView screen */
    val errorTitle: String? = null,
    /** Explanatory description displayed on the ErrorView screen */
    val errorMessage: String? = null,
    /** True while waiting for asynchronous ledger payment response */
    val isProcessing: Boolean = false,
    /** Completed transaction entity returned by the repository upon success */
    val completedTransaction: Transaction? = null,
    /** Updated wallet balance in cents following successful settlement */
    val newBalanceCents: Long = 0L,
    /** Preset quick-selection amount chips in dollars for static QR payments */
    val quickAmounts: List<Int> = listOf(5, 10, 20, 50, 100),
    /** Transient snackbar notification message */
    val snackbarMessage: String? = null
) {
    /**
     * Resolves the effective payment amount in cents.
     * Prioritizes the fixed QR invoice amount if present; otherwise parses user manual input.
     */
    val amountCents: Long
        get() {
            // Dynamic QR invoice takes absolute priority as single source of truth
            val preset = scannedQr?.presetAmountCents
            if (preset != null && preset > 0L) return preset

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

    /** Total deduction including transfer fees */
    val totalDebitCents: Long
        get() = amountCents + feeCents

    /** Anticipated balance remaining after transaction is debited */
    val remainingBalanceCents: Long
        get() = (availableBalanceCents - totalDebitCents).coerceAtLeast(0L)

    /** True if the amount is hardcoded in the scanned invoice and cannot be edited */
    val isAmountLocked: Boolean
        get() = (scannedQr?.presetAmountCents ?: 0L) > 0L

    /** Client-side pre-validation: true if total debit exceeds current available balance */
    val isInsufficientBalance: Boolean
        get() = amountCents > 0L && totalDebitCents > availableBalanceCents

    /** Limit check: demo limit capped at $2,500.00 per QR transaction */
    val isOverLimit: Boolean
        get() = amountCents > 250000L

    /** Master validation guard: payment must be non-zero, within funds, and under transaction limits */
    val isValidAmount: Boolean
        get() = amountCents > 0L && !isInsufficientBalance && !isOverLimit
}

class ScanQrViewModel(
    private val repository: WalletRepository = WalletRepositoryProvider.repository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val _uiState = MutableStateFlow(
        ScanQrUiState(
            availableBalanceCents = repository.currentWallet.value.availableBalance,
            isBiometricsEnabled = repository.isBiometricsEnabled.value
        )
    )
    val uiState: StateFlow<ScanQrUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            repository.currentWallet.collect { wallet ->
                _uiState.update { current ->
                    current.copy(availableBalanceCents = wallet.availableBalance)
                }
            }
        }
        scope.launch {
            repository.isBiometricsEnabled.collect { enabled ->
                _uiState.update { current ->
                    current.copy(isBiometricsEnabled = enabled)
                }
            }
        }
    }

    /**
     * Handles incoming QR raw payload strings from CameraX barcode scanner or Gallery image picker.
     * Enforces strict syntax validation and self-transfer protection before proceeding to confirmation.
     */
    fun onScanResult(rawPayload: String) {
        val parsed = QrCodeParser.parse(rawPayload, repository.beneficiaries.value)
        if (parsed == null) {
            _uiState.update {
                it.copy(
                    step = ScanQrStep.ERROR,
                    errorTitle = "Invalid QR Code",
                    errorMessage = "The scanned QR code is invalid, corrupted, or not recognized as a PocketPay payment format. Please scan a valid merchant or user QR code."
                )
            }
            return
        }

        // Validate self-payment
        val currentWallet = repository.currentWallet.value
        val currentUser = repository.currentUser.value
        if (parsed.receiverId == currentUser.userId ||
            parsed.receiverWalletNumber.equals(currentWallet.walletNumber, ignoreCase = true)
        ) {
            _uiState.update {
                it.copy(
                    step = ScanQrStep.ERROR,
                    errorTitle = "Self-Transfer Not Allowed",
                    errorMessage = "You cannot make a payment to your own wallet (${currentWallet.walletNumber}). Please scan a merchant or friend's QR code."
                )
            }
            return
        }

        val initialAmount = if (parsed.presetAmountCents != null && parsed.presetAmountCents > 0L) {
            Formatters.formatCentsToAmountOnly(parsed.presetAmountCents)
        } else {
            "" // Empty so user explicitly types their desired amount
        }

        _uiState.update {
            it.copy(
                scannedQr = parsed,
                amountInput = initialAmount,
                noteInput = parsed.note,
                step = ScanQrStep.PAYMENT_DETAILS,
                pinInput = "",
                pinError = null,
                errorTitle = null,
                errorMessage = null
            )
        }
    }

    /** Dispatches scanning error notifications (e.g. from gallery decoding failures) */
    fun onScanError(errorMessage: String) {
        _uiState.update { it.copy(snackbarMessage = errorMessage) }
    }

    fun onSelectPreset(preset: DemoQrPreset) {
        onScanResult(preset.rawPayload)
    }

    fun onManualCodeSubmit(code: String) {
        _uiState.update { it.copy(isManualInputVisible = false) }
        onScanResult(code)
    }

    fun toggleManualInputDialog(show: Boolean) {
        _uiState.update {
            it.copy(
                isManualInputVisible = show,
                manualInputText = if (show) it.manualInputText else ""
            )
        }
    }

    fun onManualInputTextChanged(text: String) {
        _uiState.update { it.copy(manualInputText = text) }
    }

    fun toggleTorch() {
        _uiState.update { it.copy(isTorchOn = !it.isTorchOn) }
    }

    fun toggleCamera() {
        _uiState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun onAmountChanged(amount: String) {
        if (_uiState.value.isAmountLocked) return // Locked by invoice QR

        val normalized = amount.replace(',', '.')
        val sanitized = normalized.filter { it.isDigit() || it == '.' }
        val dots = sanitized.count { it == '.' }
        if (dots > 1) return

        val dotIndex = sanitized.indexOf('.')
        if (dotIndex != -1 && sanitized.length - dotIndex - 1 > 2) return

        _uiState.update { it.copy(amountInput = sanitized, errorMessage = null) }
    }

    fun onQuickAmountSelected(dollars: Int) {
        if (_uiState.value.isAmountLocked) return
        _uiState.update { it.copy(amountInput = dollars.toString(), errorMessage = null) }
    }

    fun onNoteChanged(note: String) {
        if (note.length <= 80) {
            _uiState.update { it.copy(noteInput = note) }
        }
    }

    /**
     * Enforces client-side pre-validation (funds, limits, non-zero amount) before granting access
     * to the sensitive PIN / Biometric verification stage.
     */
    fun proceedToPin() {
        val current = _uiState.value
        if (!current.isValidAmount) {
            val error = when {
                current.amountCents <= 0L -> "Please enter an amount greater than $0.00"
                current.isInsufficientBalance -> {
                    val needed = current.totalDebitCents - current.availableBalanceCents
                    "Insufficient balance. You need ${Formatters.formatCentsToCurrency(needed)} more to complete this payment."
                }
                current.isOverLimit -> "Payment exceeds maximum demo limit of $2,500.00 per transaction"
                else -> "Invalid payment amount"
            }
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        _uiState.update {
            it.copy(
                step = ScanQrStep.PIN_VERIFICATION,
                pinInput = "",
                pinError = null,
                errorTitle = null,
                errorMessage = null
            )
        }
    }

    /**
     * Authorizes the transaction via biometric modality (Fingerprint or Face ID)
     * and triggers instant payment execution upon sensor success.
     */
    fun authenticateWithBiometrics() {
        val current = _uiState.value
        if (current.isProcessing || current.isLockedOut) return
        if (!current.isValidAmount) return

        // Biometrics verification succeeded -> proceed straight to payment execution
        executePayment()
    }

    fun onPinDigit(digit: String) {
        val current = _uiState.value
        if (current.isProcessing || current.isLockedOut || current.pinInput.length >= 4) return

        val newPin = current.pinInput + digit
        _uiState.update { it.copy(pinInput = newPin, pinError = null) }

        if (newPin.length == 4) {
            verifyPinAndExecute(newPin)
        }
    }

    fun onPinDelete() {
        _uiState.update {
            if (it.pinInput.isNotEmpty() && !it.isProcessing && !it.isLockedOut) {
                it.copy(pinInput = it.pinInput.dropLast(1), pinError = null)
            } else it
        }
    }

    fun onPinClear() {
        if (!_uiState.value.isProcessing && !_uiState.value.isLockedOut) {
            _uiState.update { it.copy(pinInput = "", pinError = null) }
        }
    }

    /**
     * Validates 4-digit PIN against the repository.
     * Decrements remaining attempts on failure and enforces temporary lockout after 3 consecutive failures.
     */
    private fun verifyPinAndExecute(pin: String) {
        val current = _uiState.value
        val actualPin = repository.currentPin.value
        if (pin == actualPin) {
            executePayment()
        } else {
            val remaining = current.pinAttemptsLeft - 1
            if (remaining > 0) {
                _uiState.update {
                    it.copy(
                        pinInput = "",
                        pinAttemptsLeft = remaining,
                        pinError = "Incorrect PIN. $remaining attempt(s) remaining (Demo PIN: $actualPin)"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        step = ScanQrStep.ERROR,
                        isLockedOut = true,
                        errorTitle = "Security Lockout",
                        errorMessage = "Too many incorrect PIN attempts. For security reasons, this transaction has been cancelled. Please try again later."
                    )
                }
            }
        }
    }

    /**
     * Dispatches payment request to the repository and updates ledger state.
     * Transitions between PROCESSING, SUCCESS, and ERROR.
     */
    fun executePayment() {
        val state = _uiState.value
        val scanned = state.scannedQr ?: return
        if (state.isProcessing) return

        _uiState.update { it.copy(step = ScanQrStep.PROCESSING, isProcessing = true) }

        scope.launch {
            delay(800) // Simulated network processing latency

            val result = repository.payQr(
                receiverWalletNumber = scanned.receiverWalletNumber,
                receiverName = scanned.receiverName,
                receiverId = scanned.receiverId,
                amountCents = state.amountCents,
                note = state.noteInput.ifBlank { "QR Payment to ${scanned.receiverName}" },
                feeCents = state.feeCents
            )

            when (result) {
                is QrPaymentResult.Success -> {
                    _uiState.update {
                        it.copy(
                            step = ScanQrStep.SUCCESS,
                            isProcessing = false,
                            completedTransaction = result.transaction,
                            newBalanceCents = result.newBalance
                        )
                    }
                }
                is QrPaymentResult.Error -> {
                    _uiState.update {
                        it.copy(
                            step = ScanQrStep.ERROR,
                            isProcessing = false,
                            errorTitle = "Payment Failed",
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun backToScanner() {
        _uiState.update {
            it.copy(
                step = ScanQrStep.SCANNER,
                scannedQr = null,
                pinInput = "",
                pinError = null,
                pinAttemptsLeft = 3,
                isLockedOut = false,
                errorTitle = null,
                errorMessage = null
            )
        }
    }

    fun backToPaymentDetails() {
        _uiState.update {
            it.copy(
                step = ScanQrStep.PAYMENT_DETAILS,
                pinInput = "",
                pinError = null
            )
        }
    }

    fun reset() {
        _uiState.update {
            ScanQrUiState(
                availableBalanceCents = repository.currentWallet.value.availableBalance,
                isBiometricsEnabled = repository.isBiometricsEnabled.value,
                isTorchOn = it.isTorchOn,
                isFrontCamera = it.isFrontCamera
            )
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
