package com.example.e_wallet.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for PocketPay authentication flows (Login, Biometrics & Account Registration).
 */
data class AuthUiState(
    val isRegisterMode: Boolean = false,
    val identifierInput: String = "alex.morgan@pocketpay.demo",
    val pinInput: String = "1234",
    val registerNameInput: String = "",
    val registerEmailInput: String = "",
    val registerPhoneInput: String = "",
    val registerPinInput: String = "",
    val registerConfirmPinInput: String = "",
    val isBiometricsAvailable: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val currentUser: User? = null
)

private data class LoginFields(
    val isRegisterMode: Boolean,
    val identifierInput: String,
    val pinInput: String,
    val registerNameInput: String,
    val registerEmailInput: String
)

private data class RegisterFields(
    val registerPhoneInput: String,
    val registerPinInput: String,
    val registerConfirmPinInput: String,
    val isLoading: Boolean,
    val errorMessage: String?
)

/**
 * ViewModel managing login, registration, demo 1-tap sign in, and biometric simulation.
 */
class AuthViewModel(
    val repository: WalletRepository = WalletRepositoryProvider.repository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val _isRegisterMode = MutableStateFlow(false)
    private val _identifierInput = MutableStateFlow("alex.morgan@pocketpay.demo")
    private val _pinInput = MutableStateFlow("1234")
    private val _registerNameInput = MutableStateFlow("")
    private val _registerEmailInput = MutableStateFlow("")
    private val _registerPhoneInput = MutableStateFlow("")
    private val _registerPinInput = MutableStateFlow("")
    private val _registerConfirmPinInput = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AuthUiState> = combine(
        combine(
            _isRegisterMode,
            _identifierInput,
            _pinInput,
            _registerNameInput,
            _registerEmailInput
        ) { isReg, id, pin, regName, regEmail ->
            LoginFields(isReg, id, pin, regName, regEmail)
        },
        combine(
            _registerPhoneInput,
            _registerPinInput,
            _registerConfirmPinInput,
            _isLoading,
            _errorMessage
        ) { regPhone, regPin, regConfirmPin, loading, error ->
            RegisterFields(regPhone, regPin, regConfirmPin, loading, error)
        },
        combine(
            _snackbarMessage,
            repository.currentUser,
            repository.isBiometricsEnabled
        ) { snackbar, user, biometrics ->
            Triple(snackbar, user, biometrics)
        }
    ) { loginFields, registerFields, (snackbar, user, biometrics) ->
        AuthUiState(
            isRegisterMode = loginFields.isRegisterMode,
            identifierInput = loginFields.identifierInput,
            pinInput = loginFields.pinInput,
            registerNameInput = loginFields.registerNameInput,
            registerEmailInput = loginFields.registerEmailInput,
            registerPhoneInput = registerFields.registerPhoneInput,
            registerPinInput = registerFields.registerPinInput,
            registerConfirmPinInput = registerFields.registerConfirmPinInput,
            isBiometricsAvailable = biometrics,
            isLoading = registerFields.isLoading,
            errorMessage = registerFields.errorMessage,
            snackbarMessage = snackbar,
            currentUser = user
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AuthUiState(
            currentUser = repository.currentUser.value,
            isBiometricsAvailable = repository.isBiometricsEnabled.value
        )
    )

    fun toggleAuthMode() {
        _isRegisterMode.update { !it }
        _errorMessage.value = null
    }

    fun onIdentifierChanged(value: String) {
        _identifierInput.value = value
        _errorMessage.value = null
    }

    fun onPinChanged(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() }) {
            _pinInput.value = value
            _errorMessage.value = null
        }
    }

    fun onRegisterNameChanged(value: String) {
        _registerNameInput.value = value
        _errorMessage.value = null
    }

    fun onRegisterEmailChanged(value: String) {
        _registerEmailInput.value = value
        _errorMessage.value = null
    }

    fun onRegisterPhoneChanged(value: String) {
        _registerPhoneInput.value = value
        _errorMessage.value = null
    }

    fun onRegisterPinChanged(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() }) {
            _registerPinInput.value = value
            _errorMessage.value = null
        }
    }

    fun onRegisterConfirmPinChanged(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() }) {
            _registerConfirmPinInput.value = value
            _errorMessage.value = null
        }
    }

    fun fillDemoUser() {
        _identifierInput.value = repository.currentUser.value.email
        _pinInput.value = repository.currentPin.value
        _errorMessage.value = null
    }

    fun login(onSuccess: () -> Unit) {
        val id = _identifierInput.value.trim()
        val pin = _pinInput.value.trim()

        if (id.isBlank()) {
            _errorMessage.value = "Please enter your email or phone number"
            return
        }
        if (pin.length != 4) {
            _errorMessage.value = "PIN must be 4 digits"
            return
        }

        scope.launch {
            _isLoading.value = true
            delay(350)
            val ok = repository.login(id, pin)
            _isLoading.value = false
            if (ok) {
                _snackbarMessage.value = "Welcome back, ${repository.currentUser.value.fullName}!"
                onSuccess()
            } else {
                _errorMessage.value = "Invalid credentials or incorrect 4-digit PIN (Demo: 1234)"
            }
        }
    }

    fun loginWithBiometrics(onSuccess: () -> Unit) {
        scope.launch {
            _isLoading.value = true
            delay(300)
            val ok = repository.loginWithBiometrics()
            _isLoading.value = false
            if (ok) {
                _snackbarMessage.value = "Biometric authenticated successfully!"
                onSuccess()
            } else {
                _errorMessage.value = "Biometrics unavailable or disabled in settings"
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val name = _registerNameInput.value.trim()
        val email = _registerEmailInput.value.trim()
        val phone = _registerPhoneInput.value.trim()
        val pin = _registerPinInput.value.trim()
        val confirmPin = _registerConfirmPinInput.value.trim()

        if (name.length < 2) {
            _errorMessage.value = "Full name must be at least 2 characters"
            return
        }
        if (!email.contains("@") || !email.contains(".")) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }
        if (phone.length < 6) {
            _errorMessage.value = "Please enter a valid phone number"
            return
        }
        if (pin.length != 4) {
            _errorMessage.value = "Security PIN must be exactly 4 digits"
            return
        }
        if (pin != confirmPin) {
            _errorMessage.value = "PIN confirmation does not match"
            return
        }

        scope.launch {
            _isLoading.value = true
            delay(400)
            val ok = repository.register(name, email, phone, pin)
            _isLoading.value = false
            if (ok) {
                _snackbarMessage.value = "Welcome to PocketPay, $name!"
                onSuccess()
            } else {
                _errorMessage.value = "Registration failed. Please check your information."
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
