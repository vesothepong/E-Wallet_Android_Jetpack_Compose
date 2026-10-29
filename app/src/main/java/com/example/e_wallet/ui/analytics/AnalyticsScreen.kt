package com.example.e_wallet.ui.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.e_wallet.model.AnalyticsTimePeriod
import com.example.e_wallet.model.SpendingCategory
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.components.TransactionItemRow
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SecondaryTeal
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = viewModel()
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
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Spending & Budgets",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = PocketPayIcons.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 12.dp),
                            color = PrimaryBlue
                        )
                    } else {
                        IconButton(onClick = viewModel::exportStatement) {
                            Icon(
                                imageVector = PocketPayIcons.Receipt,
                                contentDescription = "Export Financial Statement",
                                tint = PrimaryBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Time Period Selector Tabs
            item {
                PeriodSelectorRow(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelect = viewModel::setPeriod
                )
            }

            // 2. Financial Overview Hero Cashflow Card
            item {
                CashflowHeroCard(
                    totalIncomeCents = uiState.summary.totalIncomeCents,
                    totalExpenseCents = uiState.summary.totalExpenseCents,
                    netCashFlowCents = uiState.summary.netCashFlowCents
                )
            }

            // 3. Financial Wellness & Health Score Card
            item {
                FinancialHealthCard(
                    healthScore = uiState.summary.healthScore,
                    savingsRatePercent = uiState.summary.savingsRatePercent,
                    overBudgetCategoriesCount = uiState.summary.overBudgetCategoriesCount
                )
            }

            // 4. Interactive Donut Chart for Spending Breakdown
            item {
                SpendingDonutChart(
                    slices = uiState.summary.spendingSlices,
                    totalExpenseCents = uiState.summary.totalExpenseCents,
                    selectedCategory = uiState.selectedCategory,
                    onCategoryClick = viewModel::selectCategory
                )
            }

            // 5. Daily Spending Bar Chart Activity
            item {
                DailySpendBarChart(
                    dailyBars = uiState.summary.dailySpendBars,
                    averageDailySpendCents = uiState.summary.averageDailySpendCents
                )
            }

            // 6. Monthly Category Budgets Header & List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Budgets",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(onClick = { viewModel.openEditBudget() }) {
                        Text(
                            text = "+ Set Limit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }

            // Category Budgets Cards
            items(uiState.categoryBudgets, key = { it.category.name }) { budget ->
                val categorySpend = uiState.summary.spendingSlices
                    .firstOrNull { it.category == budget.category }
                    ?.amountCents ?: 0L

                CategoryBudgetCard(
                    budget = budget,
                    spentCents = categorySpend,
                    onEditClick = { viewModel.openEditBudget(budget.category) }
                )
            }

            // 7. Transactions Drilldown Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.selectedCategory != null) {
                            "${uiState.selectedCategory?.displayName} Transactions"
                        } else {
                            "Transactions (${uiState.categoryTransactions.size})"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (uiState.selectedCategory != null) {
                        Text(
                            text = "Showing filtered",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (uiState.categoryTransactions.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions found for this period",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.categoryTransactions, key = { it.transactionId }) { tx ->
                    TransactionItemRow(
                        transaction = tx,
                        onClick = {}
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (uiState.showEditBudgetDialog) {
        EditBudgetBottomSheet(
            selectedCategory = uiState.editingBudgetCategory,
            limitInput = uiState.editingLimitInput,
            errorMessage = uiState.editingLimitError,
            onCategorySelected = viewModel::setEditingCategory,
            onInputChange = viewModel::onLimitInputChange,
            onSave = viewModel::saveBudget,
            onDelete = viewModel::deleteBudget,
            onDismiss = viewModel::dismissEditBudget
        )
    }
}

@Composable
private fun PeriodSelectorRow(
    selectedPeriod: AnalyticsTimePeriod,
    onPeriodSelect: (AnalyticsTimePeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnalyticsTimePeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            val bg = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surface
            val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = bg,
                tonalElevation = if (isSelected) 4.dp else 1.dp,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPeriodSelect(period) }
            ) {
                Text(
                    text = period.displayName,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor,
                    modifier = Modifier.padding(vertical = 10.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CashflowHeroCard(
    totalIncomeCents: Long,
    totalExpenseCents: Long,
    netCashFlowCents: Long
) {
    val isNetPositive = netCashFlowCents >= 0L
    val netColor = if (isNetPositive) SuccessGreen else ExpenseRose

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Net Cash Flow",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isNetPositive) "+" else ""}${Formatters.formatCentsToCurrency(netCashFlowCents)}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = netColor
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = netColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (isNetPositive) "Surplus" else "Deficit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = netColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Inflow vs Outflow 2-column stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income Column
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(SuccessGreen.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PocketPayIcons.ArrowDown,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Total Income",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "+${Formatters.formatCentsToCurrency(totalIncomeCents)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Expense Column
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(ExpenseRose.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PocketPayIcons.ArrowUp,
                                contentDescription = null,
                                tint = ExpenseRose,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Total Expenses",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "-${Formatters.formatCentsToCurrency(totalExpenseCents)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
