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

enum class ScanQrStep {
    SCANNER,
    PAYMENT_DETAILS,
    PIN_VERIFICATION,
    PROCESSING,
    SUCCESS,
    ERROR
}

data class ScanQrUiState(
    val step: ScanQrStep = ScanQrStep.SCANNER,
    val scannedQr: ParsedQrPayment? = null,
    val amountInput: String = "",
    val noteInput: String = "",
    val feeCents: Long = 0L, // Free demo payment
    val availableBalanceCents: Long = 0L,
    val isTorchOn: Boolean = false,
    val isFrontCamera: Boolean = false,
    val isManualInputVisible: Boolean = false,
    val manualInputText: String = "",
    val pinInput: String = "",
    val pinError: String? = null,
    val pinAttemptsLeft: Int = 3,
    val demoPin: String = "1234",
    val errorMessage: String? = null,
    val isProcessing: Boolean = false,
    val completedTransaction: Transaction? = null,
    val newBalanceCents: Long = 0L,
    val quickAmounts: List<Int> = listOf(5, 10, 20, 50, 100),
    val snackbarMessage: String? = null
) {
    val amountCents: Long
        get() {
            // If the QR has a preset amount, use it as single source of truth
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

    val totalDebitCents: Long
        get() = amountCents + feeCents

    val remainingBalanceCents: Long
        get() = (availableBalanceCents - totalDebitCents).coerceAtLeast(0L)

    val isAmountLocked: Boolean
        get() = (scannedQr?.presetAmountCents ?: 0L) > 0L

    val isValidAmount: Boolean
        get() = amountCents > 0L && totalDebitCents <= availableBalanceCents && amountCents <= 250000L
}

class ScanQrViewModel(
    private val repository: WalletRepository = WalletRepositoryProvider.repository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val _uiState = MutableStateFlow(
        ScanQrUiState(
            availableBalanceCents = repository.currentWallet.value.availableBalance
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
    }

    fun onScanResult(rawPayload: String) {
        val parsed = QrCodeParser.parse(rawPayload, repository.beneficiaries.value)
        if (parsed == null) {
            _uiState.update { it.copy(snackbarMessage = "Invalid QR code format") }
            return
        }

        // Validate self-payment
        val currentWallet = repository.currentWallet.value
        val currentUser = repository.currentUser.value
        if (parsed.receiverId == currentUser.userId ||
            parsed.receiverWalletNumber.equals(currentWallet.walletNumber, ignoreCase = true)
        ) {
            _uiState.update { it.copy(snackbarMessage = "Cannot scan and pay to your own wallet!") }
            return
        }

        val initialAmount = if (parsed.presetAmountCents != null && parsed.presetAmountCents > 0L) {
            Formatters.formatCentsToAmountOnly(parsed.presetAmountCents)
        } else {
            "10" // default starting suggestion
        }

        _uiState.update {
            it.copy(
                scannedQr = parsed,
                amountInput = initialAmount,
                noteInput = parsed.note,
                step = ScanQrStep.PAYMENT_DETAILS,
                pinInput = "",
                pinError = null,
                errorMessage = null
            )
        }
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

    fun proceedToPin() {
        val current = _uiState.value
        if (!current.isValidAmount) {
            val error = when {
                current.amountCents <= 0L -> "Please enter an amount greater than $0.00"
                current.totalDebitCents > current.availableBalanceCents -> "Insufficient balance for this payment"
                current.amountCents > 250000L -> "Amount exceeds $2,500.00 demo limit"
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
                errorMessage = null
            )
        }
    }

    fun onPinDigit(digit: String) {
        val current = _uiState.value
        if (current.isProcessing || current.pinInput.length >= 4) return

        val newPin = current.pinInput + digit
        _uiState.update { it.copy(pinInput = newPin, pinError = null) }

        if (newPin.length == 4) {
            verifyPinAndExecute(newPin)
        }
    }

    fun onPinDelete() {
        _uiState.update {
            if (it.pinInput.isNotEmpty() && !it.isProcessing) {
                it.copy(pinInput = it.pinInput.dropLast(1), pinError = null)
            } else it
        }
    }

    fun onPinClear() {
        if (!_uiState.value.isProcessing) {
            _uiState.update { it.copy(pinInput = "", pinError = null) }
        }
    }

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
                        errorMessage = "Transaction locked: Too many incorrect PIN attempts. Please try again."
                    )
                }
            }
        }
    }

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
                isTorchOn = it.isTorchOn,
                isFrontCamera = it.isFrontCamera
            )
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
