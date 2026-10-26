package com.example.e_wallet.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.BankAccount
import com.example.e_wallet.model.CardNetwork
import com.example.e_wallet.model.CardType
import com.example.e_wallet.model.PaymentCard
import com.example.e_wallet.model.Transaction
import com.example.e_wallet.model.TransactionStatus
import com.example.e_wallet.model.Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.RoundingMode

data class WalletUiState(
    val wallet: Wallet,
    val cards: List<PaymentCard> = emptyList(),
    val selectedCardIndex: Int = 0,
    val selectedCard: PaymentCard? = null,
    val bankAccounts: List<BankAccount> = emptyList(),
    val recentCardTransactions: List<Transaction> = emptyList(),
    val isDetailsRevealed: Boolean = false,
    val showAddCardDialog: Boolean = false,
    val addCardHolderName: String = "",
    val addCardNetwork: CardNetwork = CardNetwork.VISA,
    val addCardType: CardType = CardType.VIRTUAL_DEBIT,
    val showAddBankDialog: Boolean = false,
    val addBankName: String = "",
    val addBankAccountType: String = "Checking",
    val addBankLast4: String = "",
    val showEditLimitDialog: Boolean = false,
    val editLimitInput: String = "",
    val showDepositSheet: Boolean = false,
    val showWithdrawSheet: Boolean = false,
    val showSendSheet: Boolean = false,
    val snackbarMessage: String? = null
)

private data class LocalWalletState(
    val selectedCardIndex: Int = 0,
    val isDetailsRevealed: Boolean = false,
    val showAddCardDialog: Boolean = false,
    val addCardHolderName: String = "",
    val addCardNetwork: CardNetwork = CardNetwork.VISA,
    val addCardType: CardType = CardType.VIRTUAL_DEBIT,
    val showAddBankDialog: Boolean = false,
    val addBankName: String = "",
    val addBankAccountType: String = "Checking",
    val addBankLast4: String = "",
    val showEditLimitDialog: Boolean = false,
    val editLimitInput: String = "",
    val showDepositSheet: Boolean = false,
    val showWithdrawSheet: Boolean = false,
    val showSendSheet: Boolean = false,
    val snackbarMessage: String? = null
)

