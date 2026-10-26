package com.example.e_wallet.data

import com.example.e_wallet.model.AccountStatus
import com.example.e_wallet.model.Beneficiary
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.model.TransactionStatus
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.model.User
import com.example.e_wallet.model.Wallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.e_wallet.util.Formatters

sealed class DepositResult {
    data class Success(val transaction: Transaction, val newBalance: Long) : DepositResult()
    data class Error(val message: String) : DepositResult()
}

sealed class WithdrawResult {
    data class Success(val transaction: Transaction, val newBalance: Long) : WithdrawResult()
    data class Error(val message: String) : WithdrawResult()
}

sealed class SendResult {
    data class Success(val transaction: Transaction, val newBalance: Long) : SendResult()
    data class Error(val message: String) : SendResult()
}

sealed class QrPaymentResult {
    data class Success(val transaction: Transaction, val newBalance: Long) : QrPaymentResult()
    data class Error(val message: String) : QrPaymentResult()
}

sealed class PinChangeResult {
    data object Success : PinChangeResult()
    data class Error(val message: String) : PinChangeResult()
}

interface WalletRepository {
    val currentUser: StateFlow<User>
    val currentWallet: StateFlow<Wallet>
    val transactions: StateFlow<List<Transaction>>
    val beneficiaries: StateFlow<List<Beneficiary>>
    val unreadNotificationsCount: StateFlow<Int>
    val currentPin: StateFlow<String>
    val isBiometricsEnabled: StateFlow<Boolean>
    val isNotificationsEnabled: StateFlow<Boolean>
    val isCardFrozen: StateFlow<Boolean>

    suspend fun deposit(
        amountCents: Long,
        note: String = "Simulated bank deposit",
        sourceDescription: String = "Demo Bank Top-up (•••• 4812)"
    ): DepositResult

    suspend fun withdraw(
        amountCents: Long,
        note: String = "Simulated ATM Cashout",
        destinationDescription: String = "ATM Cashout (Cardless Demo)",
        feeCents: Long = 0L
    ): WithdrawResult

    suspend fun sendMoney(
        receiverWalletNumber: String,
        receiverName: String,
        receiverId: String,
        amountCents: Long,
        note: String = "",
        feeCents: Long = 0L
    ): SendResult

    suspend fun payQr(
        receiverWalletNumber: String,
        receiverName: String,
        receiverId: String,
        amountCents: Long,
        note: String = "",
        feeCents: Long = 0L
    ): QrPaymentResult

    fun searchReceivers(query: String): List<Beneficiary>

    fun verifyPin(pin: String): Boolean
    fun changePin(oldPin: String, newPin: String): PinChangeResult
    fun updateProfile(fullName: String, email: String, phoneNumber: String): Boolean
    fun setBiometricsEnabled(enabled: Boolean)
    fun setNotificationsEnabled(enabled: Boolean)
    fun setCardFrozen(frozen: Boolean)

    fun resetDemoData()
}

/**
 * In-memory single-source-of-truth repository for PocketPay simulation.
 * Ensures wallet balance and transaction history are strictly consistent.
 */
class InMemoryWalletRepository : WalletRepository {

    private val defaultUser = User(
        userId = "usr_demo_001",
        fullName = "Alex Morgan",
        phoneNumber = "+1 (555) 234-5678",
        email = "alex.morgan@pocketpay.demo",
        accountStatus = AccountStatus.ACTIVE,
        role = "VERIFIED_DEMO_USER"
    )

    private val defaultWallet = Wallet(
        walletId = "wlt_001",
        walletNumber = "PKP-8829-4102",
        userId = "usr_demo_001",
        currentBalance = 245000L, // $2,450.00
        availableBalance = 245000L,
        currency = "USD"
    )

