package com.example.e_wallet.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.NotificationItem
import com.example.e_wallet.model.NotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class NotificationFilter(val label: String) {
    ALL("All"),
    UNREAD("Unread"),
    TRANSACTION("Transfers"),
    SECURITY("Security"),
    PROMOTION("Promos")
}

data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val filteredNotifications: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val selectedNotificationForDetail: NotificationItem? = null,
    val snackbarMessage: String? = null
)

class NotificationsViewModel(
    private val repository: WalletRepository = WalletRepositoryProvider.repository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope
    private val _selectedFilter = MutableStateFlow(NotificationFilter.ALL)
    private val _selectedDetail = MutableStateFlow<NotificationItem?>(null)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NotificationsUiState> = combine(
        repository.notifications,
        _selectedFilter,
        _selectedDetail,
        _snackbarMessage
    ) { notifs, filter, detail, snackbar ->
        val unread = notifs.count { !it.isRead }
        val filtered = when (filter) {
            NotificationFilter.ALL -> notifs
            NotificationFilter.UNREAD -> notifs.filter { !it.isRead }
            NotificationFilter.TRANSACTION -> notifs.filter { it.type == NotificationType.TRANSACTION }
            NotificationFilter.SECURITY -> notifs.filter { it.type == NotificationType.SECURITY }
            NotificationFilter.PROMOTION -> notifs.filter { it.type == NotificationType.PROMOTION || it.type == NotificationType.SYSTEM }
        }

        NotificationsUiState(
            notifications = notifs,
            filteredNotifications = filtered,
            unreadCount = unread,
            selectedFilter = filter,
            selectedNotificationForDetail = detail,
            snackbarMessage = snackbar
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = NotificationsUiState(
            notifications = repository.notifications.value,
            filteredNotifications = repository.notifications.value,
            unreadCount = repository.notifications.value.count { !it.isRead }
        )
    )

    fun setFilter(filter: NotificationFilter) {
        _selectedFilter.value = filter
    }

    fun markAsRead(id: String) {
        repository.markNotificationAsRead(id)
    }

    fun markAllAsRead() {
        repository.markAllNotificationsAsRead()
        _snackbarMessage.value = "All notifications marked as read"
    }

    fun deleteNotification(id: String) {
        repository.deleteNotification(id)
        if (_selectedDetail.value?.id == id) {
            _selectedDetail.value = null
        }
        _snackbarMessage.value = "Notification removed"
    }

    fun clearAllNotifications() {
        repository.clearAllNotifications()
        _selectedDetail.value = null
        _snackbarMessage.value = "All notifications cleared"
    }

    fun openDetail(item: NotificationItem) {
        if (!item.isRead) {
            repository.markNotificationAsRead(item.id)
        }
        _selectedDetail.value = item.copy(isRead = true)
    }

    fun dismissDetail() {
        _selectedDetail.value = null
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