class WalletViewModel(
    val repository: WalletRepository = WalletRepositoryProvider.repository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope
    private val _localState = MutableStateFlow(LocalWalletState())

    private val repoFlow = combine(
        repository.currentWallet,
        repository.cards,
        repository.bankAccounts,
        repository.transactions
    ) { wallet, cards, bankAccounts, txs ->
        Quadruple(wallet, cards, bankAccounts, txs)
    }

    val uiState: StateFlow<WalletUiState> = combine(
        repoFlow,
        _localState
    ) { (wallet, cards, bankAccounts, txs), local ->
        val safeIndex = local.selectedCardIndex.coerceIn(0, (cards.size - 1).coerceAtLeast(0))
        val currentCard = cards.getOrNull(safeIndex)

        // Filter recent completed transactions
        val recentTxs = txs
            .filter { it.status == TransactionStatus.COMPLETED }
            .take(6)

        WalletUiState(
            wallet = wallet,
            cards = cards,
            selectedCardIndex = safeIndex,
            selectedCard = currentCard,
            bankAccounts = bankAccounts,
            recentCardTransactions = recentTxs,
            isDetailsRevealed = local.isDetailsRevealed,
            showAddCardDialog = local.showAddCardDialog,
            addCardHolderName = local.addCardHolderName,
            addCardNetwork = local.addCardNetwork,
            addCardType = local.addCardType,
            showAddBankDialog = local.showAddBankDialog,
            addBankName = local.addBankName,
            addBankAccountType = local.addBankAccountType,
            addBankLast4 = local.addBankLast4,
            showEditLimitDialog = local.showEditLimitDialog,
            editLimitInput = local.editLimitInput,
            showDepositSheet = local.showDepositSheet,
            showWithdrawSheet = local.showWithdrawSheet,
            showSendSheet = local.showSendSheet,
            snackbarMessage = local.snackbarMessage
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = WalletUiState(
            wallet = repository.currentWallet.value,
            cards = repository.cards.value,
            selectedCard = repository.cards.value.firstOrNull(),
            bankAccounts = repository.bankAccounts.value
        )
    )

    fun selectCard(index: Int) {
        _localState.update { it.copy(selectedCardIndex = index, isDetailsRevealed = false) }
    }

    fun toggleFreezeCard() {
        val currentCard = uiState.value.selectedCard ?: return
        val newFrozen = !currentCard.isFrozen
        repository.setCardFrozen(currentCard.cardId, newFrozen)
        val msg = if (newFrozen) "${currentCard.cardType.displayName} frozen. Payments blocked." else "${currentCard.cardType.displayName} unfrozen and active."
        _localState.update { it.copy(snackbarMessage = msg) }
    }

    fun toggleOnlinePayments(cardId: String) {
        val card = repository.cards.value.find { it.cardId == cardId } ?: return
        val newStatus = !card.isOnlinePaymentEnabled
        repository.toggleOnlinePayments(cardId, newStatus)
        _localState.update {
            it.copy(snackbarMessage = if (newStatus) "Online payments enabled" else "Online payments disabled")
        }
    }

    fun toggleInternationalPayments(cardId: String) {
        val card = repository.cards.value.find { it.cardId == cardId } ?: return
        val newStatus = !card.isInternationalEnabled
        repository.toggleInternationalPayments(cardId, newStatus)
        _localState.update {
            it.copy(snackbarMessage = if (newStatus) "International payments enabled" else "International payments disabled")
        }
    }

    fun toggleRevealDetails() {
        _localState.update { it.copy(isDetailsRevealed = !it.isDetailsRevealed) }
    }

    fun setPrimaryCard(cardId: String) {
        repository.setDefaultCard(cardId)
        _localState.update { it.copy(snackbarMessage = "Card set as primary for wallet payments") }
    }

    // Spending Limit
    fun openEditLimitDialog() {
        val card = uiState.value.selectedCard ?: return
        val currentLimitDollars = (card.monthlyLimitCents / 100).toString()
        _localState.update {
            it.copy(showEditLimitDialog = true, editLimitInput = currentLimitDollars)
        }
    }

    fun onLimitInputChanged(input: String) {
        _localState.update { it.copy(editLimitInput = input) }
    }

    fun dismissEditLimitDialog() {
        _localState.update { it.copy(showEditLimitDialog = false) }
    }

    fun saveSpendingLimit() {
        val card = uiState.value.selectedCard ?: return
        val cleaned = _localState.value.editLimitInput.trim().replace("$", "").replace(",", "")
        val dollars = cleaned.toLongOrNull()
        if (dollars == null || dollars <= 0L) {
            _localState.update { it.copy(snackbarMessage = "Please enter a valid monthly limit.") }
            return
        }
        val cents = dollars * 100L
        repository.updateMonthlyLimit(card.cardId, cents)
        _localState.update {
            it.copy(showEditLimitDialog = false, snackbarMessage = "Monthly spending limit set to $$dollars.00")
        }
    }

    // Add Card
    fun openAddCardDialog() {
        _localState.update {
            it.copy(
                showAddCardDialog = true,
                addCardHolderName = repository.currentUser.value.fullName,
                addCardNetwork = CardNetwork.VISA,
                addCardType = CardType.VIRTUAL_DEBIT
            )
        }
    }

    fun dismissAddCardDialog() {
        _localState.update { it.copy(showAddCardDialog = false) }
    }

    fun onAddCardHolderChanged(name: String) {
        _localState.update { it.copy(addCardHolderName = name) }
    }

    fun onAddCardNetworkChanged(network: CardNetwork) {
        _localState.update { it.copy(addCardNetwork = network) }
    }

    fun onAddCardTypeChanged(type: CardType) {
        _localState.update { it.copy(addCardType = type) }
    }

    fun submitAddCard() {
        val state = _localState.value
        val name = state.addCardHolderName.trim()
        if (name.length < 2) {
            _localState.update { it.copy(snackbarMessage = "Please enter a valid cardholder name.") }
            return
        }

        val random4 = (1000..9999).random()
        val randomMiddle1 = (1000..9999).random()
        val randomMiddle2 = (1000..9999).random()
        val prefix = if (state.addCardNetwork == CardNetwork.VISA) "4532" else "5248"
        val fullNumber = "$prefix $randomMiddle1 $randomMiddle2 $random4"
        val masked = "•••• •••• •••• $random4"
        val cvv = (100..999).random().toString()
        val cardId = "crd_${System.currentTimeMillis()}"

        val (startColor, endColor) = when (state.addCardNetwork) {
            CardNetwork.VISA -> 0xFF0284C7 to 0xFF0EA5E9 // Blue gradient
            CardNetwork.MASTERCARD -> 0xFFB45309 to 0xFFEA580C // Amber / Orange gradient
        }

        val newCard = PaymentCard(
            cardId = cardId,
            cardholderName = name,
            cardNumberMasked = masked,
            fullCardNumber = fullNumber,
            expiryDate = "12/30",
            cvv = cvv,
            cardType = state.addCardType,
            cardNetwork = state.addCardNetwork,
            isFrozen = false,
            isPrimary = false,
            isOnlinePaymentEnabled = true,
            isInternationalEnabled = true,
            monthlyLimitCents = 250000L,
            monthlySpentCents = 0L,
            gradientStartColorHex = startColor,
            gradientEndColorHex = endColor
        )

        repository.addCard(newCard)
        val newIndex = repository.cards.value.lastIndex
        _localState.update {
            it.copy(
                showAddCardDialog = false,
                selectedCardIndex = if (newIndex >= 0) newIndex else 0,
                snackbarMessage = "New ${newCard.cardNetwork.displayName} ${newCard.cardType.displayName} issued successfully!"
            )
        }
    }

    // Add Bank
    fun openAddBankDialog() {
        _localState.update {
            it.copy(
                showAddBankDialog = true,
                addBankName = "",
                addBankAccountType = "Checking",
                addBankLast4 = ""
            )
        }
    }

    fun dismissAddBankDialog() {
        _localState.update { it.copy(showAddBankDialog = false) }
    }

    fun onAddBankNameChanged(name: String) {
        _localState.update { it.copy(addBankName = name) }
    }

    fun onAddBankAccountTypeChanged(type: String) {
        _localState.update { it.copy(addBankAccountType = type) }
    }

    fun onAddBankLast4Changed(last4: String) {
        val digits = last4.filter { it.isDigit() }.take(4)
        _localState.update { it.copy(addBankLast4 = digits) }
    }

    fun submitAddBank() {
        val state = _localState.value
        val bankName = state.addBankName.trim()
        val last4 = state.addBankLast4.trim()
        if (bankName.length < 3) {
            _localState.update { it.copy(snackbarMessage = "Please enter a valid institution name.") }
            return
        }
        if (last4.length != 4) {
            _localState.update { it.copy(snackbarMessage = "Please enter the last 4 digits of the account number.") }
            return
        }

        val newAccount = BankAccount(
            accountId = "bnk_${System.currentTimeMillis()}",
            bankName = bankName,
            accountType = state.addBankAccountType,
            accountNumberMasked = "•••• $last4",
            routingNumberMasked = "•••• ${(1000..9999).random()}",
            isPrimary = false,
            balanceCents = 1000000L // $10,000.00 demo balance
        )

        repository.addBankAccount(newAccount)
        _localState.update {
            it.copy(
                showAddBankDialog = false,
                snackbarMessage = "$bankName (${newAccount.accountType}) successfully linked!"
            )
        }
    }

    // Quick Actions Sheets
    fun openDepositSheet() { _localState.update { it.copy(showDepositSheet = true) } }
    fun dismissDepositSheet() { _localState.update { it.copy(showDepositSheet = false) } }

    fun openWithdrawSheet() { _localState.update { it.copy(showWithdrawSheet = true) } }
    fun dismissWithdrawSheet() { _localState.update { it.copy(showWithdrawSheet = false) } }

    fun openSendSheet() { _localState.update { it.copy(showSendSheet = true) } }
    fun dismissSendSheet() { _localState.update { it.copy(showSendSheet = false) } }

    fun clearSnackbar() {
        _localState.update { it.copy(snackbarMessage = null) }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
