package com.example.e_wallet

import com.example.e_wallet.data.InMemoryWalletRepository
import com.example.e_wallet.model.NotificationType
import com.example.e_wallet.ui.notifications.NotificationFilter
import com.example.e_wallet.ui.notifications.NotificationsViewModel
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
 * Unit tests verifying Notifications Center and Event Alerts:
 * - Notifications retrieval and binding to Single Source of Truth repository.
 * - Dynamic unread badge synchronization across top bar and notification center.
 * - Mark individual item as read and mark all as read.
 * - Delete individual item and clear all notifications.
 * - Real-time event notifications generated on transactions (deposit, withdraw, send, QR).
 * - Filter views (All, Unread, Transactions, Security, Promos).
 * - Demo data reset reverts notifications and unread count to default (3).
 */
class NotificationsBusinessLogicTest {

    private lateinit var repository: InMemoryWalletRepository
    private lateinit var notificationsViewModel: NotificationsViewModel
    private val testScope = CoroutineScope(Dispatchers.Unconfined + Job())

    @Before
    fun setUp() {
        repository = InMemoryWalletRepository()
        notificationsViewModel = NotificationsViewModel(repository, testScope)
    }

    @Test
    fun testInitialNotificationsStateMatchesRepositoryDefaults() {
        val state = notificationsViewModel.uiState.value
        assertEquals(5, state.notifications.size)
        assertEquals(3, state.unreadCount)
        assertEquals(3, repository.unreadNotificationsCount.value)
        assertEquals(NotificationFilter.ALL, state.selectedFilter)
    }

    @Test
    fun testMarkSingleNotificationAsReadDecrementsUnreadCount() {
        val unreadItem = repository.notifications.value.first { !it.isRead }
        val initialUnread = repository.unreadNotificationsCount.value

        notificationsViewModel.markAsRead(unreadItem.id)

        val updatedItem = repository.notifications.value.first { it.id == unreadItem.id }
        assertTrue(updatedItem.isRead)
        assertEquals(initialUnread - 1, repository.unreadNotificationsCount.value)
        assertEquals(initialUnread - 1, notificationsViewModel.uiState.value.unreadCount)
    }

    @Test
    fun testMarkAllAsReadSetsAllReadAndZeroUnreadCount() {
        assertTrue(notificationsViewModel.uiState.value.unreadCount > 0)

        notificationsViewModel.markAllAsRead()

        assertEquals(0, notificationsViewModel.uiState.value.unreadCount)
        assertEquals(0, repository.unreadNotificationsCount.value)
        assertTrue(repository.notifications.value.all { it.isRead })
    }

    @Test
    fun testDeleteNotificationRemovesItem() {
        val initialSize = repository.notifications.value.size
        val target = repository.notifications.value.first()

        notificationsViewModel.deleteNotification(target.id)

        assertEquals(initialSize - 1, repository.notifications.value.size)
        assertFalse(repository.notifications.value.any { it.id == target.id })
    }

    @Test
    fun testClearAllNotificationsEmptiesList() {
        notificationsViewModel.clearAllNotifications()

        assertEquals(0, repository.notifications.value.size)
        assertEquals(0, repository.unreadNotificationsCount.value)
        assertTrue(notificationsViewModel.uiState.value.notifications.isEmpty())
    }

    @Test
    fun testAddNotificationDynamicallyIncrementsCount() {
        val initialCount = repository.unreadNotificationsCount.value
        val initialTotal = repository.notifications.value.size

        repository.addNotification(
            title = "Test Alert",
            message = "Testing dynamic notification dispatch",
            type = NotificationType.SYSTEM
        )

        assertEquals(initialTotal + 1, repository.notifications.value.size)
        assertEquals(initialCount + 1, repository.unreadNotificationsCount.value)
        val latest = repository.notifications.value.first()
        assertEquals("Test Alert", latest.title)
        assertFalse(latest.isRead)
    }

    @Test
    fun testFilterNotifications() {
        notificationsViewModel.setFilter(NotificationFilter.UNREAD)
        assertEquals(3, notificationsViewModel.uiState.value.filteredNotifications.size)
        assertTrue(notificationsViewModel.uiState.value.filteredNotifications.all { !it.isRead })

        notificationsViewModel.setFilter(NotificationFilter.TRANSACTION)
        assertTrue(notificationsViewModel.uiState.value.filteredNotifications.all {
            it.type == NotificationType.TRANSACTION
        })

        notificationsViewModel.setFilter(NotificationFilter.SECURITY)
        assertTrue(notificationsViewModel.uiState.value.filteredNotifications.all {
            it.type == NotificationType.SECURITY
        })
    }

    @Test
    fun testTransactionsAutomaticallyDispatchNotifications() = runBlocking {
        val initialTotal = repository.notifications.value.size

        // Execute deposit
        repository.deposit(5000L, "Coffee topup")
        assertEquals(initialTotal + 1, repository.notifications.value.size)
        assertEquals("Deposit Successful", repository.notifications.value.first().title)

        // Execute send money
        repository.sendMoney(
            receiverWalletNumber = "PKP-1049-5521",
            receiverName = "Sarah Chen",
            receiverId = "usr_demo_002",
            amountCents = 1000L
        )
        assertEquals(initialTotal + 2, repository.notifications.value.size)
        assertEquals("Money Sent", repository.notifications.value.first().title)
    }

    @Test
    fun testOpenDetailMarksNotificationAsRead() {
        val unreadItem = repository.notifications.value.first { !it.isRead }
        assertFalse(unreadItem.isRead)

        notificationsViewModel.openDetail(unreadItem)

        assertNotNull(notificationsViewModel.uiState.value.selectedNotificationForDetail)
        assertTrue(notificationsViewModel.uiState.value.selectedNotificationForDetail!!.isRead)
        val inRepo = repository.notifications.value.first { it.id == unreadItem.id }
        assertTrue(inRepo.isRead)

        notificationsViewModel.dismissDetail()
        assertNull(notificationsViewModel.uiState.value.selectedNotificationForDetail)
    }

    @Test
    fun testResetDemoDataRestoresDefaultNotifications() {
        notificationsViewModel.clearAllNotifications()
        assertEquals(0, repository.notifications.value.size)

        repository.resetDemoData()

        assertEquals(5, repository.notifications.value.size)
        assertEquals(3, repository.unreadNotificationsCount.value)
    }
}
