package com.example.e_wallet

import com.example.e_wallet.data.InMemoryWalletRepository
import com.example.e_wallet.model.CardNetwork
import com.example.e_wallet.model.CardType
import com.example.e_wallet.ui.wallet.WalletViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying My Wallet & Multi-Card Management:
 * - Cards and bank accounts retrieval and binding to Single Source of Truth repository.
 * - Card controls: Freeze/Unfreeze, Online transactions permission, International usage.
 * - Dynamic card switching and Primary Card assignment.
 * - Spending limit adjustment.
 * - Issuing new virtual/physical cards dynamically.
 * - Linking new bank accounts.
 * - Demo data reset restores all default cards and accounts.
 */
class WalletBusinessLogicTest {

    private lateinit var repository: InMemoryWalletRepository
    private lateinit var walletViewModel: WalletViewModel
    private val testScope = CoroutineScope(Dispatchers.Unconfined + Job())

    @Before
    fun setUp() {
        repository = InMemoryWalletRepository()
        walletViewModel = WalletViewModel(repository, testScope)
    }

    @Test
    fun testInitialWalletStateContainsDefaultCardsAndBanks() {
        val state = walletViewModel.uiState.value
        assertEquals(2, state.cards.size)
        assertEquals(2, state.bankAccounts.size)

        val primaryCard = state.cards.first()
        assertEquals("crd_001", primaryCard.cardId)
        assertEquals("Kantumruy Dev", primaryCard.cardholderName)
        assertEquals(CardNetwork.VISA, primaryCard.cardNetwork)
        assertEquals(CardType.VIRTUAL_DEBIT, primaryCard.cardType)
        assertTrue(primaryCard.isPrimary)
        assertFalse(primaryCard.isFrozen)
        assertTrue(primaryCard.isOnlinePaymentEnabled)

        val primaryBank = state.bankAccounts.first()
        assertEquals("Chase Bank Checking", primaryBank.bankName)
        assertTrue(primaryBank.isPrimary)
    }

    @Test
    fun testSelectCardUpdatesSelectedCard() {
        assertEquals(0, walletViewModel.uiState.value.selectedCardIndex)
        assertEquals("crd_001", walletViewModel.uiState.value.selectedCard?.cardId)

        walletViewModel.selectCard(1)
        assertEquals(1, walletViewModel.uiState.value.selectedCardIndex)
        assertEquals("crd_002", walletViewModel.uiState.value.selectedCard?.cardId)
        assertEquals(CardNetwork.MASTERCARD, walletViewModel.uiState.value.selectedCard?.cardNetwork)
    }

    @Test
    fun testToggleFreezeCard() {
        val initialCard = walletViewModel.uiState.value.selectedCard
        assertNotNull(initialCard)
        assertFalse(initialCard!!.isFrozen)

        walletViewModel.toggleFreezeCard()
        assertTrue(repository.cards.value.first().isFrozen)
        assertTrue(repository.isCardFrozen.value)

        walletViewModel.toggleFreezeCard()
        assertFalse(repository.cards.value.first().isFrozen)
        assertFalse(repository.isCardFrozen.value)
    }

    @Test
    fun testToggleOnlineAndInternationalPermissions() {
        val cardId = "crd_001"
        assertTrue(repository.cards.value.first().isOnlinePaymentEnabled)
        assertFalse(repository.cards.value.first().isInternationalEnabled)

        walletViewModel.toggleOnlinePayments(cardId)
        assertFalse(repository.cards.value.first().isOnlinePaymentEnabled)

        walletViewModel.toggleInternationalPayments(cardId)
        assertTrue(repository.cards.value.first().isInternationalEnabled)
    }

    @Test
    fun testSetPrimaryCard() {
        val initialPrimary = repository.cards.value.first()
        assertTrue(initialPrimary.isPrimary)

        walletViewModel.setPrimaryCard("crd_002")
        assertFalse(repository.cards.value.first { it.cardId == "crd_001" }.isPrimary)
        assertTrue(repository.cards.value.first { it.cardId == "crd_002" }.isPrimary)
    }

    @Test
    fun testSaveSpendingLimit() {
        walletViewModel.openEditLimitDialog()
        walletViewModel.onLimitInputChanged("4500")
        walletViewModel.saveSpendingLimit()

        val updatedCard = repository.cards.value.first()
        assertEquals(450000L, updatedCard.monthlyLimitCents)
        assertFalse(walletViewModel.uiState.value.showEditLimitDialog)
    }

    @Test
    fun testIssueNewCard() {
        assertEquals(2, repository.cards.value.size)

        walletViewModel.openAddCardDialog()
        walletViewModel.onAddCardHolderChanged("Alex Morgan")
        walletViewModel.onAddCardNetworkChanged(CardNetwork.MASTERCARD)
        walletViewModel.onAddCardTypeChanged(CardType.CREDIT)
        walletViewModel.submitAddCard()

        assertEquals(3, repository.cards.value.size)
        val added = repository.cards.value.last()
        assertEquals("Alex Morgan", added.cardholderName)
        assertEquals(CardNetwork.MASTERCARD, added.cardNetwork)
        assertEquals(CardType.CREDIT, added.cardType)
        assertFalse(added.isFrozen)
    }

    @Test
    fun testLinkNewBankAccount() {
        assertEquals(2, repository.bankAccounts.value.size)

        walletViewModel.openAddBankDialog()
        walletViewModel.onAddBankNameChanged("Citibank Gold")
        walletViewModel.onAddBankAccountTypeChanged("Checking")
        walletViewModel.onAddBankLast4Changed("5522")
        walletViewModel.submitAddBank()

        assertEquals(3, repository.bankAccounts.value.size)
        val added = repository.bankAccounts.value.last()
        assertEquals("Citibank Gold", added.bankName)
        assertEquals("Checking", added.accountType)
        assertEquals("•••• 5522", added.accountNumberMasked)
    }

    @Test
    fun testResetDemoDataRestoresCardsAndBanks() {
        // Issue card, link bank, freeze card
        walletViewModel.toggleFreezeCard()
        walletViewModel.openAddCardDialog()
        walletViewModel.onAddCardHolderChanged("Temporary Card")
        walletViewModel.submitAddCard()
        assertEquals(3, repository.cards.value.size)

        // Reset
        repository.resetDemoData()

        assertEquals(2, repository.cards.value.size)
        assertEquals(2, repository.bankAccounts.value.size)
        assertFalse(repository.cards.value.first().isFrozen)
    }
}
