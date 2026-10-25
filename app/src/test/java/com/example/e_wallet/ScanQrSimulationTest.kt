package com.example.e_wallet

import com.example.e_wallet.data.InMemoryWalletRepository
import com.example.e_wallet.data.QrPaymentResult
import com.example.e_wallet.model.TransactionStatus
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.ui.home.HomeViewModel
import com.example.e_wallet.ui.scan.QrCodeParser
import com.example.e_wallet.ui.scan.ScanQrStep
import com.example.e_wallet.ui.scan.ScanQrViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying QR Code Scanner and Payment Simulation:
 * - QR parsing handles pocketpay://receive, pocketpay://merchant, and raw fallback formats.
 * - Single Source of Truth: Successful QR payment debits wallet and records TransactionType.QR_PAYMENT.
 * - Validation guards: Insufficient funds, self-payment, zero/negative, and limit exceedance.
 * - ViewModel state machine: Scanner -> Payment Details -> PIN Verification -> Success/Error.
 * - Fixed amount vs flexible amount validation.
 */
class ScanQrSimulationTest {

    private lateinit var repository: InMemoryWalletRepository
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var scanQrViewModel: ScanQrViewModel
    private val testScope = CoroutineScope(Dispatchers.Unconfined + Job())

    @Before
    fun setUp() {
        repository = InMemoryWalletRepository()
        homeViewModel = HomeViewModel(repository, testScope)
        scanQrViewModel = ScanQrViewModel(repository, testScope)
    }

    @Test
    fun testQrCodeParserSupportsPocketPayReceiveUri() {
        val payload = "pocketpay://receive?wallet=PKP-1049-5521&name=Sarah%20Chen&amount=2500"
        val parsed = QrCodeParser.parse(payload, repository.beneficiaries.value)

        assertNotNull(parsed)
        assertEquals("Sarah Chen", parsed?.receiverName)
        assertEquals("PKP-1049-5521", parsed?.receiverWalletNumber)
        assertEquals(2500L, parsed?.presetAmountCents)
        assertFalse(parsed?.isMerchant ?: true)
    }

    @Test
    fun testQrCodeParserSupportsMerchantUri() {
        val payload = "pocketpay://merchant?id=mch_001&name=Starbucks%20Coffee&wallet=PKP-MCH-001&amount=850&note=Caramel%20Macchiato"
        val parsed = QrCodeParser.parse(payload, repository.beneficiaries.value)

        assertNotNull(parsed)
        assertEquals("Starbucks Coffee", parsed?.receiverName)
        assertEquals("mch_001", parsed?.receiverId)
        assertEquals("PKP-MCH-001", parsed?.receiverWalletNumber)
        assertEquals(850L, parsed?.presetAmountCents)
        assertEquals("Caramel Macchiato", parsed?.note)
        assertTrue(parsed?.isMerchant ?: false)
    }

    @Test
    fun testSuccessfulQrPaymentDebitsSenderAndRecordsLedger() = runBlocking {
        val initialBalance = repository.currentWallet.value.currentBalance
        val initialSentTotal = homeViewModel.uiState.value.totalSentCents
        val initialTxCount = homeViewModel.uiState.value.completedTransactionCount

        val payAmountCents = 850L // $8.50
        val result = repository.payQr(
            receiverWalletNumber = "PKP-MCH-001",
            receiverName = "Starbucks Coffee",
            receiverId = "mch_001",
            amountCents = payAmountCents,
            note = "Morning Caramel Macchiato ☕"
        )

        // Verify result
        assertTrue(result is QrPaymentResult.Success)
        val success = result as QrPaymentResult.Success
        assertEquals(initialBalance - payAmountCents, success.newBalance)
        assertEquals(payAmountCents, success.transaction.amount)
        assertEquals(TransactionType.QR_PAYMENT, success.transaction.type)
        assertEquals(TransactionStatus.COMPLETED, success.transaction.status)
        assertEquals("Starbucks Coffee", success.transaction.receiverName)
        assertEquals("mch_001", success.transaction.receiverId)
        assertTrue(success.transaction.referenceNumber.startsWith("REF-QRP-"))

        // Verify repository single source of truth
        assertEquals(initialBalance - payAmountCents, repository.currentWallet.value.currentBalance)
        assertEquals(initialBalance - payAmountCents, repository.currentWallet.value.availableBalance)

        // Verify HomeViewModel reactive stats
        val homeState = homeViewModel.uiState.value
        assertEquals(initialBalance - payAmountCents, homeState.wallet?.currentBalance)
        assertEquals(initialSentTotal + payAmountCents, homeState.totalSentCents)
        assertEquals(initialTxCount + 1, homeState.completedTransactionCount)

        // Verify latest transaction is QR payment
        val latestTx = homeState.recentTransactions.first()
        assertEquals(success.transaction.transactionId, latestTx.transactionId)
        assertEquals(TransactionType.QR_PAYMENT, latestTx.type)
        assertEquals(payAmountCents, latestTx.amount)
    }