    private val defaultBeneficiaries = listOf(
        Beneficiary(
            beneficiaryId = "ben_001",
            ownerUserId = "usr_demo_001",
            receiverUserId = "usr_demo_002",
            displayName = "Sarah Chen",
            walletNumber = "PKP-1049-5521",
            avatarInitials = "SC",
            isFavorite = true
        ),
        Beneficiary(
            beneficiaryId = "ben_002",
            ownerUserId = "usr_demo_001",
            receiverUserId = "usr_demo_003",
            displayName = "David Miller",
            walletNumber = "PKP-3392-8810",
            avatarInitials = "DM",
            isFavorite = true
        ),
        Beneficiary(
            beneficiaryId = "ben_003",
            ownerUserId = "usr_demo_001",
            receiverUserId = "usr_demo_004",
            displayName = "Emma Watson",
            walletNumber = "PKP-7721-3904",
            avatarInitials = "EW",
            isFavorite = true
        ),
        Beneficiary(
            beneficiaryId = "ben_004",
            ownerUserId = "usr_demo_001",
            receiverUserId = "usr_demo_005",
            displayName = "John Doe",
            walletNumber = "PKP-4482-1983",
            avatarInitials = "JD",
            isFavorite = false
        )
    )

    private fun generateDefaultTransactions(): List<Transaction> {
        val now = System.currentTimeMillis()
        val hour = 3600 * 1000L
        val day = 24 * hour

        return listOf(
            Transaction(
                transactionId = "tx_1001",
                type = TransactionType.RECEIVE,
                senderId = "usr_demo_002",
                senderName = "Sarah Chen",
                receiverId = "usr_demo_001",
                receiverName = "Alex Morgan",
                amount = 15000L, // $150.00
                fee = 0L,
                timestamp = now - 2 * hour,
                description = "Dinner split share 🍕",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-20261021-001"
            ),
            Transaction(
                transactionId = "tx_1002",
                type = TransactionType.DEPOSIT,
                senderId = null,
                senderName = "Demo Bank Top-up",
                receiverId = "usr_demo_001",
                receiverName = "Alex Morgan",
                amount = 50000L, // $500.00
                fee = 0L,
                timestamp = now - 26 * hour,
                description = "Simulated bank deposit",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-20261020-002"
            ),
            Transaction(
                transactionId = "tx_1003",
                type = TransactionType.SEND,
                senderId = "usr_demo_001",
                senderName = "Alex Morgan",
                receiverId = "usr_demo_003",
                receiverName = "David Miller",
                amount = 4500L, // $45.00
                fee = 0L,
                timestamp = now - 30 * hour,
                description = "Movie tickets 🎬",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-20261020-003"
            ),
            Transaction(
                transactionId = "tx_1004",
                type = TransactionType.QR_PAYMENT,
                senderId = "usr_demo_001",
                senderName = "Alex Morgan",
                receiverId = "mch_001",
                receiverName = "Starbucks Coffee",
                amount = 850L, // $8.50
                fee = 0L,
                timestamp = now - 2 * day,
                description = "Morning Caramel Macchiato ☕",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-20261019-004"
            ),
            Transaction(
                transactionId = "tx_1005",
                type = TransactionType.WITHDRAW,
                senderId = "usr_demo_001",
                senderName = "Alex Morgan",
                receiverId = null,
                receiverName = "ATM Cash Withdrawal",
                amount = 10000L, // $100.00
                fee = 0L,
                timestamp = now - 3 * day,
                description = "Simulated ATM Cashout",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-20261018-005"
            ),
            Transaction(
                transactionId = "tx_1006",
                type = TransactionType.SEND,
                senderId = "usr_demo_001",
                senderName = "Alex Morgan",
                receiverId = "usr_demo_004",
                receiverName = "Emma Watson",
                amount = 2500L, // $25.00
                fee = 0L,
                timestamp = now - 4 * day,
                description = "Book club subscription 📚",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-20261017-006"
            ),
            Transaction(
                transactionId = "tx_1007",
                type = TransactionType.RECEIVE,
                senderId = "usr_demo_005",
                senderName = "John Doe",
                receiverId = "usr_demo_001",
                receiverName = "Alex Morgan",
                amount = 8000L, // $80.00
                fee = 0L,
                timestamp = now - 5 * day,
                description = "Freelance consulting share",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-20261016-007"
            )
        )
    }

