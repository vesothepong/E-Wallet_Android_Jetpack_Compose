package com.example.e_wallet

import com.example.e_wallet.data.InMemoryWalletRepository
import com.example.e_wallet.model.SplitBillCategory
import com.example.e_wallet.model.SplitBillStatus
import com.example.e_wallet.model.SplitParticipant
import com.example.e_wallet.model.TransactionType
import com.example.e_wallet.ui.split.SplitBillTab
import com.example.e_wallet.ui.split.SplitBillViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying Split Bill & Group Expenses business logic, wallet balance credit on payment,
 * reminder increments, and tab filtering.
 */
class SplitBillBusinessLogicTest {

    private lateinit var repository: InMemoryWalletRepository
    private lateinit var viewModel: SplitBillViewModel
    private val testScope = CoroutineScope(Dispatchers.Unconfined + Job())

    @Before
    fun setUp() {
        repository = InMemoryWalletRepository()
        viewModel = SplitBillViewModel(repository = repository, scope = testScope)
    }

    @Test
    fun testDefaultSplitBillsLoaded() {
        val bills = repository.splitBills.value
        assertEquals(2, bills.size)

        val active = bills.first { it.status == SplitBillStatus.ACTIVE }
        assertEquals("Team Dinner & Drinks 🍕", active.title)
        assertEquals(14000L, active.totalAmountCents)
        assertEquals(4, active.participants.size)

        val settled = bills.first { it.status == SplitBillStatus.SETTLED }
        assertEquals("Weekend Cabin Getaway 🏕️", settled.title)
        assertTrue(settled.isFullySettled)
    }

    @Test
    fun testCreateSplitBillCalculationAndHostRemainder() = runBlocking {
        val participants = listOf(
            SplitParticipant("u1", "Alex Morgan", "+15551111", "AM", 3334L, isPaid = true, isHost = true),
            SplitParticipant("u2", "Sarah Chen", "+15552222", "SC", 3333L, isPaid = false),
            SplitParticipant("u3", "David Miller", "+15553333", "DM", 3333L, isPaid = false)
        )

        val bill = repository.createSplitBill(
            title = "Brunch at Cafe 🥐",
            category = SplitBillCategory.DINING,
            totalAmountCents = 10000L, // $100.00
            participants = participants,
            note = "Sunday brunch"
        )

        assertNotNull(bill.id)
        assertEquals("Brunch at Cafe 🥐", bill.title)
        assertEquals(SplitBillStatus.ACTIVE, bill.status)
        assertEquals(10000L, bill.totalAmountCents)

        val inRepo = repository.splitBills.value.first { it.id == bill.id }
        assertEquals(3, inRepo.participants.size)
        assertEquals(3334L, inRepo.collectedAmountCents)
        assertEquals(6666L, inRepo.pendingAmountCents)
    }

    @Test
    fun testMarkParticipantPaidCreditsWalletBalanceAndCreatesTransaction() = runBlocking {
        val initialBalance = repository.currentWallet.value.availableBalance
        val activeBill = repository.splitBills.value.first { it.id == "split_001" }

        // Participant "usr_demo_003" (David Miller) owes $35.00 (3500 cents)
        val unpaidParticipant = activeBill.participants.first { it.participantId == "usr_demo_003" }
        assertFalse(unpaidParticipant.isPaid)

        val success = repository.markParticipantPaid(activeBill.id, unpaidParticipant.participantId)
        assertTrue(success)

        // Verify participant is now marked paid
        val updatedBill = repository.splitBills.value.first { it.id == activeBill.id }
        val updatedParticipant = updatedBill.participants.first { it.participantId == unpaidParticipant.participantId }
        assertTrue(updatedParticipant.isPaid)
        assertNotNull(updatedParticipant.paidAtMillis)

        // Verify host wallet balance was credited by exactly $35.00
        val newBalance = repository.currentWallet.value.availableBalance
        assertEquals(initialBalance + 3500L, newBalance)

        // Verify a new RECEIVE transaction was recorded
        val latestTx = repository.transactions.value.first()
        assertEquals(TransactionType.RECEIVE, latestTx.type)
        assertEquals(3500L, latestTx.amount)
        assertTrue(latestTx.description.contains("David Miller"))
    }