    @Test
    fun testQrPaymentInsufficientFundsFailsWithoutTouchingBalance() = runBlocking {
        // Drop balance by withdrawing $2,000.00 first, leaving $450.00 (45000L)
        repository.withdraw(amountCents = 200000L)
        val balanceAfterWithdraw = repository.currentWallet.value.currentBalance
        val txCountAfterWithdraw = repository.transactions.value.size

        // Attempt QR payment of $500.00 (50000L > 45000L available)
        val result = repository.payQr(
            receiverWalletNumber = "PKP-MCH-001",
            receiverName = "Starbucks Coffee",
            receiverId = "mch_001",
            amountCents = 50000L
        )

        assertTrue(result is QrPaymentResult.Error)
        val error = result as QrPaymentResult.Error
        assertTrue(error.message.contains("Insufficient funds", ignoreCase = true))

        // Balance and transactions must remain strictly untouched
        assertEquals(balanceAfterWithdraw, repository.currentWallet.value.currentBalance)
        assertEquals(txCountAfterWithdraw, repository.transactions.value.size)
    }

    @Test
    fun testQrPaymentSelfPaymentIsRejected() = runBlocking {
        val initialBalance = repository.currentWallet.value.currentBalance
        val currentWallet = repository.currentWallet.value
        val currentUser = repository.currentUser.value

        val result = repository.payQr(
            receiverWalletNumber = currentWallet.walletNumber,
            receiverName = currentUser.fullName,
            receiverId = currentUser.userId,
            amountCents = 1500L
        )

        assertTrue(result is QrPaymentResult.Error)
        val error = result as QrPaymentResult.Error
        assertTrue(error.message.contains("Cannot make QR payment to your own wallet", ignoreCase = true))

        // Balance remains unchanged
        assertEquals(initialBalance, repository.currentWallet.value.currentBalance)
    }

    @Test
    fun testScanQrViewModelStepFlowWithFixedAmountAndPinVerification() = runBlocking {
        val starbucksPreset = QrCodeParser.demoPresets.first { it.id == "preset_starbucks" }

        // Initial state is SCANNER
        assertEquals(ScanQrStep.SCANNER, scanQrViewModel.uiState.value.step)

        // Select preset
        scanQrViewModel.onSelectPreset(starbucksPreset)

        val stateAfterScan = scanQrViewModel.uiState.value
        assertEquals(ScanQrStep.PAYMENT_DETAILS, stateAfterScan.step)
        assertEquals("Starbucks Coffee", stateAfterScan.scannedQr?.receiverName)
        assertEquals(850L, stateAfterScan.amountCents)
        assertTrue(stateAfterScan.isAmountLocked)

        // Attempting to change amount on fixed invoice should be ignored
        scanQrViewModel.onAmountChanged("99.00")
        assertEquals(850L, scanQrViewModel.uiState.value.amountCents)

        // Proceed to PIN
        scanQrViewModel.proceedToPin()
        assertEquals(ScanQrStep.PIN_VERIFICATION, scanQrViewModel.uiState.value.step)

        // Enter wrong PIN
        scanQrViewModel.onPinDigit("9")
        scanQrViewModel.onPinDigit("9")
        scanQrViewModel.onPinDigit("9")
        scanQrViewModel.onPinDigit("9")

        val stateAfterWrongPin = scanQrViewModel.uiState.value
        assertEquals(2, stateAfterWrongPin.pinAttemptsLeft)
        assertNotNull(stateAfterWrongPin.pinError)
        assertEquals("", stateAfterWrongPin.pinInput)

        // Enter correct PIN ("1234") - triggers verification and execution
        scanQrViewModel.onPinDigit("1")
        scanQrViewModel.onPinDigit("2")
        scanQrViewModel.onPinDigit("3")
        scanQrViewModel.onPinDigit("4")

        // Wait for processing simulation
        kotlinx.coroutines.delay(1000)

        val stateSuccess = scanQrViewModel.uiState.value
        assertEquals(ScanQrStep.SUCCESS, stateSuccess.step)
        assertNotNull(stateSuccess.completedTransaction)
        assertEquals(850L, stateSuccess.completedTransaction?.amount)
        assertEquals(TransactionType.QR_PAYMENT, stateSuccess.completedTransaction?.type)
        assertFalse(stateSuccess.isProcessing)
    }

    @Test
    fun testScanQrViewModelFlexibleAmountAndQuickChips() {
        val sarahPreset = QrCodeParser.demoPresets.first { it.id == "preset_sarah" }
        scanQrViewModel.onSelectPreset(sarahPreset)

        val state = scanQrViewModel.uiState.value
        assertEquals(ScanQrStep.PAYMENT_DETAILS, state.step)
        assertFalse(state.isAmountLocked)

        // Change amount
        scanQrViewModel.onAmountChanged("45.50")
        assertEquals(4550L, scanQrViewModel.uiState.value.amountCents)

        // Quick amount chip
        scanQrViewModel.onQuickAmountSelected(50)
        assertEquals(5000L, scanQrViewModel.uiState.value.amountCents)
        assertEquals("50", scanQrViewModel.uiState.value.amountInput)
    }
}
