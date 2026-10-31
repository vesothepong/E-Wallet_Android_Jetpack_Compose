package com.example.e_wallet.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.ui.components.BottomNavTab
import com.example.e_wallet.ui.components.PocketPayBottomBar
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.components.TransactionItemRow
import com.example.e_wallet.ui.home.TransactionFilter
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.util.Formatters

/**
 * Screen 20: Transaction History List
 * Matches Image 9/24 and specifications:
 * - Header: "All Transactions"
 * - Search bar: "Search by name, merchant, or ID"
 * - Filter Chips Row: "[All (Active)] [Income] [Expenses] [Transfers] [Bills]"
 * - Grouped Date Sections:
 *   - Today, Oct 31, 2026: Apple Store (-$799.00), Starbucks (-$8.50)
 *   - Yesterday, Oct 30, 2026: Salary Deposit (+$4,250.00), City Water Utility (-$42.50)
 *   - Oct 28, 2026: David Miller (+$20.00)
 * - Tapping opens Screen 21 (TransactionDetailSheet)
 * - Bottom navigation bar with Transfer tab active
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: TransactionHistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBackClick: () -> Unit = {},
    onTabSelected: (BottomNavTab) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PocketPayBottomBar(
                currentTab = BottomNavTab.TRANSFER,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header: Back Arrow + All Transactions Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
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
                    text = "All Transactions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = {
                    Text(
                        text = "Search by name, merchant, or ID",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = PocketPayIcons.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row: [All] [Income] [Expenses] [Transfers] [Bills]
            val filters = listOf(
                TransactionFilter.ALL,
                TransactionFilter.RECEIVE,
                TransactionFilter.SEND,
                TransactionFilter.DEPOSIT,
                TransactionFilter.WITHDRAW
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = filter == uiState.selectedFilter
                    val label = when (filter) {
                        TransactionFilter.ALL -> "All"
                        TransactionFilter.RECEIVE -> "Income"
                        TransactionFilter.SEND -> "Expenses"
                        TransactionFilter.DEPOSIT -> "Transfers"
                        TransactionFilter.WITHDRAW -> "Bills"
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surface,
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.setFilter(filter) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transactions grouped by date
            if (uiState.allTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.groupedTransactions.forEach { (dateLabel, txList) ->
                        item(key = "header_$dateLabel") {
                            Text(
                                text = dateLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                            )
                        }
                        items(txList, key = { it.transactionId }) { tx ->
                            TransactionItemRow(
                                transaction = tx,
                                onClick = { viewModel.onTransactionClick(tx) }
                            )
                        }
                    }
                }
            }
        }

        // Screen 21: Full Transaction Detail Modal
        uiState.selectedTransaction?.let { tx ->
            TransactionDetailSheet(
                transaction = tx,
                onDismiss = viewModel::dismissDetail
            )
        }
    }
}