    private val _currentUser = MutableStateFlow(defaultUser)
    override val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _currentWallet = MutableStateFlow(defaultWallet)
    override val currentWallet: StateFlow<Wallet> = _currentWallet.asStateFlow()

    private val _transactions = MutableStateFlow(generateDefaultTransactions())
    override val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _beneficiaries = MutableStateFlow(defaultBeneficiaries)
    override val beneficiaries: StateFlow<List<Beneficiary>> = _beneficiaries.asStateFlow()

    private val _unreadNotificationsCount = MutableStateFlow(3)
    override val unreadNotificationsCount: StateFlow<Int> = _unreadNotificationsCount.asStateFlow()

    private val defaultPin = "1234"
    private val _currentPin = MutableStateFlow(defaultPin)
    override val currentPin: StateFlow<String> = _currentPin.asStateFlow()

    private val _isBiometricsEnabled = MutableStateFlow(true)
    override val isBiometricsEnabled: StateFlow<Boolean> = _isBiometricsEnabled.asStateFlow()

    private val _isNotificationsEnabled = MutableStateFlow(true)
    override val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

    private val _isCardFrozen = MutableStateFlow(false)
    override val isCardFrozen: StateFlow<Boolean> = _isCardFrozen.asStateFlow()

    private val depositMutex = Mutex()
    private val withdrawMutex = Mutex()
    private val sendMutex = Mutex()
    private val qrPayMutex = Mutex()

    override suspend fun deposit(
        amountCents: Long,
        note: String,
        sourceDescription: String
    ): DepositResult = depositMutex.withLock {
        if (amountCents <= 0L) {
            return DepositResult.Error("Deposit amount must be greater than $0.00.")
        }
        val maxLimitCents = 500000L // $5,000.00 demo limit
        if (amountCents > maxLimitCents) {
            return DepositResult.Error("Deposit exceeds maximum demo limit of $5,000.00 per transaction.")
        }

        val currentWlt = _currentWallet.value
        val newBalance = currentWlt.currentBalance + amountCents
        val newAvailable = currentWlt.availableBalance + amountCents

        val timestamp = System.currentTimeMillis()
        val randomSuffix = (1000..9999).random()
        val tx = Transaction(
            transactionId = "tx_dep_${timestamp}_$randomSuffix",
            type = TransactionType.DEPOSIT,
            senderId = null,
            senderName = sourceDescription,
            receiverId = _currentUser.value.userId,
            receiverName = _currentUser.value.fullName,
            amount = amountCents,
            fee = 0L,
            timestamp = timestamp,
            description = note.ifBlank { "Simulated Demo Bank Top-up" },
            status = TransactionStatus.COMPLETED,
            referenceNumber = "REF-DEP-${timestamp.toString().takeLast(6)}-$randomSuffix"
        )

        // Single atomic state update
        _currentWallet.value = currentWlt.copy(
            currentBalance = newBalance,
            availableBalance = newAvailable
        )
        _transactions.value = listOf(tx) + _transactions.value

        return DepositResult.Success(tx, newBalance)
    }

