package com.example.e_wallet

import com.example.e_wallet.data.InMemoryWalletRepository
import com.example.e_wallet.ui.receive.QrCodeGenerator
import com.example.e_wallet.ui.receive.ReceiveViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Receive Money, dynamic QR payload generation, and ZXing encoder compliance.
 */
class ReceiveQrCodeTest {

    private lateinit var repository: InMemoryWalletRepository
    private lateinit var receiveViewModel: ReceiveViewModel
    private val testScope = CoroutineScope(Dispatchers.Unconfined + Job())

    @Before
    fun setUp() {
        repository = InMemoryWalletRepository()
        receiveViewModel = ReceiveViewModel(repository, testScope)
    }

    @Test
    fun testStandardQrCodeBitMatrixGenerationAndRoundtripDecoding() {
        val payload = "pocketpay://receive?wallet=PKP-8829-4102&name=Alex%20Morgan"
        val matrix = QrCodeGenerator.generateBitMatrix(payload, size = 512, margin = 1)

        assertNotNull(matrix)
        assertEquals(512, matrix.width)
        assertEquals(512, matrix.height)

        // Verify round-trip decoding via ZXing
        val decoded = QrCodeGenerator.decodeBitMatrix(matrix)
        assertEquals(payload, decoded)
    }

    @Test
    fun testQrCodeWithSpecifiedRequestAmountDecodesAccurately() {
        val payloadWithAmount = "pocketpay://receive?wallet=PKP-8829-4102&name=Alex%20Morgan&amount=7500"
        val matrix = QrCodeGenerator.generateBitMatrix(payloadWithAmount, size = 512, margin = 2)

        val decoded = QrCodeGenerator.decodeBitMatrix(matrix)
        assertEquals(payloadWithAmount, decoded)
    }

    @Test
    fun testReceiveViewModelDynamicAmountUpdatesQrPayload() {
        val state = receiveViewModel.uiState.value
        assertNotNull(state.wallet)
        assertNotNull(state.user)
        assertFalse(state.isAmountFieldVisible)
        assertNull(state.requestAmountCents)

        // Default QR string contains base URI
        val initialQr = state.qrData
        assertTrue(initialQr.startsWith("pocketpay://receive?wallet="))
        assertTrue(initialQr.contains("name=Kantumruy%20Dev"))
        assertFalse(initialQr.contains("&amount="))

        // Toggle request amount field on
        receiveViewModel.toggleAmountField()
        assertTrue(receiveViewModel.uiState.value.isAmountFieldVisible)

        // Enter amount "$45.25"
        receiveViewModel.onAmountInputChange("45.25")
        val updatedState = receiveViewModel.uiState.value
        assertEquals("45.25", updatedState.requestAmountInput)
        assertEquals(4525L, updatedState.requestAmountCents)
        assertTrue(updatedState.qrData.contains("&amount=4525"))

        // Verify the dynamically formed QR payload generates a valid scannable ZXing matrix
        val matrix = QrCodeGenerator.generateBitMatrix(updatedState.qrData)
        val decoded = QrCodeGenerator.decodeBitMatrix(matrix)
        assertEquals(updatedState.qrData, decoded)

        // Clear amount toggle
        receiveViewModel.toggleAmountField()
        assertFalse(receiveViewModel.uiState.value.isAmountFieldVisible)
        assertEquals("", receiveViewModel.uiState.value.requestAmountInput)
        assertFalse(receiveViewModel.uiState.value.qrData.contains("&amount="))
    }

    @Test
    fun testAmountInputSanitization() {
        // Set a valid initial decimal amount
        receiveViewModel.onAmountInputChange("10.50")
        assertEquals("10.50", receiveViewModel.uiState.value.requestAmountInput)

        // Attempt invalid input with multiple dots; should be rejected and retain existing value
        receiveViewModel.onAmountInputChange("10.50.25")
        assertEquals("10.50", receiveViewModel.uiState.value.requestAmountInput)

        // Attempt invalid input with more than 2 decimal places; should be rejected
        receiveViewModel.onAmountInputChange("10.555")
        assertEquals("10.50", receiveViewModel.uiState.value.requestAmountInput)

        // Valid update
        receiveViewModel.onAmountInputChange("99.99")
        assertEquals("99.99", receiveViewModel.uiState.value.requestAmountInput)
    }
}