    @Test
    fun testAllParticipantsPaidTransitionsStatusToSettled() = runBlocking {
        val activeBill = repository.splitBills.value.first { it.id == "split_001" }

        // Settle David Miller (3500) and Emma Watson (3500)
        repository.markParticipantPaid(activeBill.id, "usr_demo_003")
        repository.markParticipantPaid(activeBill.id, "usr_demo_004")

        val settledBill = repository.splitBills.value.first { it.id == activeBill.id }
        assertTrue(settledBill.isFullySettled)
        assertEquals(SplitBillStatus.SETTLED, settledBill.status)
        assertEquals(14000L, settledBill.collectedAmountCents)
        assertEquals(0L, settledBill.pendingAmountCents)
    }

    @Test
    fun testSendSplitReminderIncrementsCount() = runBlocking {
        val activeBill = repository.splitBills.value.first { it.id == "split_001" }
        val david = activeBill.participants.first { it.participantId == "usr_demo_003" }
        assertEquals(0, david.reminderCount)

        val success = repository.sendSplitReminder(activeBill.id, david.participantId)
        assertTrue(success)

        val updatedBill = repository.splitBills.value.first { it.id == activeBill.id }
        val updatedDavid = updatedBill.participants.first { it.participantId == david.participantId }
        assertEquals(1, updatedDavid.reminderCount)
    }

    @Test
    fun testCancelSplitBill() = runBlocking {
        val activeBill = repository.splitBills.value.first { it.id == "split_001" }
        val success = repository.cancelSplitBill(activeBill.id)
        assertTrue(success)

        val cancelled = repository.splitBills.value.first { it.id == activeBill.id }
        assertEquals(SplitBillStatus.CANCELLED, cancelled.status)
    }

    @Test
    fun testSplitBillViewModelFiltering() {
        // Default ACTIVE tab
        assertEquals(SplitBillTab.ACTIVE, viewModel.uiState.value.selectedTab)
        assertTrue(viewModel.uiState.value.filteredSplitBills.all { it.status == SplitBillStatus.ACTIVE })

        // SETTLED tab
        viewModel.selectTab(SplitBillTab.SETTLED)
        assertEquals(SplitBillTab.SETTLED, viewModel.uiState.value.selectedTab)
        assertTrue(viewModel.uiState.value.filteredSplitBills.all { it.status == SplitBillStatus.SETTLED })

        // ALL tab
        viewModel.selectTab(SplitBillTab.ALL)
        assertEquals(SplitBillTab.ALL, viewModel.uiState.value.selectedTab)
        assertEquals(2, viewModel.uiState.value.filteredSplitBills.size)
    }

    @Test
    fun testSplitBillViewModelCreationValidation() {
        // Blank title error
        viewModel.createSplitBill("", SplitBillCategory.DINING, 1000L, listOf("ben_001"))
        assertEquals("Please enter a title for the split bill.", viewModel.uiState.value.snackbarMessage)

        // Zero amount error
        viewModel.createSplitBill("Dinner", SplitBillCategory.DINING, 0L, listOf("ben_001"))
        assertEquals("Total amount must be greater than $0.00.", viewModel.uiState.value.snackbarMessage)

        // No participants error
        viewModel.createSplitBill("Dinner", SplitBillCategory.DINING, 1000L, emptyList())
        assertEquals("Select at least one friend to split with.", viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun testResetDemoDataRestoresSplitBills() = runBlocking {
        repository.cancelSplitBill("split_001")
        assertEquals(SplitBillStatus.CANCELLED, repository.splitBills.value.first { it.id == "split_001" }.status)

        repository.resetDemoData()
        assertEquals(SplitBillStatus.ACTIVE, repository.splitBills.value.first { it.id == "split_001" }.status)
    }
}
