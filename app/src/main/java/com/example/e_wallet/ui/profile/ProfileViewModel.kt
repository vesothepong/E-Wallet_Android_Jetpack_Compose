package com.example.e_wallet.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.PinChangeResult
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.BankAccount
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.model.TransactionStatus
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.model.User
import com.example.e_wallet.model.Wallet
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class ChangePinStep {
    OLD_PIN,
    NEW_PIN,
    CONFIRM_PIN
}

data class LinkedBank(
    val id: String,
    val bankName: String,
    val accountNumberMasked: String,
    val isPrimary: Boolean
)

data class ProfileUiState(
    val user: User,
    val wallet: Wallet,
    val currentPin: String = "1234",
    val isBiometricsEnabled: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val isCardFrozen: Boolean = false,
    val isCardDetailsRevealed: Boolean = false,
    val selectedCurrency: String = "USD",
    val dailyLimitCents: Long = 500000L, // $5,000.00
    val dailyUsedCents: Long = 125000L, // $1,250.00
    val singleLimitCents: Long = 200000L, // $2,000.00
    val kycTier: String = "Tier 2 • Verified User",
    val linkedBankAccounts: List<LinkedBank> = listOf(
        LinkedBank("bank_01", "Chase Premier Checking", "•••• 4812", true),
        LinkedBank("bank_02", "Bank of America Savings", "•••• 9021", false)
    ),
    // Edit Profile state
    val showEditProfileDialog: Boolean = false,
    val editNameInput: String = "",
    val editEmailInput: String = "",
    val editPhoneInput: String = "",
    val editProfileError: String? = null,
    // Change PIN state
    val showChangePinSheet: Boolean = false,
    val pinStep: ChangePinStep = ChangePinStep.OLD_PIN,
    val oldPinInput: String = "",
    val newPinInput: String = "",
    val confirmPinInput: String = "",
    val pinError: String? = null,
    // Logout dialog state
    val showLogoutDialog: Boolean = false,
    val snackbarMessage: String? = null
)

private data class RepoState(
    val user: User,
    val wallet: Wallet,
    val pin: String,
    val biometrics: Boolean,
    val notifications: Boolean,
    val cardFrozen: Boolean,
    val bankAccounts: List<BankAccount>,
    val currency: String,
    val transactions: List<Transaction>
)

private data class LocalUiState(
    val isCardDetailsRevealed: Boolean = false,
    val selectedCurrency: String = "USD",
    val showEditProfileDialog: Boolean = false,
    val editNameInput: String = "",
    val editEmailInput: String = "",
    val editPhoneInput: String = "",
    val editProfileError: String? = null,
    val showChangePinSheet: Boolean = false,
    val pinStep: ChangePinStep = ChangePinStep.OLD_PIN,
    val oldPinInput: String = "",
    val newPinInput: String = "",
    val confirmPinInput: String = "",
    val pinError: String? = null,
    val showLogoutDialog: Boolean = false,
    val snackbarMessage: String? = null
)