    override suspend fun withdraw(
        amountCents: Long,
        note: String,
        destinationDescription: String,
        feeCents: Long
    ): WithdrawResult = withdrawMutex.withLock {
        if (amountCents <= 0L) {
            return WithdrawResult.Error("Withdrawal amount must be greater than $0.00.")
        }
        val maxLimitCents = 200000L // $2,000.00 demo limit
        if (amountCents > maxLimitCents) {
            return WithdrawResult.Error("Withdrawal exceeds maximum demo limit of $2,000.00 per transaction.")
        }

        val currentWlt = _currentWallet.value
        val totalDebit = amountCents + feeCents
        if (currentWlt.availableBalance < totalDebit) {
            return WithdrawResult.Error(
                "Insufficient funds. Available balance is ${Formatters.formatCentsToCurrency(currentWlt.availableBalance)}, but required total is ${Formatters.formatCentsToCurrency(totalDebit)}."
            )
        }

        val newBalance = currentWlt.currentBalance - totalDebit
        val newAvailable = currentWlt.availableBalance - totalDebit

        val timestamp = System.currentTimeMillis()
        val randomSuffix = (1000..9999).random()
        val tx = Transaction(
            transactionId = "tx_wdr_${timestamp}_$randomSuffix",
            type = TransactionType.WITHDRAW,
            senderId = _currentUser.value.userId,
            senderName = _currentUser.value.fullName,
            receiverId = null,
            receiverName = destinationDescription,
            amount = amountCents,
            fee = feeCents,
            timestamp = timestamp,
            description = note.ifBlank { "Simulated ATM Cashout" },
            status = TransactionStatus.COMPLETED,
            referenceNumber = "REF-WDR-${timestamp.toString().takeLast(6)}-$randomSuffix"
        )

        // Single atomic state update
        _currentWallet.value = currentWlt.copy(
            currentBalance = newBalance,
            availableBalance = newAvailable
        )
        _transactions.value = listOf(tx) + _transactions.value

        return WithdrawResult.Success(tx, newBalance)
    }

    override suspend fun sendMoney(
        receiverWalletNumber: String,
        receiverName: String,
        receiverId: String,
        amountCents: Long,
        note: String,
        feeCents: Long
    ): SendResult = sendMutex.withLock {
        if (amountCents <= 0L) {
            return SendResult.Error("Transfer amount must be greater than $0.00.")
        }
        val maxLimitCents = 250000L // $2,500.00 demo transfer limit
        if (amountCents > maxLimitCents) {
            return SendResult.Error("Transfer exceeds maximum demo limit of $2,500.00 per transaction.")
        }

        // Self-transfer validation
        if (receiverId == _currentUser.value.userId ||
            receiverWalletNumber.equals(_currentWallet.value.walletNumber, ignoreCase = true)
        ) {
            return SendResult.Error("Self-transfer is not supported. Please select a valid recipient.")
        }

        val currentWlt = _currentWallet.value
        val totalDebit = amountCents + feeCents
        if (currentWlt.availableBalance < totalDebit) {
            return SendResult.Error(
                "Insufficient funds. Available balance is ${Formatters.formatCentsToCurrency(currentWlt.availableBalance)}, but transfer total is ${Formatters.formatCentsToCurrency(totalDebit)}."
            )
        }

        val newBalance = currentWlt.currentBalance - totalDebit
        val newAvailable = currentWlt.availableBalance - totalDebit

        val timestamp = System.currentTimeMillis()
        val randomSuffix = (1000..9999).random()
        val tx = Transaction(
            transactionId = "tx_snd_${timestamp}_$randomSuffix",
            type = TransactionType.SEND,
            senderId = _currentUser.value.userId,
            senderName = _currentUser.value.fullName,
            receiverId = receiverId,
            receiverName = receiverName,
            amount = amountCents,
            fee = feeCents,
            timestamp = timestamp,
            description = note.ifBlank { "Transfer to $receiverName" },
            status = TransactionStatus.COMPLETED,
            referenceNumber = "REF-SND-${timestamp.toString().takeLast(6)}-$randomSuffix"
        )

        // Single atomic state update
        _currentWallet.value = currentWlt.copy(
            currentBalance = newBalance,
            availableBalance = newAvailable
        )
        _transactions.value = listOf(tx) + _transactions.value

        return SendResult.Success(tx, newBalance)
    }

