package com.example.e_wallet.data

import com.example.e_wallet.model.AccountStatus
import com.example.e_wallet.model.BankAccount
import com.example.e_wallet.model.Beneficiary
import com.example.e_wallet.model.BillCategory
import com.example.e_wallet.model.BillPaymentResult
import com.example.e_wallet.model.Biller
import com.example.e_wallet.model.CardNetwork
import com.example.e_wallet.model.CardType
import com.example.e_wallet.model.NotificationItem
import com.example.e_wallet.model.NotificationType
import com.example.e_wallet.model.PaymentCard
import com.example.e_wallet.model.SavingsCategory
import com.example.e_wallet.model.SavingsGoal
import com.example.e_wallet.model.SavingsGoalResult
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.model.TransactionStatus
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.model.User
import com.example.e_wallet.model.Wallet
import com.example.e_wallet.model.AnalyticsSummary
import com.example.e_wallet.model.AnalyticsTimePeriod
import com.example.e_wallet.model.CategoryBudget
import com.example.e_wallet.model.DailySpendBar
import com.example.e_wallet.model.FinancialHealthScore
import com.example.e_wallet.model.SpendingCategory
import com.example.e_wallet.model.SpendingSlice
import com.example.e_wallet.model.SplitBill
import com.example.e_wallet.model.SplitBillCategory
import com.example.e_wallet.model.SplitBillStatus
import com.example.e_wallet.model.SplitParticipant
import com.example.e_wallet.model.TransactionClassifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
    val cards: StateFlow<List<PaymentCard>>
    val bankAccounts: StateFlow<List<BankAccount>>
    val notifications: StateFlow<List<NotificationItem>>
    val billers: StateFlow<List<Biller>>
    val savingsGoals: StateFlow<List<SavingsGoal>>
    val totalSavingsCents: StateFlow<Long>
    val categoryBudgets: StateFlow<List<CategoryBudget>>
    val splitBills: StateFlow<List<SplitBill>>
    val isAuthenticated: StateFlow<Boolean>
    val isOnboardingCompleted: StateFlow<Boolean>
    val selectedCurrency: StateFlow<String>
    val isDarkTheme: StateFlow<Boolean>

    fun login(identifier: String, pin: String): Boolean
    fun loginWithBiometrics(): Boolean
    fun register(fullName: String, email: String, phoneNumber: String, pin: String): Boolean
    fun logout()
    fun completeOnboarding()
    fun toggleDarkTheme()
    fun setDarkTheme(isDark: Boolean)

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

    suspend fun payBill(
        billerId: String,
        billerName: String,
        category: BillCategory,
        accountNumber: String,
        amountCents: Long,
        feeCents: Long = 0L,
        note: String = ""
    ): BillPaymentResult

    suspend fun createSavingsGoal(
        name: String,
        targetAmountCents: Long,
        initialDepositCents: Long = 0L,
        category: SavingsCategory = SavingsCategory.GENERAL,
        targetDateMillis: Long? = null,
        isLocked: Boolean = false,
        note: String = ""
    ): SavingsGoalResult

    suspend fun depositToGoal(
        goalId: String,
        amountCents: Long,
        note: String = ""
    ): SavingsGoalResult

    suspend fun withdrawFromGoal(
        goalId: String,
        amountCents: Long,
        pin: String,
        note: String = ""
    ): SavingsGoalResult

    suspend fun deleteSavingsGoal(
        goalId: String,
        pin: String
    ): SavingsGoalResult

    fun updateSavingsGoal(
        goalId: String,
        name: String,
        targetAmountCents: Long,
        targetDateMillis: Long?,
        isLocked: Boolean
    ): Boolean

    fun searchReceivers(query: String): List<Beneficiary>

    fun verifyPin(pin: String): Boolean
    fun changePin(oldPin: String, newPin: String): PinChangeResult
    fun updateProfile(fullName: String, email: String, phoneNumber: String): Boolean
    fun setCurrency(currency: String)
    fun setBiometricsEnabled(enabled: Boolean)
    fun setNotificationsEnabled(enabled: Boolean)
    fun setCardFrozen(frozen: Boolean)

    fun setCardFrozen(cardId: String, frozen: Boolean)
    fun toggleOnlinePayments(cardId: String, enabled: Boolean)
    fun toggleInternationalPayments(cardId: String, enabled: Boolean)
    fun updateMonthlyLimit(cardId: String, limitCents: Long)
    fun addCard(card: PaymentCard)
    fun addBankAccount(account: BankAccount)
    fun setDefaultCard(cardId: String)

    fun markNotificationAsRead(id: String)
    fun markAllNotificationsAsRead()
    fun deleteNotification(id: String)
    fun clearAllNotifications()
    fun addNotification(
        title: String,
        message: String,
        type: NotificationType,
        amountCents: Long? = null,
        transactionId: String? = null
    )

    fun resetDemoData()

    fun setCategoryBudget(category: SpendingCategory, limitCents: Long)
    fun deleteCategoryBudget(category: SpendingCategory)
    fun getAnalyticsSummary(period: AnalyticsTimePeriod): AnalyticsSummary

    suspend fun createSplitBill(
        title: String,
        category: SplitBillCategory,
        totalAmountCents: Long,
        participants: List<SplitParticipant>,
        note: String = ""
    ): SplitBill

    suspend fun markParticipantPaid(splitBillId: String, participantId: String): Boolean
    suspend fun sendSplitReminder(splitBillId: String, participantId: String): Boolean
    suspend fun cancelSplitBill(splitBillId: String): Boolean

    /** Persist an updated [User] (e.g. after profile edits). */
    fun updateUser(user: User)
}

/**
 * In-memory single-source-of-truth repository for PocketPay simulation.
 * Ensures wallet balance and transaction history are strictly consistent.
 */
class InMemoryWalletRepository : WalletRepository {

    private val defaultUser = User(
        userId = "usr_demo_001",
        fullName = "Kantumruy Dev",
        phoneNumber = "+1 (555) 019-8810",
        email = "kantumruy.dev@pocketpay.io",
        accountStatus = AccountStatus.ACTIVE,
        role = "KYC_VERIFIED_TIER_2"
    )