class ProfileViewModel(
    private val repository: WalletRepository = WalletRepositoryProvider.repository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope
    private val _localState = MutableStateFlow(LocalUiState())

    private fun calculateDailyUsed(transactions: List<Transaction>): Long {
        val startOfDayMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return transactions.filter {
            it.status == TransactionStatus.COMPLETED &&
            it.timestamp >= startOfDayMillis &&
            (it.type == TransactionType.SEND ||
             it.type == TransactionType.WITHDRAW ||
             it.type == TransactionType.QR_PAYMENT ||
             it.type == TransactionType.BILL_PAYMENT)
        }.sumOf { it.amount + it.fee }
    }

    private fun mapBankAccounts(accounts: List<BankAccount>): List<LinkedBank> {
        return accounts.map {
            LinkedBank(
                id = it.accountId,
                bankName = it.bankName,
                accountNumberMasked = it.accountNumberMasked,
                isPrimary = it.isPrimary
            )
        }
    }

    private val userAndWalletFlow = combine(
        repository.currentUser,
        repository.currentWallet,
        repository.currentPin
    ) { user, wallet, pin ->
        Triple(user, wallet, pin)
    }

    private val settingsFlow = combine(
        repository.isBiometricsEnabled,
        repository.isNotificationsEnabled,
        repository.isCardFrozen
    ) { biometrics, notifications, cardFrozen ->
        Triple(biometrics, notifications, cardFrozen)
    }

    private val accountsAndTxFlow = combine(
        repository.bankAccounts,
        repository.selectedCurrency,
        repository.transactions
    ) { banks, currency, txs ->
        Triple(banks, currency, txs)
    }

    private val repoFlow = combine(
        userAndWalletFlow,
        settingsFlow,
        accountsAndTxFlow
    ) { (user, wallet, pin), (biometrics, notifications, cardFrozen), (banks, currency, txs) ->
        RepoState(user, wallet, pin, biometrics, notifications, cardFrozen, banks, currency, txs)
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        repoFlow,
        _localState
    ) { repo, local ->
        val mappedBanks = mapBankAccounts(repo.bankAccounts)
        val dailyUsed = calculateDailyUsed(repo.transactions)

        ProfileUiState(
            user = repo.user,
            wallet = repo.wallet,
            currentPin = repo.pin,
            isBiometricsEnabled = repo.biometrics,
            isNotificationsEnabled = repo.notifications,
            isCardFrozen = repo.cardFrozen,
            isCardDetailsRevealed = local.isCardDetailsRevealed,
            selectedCurrency = repo.currency,
            dailyUsedCents = dailyUsed,
            linkedBankAccounts = mappedBanks,
            showEditProfileDialog = local.showEditProfileDialog,
            editNameInput = local.editNameInput,
            editEmailInput = local.editEmailInput,
            editPhoneInput = local.editPhoneInput,
            editProfileError = local.editProfileError,
            showChangePinSheet = local.showChangePinSheet,
            pinStep = local.pinStep,
            oldPinInput = local.oldPinInput,
            newPinInput = local.newPinInput,
            confirmPinInput = local.confirmPinInput,
            pinError = local.pinError,
            showLogoutDialog = local.showLogoutDialog,
            snackbarMessage = local.snackbarMessage
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = ProfileUiState(
            user = repository.currentUser.value,
            wallet = repository.currentWallet.value,
            currentPin = repository.currentPin.value,
            isBiometricsEnabled = repository.isBiometricsEnabled.value,
            isNotificationsEnabled = repository.isNotificationsEnabled.value,
            isCardFrozen = repository.isCardFrozen.value,
            selectedCurrency = repository.selectedCurrency.value,
            dailyUsedCents = calculateDailyUsed(repository.transactions.value),
            linkedBankAccounts = mapBankAccounts(repository.bankAccounts.value)
        )
    )

    // Edit Profile
    fun openEditProfileDialog() {
        val user = repository.currentUser.value
        _localState.update {
            it.copy(
                showEditProfileDialog = true,
                editNameInput = user.fullName,
                editEmailInput = user.email,
                editPhoneInput = user.phoneNumber,
                editProfileError = null
            )
        }
    }

    fun onEditNameChanged(value: String) {
        _localState.update { it.copy(editNameInput = value, editProfileError = null) }
    }

    fun onEditEmailChanged(value: String) {
        _localState.update { it.copy(editEmailInput = value, editProfileError = null) }
    }

    fun onEditPhoneChanged(value: String) {
        _localState.update { it.copy(editPhoneInput = value, editProfileError = null) }
    }

    fun dismissEditProfileDialog() {
        _localState.update { it.copy(showEditProfileDialog = false, editProfileError = null) }
    }

    fun saveProfile() {
        val local = _localState.value
        val name = local.editNameInput.trim()
        val email = local.editEmailInput.trim()
        val phone = local.editPhoneInput.trim()

        if (name.length < 2) {
            _localState.update { it.copy(editProfileError = "Please enter a valid full name (at least 2 characters).") }
            return
        }
        if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
            _localState.update { it.copy(editProfileError = "Please enter a valid email address.") }
            return
        }
        if (phone.length < 7) {
            _localState.update { it.copy(editProfileError = "Please enter a valid phone number.") }
            return
        }

        val success = repository.updateProfile(name, email, phone)
        if (success) {
            _localState.update {
                it.copy(
                    showEditProfileDialog = false,
                    editProfileError = null,
                    snackbarMessage = "Profile updated successfully"
                )
            }
        } else {
            _localState.update { it.copy(editProfileError = "Failed to update profile.") }
        }
    }

    // Change PIN Flow
    fun openChangePinSheet() {
        _localState.update {
            it.copy(
                showChangePinSheet = true,
                pinStep = ChangePinStep.OLD_PIN,
                oldPinInput = "",
                newPinInput = "",
                confirmPinInput = "",
                pinError = null
            )
        }
    }

    fun dismissChangePinSheet() {
        _localState.update {
            it.copy(
                showChangePinSheet = false,
                oldPinInput = "",
                newPinInput = "",
                confirmPinInput = "",
                pinError = null
            )
        }
    }

    fun onPinDigit(digit: String) {
        val current = _localState.value
        when (current.pinStep) {
            ChangePinStep.OLD_PIN -> {
                if (current.oldPinInput.length < 4) {
                    val updated = current.oldPinInput + digit
                    if (updated.length == 4) {
                        if (repository.verifyPin(updated)) {
                            _localState.update {
                                it.copy(
                                    oldPinInput = updated,
                                    pinStep = ChangePinStep.NEW_PIN,
                                    pinError = null
                                )
                            }
                        } else {
                            _localState.update {
                                it.copy(
                                    oldPinInput = "",
                                    pinError = "Current PIN is incorrect. (Default demo: ${repository.currentPin.value})"
                                )
                            }
                        }
                    } else {
                        _localState.update { it.copy(oldPinInput = updated, pinError = null) }
                    }
                }
            }
            ChangePinStep.NEW_PIN -> {
                if (current.newPinInput.length < 4) {
                    val updated = current.newPinInput + digit
                    if (updated.length == 4) {
                        if (updated == current.oldPinInput) {
                            _localState.update {
                                it.copy(
                                    newPinInput = "",
                                    pinError = "New PIN must be different from current PIN."
                                )
                            }
                        } else {
                            _localState.update {
                                it.copy(
                                    newPinInput = updated,
                                    pinStep = ChangePinStep.CONFIRM_PIN,
                                    pinError = null
                                )
                            }
                        }
                    } else {
                        _localState.update { it.copy(newPinInput = updated, pinError = null) }
                    }
                }
            }
            ChangePinStep.CONFIRM_PIN -> {
                if (current.confirmPinInput.length < 4) {
                    val updated = current.confirmPinInput + digit
                    if (updated.length == 4) {
                        if (updated == current.newPinInput) {
                            when (val result = repository.changePin(current.oldPinInput, updated)) {
                                is PinChangeResult.Success -> {
                                    _localState.update {
                                        it.copy(
                                            showChangePinSheet = false,
                                            oldPinInput = "",
                                            newPinInput = "",
                                            confirmPinInput = "",
                                            pinError = null,
                                            snackbarMessage = "Security PIN updated successfully"
                                        )
                                    }
                                }
                                is PinChangeResult.Error -> {
                                    _localState.update {
                                        it.copy(
                                            confirmPinInput = "",
                                            pinError = result.message
                                        )
                                    }
                                }
                            }
                        } else {
                            _localState.update {
                                it.copy(
                                    confirmPinInput = "",
                                    pinError = "PINs do not match. Please try again."
                                )
                            }
                        }
                    } else {
                        _localState.update { it.copy(confirmPinInput = updated, pinError = null) }
                    }
                }
            }
        }
    }

    fun onPinBackspace() {
        val current = _localState.value
        when (current.pinStep) {
            ChangePinStep.OLD_PIN -> {
                if (current.oldPinInput.isNotEmpty()) {
                    _localState.update { it.copy(oldPinInput = current.oldPinInput.dropLast(1), pinError = null) }
                }
            }
            ChangePinStep.NEW_PIN -> {
                if (current.newPinInput.isNotEmpty()) {
                    _localState.update { it.copy(newPinInput = current.newPinInput.dropLast(1), pinError = null) }
                }
            }
            ChangePinStep.CONFIRM_PIN -> {
                if (current.confirmPinInput.isNotEmpty()) {
                    _localState.update { it.copy(confirmPinInput = current.confirmPinInput.dropLast(1), pinError = null) }
                }
            }
        }
    }

    // Virtual Card Actions
    fun toggleCardFreeze() {
        val newStatus = !repository.isCardFrozen.value
        repository.setCardFrozen(newStatus)
        val msg = if (newStatus) "PocketPay Card frozen. Payments disabled." else "PocketPay Card unfrozen and active."
        _localState.update { it.copy(snackbarMessage = msg) }
    }

    fun toggleRevealCardDetails() {
        _localState.update { it.copy(isCardDetailsRevealed = !it.isCardDetailsRevealed) }
    }

    // Preferences & Toggles
    fun setBiometricsEnabled(enabled: Boolean) {
        repository.setBiometricsEnabled(enabled)
        _localState.update {
            it.copy(
                snackbarMessage = if (enabled) "Biometric login enabled" else "Biometric login disabled"
            )
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        repository.setNotificationsEnabled(enabled)
        _localState.update {
            it.copy(
                snackbarMessage = if (enabled) "Push notifications enabled" else "Push notifications disabled"
            )
        }
    }

    fun setCurrency(currency: String) {
        repository.setCurrency(currency)
        _localState.update {
            it.copy(
                selectedCurrency = currency,
                snackbarMessage = "Primary display currency set to $currency"
            )
        }
    }

    // System Actions
    fun openLogoutDialog() {
        _localState.update { it.copy(showLogoutDialog = true) }
    }

    fun dismissLogoutDialog() {
        _localState.update { it.copy(showLogoutDialog = false) }
    }

    fun confirmLogout() {
        repository.logout()
        _localState.update {
            it.copy(
                showLogoutDialog = false,
                snackbarMessage = "Demo session logged out. Session reset to default demo account."
            )
        }
    }

    fun resetDemoData() {
        repository.resetDemoData()
        _localState.update {
            it.copy(
                isCardDetailsRevealed = false,
                selectedCurrency = "USD",
                snackbarMessage = "Demo wallet and user profile reset to defaults"
            )
        }
    }

    fun clearSnackbar() {
        _localState.update { it.copy(snackbarMessage = null) }
    }
}
