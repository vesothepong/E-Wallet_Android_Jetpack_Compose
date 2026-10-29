package com.example.e_wallet.ui.split

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.Beneficiary
import com.example.e_wallet.model.SplitBill
import com.example.e_wallet.model.SplitBillCategory
import com.example.e_wallet.model.SplitBillStatus
import com.example.e_wallet.model.SplitParticipant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SplitBillTab(val title: String) {
    ACTIVE("Active"),
    SETTLED("Settled"),
    ALL("All Splits")
}

data class SplitBillUiState(
    val splitBills: List<SplitBill> = emptyList(),
    val filteredSplitBills: List<SplitBill> = emptyList(),
    val selectedTab: SplitBillTab = SplitBillTab.ACTIVE,
    val totalPendingToCollectCents: Long = 0L,
    val totalCollectedCents: Long = 0L,
    val beneficiaries: List<Beneficiary> = emptyList(),
    val selectedBillForDetail: SplitBill? = null,
    val showCreateSheet: Boolean = false,
    val showDetailSheet: Boolean = false,
    val isProcessing: Boolean = false,
    val snackbarMessage: String? = null
)

class SplitBillViewModel(
    val repository: WalletRepository = WalletRepositoryProvider.repository,
    private val scope: CoroutineScope? = null
) : ViewModel() {

    private val coroutineScope: CoroutineScope
        get() = scope ?: viewModelScope

    private val _selectedTab = MutableStateFlow(SplitBillTab.ACTIVE)
    private val _selectedBillForDetail = MutableStateFlow<SplitBill?>(null)
    private val _showCreateSheet = MutableStateFlow(false)
    private val _showDetailSheet = MutableStateFlow(false)
    private val _isProcessing = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SplitBillUiState> = combine(
        repository.splitBills,
        repository.beneficiaries,
        _selectedTab,
        _selectedBillForDetail,
        _showCreateSheet,
        _showDetailSheet,
        _isProcessing,
        _snackbarMessage
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val bills = args[0] as List<SplitBill>
        @Suppress("UNCHECKED_CAST")
        val bens = args[1] as List<Beneficiary>
        val tab = args[2] as SplitBillTab
        val detailBill = args[3] as SplitBill?
        val showCreate = args[4] as Boolean
        val showDetail = args[5] as Boolean
        val processing = args[6] as Boolean
        val snackbar = args[7] as String?

        val filtered = when (tab) {
            SplitBillTab.ACTIVE -> bills.filter { it.status == SplitBillStatus.ACTIVE }
            SplitBillTab.SETTLED -> bills.filter { it.status == SplitBillStatus.SETTLED }
            SplitBillTab.ALL -> bills.filter { it.status != SplitBillStatus.CANCELLED }
        }

        // Active pending amount to collect from friends (excluding host share)
        val activeBills = bills.filter { it.status == SplitBillStatus.ACTIVE }
        val pendingTotal = activeBills.sumOf { bill ->
            bill.participants.filter { !it.isPaid && !it.isHost }.sumOf { it.amountCents }
        }

        val collectedTotal = bills.sumOf { bill ->
            bill.participants.filter { it.isPaid && !it.isHost }.sumOf { it.amountCents }
        }

        // Keep detail bill synced with latest repository state
        val updatedDetailBill = detailBill?.let { current ->
            bills.firstOrNull { it.id == current.id } ?: current
        }

        SplitBillUiState(
            splitBills = bills,
            filteredSplitBills = filtered,
            selectedTab = tab,
            totalPendingToCollectCents = pendingTotal,
            totalCollectedCents = collectedTotal,
            beneficiaries = bens,
            selectedBillForDetail = updatedDetailBill,
            showCreateSheet = showCreate,
            showDetailSheet = showDetail,
            isProcessing = processing,
            snackbarMessage = snackbar
        )
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = SplitBillUiState()
    )

    fun selectTab(tab: SplitBillTab) {
        _selectedTab.value = tab
    }

    fun openCreateSheet() {
        _showCreateSheet.value = true
    }

    fun dismissCreateSheet() {
        _showCreateSheet.value = false
    }

    fun openDetailSheet(bill: SplitBill) {
        _selectedBillForDetail.value = bill
        _showDetailSheet.value = true
    }

    fun dismissDetailSheet() {
        _showDetailSheet.value = false
        _selectedBillForDetail.value = null
    }

    fun createSplitBill(
        title: String,
        category: SplitBillCategory,
        totalAmountCents: Long,
        selectedBeneficiaryIds: List<String>,
        includeHost: Boolean = true,
        note: String = ""
    ) {
        if (title.isBlank()) {
            _snackbarMessage.value = "Please enter a title for the split bill."
            return
        }
        if (totalAmountCents <= 0L) {
            _snackbarMessage.value = "Total amount must be greater than $0.00."
            return
        }
        if (selectedBeneficiaryIds.isEmpty()) {
            _snackbarMessage.value = "Select at least one friend to split with."
            return
        }

        coroutineScope.launch {
            _isProcessing.value = true

            val allBens = repository.beneficiaries.value
            val selectedBens = allBens.filter { selectedBeneficiaryIds.contains(it.beneficiaryId) }

            val totalPeopleCount = selectedBens.size + if (includeHost) 1 else 0
            val baseShare = totalAmountCents / totalPeopleCount
            val remainder = totalAmountCents % totalPeopleCount

            val participants = mutableListOf<SplitParticipant>()

            val currentUser = repository.currentUser.value
            if (includeHost) {
                participants.add(
                    SplitParticipant(
                        participantId = currentUser.userId,
                        name = "${currentUser.fullName} (You)",
                        phoneNumber = currentUser.phoneNumber,
                        avatarInitials = "ME",
                        amountCents = baseShare + remainder, // host takes the extra cents remainder
                        isPaid = true,
                        paidAtMillis = System.currentTimeMillis(),
                        isHost = true
                    )
                )
            }

            selectedBens.forEach { ben ->
                participants.add(
                    SplitParticipant(
                        participantId = ben.receiverUserId,
                        name = ben.displayName,
                        phoneNumber = "+1 (555) ${ben.walletNumber.takeLast(7)}",
                        avatarInitials = ben.avatarInitials,
                        amountCents = baseShare,
                        isPaid = false,
                        isHost = false
                    )
                )
            }

            repository.createSplitBill(
                title = title,
                category = category,
                totalAmountCents = totalAmountCents,
                participants = participants,
                note = note
            )

            _isProcessing.value = false
            _showCreateSheet.value = false
            _snackbarMessage.value = "Split bill '$title' created with ${participants.size} participants!"
        }
    }

    fun sendReminder(splitBillId: String, participantId: String) {
        coroutineScope.launch {
            repository.sendSplitReminder(splitBillId, participantId)
            _snackbarMessage.value = "Payment reminder sent!"
        }
    }

    fun markParticipantPaid(splitBillId: String, participantId: String) {
        coroutineScope.launch {
            val success = repository.markParticipantPaid(splitBillId, participantId)
            if (success) {
                _snackbarMessage.value = "Payment confirmed & credited to wallet balance!"
            }
        }
    }

    fun cancelBill(splitBillId: String) {
        coroutineScope.launch {
            repository.cancelSplitBill(splitBillId)
            dismissDetailSheet()
            _snackbarMessage.value = "Split bill cancelled."
        }
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }
}
