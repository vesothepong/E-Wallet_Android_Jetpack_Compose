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
import com.example.e_wallet.ui.components.BottomNavTab
import com.example.e_wallet.ui.components.PocketPayBottomBar
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.components.TransactionItemRow
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SecondaryTeal
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.util.Formatters

/**
 * Screen 17: Spending Analytics Dashboard & Screen 18: Category Budgets
 * Matches Image 20, 21, 22 and specifications:
 * - Top Filter Bar: Pill segmented controls "[Week] [Month (Active)] [Year]"
 * - Clean multi-colored donut chart with center label "Total Spend / $1,635.20"
 * - Category legend: Food 38%, Shopping 26%, Utilities 18%, Entertainment 12%, Others 6%
 * - Daily spend bar chart: Mon through Sun, Friday highlighted in indigo as peak spend day ($320.00)
 * - Financial Health card: "86 / 100 — Excellent Financial Health", "You saved 18% more than last month!"
 * - Screen 18 Monthly Budgets: Food & Dining ($380/$500), Shopping ($210/$300), Entertainment Near Limit ($145/$150)
 * - Modal slider editor for budget cap
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = viewModel(),
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
                currentTab = BottomNavTab.HOME,
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
            // Header: Back Arrow + Title + Export Button
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
                            text = "Spending & Budgets",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (uiState.isExporting) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp),
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
                }
            }

            // 1. Time Period Selector Tabs (Week, Month Active, Year)
            item {
                PeriodSelectorRow(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelect = viewModel::setPeriod
                )
            }

            // 2. Financial Wellness & Health Score Card (86/100)
            item {
                FinancialHealthCard(
                    healthScore = uiState.summary.healthScore,
                    savingsRatePercent = uiState.summary.savingsRatePercent,
                    overBudgetCategoriesCount = uiState.summary.overBudgetCategoriesCount
                )
            }

            // 3. Interactive Donut Chart for Spending Breakdown ($1,635.20)
            item {
                SpendingDonutChart(
                    slices = uiState.summary.spendingSlices,
                    totalExpenseCents = uiState.summary.totalExpenseCents,
                    selectedCategory = uiState.selectedCategory,
                    onCategoryClick = viewModel::selectCategory
                )
            }

            // 4. Daily Spending Bar Chart Activity (Peak Friday $320.00)
            item {
                DailySpendBarChart(
                    dailyBars = uiState.summary.dailySpendBars,
                    averageDailySpendCents = uiState.summary.averageDailySpendCents
                )
            }

            // 5. Monthly Category Budgets Header & List (Screen 18)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Budgets",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = { viewModel.openEditBudget() },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue.copy(alpha = 0.1f),
                            contentColor = PrimaryBlue
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                    ) {
                        Text(
                            text = "+ Add Budget",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
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

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    // Modal Sheet: Edit Budget (Screen 18)
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
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AnalyticsTimePeriod.entries.forEach { period ->
                val isSelected = period == selectedPeriod
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) PrimaryBlue else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPeriodSelect(period) }
                ) {
                    Text(
                        text = when (period) {
                            AnalyticsTimePeriod.THIS_MONTH -> "Month"
                            AnalyticsTimePeriod.LAST_MONTH -> "Week"
                            AnalyticsTimePeriod.LAST_30_DAYS -> "30 Days"
                            AnalyticsTimePeriod.ALL_TIME -> "Year"
                        },
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
