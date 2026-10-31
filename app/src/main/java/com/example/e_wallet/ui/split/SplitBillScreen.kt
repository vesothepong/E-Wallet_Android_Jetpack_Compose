package com.example.e_wallet.ui.split

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.e_wallet.model.SplitBill
import com.example.e_wallet.model.SplitBillStatus
import com.example.e_wallet.model.SplitParticipant
import com.example.e_wallet.ui.components.BottomNavTab
import com.example.e_wallet.ui.components.PocketPayBottomBar
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.PurpleAccent
import com.example.e_wallet.ui.theme.SecondaryTeal
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.ui.theme.WarningAmber
import com.example.e_wallet.util.Formatters

/**
 * Screen 14: Split Bill Dashboard
 * Matches Image 15 and specifications:
 * - Header: "Split Bills" with a "+ Create Split" pill button
 * - Summary Card (Two-column gradient card):
 *   - Left: "You are owed" / "$145.00" in emerald green
 *   - Right: "You owe" / "$30.00" in soft coral rose
 * - Active Splits List:
 *   - Card 1: "Friday Sushi Dinner 🍣"
 *     - Total: $120.00 • Your share: $30.00
 *     - Progress: "3 of 4 Paid" with horizontal green progress bar
 *     - Participant avatars with green checkmarks for paid members (Sarah, Alex, David) and clock icon for pending (Emily)
 *     - Action button: "Remind Emily"
 *   - Card 2: "Apartment Internet & Wifi 🌐"
 *     - Total: $60.00 • Status: "All Paid" green badge
 * - Bottom navigation bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitBillScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplitBillViewModel = viewModel(),
    onTabSelected: (BottomNavTab) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            PocketPayBottomBar(
                currentTab = BottomNavTab.TRANSFER,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header: Back Arrow + Title + "+ Create Split"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = PocketPayIcons.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Split Bills",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = viewModel::openCreateSheet,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        )
                    ) {
                        Text(
                            text = "+ Create Split",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Summary Card (Two-column gradient surface)
            item {
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: You are owed
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "You are owed",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$145.00",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SuccessGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "3 pending requests",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Vertical separator
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(50.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Right: You owe
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "You owe",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$30.00",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ExpenseRose
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "1 split incoming",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Splits",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${uiState.filteredSplitBills.size} Total",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // List of Splits
            items(uiState.filteredSplitBills) { bill ->
                SplitBillCard(
                    bill = bill,
                    onRemind = { participantId ->
                        viewModel.sendReminder(bill.id, participantId)
                    },
                    onClick = { viewModel.openDetailSheet(bill) }
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // Screen 15: Create New Split Bill Modal Sheet
        if (uiState.showCreateSheet) {
            CreateSplitBillBottomSheet(
                beneficiaries = uiState.beneficiaries,
                isProcessing = uiState.isProcessing,
                onCreate = { title, cat, totalCents, participantIds, includeHost, note ->
                    viewModel.createSplitBill(
                        title = title,
                        category = cat,
                        totalAmountCents = totalCents,
                        selectedBeneficiaryIds = participantIds,
                        includeHost = includeHost,
                        note = note
                    )
                },
                onDismiss = viewModel::dismissCreateSheet
            )
        }
    }
}

/**
 * Split Bill Card item matching Screen 14 design
 */
@Composable
private fun SplitBillCard(
    bill: SplitBill,
    onRemind: (String) -> Unit,
    onClick: () -> Unit
) {
    val nonHostParticipants = bill.participants.filter { !it.isHost }
    val paidCount = nonHostParticipants.count { it.isPaid }
    val totalCount = nonHostParticipants.size
    val allPaid = bill.status == SplitBillStatus.SETTLED || (totalCount > 0 && paidCount == totalCount)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Title + Total + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bill.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Total: ${Formatters.formatCentsToCurrency(bill.totalAmountCents)} • Your share: ${Formatters.formatCentsToCurrency(bill.participants.firstOrNull { it.isHost }?.amountCents ?: (bill.totalAmountCents / (bill.participants.size.coerceAtLeast(1))))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (allPaid) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "All Paid",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryBlue.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "$paidCount of $totalCount Paid",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            val progress = if (totalCount > 0) paidCount.toFloat() / totalCount else 1f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (allPaid) SuccessGreen else PrimaryBlue,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Participant Avatars Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bill.participants.take(4).forEach { p ->
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                shape = CircleShape,
                                color = if (p.isHost) PrimaryBlue else Color(0xFF64748B),
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = p.avatarInitials.take(2),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Paid Checkmark or Pending clock
                            if (p.isPaid) {
                                Surface(
                                    shape = CircleShape,
                                    color = SuccessGreen,
                                    modifier = Modifier.size(12.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = PocketPayIcons.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(8.dp)
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = WarningAmber,
                                    modifier = Modifier.size(12.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = PocketPayIcons.Schedule,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // If not all paid, find unpaid member and show "Remind <Name>" button
                val unpaidParticipant = nonHostParticipants.firstOrNull { !it.isPaid }
                if (unpaidParticipant != null) {
                    val firstName = unpaidParticipant.name.split(" ").firstOrNull() ?: unpaidParticipant.name
                    Button(
                        onClick = { onRemind(unpaidParticipant.participantId) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = PrimaryBlue
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                    ) {
                        Text(
                            text = "Remind $firstName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
