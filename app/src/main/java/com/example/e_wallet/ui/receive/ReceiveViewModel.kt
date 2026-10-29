package com.example.e_wallet.ui.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.User
import com.example.e_wallet.model.Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ReceiveUiState(
    val user: User? = null,
    val wallet: Wallet? = null,
    /** Optional amount the sender should pay — encodes into QR data string. */
    val requestAmountInput: String = "",
    val isAmountFieldVisible: Boolean = false,
    val snackbarMessage: String? = null
) {
    /**
     * The canonical string encoded in the QR code.
     * Format: "pocketpay://receive?wallet=<walletNumber>&name=<name>[&amount=<cents>]"
     */
    val qrData: String
        get() {
            val base = "pocketpay://receive?wallet=${wallet?.walletNumber ?: ""}" +
                    "&name=${user?.fullName?.replace(" ", "%20") ?: ""}"
            val amountCents = requestAmountInput.toDoubleOrNull()?.let { (it * 100).toLong() }
            return if (amountCents != null && amountCents > 0) "$base&amount=$amountCents" else base
        }

    val requestAmountCents: Long?
        get() = requestAmountInput.toDoubleOrNull()?.let { (it * 100).toLong() }
            ?.takeIf { it > 0L }
}

class ReceiveViewModel(
    private val repository: WalletRepository = WalletRepositoryProvider.repository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val _requestAmountInput = MutableStateFlow("")
    private val _isAmountFieldVisible = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ReceiveUiState> = combine(
        repository.currentUser,
        repository.currentWallet,
        _requestAmountInput,
        _isAmountFieldVisible,
        _snackbarMessage
    ) { user, wallet, amount, isAmountVisible, snackbar ->
        ReceiveUiState(
            user = user,
            wallet = wallet,
            requestAmountInput = amount,
            isAmountFieldVisible = isAmountVisible,
            snackbarMessage = snackbar
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = ReceiveUiState(
            user = repository.currentUser.value,
            wallet = repository.currentWallet.value
        )
    )

    fun toggleAmountField() {
        _isAmountFieldVisible.update { !it }
        if (!_isAmountFieldVisible.value) {
            _requestAmountInput.value = ""
        }
    }

    fun onAmountInputChange(raw: String) {
        // Allow only valid decimal numeric input (max 2 decimal places)
        val sanitized = raw.filter { it.isDigit() || it == '.' }
        val dotCount = sanitized.count { it == '.' }
        if (dotCount > 1) return
        val afterDot = sanitized.substringAfter('.', "")
        if (afterDot.length > 2) return
        _requestAmountInput.value = sanitized
    }

    fun onWalletNumberCopied() {
        _snackbarMessage.value = "Wallet number copied to clipboard"
    }

    fun onQrShared() {
        _snackbarMessage.value = "Share sheet opened"
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