    private val defaultWallet = Wallet(
        walletId = "wlt_001",
        walletNumber = "PKP-8821-4402",
        userId = "usr_demo_001",
        currentBalance = 1245080L, // $12,450.80
        availableBalance = 1245080L,
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
            displayName = "Alex Vance",
            walletNumber = "PKP-7721-3904",
            avatarInitials = "AV",
            isFavorite = true
        ),
        Beneficiary(
            beneficiaryId = "ben_004",
            ownerUserId = "usr_demo_001",
            receiverUserId = "usr_demo_005",
            displayName = "Emily Rose",
            walletNumber = "PKP-4482-1983",
            avatarInitials = "ER",
            isFavorite = false
        )
    )

    private fun generateDefaultTransactions(): List<Transaction> {
        val now = System.currentTimeMillis()
        val hour = 3600 * 1000L
        val day = 24 * hour

        return listOf(
            Transaction(
                transactionId = "tx_992140188",
                type = TransactionType.SEND,
                senderId = "usr_demo_001",
                senderName = "Kantumruy Dev",
                receiverId = "mch_apple",
                receiverName = "Apple Store",
                amount = 79900L, // $799.00
                fee = 0L,
                timestamp = now - 15 * 60 * 1000L,
                description = "MacBook accessories & adapter",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-APL-88219"
            ),
            Transaction(
                transactionId = "tx_1004",
                type = TransactionType.QR_PAYMENT,
                senderId = "usr_demo_001",
                senderName = "Kantumruy Dev",
                receiverId = "mch_001",
                receiverName = "Starbucks Coffee",
                amount = 850L, // $8.50
                fee = 0L,
                timestamp = now - 2 * hour,
                description = "Coffee & Pastry",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-QRP-883921"
            ),
            Transaction(
                transactionId = "tx_1002",
                type = TransactionType.RECEIVE,
                senderId = "corp_payroll",
                senderName = "Salary Deposit",
                receiverId = "usr_demo_001",
                receiverName = "Kantumruy Dev",
                amount = 425000L, // $4,250.00
                fee = 0L,
                timestamp = now - 24 * hour,
                description = "Monthly Payroll Deposit",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-DEP-773190"
            ),
            Transaction(
                transactionId = "tx_1008",
                type = TransactionType.BILL_PAYMENT,
                senderId = "usr_demo_001",
                senderName = "Kantumruy Dev",
                receiverId = "biller_water_01",
                receiverName = "City Water Utility",
                amount = 4250L, // $42.50
                fee = 0L,
                timestamp = now - 28 * hour,
                description = "Water & Sewage Utility",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-BIL-449102"
            ),
            Transaction(
                transactionId = "tx_1009",
                type = TransactionType.RECEIVE,
                senderId = "usr_demo_003",
                senderName = "David Miller",
                receiverId = "usr_demo_001",
                receiverName = "Kantumruy Dev",
                amount = 2000L, // $20.00
                fee = 0L,
                timestamp = now - 3 * day,
                description = "Peer Transfer",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-P2P-339211"
            )
        )
    }

    private val defaultCards = listOf(
        PaymentCard(
            cardId = "crd_001",
            cardholderName = "Kantumruy Dev",
            cardNumberMasked = "•••• •••• •••• 5521",
            fullCardNumber = "4812 9012 3456 5521",
            expiryDate = "09/29",
            cvv = "742",
            cardType = CardType.VIRTUAL_DEBIT,
            cardNetwork = CardNetwork.VISA,
            isFrozen = false,
            isPrimary = true,
            isOnlinePaymentEnabled = true,
            isInternationalEnabled = false,
            monthlyLimitCents = 300000L, // $3,000.00
            monthlySpentCents = 85000L,  // $850.00
            gradientStartColorHex = 0xFF1E1B4B,
            gradientEndColorHex = 0xFF312E81
        ),
        PaymentCard(
            cardId = "crd_002",
            cardholderName = "Kantumruy Dev",
            cardNumberMasked = "•••• •••• •••• 8812",
            fullCardNumber = "5412 8819 2201 8812",
            expiryDate = "11/28",
            cvv = "319",
            cardType = CardType.PHYSICAL_DEBIT,
            cardNetwork = CardNetwork.MASTERCARD,
            isFrozen = false,
            isPrimary = false,
            isOnlinePaymentEnabled = true,
            isInternationalEnabled = true,
            monthlyLimitCents = 500000L,
            monthlySpentCents = 120000L,
            gradientStartColorHex = 0xFF0D9488,
            gradientEndColorHex = 0xFF065F46
        )
    )

    private val defaultBankAccounts = listOf(
        BankAccount(
            accountId = "bnk_001",
            bankName = "Chase Bank Checking",
            accountType = "Checking",
            accountNumberMasked = "•••• 4412",
            routingNumberMasked = "•••• 0210",
            isPrimary = true,
            balanceCents = 832000L // $8,320.00
        ),
        BankAccount(
            accountId = "bnk_002",
            bankName = "Bank of America Savings",
            accountType = "Savings",
            accountNumberMasked = "•••• 9011",
            routingNumberMasked = "•••• 0440",
            isPrimary = false,
            balanceCents = 1450000L // $14,500.00
        )
    )

    private fun generateDefaultNotifications(): List<NotificationItem> {
        val now = System.currentTimeMillis()
        val hour = 3600 * 1000L
        val day = 24 * hour

        return listOf(
            NotificationItem(
                id = "notif_001",
                title = "Payment Received",
                message = "You received $80.00 from John Doe for dinner split.",
                timestamp = now - 45 * 60 * 1000L,
                type = NotificationType.TRANSACTION,
                isRead = false,
                amountCents = 8000L,
                transactionId = "tx_1007"
            ),
            NotificationItem(
                id = "notif_002",
                title = "Security Alert: New Device Login",
                message = "Your PocketPay account was accessed from Pixel 10 Pro (Android 16). If this wasn't you, review your security settings.",
                timestamp = now - 3 * hour,
                type = NotificationType.SECURITY,
                isRead = false
            ),
            NotificationItem(
                id = "notif_003",
                title = "Top-Up Successful",
                message = "Your wallet was credited with $500.00 from Chase Checking •••• 4812.",
                timestamp = now - 1 * day,
                type = NotificationType.TRANSACTION,
                isRead = false,
                amountCents = 50000L,
                transactionId = "tx_1002"
            ),
            NotificationItem(
                id = "notif_004",
                title = "2% Cashback Credited",
                message = "You earned $0.17 cash rewards on your Starbucks coffee purchase.",
                timestamp = now - 2 * day,
                type = NotificationType.PROMOTION,
                isRead = true,
                amountCents = 17L
            ),
            NotificationItem(
                id = "notif_005",
                title = "Welcome to PocketPay v2.4",
                message = "Explore QR Scan & Pay, Multi-card management, and instant transfers.",
                timestamp = now - 5 * day,
                type = NotificationType.SYSTEM,
                isRead = true
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

    private val _cards = MutableStateFlow(defaultCards)
    override val cards: StateFlow<List<PaymentCard>> = _cards.asStateFlow()

    private val _bankAccounts = MutableStateFlow(defaultBankAccounts)
    override val bankAccounts: StateFlow<List<BankAccount>> = _bankAccounts.asStateFlow()

    private val _notifications = MutableStateFlow(generateDefaultNotifications())
    override val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val defaultBillers = listOf(
        Biller(
            billerId = "biller_elec_01",
            name = "City Power & Electric",
            category = BillCategory.ELECTRICITY,
            iconName = "FlashOn",
            accountHint = "e.g. ELEC-992144",
            feeCents = 0L,
            standardAmountsCents = listOf(2500L, 5000L, 7500L, 10000L),
            isPopular = true,
            providerCode = "CPE"
        ),
        Biller(
            billerId = "biller_water_01",
            name = "Metro Clean Water Authority",
            category = BillCategory.WATER,
            iconName = "Water",
            accountHint = "e.g. WTR-440182",
            feeCents = 0L,
            standardAmountsCents = listOf(1500L, 3000L, 4500L, 6000L),
            isPopular = true,
            providerCode = "MCW"
        ),
        Biller(
            billerId = "biller_net_01",
            name = "Swift FiberNet Broadband",
            category = BillCategory.INTERNET,
            iconName = "Wifi",
            accountHint = "e.g. FIB-881920",
            feeCents = 0L,
            standardAmountsCents = listOf(3500L, 5500L, 8000L, 12000L),
            isPopular = true,
            providerCode = "SFN"
        ),
        Biller(
            billerId = "biller_mob_01",
            name = "StarMobile 5G Top-Up",
            category = BillCategory.MOBILE_TOPUP,
            iconName = "Smartphone",
            accountHint = "e.g. +1 (555) 019-2834",
            feeCents = 0L,
            standardAmountsCents = listOf(1000L, 2000L, 3000L, 5000L),
            isPopular = true,
            providerCode = "SM5G"
        ),
        Biller(
            billerId = "biller_mob_02",
            name = "NovaTel Prepaid Airtime",
            category = BillCategory.MOBILE_TOPUP,
            iconName = "Smartphone",
            accountHint = "e.g. +1 (555) 774-9021",
            feeCents = 0L,
            standardAmountsCents = listOf(500L, 1500L, 2500L, 5000L),
            isPopular = false,
            providerCode = "NVT"
        ),
        Biller(
            billerId = "biller_tv_01",
            name = "SkyStream Cable & TV",
            category = BillCategory.INTERNET,
            iconName = "Tv",
            accountHint = "e.g. SKY-310492",
            feeCents = 0L,
            standardAmountsCents = listOf(2000L, 4000L, 6000L, 8500L),
            isPopular = false,
            providerCode = "SKY"
        ),
        Biller(
            billerId = "biller_edu_01",
            name = "Horizon State University Tuition",
            category = BillCategory.EDUCATION,
            iconName = "School",
            accountHint = "e.g. STU-2026-991",
            feeCents = 50L,
            standardAmountsCents = listOf(10000L, 25000L, 50000L),
            isPopular = false,
            providerCode = "HSU"
        ),
        Biller(
            billerId = "biller_gov_01",
            name = "Metropolitan Municipal Tax",
            category = BillCategory.GOVERNMENT,
            iconName = "AccountBalance",
            accountHint = "e.g. TAX-778819",
            feeCents = 50L,
            standardAmountsCents = listOf(5000L, 10000L, 20000L),
            isPopular = false,
            providerCode = "MMT"
        )
    )

    private val _billers = MutableStateFlow(defaultBillers)
    override val billers: StateFlow<List<Biller>> = _billers.asStateFlow()

    private val defaultSavingsGoals = listOf(
        SavingsGoal(
            id = "goal_001",
            name = "Summer Vacation to Bali",
            targetAmountCents = 300000L, // $3,000.00
            currentAmountCents = 180000L, // $1,800.00 (60%)
            category = SavingsCategory.TRAVEL,
            targetDateMillis = System.currentTimeMillis() + 300L * 24 * 3600 * 1000,
            isLocked = false,
            apyRatePercent = 4.50,
            note = "$1,200.00 remaining • Target: Aug 2027"
        ),
        SavingsGoal(
            id = "goal_002",
            name = "MacBook Pro M4 Setup",
            targetAmountCents = 250000L, // $2,500.00
            currentAmountCents = 240000L, // $2,400.00 (96%)
            category = SavingsCategory.TECH,
            targetDateMillis = System.currentTimeMillis() + 60L * 24 * 3600 * 1000,
            isLocked = false,
            apyRatePercent = 4.50,
            note = "Almost Done!"
        ),
        SavingsGoal(
            id = "goal_003",
            name = "6-Month Emergency Fund",
            targetAmountCents = 500000L, // $5,000.00
            currentAmountCents = 225000L, // $2,250.00 (45%)
            category = SavingsCategory.EMERGENCY,
            targetDateMillis = System.currentTimeMillis() + 365L * 24 * 3600 * 1000,
            isLocked = true,
            apyRatePercent = 4.50,
            note = "Buffer fund"
        )
    )

    private val savingsMutex = Mutex()
    private val _savingsGoals = MutableStateFlow(defaultSavingsGoals)
    override val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals.asStateFlow()

    private val _totalSavingsCents = MutableStateFlow(645000L) // $6,450.00
    override val totalSavingsCents: StateFlow<Long> = _totalSavingsCents.asStateFlow()

    private fun refreshTotalSavings() {
        _totalSavingsCents.value = _savingsGoals.value.sumOf { it.currentAmountCents }
    }

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

    private val _isAuthenticated = MutableStateFlow(false)
    override val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    override val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _selectedCurrency = MutableStateFlow("USD")
    override val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    override val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    override fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    override fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    private val defaultCategoryBudgets = listOf(
        CategoryBudget(SpendingCategory.FOOD_DINING, 50000L),      // $500.00 limit ($380.00 spent)
        CategoryBudget(SpendingCategory.SHOPPING, 30000L),         // $300.00 limit ($210.00 spent)
        CategoryBudget(SpendingCategory.ENTERTAINMENT, 15000L),    // $150.00 limit ($145.00 spent - near limit)
        CategoryBudget(SpendingCategory.BILLS_UTILITIES, 20000L),  // $200.00
        CategoryBudget(SpendingCategory.TRANSPORT, 10000L),        // $100.00
        CategoryBudget(SpendingCategory.TRANSFERS, 25000L),        // $250.00
        CategoryBudget(SpendingCategory.SAVINGS, 30000L),          // $300.00
        CategoryBudget(SpendingCategory.OTHER, 15000L)             // $150.00
    )

    private val budgetMutex = Mutex()
    private val _categoryBudgets = MutableStateFlow(defaultCategoryBudgets)
    override val categoryBudgets: StateFlow<List<CategoryBudget>> = _categoryBudgets.asStateFlow()

    private val defaultSplitBills = listOf(
        SplitBill(
            id = "split_001",
            title = "Friday Sushi Dinner 🍣",
            category = SplitBillCategory.DINING,
            totalAmountCents = 12000L, // $120.00
            createdAtMillis = System.currentTimeMillis() - 2 * 3600 * 1000L,
            note = "Total: $120.00 • Your share: $30.00",
            participants = listOf(
                SplitParticipant(
                    participantId = "usr_demo_001",
                    name = "Kantumruy Dev (You)",
                    phoneNumber = "+1 (555) 019-8810",
                    avatarInitials = "KD",
                    amountCents = 3000L,
                    isPaid = true,
                    isHost = true
                ),
                SplitParticipant(
                    participantId = "usr_demo_002",
                    name = "Sarah",
                    phoneNumber = "+1 (555) 104-5521",
                    avatarInitials = "SC",
                    amountCents = 3000L,
                    isPaid = true,
                    paidAtMillis = System.currentTimeMillis() - 3600 * 1000L
                ),
                SplitParticipant(
                    participantId = "usr_demo_004",
                    name = "Alex",
                    phoneNumber = "+1 (555) 771-4490",
                    avatarInitials = "AV",
                    amountCents = 3000L,
                    isPaid = true,
                    paidAtMillis = System.currentTimeMillis() - 2 * 3600 * 1000L
                ),
                SplitParticipant(
                    participantId = "usr_demo_005",
                    name = "Emily",
                    phoneNumber = "+1 (555) 448-1983",
                    avatarInitials = "ER",
                    amountCents = 3000L,
                    isPaid = false
                )
            ),
            status = SplitBillStatus.ACTIVE
        ),
        SplitBill(
            id = "split_002",
            title = "Apartment Internet & Wifi 🌐",
            category = SplitBillCategory.HOUSEHOLD,
            totalAmountCents = 6000L, // $60.00
            createdAtMillis = System.currentTimeMillis() - 4 * 24 * 3600 * 1000L,
            note = "Status: All Paid",
            participants = listOf(
                SplitParticipant(
                    participantId = "usr_demo_001",
                    name = "Kantumruy Dev (You)",
                    phoneNumber = "+1 (555) 019-8810",
                    avatarInitials = "KD",
                    amountCents = 2000L,
                    isPaid = true,
                    isHost = true
                ),
                SplitParticipant(
                    participantId = "usr_demo_002",
                    name = "Sarah",
                    phoneNumber = "+1 (555) 104-5521",
                    avatarInitials = "SC",
                    amountCents = 2000L,
                    isPaid = true,
                    paidAtMillis = System.currentTimeMillis() - 3 * 24 * 3600 * 1000L
                ),
                SplitParticipant(
                    participantId = "usr_demo_003",
                    name = "David",
                    phoneNumber = "+1 (555) 339-8810",
                    avatarInitials = "DM",
                    amountCents = 2000L,
                    isPaid = true,
                    paidAtMillis = System.currentTimeMillis() - 2 * 24 * 3600 * 1000L
                )
            ),
            status = SplitBillStatus.SETTLED
        )
    )

    private val splitMutex = Mutex()
    private val _splitBills = MutableStateFlow(defaultSplitBills)
    override val splitBills: StateFlow<List<SplitBill>> = _splitBills.asStateFlow()

    private val depositMutex = Mutex()
    private val withdrawMutex = Mutex()
    private val sendMutex = Mutex()
    private val qrPayMutex = Mutex()
    private val billPayMutex = Mutex()

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

        addNotification(
            title = "Deposit Successful",
            message = "Deposited ${Formatters.formatCentsToCurrency(amountCents)} into your PocketPay wallet.",
            type = NotificationType.TRANSACTION,
            amountCents = amountCents,
            transactionId = tx.transactionId
        )

        return DepositResult.Success(tx, newBalance)
    }

    override suspend fun withdraw(
        amountCents: Long,
        note: String,
        destinationDescription: String,
        feeCents: Long
    ): WithdrawResult = withdrawMutex.withLock {
        if (_isCardFrozen.value) {
            return WithdrawResult.Error("Your PocketPay card/wallet is currently frozen. Please unfreeze in Wallet or Profile settings to cash out.")
        }
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

        addNotification(
            title = "Cashout Completed",
            message = "Withdrew ${Formatters.formatCentsToCurrency(amountCents)} from your PocketPay wallet.",
            type = NotificationType.TRANSACTION,
            amountCents = amountCents,
            transactionId = tx.transactionId
        )

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
        if (_isCardFrozen.value) {
            return SendResult.Error("Your PocketPay card/wallet is currently frozen. Please unfreeze in Wallet or Profile settings to send money.")
        }
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

        addNotification(
            title = "Money Sent",
            message = "Sent ${Formatters.formatCentsToCurrency(amountCents)} to $receiverName.",
            type = NotificationType.TRANSACTION,
            amountCents = amountCents,
            transactionId = tx.transactionId
        )

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
        if (_isCardFrozen.value) {
            return QrPaymentResult.Error("Your PocketPay card/wallet is currently frozen. Please unfreeze in Wallet or Profile settings to make QR payments.")
        }
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

        addNotification(
            title = "QR Payment Completed",
            message = "Paid ${Formatters.formatCentsToCurrency(amountCents)} to $receiverName via QR.",
            type = NotificationType.TRANSACTION,
            amountCents = amountCents,
            transactionId = tx.transactionId
        )

        return QrPaymentResult.Success(tx, newBalance)
    }

    override suspend fun payBill(
        billerId: String,
        billerName: String,
        category: BillCategory,
        accountNumber: String,
        amountCents: Long,
        feeCents: Long,
        note: String
    ): BillPaymentResult = billPayMutex.withLock {
        if (amountCents <= 0L) {
            return@withLock BillPaymentResult.Error("Bill amount must be greater than zero.")
        }
        if (accountNumber.isBlank()) {
            return@withLock BillPaymentResult.Error("Please enter a valid account or meter number.")
        }
        if (_isCardFrozen.value) {
            return@withLock BillPaymentResult.Error(
                "PocketPay account card is currently frozen. Please unfreeze before paying bills."
            )
        }

        val totalDebit = amountCents + feeCents
        val currentWlt = _currentWallet.value
        if (totalDebit > currentWlt.availableBalance) {
            return@withLock BillPaymentResult.Error(
                "Insufficient wallet balance. Available: ${Formatters.formatCentsToCurrency(currentWlt.availableBalance)}, Required: ${Formatters.formatCentsToCurrency(totalDebit)}"
            )
        }

        val newBalance = currentWlt.currentBalance - totalDebit
        val newAvailable = currentWlt.availableBalance - totalDebit
        val timestamp = System.currentTimeMillis()
        val randomSuffix = (100000..999999).random()
        val refNumber = "REF-BILL-${timestamp.toString().takeLast(6)}-$randomSuffix"

        val tx = Transaction(
            transactionId = "tx_bill_${timestamp}",
            type = TransactionType.BILL_PAYMENT,
            senderId = _currentUser.value.userId,
            senderName = _currentUser.value.fullName,
            receiverId = billerId,
            receiverName = billerName,
            amount = amountCents,
            fee = feeCents,
            timestamp = timestamp,
            description = note.ifBlank { "Paid $billerName ($accountNumber)" },
            status = TransactionStatus.COMPLETED,
            referenceNumber = refNumber
        )

        // Single atomic state update
        _currentWallet.value = currentWlt.copy(
            currentBalance = newBalance,
            availableBalance = newAvailable
        )
        _transactions.value = listOf(tx) + _transactions.value

        addNotification(
            title = "Bill Payment Successful",
            message = "Paid ${Formatters.formatCentsToCurrency(amountCents)} to $billerName (Acc: $accountNumber). Ref: $refNumber",
            type = NotificationType.TRANSACTION,
            amountCents = totalDebit,
            transactionId = tx.transactionId
        )

        return@withLock BillPaymentResult.Success(
            transaction = tx,
            newBalance = newBalance,
            billerName = billerName,
            referenceNumber = refNumber
        )
    }

    override suspend fun createSavingsGoal(
        name: String,
        targetAmountCents: Long,
        initialDepositCents: Long,
        category: SavingsCategory,
        targetDateMillis: Long?,
        isLocked: Boolean,
        note: String
    ): SavingsGoalResult = savingsMutex.withLock {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            return@withLock SavingsGoalResult.Error("Please enter a goal name.")
        }
        if (targetAmountCents <= 0L) {
            return@withLock SavingsGoalResult.Error("Target amount must be greater than zero.")
        }
        if (initialDepositCents < 0L) {
            return@withLock SavingsGoalResult.Error("Initial deposit cannot be negative.")
        }
        if (initialDepositCents > 0L && initialDepositCents > _currentWallet.value.availableBalance) {
            return@withLock SavingsGoalResult.Error("Insufficient balance for initial deposit.")
        }

        val goalId = "goal_${System.currentTimeMillis()}"
        val newGoal = SavingsGoal(
            id = goalId,
            name = trimmedName,
            targetAmountCents = targetAmountCents,
            currentAmountCents = initialDepositCents,
            category = category,
            targetDateMillis = targetDateMillis,
            isLocked = isLocked,
            apyRatePercent = 4.50,
            note = note.trim()
        )

        var newBalance = _currentWallet.value.currentBalance
        if (initialDepositCents > 0L) {
            newBalance = _currentWallet.value.currentBalance - initialDepositCents
            val newAvailable = _currentWallet.value.availableBalance - initialDepositCents
            _currentWallet.value = _currentWallet.value.copy(
                currentBalance = newBalance,
                availableBalance = newAvailable
            )

            val tx = Transaction(
                transactionId = "tx_sav_${System.currentTimeMillis()}",
                type = TransactionType.SAVINGS_DEPOSIT,
                senderId = _currentUser.value.userId,
                senderName = _currentUser.value.fullName,
                receiverId = goalId,
                receiverName = trimmedName,
                amount = initialDepositCents,
                timestamp = System.currentTimeMillis(),
                description = "Initial deposit to $trimmedName",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-SAV-${System.currentTimeMillis().toString().takeLast(6)}"
            )
            _transactions.value = listOf(tx) + _transactions.value
        }

        _savingsGoals.value = listOf(newGoal) + _savingsGoals.value
        refreshTotalSavings()

        addNotification(
            title = "New Savings Goal Created",
            message = "Created '$trimmedName' with target ${Formatters.formatCentsToCurrency(targetAmountCents)}.",
            type = NotificationType.TRANSACTION,
            amountCents = if (initialDepositCents > 0L) initialDepositCents else null
        )

        return@withLock SavingsGoalResult.Success(
            goal = newGoal,
            updatedWalletBalance = newBalance,
            message = "Savings vault '$trimmedName' created successfully."
        )
    }

    override suspend fun depositToGoal(
        goalId: String,
        amountCents: Long,
        note: String
    ): SavingsGoalResult = savingsMutex.withLock {
        if (amountCents <= 0L) {
            return@withLock SavingsGoalResult.Error("Deposit amount must be greater than zero.")
        }
        val currentWlt = _currentWallet.value
        if (amountCents > currentWlt.availableBalance) {
            return@withLock SavingsGoalResult.Error("Insufficient balance in your wallet.")
        }
        val goalIndex = _savingsGoals.value.indexOfFirst { it.id == goalId }
        if (goalIndex == -1) {
            return@withLock SavingsGoalResult.Error("Savings goal not found.")
        }
        val targetGoal = _savingsGoals.value[goalIndex]
        val updatedGoal = targetGoal.copy(
            currentAmountCents = targetGoal.currentAmountCents + amountCents
        )

        val newBalance = currentWlt.currentBalance - amountCents
        val newAvailable = currentWlt.availableBalance - amountCents
        _currentWallet.value = currentWlt.copy(
            currentBalance = newBalance,
            availableBalance = newAvailable
        )

        val updatedGoals = _savingsGoals.value.toMutableList()
        updatedGoals[goalIndex] = updatedGoal
        _savingsGoals.value = updatedGoals
        refreshTotalSavings()

        val tx = Transaction(
            transactionId = "tx_sav_${System.currentTimeMillis()}",
            type = TransactionType.SAVINGS_DEPOSIT,
            senderId = _currentUser.value.userId,
            senderName = _currentUser.value.fullName,
            receiverId = goalId,
            receiverName = targetGoal.name,
            amount = amountCents,
            timestamp = System.currentTimeMillis(),
            description = note.ifBlank { "Saved to ${targetGoal.name}" },
            status = TransactionStatus.COMPLETED,
            referenceNumber = "REF-SAV-${System.currentTimeMillis().toString().takeLast(6)}"
        )
        _transactions.value = listOf(tx) + _transactions.value

        addNotification(
            title = "Goal Top-Up Successful",
            message = "Saved ${Formatters.formatCentsToCurrency(amountCents)} into ${targetGoal.name}.",
            type = NotificationType.TRANSACTION,
            amountCents = amountCents
        )

        return@withLock SavingsGoalResult.Success(
            goal = updatedGoal,
            updatedWalletBalance = newBalance,
            message = "Successfully deposited ${Formatters.formatCentsToCurrency(amountCents)}."
        )
    }

    override suspend fun withdrawFromGoal(
        goalId: String,
        amountCents: Long,
        pin: String,
        note: String
    ): SavingsGoalResult = savingsMutex.withLock {
        if (!verifyPin(pin)) {
            return@withLock SavingsGoalResult.Error("Incorrect PIN entered. Transaction rejected.")
        }
        if (amountCents <= 0L) {
            return@withLock SavingsGoalResult.Error("Withdrawal amount must be greater than zero.")
        }
        val goalIndex = _savingsGoals.value.indexOfFirst { it.id == goalId }
        if (goalIndex == -1) {
            return@withLock SavingsGoalResult.Error("Savings goal not found.")
        }
        val targetGoal = _savingsGoals.value[goalIndex]
        if (amountCents > targetGoal.currentAmountCents) {
            return@withLock SavingsGoalResult.Error("Amount exceeds available balance in this vault.")
        }
        if (targetGoal.isLocked && targetGoal.currentAmountCents < targetGoal.targetAmountCents) {
            return@withLock SavingsGoalResult.Error("This vault is locked until the target amount is reached.")
        }

        val updatedGoal = targetGoal.copy(
            currentAmountCents = targetGoal.currentAmountCents - amountCents
        )

        val currentWlt = _currentWallet.value
        val newBalance = currentWlt.currentBalance + amountCents
        val newAvailable = currentWlt.availableBalance + amountCents
        _currentWallet.value = currentWlt.copy(
            currentBalance = newBalance,
            availableBalance = newAvailable
        )

        val updatedGoals = _savingsGoals.value.toMutableList()
        updatedGoals[goalIndex] = updatedGoal
        _savingsGoals.value = updatedGoals
        refreshTotalSavings()

        val tx = Transaction(
            transactionId = "tx_sav_${System.currentTimeMillis()}",
            type = TransactionType.SAVINGS_WITHDRAWAL,
            senderId = goalId,
            senderName = targetGoal.name,
            receiverId = _currentUser.value.userId,
            receiverName = _currentUser.value.fullName,
            amount = amountCents,
            timestamp = System.currentTimeMillis(),
            description = note.ifBlank { "Cashout from ${targetGoal.name}" },
            status = TransactionStatus.COMPLETED,
            referenceNumber = "REF-WTH-SAV-${System.currentTimeMillis().toString().takeLast(6)}"
        )
        _transactions.value = listOf(tx) + _transactions.value

        addNotification(
            title = "Savings Cashout",
            message = "Transferred ${Formatters.formatCentsToCurrency(amountCents)} from ${targetGoal.name} back to your wallet.",
            type = NotificationType.TRANSACTION,
            amountCents = amountCents
        )

        return@withLock SavingsGoalResult.Success(
            goal = updatedGoal,
            updatedWalletBalance = newBalance,
            message = "Transferred ${Formatters.formatCentsToCurrency(amountCents)} to your wallet."
        )
    }

    override suspend fun deleteSavingsGoal(
        goalId: String,
        pin: String
    ): SavingsGoalResult = savingsMutex.withLock {
        if (!verifyPin(pin)) {
            return@withLock SavingsGoalResult.Error("Incorrect PIN entered. Action rejected.")
        }
        val goal = _savingsGoals.value.firstOrNull { it.id == goalId }
            ?: return@withLock SavingsGoalResult.Error("Savings goal not found.")

        var newBalance = _currentWallet.value.currentBalance
        if (goal.currentAmountCents > 0L) {
            val refund = goal.currentAmountCents
            newBalance = _currentWallet.value.currentBalance + refund
            val newAvailable = _currentWallet.value.availableBalance + refund
            _currentWallet.value = _currentWallet.value.copy(
                currentBalance = newBalance,
                availableBalance = newAvailable
            )

            val tx = Transaction(
                transactionId = "tx_sav_del_${System.currentTimeMillis()}",
                type = TransactionType.SAVINGS_WITHDRAWAL,
                senderId = goalId,
                senderName = goal.name,
                receiverId = _currentUser.value.userId,
                receiverName = _currentUser.value.fullName,
                amount = refund,
                timestamp = System.currentTimeMillis(),
                description = "Closed goal: ${goal.name} (Balance Refund)",
                status = TransactionStatus.COMPLETED,
                referenceNumber = "REF-REFUND-${System.currentTimeMillis().toString().takeLast(6)}"
            )
            _transactions.value = listOf(tx) + _transactions.value
        }

        _savingsGoals.value = _savingsGoals.value.filterNot { it.id == goalId }
        refreshTotalSavings()

        addNotification(
            title = "Savings Goal Closed",
            message = "Goal '${goal.name}' was closed and funds refunded to your wallet.",
            type = NotificationType.TRANSACTION
        )

        return@withLock SavingsGoalResult.Success(
            goal = goal,
            updatedWalletBalance = newBalance,
            message = "Savings goal '${goal.name}' closed."
        )
    }

    override fun updateSavingsGoal(
        goalId: String,
        name: String,
        targetAmountCents: Long,
        targetDateMillis: Long?,
        isLocked: Boolean
    ): Boolean {
        val list = _savingsGoals.value.toMutableList()
        val index = list.indexOfFirst { it.id == goalId }
        if (index == -1) return false
        val existing = list[index]
        list[index] = existing.copy(
            name = name.ifBlank { existing.name },
            targetAmountCents = if (targetAmountCents > 0L) targetAmountCents else existing.targetAmountCents,
            targetDateMillis = targetDateMillis,
            isLocked = isLocked
        )
        _savingsGoals.value = list
        return true
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
        addNotification(
            title = "Security PIN Changed",
            message = "Your 4-digit security PIN was successfully updated.",
            type = NotificationType.SECURITY
        )
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

    override fun setCurrency(currency: String) {
        _selectedCurrency.value = currency
        _currentWallet.value = _currentWallet.value.copy(currency = currency)
        Formatters.defaultCurrency = currency
    }

    override fun setBiometricsEnabled(enabled: Boolean) {
        _isBiometricsEnabled.value = enabled
    }

    override fun setNotificationsEnabled(enabled: Boolean) {
        _isNotificationsEnabled.value = enabled
    }

    override fun setCardFrozen(frozen: Boolean) {
        _isCardFrozen.value = frozen
        _cards.value = _cards.value.map { card ->
            if (card.isPrimary) card.copy(isFrozen = frozen) else card
        }
    }

    override fun setCardFrozen(cardId: String, frozen: Boolean) {
        _cards.value = _cards.value.map { card ->
            if (card.cardId == cardId) {
                if (card.isPrimary) _isCardFrozen.value = frozen
                card.copy(isFrozen = frozen)
            } else card
        }
    }

    override fun toggleOnlinePayments(cardId: String, enabled: Boolean) {
        _cards.value = _cards.value.map { card ->
            if (card.cardId == cardId) card.copy(isOnlinePaymentEnabled = enabled) else card
        }
    }

    override fun toggleInternationalPayments(cardId: String, enabled: Boolean) {
        _cards.value = _cards.value.map { card ->
            if (card.cardId == cardId) card.copy(isInternationalEnabled = enabled) else card
        }
    }

    override fun updateMonthlyLimit(cardId: String, limitCents: Long) {
        _cards.value = _cards.value.map { card ->
            if (card.cardId == cardId) card.copy(monthlyLimitCents = limitCents) else card
        }
    }

    override fun addCard(card: PaymentCard) {
        _cards.value = _cards.value + card
    }

    override fun addBankAccount(account: BankAccount) {
        _bankAccounts.value = _bankAccounts.value + account
    }

    override fun setDefaultCard(cardId: String) {
        _cards.value = _cards.value.map { card ->
            val isTarget = card.cardId == cardId
            if (isTarget) {
                _isCardFrozen.value = card.isFrozen
            }
            card.copy(isPrimary = isTarget)
        }
    }

    private fun syncUnreadNotificationsCount() {
        _unreadNotificationsCount.value = _notifications.value.count { !it.isRead }
    }

    override fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        syncUnreadNotificationsCount()
    }

    override fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        syncUnreadNotificationsCount()
    }

    override fun deleteNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
        syncUnreadNotificationsCount()
    }

    override fun clearAllNotifications() {
        _notifications.value = emptyList()
        syncUnreadNotificationsCount()
    }

    override fun addNotification(
        title: String,
        message: String,
        type: NotificationType,
        amountCents: Long?,
        transactionId: String?
    ) {
        val newNotif = NotificationItem(
            id = "notif_${System.currentTimeMillis()}_${(100..999).random()}",
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            type = type,
            isRead = false,
            amountCents = amountCents,
            transactionId = transactionId
        )
        _notifications.value = listOf(newNotif) + _notifications.value
        syncUnreadNotificationsCount()
    }

    override fun login(identifier: String, pin: String): Boolean {
        val trimmedId = identifier.trim()
        val user = _currentUser.value
        val isUserMatch = trimmedId.equals(user.email, ignoreCase = true) ||
                trimmedId == user.phoneNumber ||
                trimmedId.equals(user.fullName, ignoreCase = true) ||
                trimmedId.equals("demo", ignoreCase = true) ||
                trimmedId.equals("alex", ignoreCase = true)

        if (isUserMatch && pin == _currentPin.value) {
            _isAuthenticated.value = true
            return true
        }
        return false
    }

    override fun loginWithBiometrics(): Boolean {
        if (_isBiometricsEnabled.value) {
            _isAuthenticated.value = true
            return true
        }
        return false
    }

    override fun register(fullName: String, email: String, phoneNumber: String, pin: String): Boolean {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim()
        val cleanPhone = phoneNumber.trim()

        if (cleanName.length < 2 || !cleanEmail.contains("@") || cleanPhone.length < 6 || pin.length != 4) {
            return false
        }

        val newUser = User(
            userId = "usr_${System.currentTimeMillis()}",
            fullName = cleanName,
            email = cleanEmail,
            phoneNumber = cleanPhone,
            accountStatus = AccountStatus.ACTIVE,
            role = "VERIFIED_USER"
        )
        val newWallet = Wallet(
            walletId = "wlt_${System.currentTimeMillis()}",
            walletNumber = "PKP-${(1000..9999).random()}-${(1000..9999).random()}",
            userId = newUser.userId,
            currentBalance = 10000L, // $100.00 welcome bonus for new accounts
            availableBalance = 10000L,
            currency = "USD"
        )

        _currentUser.value = newUser
        _currentWallet.value = newWallet
        _currentPin.value = pin
        _isAuthenticated.value = true
        _isOnboardingCompleted.value = true

        addNotification(
            title = "Welcome to PocketPay!",
            message = "Your account is activated with a $100.00 simulated welcome balance.",
            type = NotificationType.SYSTEM
        )
        return true
    }

    override fun logout() {
        _isAuthenticated.value = false
    }

    override fun completeOnboarding() {
        _isOnboardingCompleted.value = true
    }

    override fun setCategoryBudget(category: SpendingCategory, limitCents: Long) {
        val currentList = _categoryBudgets.value.toMutableList()
        val index = currentList.indexOfFirst { it.category == category }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(monthlyLimitCents = limitCents)
        } else {
            currentList.add(CategoryBudget(category, limitCents))
        }
        _categoryBudgets.value = currentList
    }

    override fun deleteCategoryBudget(category: SpendingCategory) {
        _categoryBudgets.value = _categoryBudgets.value.filterNot { it.category == category }
    }

    override fun getAnalyticsSummary(period: AnalyticsTimePeriod): AnalyticsSummary {
        val now = System.currentTimeMillis()
        val startTs = period.getStartTimestampMillis(now)
        val endTs = period.getEndTimestampMillis(now)

        val periodTxs = _transactions.value.filter {
            it.status == TransactionStatus.COMPLETED && it.timestamp in startTs..endTs
        }

        var totalIncome = 0L
        var totalExpense = 0L
        var largestExpense = 0L
        val categoryTotals = mutableMapOf<SpendingCategory, Long>()
        val categoryCounts = mutableMapOf<SpendingCategory, Int>()
        val outflows = mutableListOf<Transaction>()

        for (tx in periodTxs) {
            when (tx.type) {
                TransactionType.RECEIVE,
                TransactionType.DEPOSIT,
                TransactionType.SAVINGS_WITHDRAWAL -> {
                    totalIncome += tx.amount
                }
                TransactionType.SEND,
                TransactionType.WITHDRAW,
                TransactionType.QR_PAYMENT,
                TransactionType.BILL_PAYMENT,
                TransactionType.SAVINGS_DEPOSIT -> {
                    val expenseAmount = tx.amount + tx.fee
                    totalExpense += expenseAmount
                    if (expenseAmount > largestExpense) {
                        largestExpense = expenseAmount
                    }
                    val cat = TransactionClassifier.categorize(tx)
                    categoryTotals[cat] = (categoryTotals[cat] ?: 0L) + expenseAmount
                    categoryCounts[cat] = (categoryCounts[cat] ?: 0) + 1
                    outflows.add(tx)
                }
            }
        }

        val netCashFlow = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0L) {
            (((totalIncome - totalExpense).coerceAtLeast(0L).toDouble() / totalIncome.toDouble()) * 100).toInt()
        } else 0

        var currentAngle = -90f
        val slices = categoryTotals.entries
            .sortedByDescending { it.value }
            .map { (cat, amount) ->
                val percentage = if (totalExpense > 0L) {
                    ((amount.toDouble() / totalExpense.toDouble()) * 100).toFloat()
                } else 0f
                val sweep = (percentage / 100f) * 360f
                val slice = SpendingSlice(
                    category = cat,
                    amountCents = amount,
                    percentage = percentage,
                    transactionCount = categoryCounts[cat] ?: 0,
                    sweepAngle = sweep,
                    startAngle = currentAngle
                )
                currentAngle += sweep
                slice
            }

        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val cal = Calendar.getInstance()
        val dayMillis = 24 * 3600 * 1000L
        val past7Days = (6 downTo 0).map { dayOffset ->
            val targetMillis = now - (dayOffset * dayMillis)
            cal.timeInMillis = targetMillis
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            val dayEnd = dayStart + dayMillis - 1
            val label = dayFormat.format(Date(dayStart))

            val daySpent = outflows
                .filter { it.timestamp in dayStart..dayEnd }
                .sumOf { it.amount + it.fee }

            Triple(label, dayStart, daySpent)
        }

        val maxDailySpent = past7Days.maxOfOrNull { it.third } ?: 0L
        val dailySpendPoints = past7Days.map { (label, start, spent) ->
            val norm = if (maxDailySpent > 0L) (spent.toFloat() / maxDailySpent.toFloat()).coerceIn(0.08f, 1f) else 0.08f
            DailySpendBar(
                label = label,
                dateMillis = start,
                amountCents = spent,
                normalizedHeight = if (spent > 0L) norm else 0.08f
            )
        }

        val avgDailySpend = if (dailySpendPoints.isNotEmpty()) {
            dailySpendPoints.sumOf { it.amountCents } / dailySpendPoints.size
        } else 0L

        val budgets = _categoryBudgets.value
        val overBudgetCount = budgets.count { b ->
            val spent = categoryTotals[b.category] ?: 0L
            b.isOverBudget(spent)
        }

        val health = FinancialHealthScore.calculate(
            incomeCents = totalIncome,
            expenseCents = totalExpense,
            savingsRatePercent = savingsRate,
            overBudgetCategories = overBudgetCount
        )

        return AnalyticsSummary(
            period = period,
            totalIncomeCents = totalIncome,
            totalExpenseCents = totalExpense,
            netCashFlowCents = netCashFlow,
            savingsRatePercent = savingsRate,
            averageDailySpendCents = avgDailySpend,
            largestExpenseCents = largestExpense,
            spendingSlices = slices,
            dailySpendBars = dailySpendPoints,
            healthScore = health,
            overBudgetCategoriesCount = overBudgetCount
        )
    }

    override suspend fun createSplitBill(
        title: String,
        category: SplitBillCategory,
        totalAmountCents: Long,
        participants: List<SplitParticipant>,
        note: String
    ): SplitBill = splitMutex.withLock {
        val newBill = SplitBill(
            id = "split_${System.currentTimeMillis()}",
            title = title.trim(),
            category = category,
            totalAmountCents = totalAmountCents,
            createdAtMillis = System.currentTimeMillis(),
            note = note.trim(),
            participants = participants,
            status = if (participants.all { it.isPaid }) SplitBillStatus.SETTLED else SplitBillStatus.ACTIVE
        )
        _splitBills.value = listOf(newBill) + _splitBills.value
        addNotification(
            title = "Split Bill Created",
            message = "Created split request '${newBill.title}' for ${Formatters.formatCentsToCurrency(totalAmountCents)}.",
            type = NotificationType.TRANSACTION,
            amountCents = totalAmountCents
        )
        return newBill
    }

    override suspend fun markParticipantPaid(splitBillId: String, participantId: String): Boolean = splitMutex.withLock {
        val bills = _splitBills.value.toMutableList()
        val billIndex = bills.indexOfFirst { it.id == splitBillId }
        if (billIndex < 0) return false
        val bill = bills[billIndex]

        val participantIndex = bill.participants.indexOfFirst { it.participantId == participantId }
        if (participantIndex < 0) return false
        val p = bill.participants[participantIndex]
        if (p.isPaid) return true

        val updatedParticipant = p.copy(
            isPaid = true,
            paidAtMillis = System.currentTimeMillis()
        )
        val updatedParticipants = bill.participants.toMutableList()
        updatedParticipants[participantIndex] = updatedParticipant

        val allPaid = updatedParticipants.all { it.isPaid }
        val updatedBill = bill.copy(
            participants = updatedParticipants,
            status = if (allPaid) SplitBillStatus.SETTLED else bill.status
        )
        bills[billIndex] = updatedBill
        _splitBills.value = bills

        // Credit host wallet balance and record transaction
        val currentWlt = _currentWallet.value
        val newBalance = currentWlt.currentBalance + p.amountCents
        val newAvailable = currentWlt.availableBalance + p.amountCents
        _currentWallet.value = currentWlt.copy(
            currentBalance = newBalance,
            availableBalance = newAvailable
        )

        val tx = Transaction(
            transactionId = "tx_split_${System.currentTimeMillis()}",
            type = TransactionType.RECEIVE,
            senderId = p.participantId,
            senderName = p.name,
            receiverId = _currentUser.value.userId,
            receiverName = _currentUser.value.fullName,
            amount = p.amountCents,
            fee = 0L,
            timestamp = System.currentTimeMillis(),
            description = "Split Share: ${bill.title} (${p.name})",
            status = TransactionStatus.COMPLETED,
            referenceNumber = "REF-SPLIT-${System.currentTimeMillis().toString().takeLast(6)}"
        )
        _transactions.value = listOf(tx) + _transactions.value

        addNotification(
            title = "Split Payment Received",
            message = "${p.name} paid ${Formatters.formatCentsToCurrency(p.amountCents)} for '${bill.title}'.",
            type = NotificationType.TRANSACTION,
            amountCents = p.amountCents,
            transactionId = tx.transactionId
        )
        return true
    }

    override suspend fun sendSplitReminder(splitBillId: String, participantId: String): Boolean = splitMutex.withLock {
        val bills = _splitBills.value.toMutableList()
        val billIndex = bills.indexOfFirst { it.id == splitBillId }
        if (billIndex < 0) return false
        val bill = bills[billIndex]

        val participantIndex = bill.participants.indexOfFirst { it.participantId == participantId }
        if (participantIndex < 0) return false
        val p = bill.participants[participantIndex]

        val updatedParticipants = bill.participants.toMutableList()
        updatedParticipants[participantIndex] = p.copy(reminderCount = p.reminderCount + 1)
        bills[billIndex] = bill.copy(participants = updatedParticipants)
        _splitBills.value = bills

        addNotification(
            title = "Payment Reminder Sent",
            message = "Reminder sent to ${p.name} for ${Formatters.formatCentsToCurrency(p.amountCents)}.",
            type = NotificationType.SYSTEM
        )
        return true
    }

    override suspend fun cancelSplitBill(splitBillId: String): Boolean = splitMutex.withLock {
        val bills = _splitBills.value.toMutableList()
        val billIndex = bills.indexOfFirst { it.id == splitBillId }
        if (billIndex < 0) return false
        bills[billIndex] = bills[billIndex].copy(status = SplitBillStatus.CANCELLED)
        _splitBills.value = bills
        return true
    }

    override fun resetDemoData() {
        _currentUser.value = defaultUser
        _currentWallet.value = defaultWallet
        _transactions.value = generateDefaultTransactions()
        _beneficiaries.value = defaultBeneficiaries
        _cards.value = defaultCards
        _bankAccounts.value = defaultBankAccounts
        _notifications.value = generateDefaultNotifications()
        _billers.value = defaultBillers
        _savingsGoals.value = defaultSavingsGoals
        _categoryBudgets.value = defaultCategoryBudgets
        _splitBills.value = defaultSplitBills
        refreshTotalSavings()
        _unreadNotificationsCount.value = 3
        _currentPin.value = defaultPin
        _isBiometricsEnabled.value = true
        _isNotificationsEnabled.value = true
        _isCardFrozen.value = false
        _selectedCurrency.value = "USD"
        Formatters.defaultCurrency = "USD"
        _isAuthenticated.value = false
        _isOnboardingCompleted.value = false
    }

    override fun updateUser(user: User) {
        _currentUser.value = user
    }
}


/**
 * Shared provider ensuring ViewModels observe the same in-memory repository instance.
 */
object WalletRepositoryProvider {
    val repository: WalletRepository by lazy { InMemoryWalletRepository() }
}

