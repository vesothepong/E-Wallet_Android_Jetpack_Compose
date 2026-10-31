package com.example.e_wallet.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.model.User
import com.example.e_wallet.ui.components.BottomNavTab
import com.example.e_wallet.ui.components.PocketPayBottomBar
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.PurpleAccentDark
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.ui.theme.WarningAmber

/**
 * Screen 22: Profile & Security Settings (Tab 4)
 * Matches Image 26 and specifications:
 * - Header Profile Card:
 *   - Purple avatar circle "K" with verified green check badge
 *   - Name "Kantumruy Dev" (18px bold)
 *   - Email "kantumruy.dev@pocketpay.io"
 *   - Tier badge: "KYC Verified • Tier 2 Account"
 * - Menu Sections (White card groups with arrow icons):
 *   - Group 1 — Account & Security:
 *     - Change 4-Digit Security PIN
 *     - Biometric Authentication (switch - ON)
 *     - Two-Factor Authentication (2FA) (Active)
 *   - Group 2 — Preferences:
 *     - Currency Selector ("USD ($)" pill badge)
 *     - App Theme ("Light" / "Dark" toggle)
 *     - Push Notifications (switch - ON)
 *   - Group 3 — Support & Legal:
 *     - Help Center & 24/7 Live Chat
 *     - Privacy Policy & Terms
 * - Red Logout Button: "Log Out of PocketPay" with exit icon
 * - Bottom navigation bar with "Profile" tab active
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onTabSelected: (BottomNavTab) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDarkTheme by viewModel.repository.isDarkTheme.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            PocketPayBottomBar(
                currentTab = BottomNavTab.PROFILE,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header: Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = PocketPayIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Profile & Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Header Profile Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            shape = CircleShape,
                            color = PurpleAccentDark,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "K",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        // Verified Green Check Badge
                        Surface(
                            shape = CircleShape,
                            color = SuccessGreen,
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = PocketPayIcons.Check,
                                    contentDescription = "Verified",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = uiState.user.fullName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = uiState.user.email,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tier Badge: KYC Verified • Tier 2 Account
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SuccessGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "KYC Verified • Tier 2 Account",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Group 1 — Account & Security
            SettingsGroup(title = "Account & Security") {
                SettingsItemRow(
                    title = "Change 4-Digit Security PIN",
                    icon = PocketPayIcons.Lock,
                    onClick = viewModel::openChangePinSheet
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Biometric Authentication",
                    subtitle = "Fingerprint / Face ID unlock",
                    icon = PocketPayIcons.Fingerprint,
                    checked = uiState.isBiometricsEnabled,
                    onCheckedChange = viewModel::setBiometricsEnabled
                )
                SettingsDivider()
                SettingsItemRow(
                    title = "Two-Factor Authentication (2FA)",
                    icon = PocketPayIcons.Shield,
                    trailingContent = {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    },
                    onClick = {
                        viewModel.clearSnackbar()
                    }
                )
            }

            // Group 2 — Preferences
            SettingsGroup(title = "App Preferences") {
                SettingsItemRow(
                    title = "Currency",
                    icon = PocketPayIcons.AttachMoney,
                    trailingContent = {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "USD ($)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    },
                    onClick = {
                        viewModel.setCurrency(if (uiState.selectedCurrency == "USD") "EUR" else "USD")
                    }
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "App Theme",
                    subtitle = if (isDarkTheme) "Dark Mode" else "Light Mode",
                    icon = if (isDarkTheme) PocketPayIcons.DarkMode else PocketPayIcons.Sun,
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.repository.toggleDarkTheme() }
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Push Notifications",
                    subtitle = "Instant transaction updates",
                    icon = PocketPayIcons.Notifications,
                    checked = uiState.isNotificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled
                )
            }

            // Group 3 — Support & Legal
            SettingsGroup(title = "Support & Legal") {
                SettingsItemRow(
                    title = "Help Center & 24/7 Live Chat",
                    icon = PocketPayIcons.Help,
                    onClick = {}
                )
                SettingsDivider()
                SettingsItemRow(
                    title = "Privacy Policy & Terms",
                    icon = PocketPayIcons.Description,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Red Logout Button
            Button(
                onClick = viewModel::openLogoutDialog,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExpenseRose.copy(alpha = 0.1f),
                    contentColor = ExpenseRose
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRose.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = PocketPayIcons.Logout,
                        contentDescription = "Log Out",
                        tint = ExpenseRose,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Out of PocketPay",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRose
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Change PIN BottomSheet
        if (uiState.showChangePinSheet) {
            ChangePinBottomSheet(
                step = uiState.pinStep,
                oldPin = uiState.oldPinInput,
                newPin = uiState.newPinInput,
                confirmPin = uiState.confirmPinInput,
                pinError = uiState.pinError,
                demoPin = uiState.currentPin,
                onDigit = viewModel::onPinDigit,
                onBackspace = viewModel::onPinBackspace,
                onDismiss = viewModel::dismissChangePinSheet
            )
        }

        // Logout Confirmation Dialog
        if (uiState.showLogoutDialog) {
            LogoutDialog(
                onConfirm = {
                    viewModel.confirmLogout()
                    onLogoutClick()
                },
                onDismiss = viewModel::dismissLogoutDialog
            )
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        thickness = 1.dp
    )
}

@Composable
private fun SettingsItemRow(
    title: String,
    icon: ImageVector,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (trailingContent != null) {
            trailingContent()
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = PocketPayIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryBlue
            )
        )
    }
}

@Composable
private fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Out",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = "Are you sure you want to log out of PocketPay?",
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRose)
            ) {
                Text("Log Out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePinBottomSheet(
    step: ChangePinStep,
    oldPin: String,
    newPin: String,
    confirmPin: String,
    pinError: String?,
    demoPin: String,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val (title, currentInput) = when (step) {
        ChangePinStep.OLD_PIN -> "Enter Current 4-Digit PIN" to oldPin
        ChangePinStep.NEW_PIN -> "Enter New 4-Digit PIN" to newPin
        ChangePinStep.CONFIRM_PIN -> "Confirm New 4-Digit PIN" to confirmPin
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4 Dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (i in 0 until 4) {
                    val isFilled = i < currentInput.length
                    Surface(
                        shape = CircleShape,
                        color = if (isFilled) PrimaryBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    ) {}
                }
            }

            if (pinError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = pinError,
                    fontSize = 12.sp,
                    color = ExpenseRose,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Keypad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "⌫")
            )

            keys.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowKeys.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.size(64.dp))
                        } else {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(64.dp)
                                    .clickable {
                                        if (key == "⌫") onBackspace() else onDigit(key)
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