    override suspend fun payQr(
        receiverWalletNumber: String,
        receiverName: String,
        receiverId: String,
        amountCents: Long,
        note: String,
        feeCents: Long
    ): QrPaymentResult = qrPayMutex.withLock {
        if (amountCents <= 0L) {
            return QrPaymentResult.Error("Payment amount must be greater than $0.00.")
        }
        val maxLimitCents = 250000L // $2,500.00 demo QR limit
        if (amountCents > maxLimitCents) {
            return QrPaymentResult.Error("Payment exceeds maximum demo limit of $2,500.00 per transaction.")
        }

        // Self-payment validation
        if (receiverId == _currentUser.value.userId ||
            receiverWalletNumber.equals(_currentWallet.value.walletNumber, ignoreCase = true)
        ) {
            return QrPaymentResult.Error("Cannot make QR payment to your own wallet.")
        }

        val currentWlt = _currentWallet.value
        val totalDebit = amountCents + feeCents
        if (currentWlt.availableBalance < totalDebit) {
            return QrPaymentResult.Error(
                "Insufficient funds. Available balance is ${Formatters.formatCentsToCurrency(currentWlt.availableBalance)}, but payment total is ${Formatters.formatCentsToCurrency(totalDebit)}."
            )
        }

        val newBalance = currentWlt.currentBalance - totalDebit
        val newAvailable = currentWlt.availableBalance - totalDebit

        val timestamp = System.currentTimeMillis()
        val randomSuffix = (1000..9999).random()
        val tx = Transaction(
            transactionId = "tx_qr_${timestamp}_$randomSuffix",
            type = TransactionType.QR_PAYMENT,
            senderId = _currentUser.value.userId,
            senderName = _currentUser.value.fullName,
            receiverId = receiverId,
            receiverName = receiverName,
            amount = amountCents,
            fee = feeCents,
            timestamp = timestamp,
            description = note.ifBlank { "QR Payment to $receiverName" },
            status = TransactionStatus.COMPLETED,
            referenceNumber = "REF-QRP-${timestamp.toString().takeLast(6)}-$randomSuffix"
        )

        // Single atomic state update
        _currentWallet.value = currentWlt.copy(
            currentBalance = newBalance,
            availableBalance = newAvailable
        )
        _transactions.value = listOf(tx) + _transactions.value

        return QrPaymentResult.Success(tx, newBalance)
    }

    override fun searchReceivers(query: String): List<Beneficiary> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return _beneficiaries.value
        return _beneficiaries.value.filter {
            it.displayName.lowercase().contains(q) ||
            it.walletNumber.lowercase().contains(q) ||
            it.receiverUserId.lowercase().contains(q)
        }
    }

    override fun verifyPin(pin: String): Boolean {
        return pin == _currentPin.value
    }

    override fun changePin(oldPin: String, newPin: String): PinChangeResult {
        if (oldPin != _currentPin.value) {
            return PinChangeResult.Error("Incorrect current PIN.")
        }
        if (newPin.length != 4 || !newPin.all { it.isDigit() }) {
            return PinChangeResult.Error("New PIN must be exactly 4 digits.")
        }
        if (newPin == oldPin) {
            return PinChangeResult.Error("New PIN cannot be identical to the current PIN.")
        }
        _currentPin.value = newPin
        return PinChangeResult.Success
    }

    override fun updateProfile(fullName: String, email: String, phoneNumber: String): Boolean {
        if (fullName.isBlank()) return false
        _currentUser.value = _currentUser.value.copy(
            fullName = fullName.trim(),
            email = email.trim(),
            phoneNumber = phoneNumber.trim()
        )
        return true
    }

    override fun setBiometricsEnabled(enabled: Boolean) {
        _isBiometricsEnabled.value = enabled
    }

    override fun setNotificationsEnabled(enabled: Boolean) {
        _isNotificationsEnabled.value = enabled
    }

    override fun setCardFrozen(frozen: Boolean) {
        _isCardFrozen.value = frozen
    }

    override fun resetDemoData() {
        _currentUser.value = defaultUser
        _currentWallet.value = defaultWallet
        _transactions.value = generateDefaultTransactions()
        _beneficiaries.value = defaultBeneficiaries
        _unreadNotificationsCount.value = 3
        _currentPin.value = defaultPin
        _isBiometricsEnabled.value = true
        _isNotificationsEnabled.value = true
        _isCardFrozen.value = false
    }
}

/**
 * Shared provider ensuring ViewModels observe the same in-memory repository instance.
 */
object WalletRepositoryProvider {
    val repository: WalletRepository by lazy { InMemoryWalletRepository() }
}

