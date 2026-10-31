package com.example.e_wallet.ui.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.model.SavingsCategory
import com.example.e_wallet.model.SavingsGoal
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SecondaryTeal
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.ui.theme.WarningAmber
import com.example.e_wallet.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    viewModel: SavingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBackClick: () -> Unit = {},
    onTabSelected: (com.example.e_wallet.ui.components.BottomNavTab) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Savings Goals",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = PocketPayIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::openCreateGoalDialog) {
                        Icon(
                            imageVector = PocketPayIcons.Add,
                            contentDescription = "New Goal",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            com.example.e_wallet.ui.components.PocketPayBottomBar(
                currentTab = com.example.e_wallet.ui.components.BottomNavTab.HOME,
                onTabSelected = onTabSelected
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Hero Overview Card
            SavingsOverviewHero(
                totalSavedCents = uiState.totalSavedCents,
                totalTargetCents = uiState.totalTargetCents,
                progressPercent = uiState.overallProgressPercent,
                projectedMonthlyInterestCents = uiState.projectedMonthlyInterestCents,
                onCreateGoalClick = viewModel::openCreateGoalDialog
            )

            // 2. Filter Chips
            SavingsFilterRow(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = viewModel::setFilter
            )

            // 3. Vaults List
            if (uiState.filteredGoals.isEmpty()) {
                EmptySavingsState(
                    selectedFilter = uiState.selectedFilter,
                    onCreateClick = viewModel::openCreateGoalDialog
                )
            } else {
                Text(
                    text = "${uiState.filteredGoals.size} Active Goals",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                uiState.filteredGoals.forEach { goal ->
                    SavingsGoalCard(
                        goal = goal,
                        onAddMoneyClick = { viewModel.openGoalDetail(goal) },
                        onManageClick = { viewModel.openGoalDetail(goal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Modals
        if (uiState.showCreateDialog) {
            CreateGoalBottomSheet(
                availableBalanceCents = uiState.walletAvailableBalanceCents,
                isProcessing = uiState.isProcessing,
                onDismiss = viewModel::dismissCreateGoalDialog,
                onCreate = { name, target, initial, cat, date, locked, note ->
                    viewModel.createGoal(name, target, initial, cat, date, locked, note)
                }
            )
        }

        if (uiState.showDetailSheet && uiState.selectedGoal != null) {
            GoalDetailBottomSheet(
                goal = uiState.selectedGoal!!,
                walletBalanceCents = uiState.walletAvailableBalanceCents,
                isProcessing = uiState.isProcessing,
                onDismiss = viewModel::dismissGoalDetail,
                onDeposit = { amount, note ->
                    viewModel.depositToGoal(uiState.selectedGoal!!.id, amount, note)
                },
                onWithdraw = { amount, pin, note ->
                    viewModel.withdrawFromGoal(uiState.selectedGoal!!.id, amount, pin, note)
                },
                onDelete = { pin ->
                    viewModel.deleteGoal(uiState.selectedGoal!!.id, pin)
                }
            )
        }
    }
}

@Composable
private fun SavingsOverviewHero(
    totalSavedCents: Long,
    totalTargetCents: Long,
    progressPercent: Int,
    projectedMonthlyInterestCents: Long,
    onCreateGoalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A), // Deep Blue
                        Color(0xFF3B82F6), // Vibrant Blue
                        Color(0xFF0D9488)  // Teal
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = PocketPayIcons.Savings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Total Vault Savings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = PocketPayIcons.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF6EE7B7),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "4.5% APY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Big Saved Amount
            Text(
                text = Formatters.formatCentsToCurrency(totalSavedCents),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            // Progress Bar & Target
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$progressPercent% of Target (${Formatters.formatCentsToCurrency(totalTargetCents)})",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "+${Formatters.formatCentsToCurrency(projectedMonthlyInterestCents)}/mo est.",
                        fontSize = 12.sp,
                        color = Color(0xFF6EE7B7),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                LinearProgressIndicator(
                    progress = { (progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF6EE7B7),
                    trackColor = Color.White.copy(alpha = 0.25f),
                    strokeCap = StrokeCap.Round
                )
            }

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onCreateGoalClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PrimaryBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = PocketPayIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "New Vault", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun SavingsFilterRow(
    selectedFilter: SavingsGoalFilter,
    onFilterSelected: (SavingsGoalFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SavingsGoalFilter.values()) { filter ->
            val isSelected = filter == selectedFilter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = null
            )
        }
    }
}

@Composable
private fun SavingsGoalCard(
    goal: SavingsGoal,
    onAddMoneyClick: () -> Unit,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = Color(goal.category.colorHex)
    val icon = getCategoryIcon(goal.category)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onManageClick)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Category Icon + Title + Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = categoryColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = goal.category.displayName,
                                tint = categoryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = goal.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = goal.category.displayName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (goal.isLocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarningAmber.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = PocketPayIcons.Lock,
                                    contentDescription = "Locked",
                                    tint = WarningAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Locked",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber
                                )
                            }
                        }
                    }

                    if (goal.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Reached 🎉",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Balances
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Saved Balance",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Formatters.formatCentsToCurrency(goal.currentAmountCents),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target: ${Formatters.formatCentsToCurrency(goal.targetAmountCents)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (goal.isCompleted) "Target achieved!" else "${Formatters.formatCentsToCurrency(goal.remainingCents)} left",
                        fontSize = 11.sp,
                        color = if (goal.isCompleted) SuccessGreen else PrimaryBlue
                    )
                }
            }

            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { goal.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (goal.isCompleted) SuccessGreen else categoryColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    strokeCap = StrokeCap.Round
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${goal.progressPercent}% achieved",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "+4.5% APY yield",
                        fontSize = 11.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onManageClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(text = "Manage", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onAddMoneyClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = PocketPayIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Add Money", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptySavingsState(
    selectedFilter: SavingsGoalFilter,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.12f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = PocketPayIcons.Savings,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = if (selectedFilter == SavingsGoalFilter.ALL) "No Savings Vaults Yet" else "No ${selectedFilter.label} Vaults",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Set financial goals, lock funds to avoid impulse spending, and earn a simulated 4.5% APY yield.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onCreateClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(imageVector = PocketPayIcons.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Create First Vault", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalBottomSheet(
    availableBalanceCents: Long,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onCreate: (name: String, targetAmountCents: Long, initialDepositCents: Long, category: SavingsCategory, targetDateMillis: Long?, isLocked: Boolean, note: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var targetAmountInput by remember { mutableStateOf("") }
    var initialDepositInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SavingsCategory.GENERAL) }
    var isLocked by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val presetAmounts = listOf(50000L, 100000L, 250000L, 500000L) // $500, $1k, $2.5k, $5k

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Savings Vault",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = PocketPayIcons.Close, contentDescription = "Close")
                }
            }

            // Goal Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    validationError = null
                },
                label = { Text("Vault Name") },
                placeholder = { Text("e.g. Dream Vacation, New Laptop") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // Category Selector
            Text(
                text = "Category",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SavingsCategory.values()) { cat ->
                    val isCatSelected = cat == selectedCategory
                    val catColor = Color(cat.colorHex)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCatSelected) catColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isCatSelected) androidx.compose.foundation.BorderStroke(1.5.dp, catColor) else null,
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = cat.displayName,
                                tint = if (isCatSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCatSelected) catColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Target Amount Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = targetAmountInput,
                    onValueChange = {
                        targetAmountInput = it.filter { ch -> ch.isDigit() || ch == '.' }
                        validationError = null
                    },
                    label = { Text("Target Amount ($)") },
                    placeholder = { Text("e.g. 1000.00") },
                    leadingIcon = { Text("$", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                // Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetAmounts.forEach { cents ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    targetAmountInput = (cents / 100).toString()
                                    validationError = null
                                }
                        ) {
                            Text(
                                text = Formatters.formatCentsToCurrency(cents),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Initial Deposit (Optional)
            OutlinedTextField(
                value = initialDepositInput,
                onValueChange = {
                    initialDepositInput = it.filter { ch -> ch.isDigit() || ch == '.' }
                    validationError = null
                },
                label = { Text("Initial Deposit (Optional)") },
                placeholder = { Text("0.00") },
                supportingText = {
                    Text("Available Wallet Balance: ${Formatters.formatCentsToCurrency(availableBalanceCents)}")
                },
                leadingIcon = { Text("$", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // Lock Vault Toggle
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = PocketPayIcons.Lock,
                                contentDescription = null,
                                tint = if (isLocked) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lock This Vault",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Locks withdrawals until target amount is achieved to ensure discipline.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isLocked,
                        onCheckedChange = { isLocked = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryBlue)
                    )
                }
            }

            // Note (Optional)
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notes / Purpose (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // Error display
            validationError?.let { err ->
                Text(
                    text = err,
                    color = ExpenseRose,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Create Button
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isBlank()) {
                        validationError = "Please enter a vault name."
                        return@Button
                    }
                    val targetCents = (targetAmountInput.toDoubleOrNull()?.let { it * 100 })?.toLong() ?: 0L
                    if (targetCents <= 0L) {
                        validationError = "Please enter a valid target amount greater than $0."
                        return@Button
                    }
                    val initialCents = (initialDepositInput.toDoubleOrNull()?.let { it * 100 })?.toLong() ?: 0L
                    if (initialCents > availableBalanceCents) {
                        validationError = "Initial deposit exceeds your available wallet balance."
                        return@Button
                    }

                    onCreate(trimmedName, targetCents, initialCents, selectedCategory, null, isLocked, note)
                },
                enabled = !isProcessing,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Create Savings Vault", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailBottomSheet(
    goal: SavingsGoal,
    walletBalanceCents: Long,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onDeposit: (amountCents: Long, note: String) -> Unit,
    onWithdraw: (amountCents: Long, pin: String, note: String) -> Unit,
    onDelete: (pin: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Deposit, 1 = Withdraw
    var depositInput by remember { mutableStateOf("") }
    var withdrawInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deletePinInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val categoryColor = Color(goal.category.colorHex)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = categoryColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getCategoryIcon(goal.category),
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = goal.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = goal.category.displayName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = PocketPayIcons.Close, contentDescription = "Close")
                }
            }

            // Stats Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Saved in Vault", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = Formatters.formatCentsToCurrency(goal.currentAmountCents),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Target Goal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = Formatters.formatCentsToCurrency(goal.targetAmountCents),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { goal.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (goal.isCompleted) SuccessGreen else categoryColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${goal.progressPercent}% achieved",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (goal.isCompleted) SuccessGreen else PrimaryBlue
                        )
                        Text(
                            text = if (goal.isCompleted) "Target Complete!" else "${Formatters.formatCentsToCurrency(goal.remainingCents)} remaining",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = PocketPayIcons.TrendingUp,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Simulated APY: ${goal.apyRatePercent}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "+${Formatters.formatCentsToCurrency(goal.projectedMonthlyInterestCents)} / month",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            // Tabs: Deposit vs Cashout
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        errorMsg = null
                    },
                    text = { Text("Add Money", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        errorMsg = null
                    },
                    text = { Text("Withdraw", fontWeight = FontWeight.Bold) }
                )
            }

            // Tab 0: Deposit
            if (selectedTab == 0) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = depositInput,
                        onValueChange = {
                            depositInput = it.filter { ch -> ch.isDigit() || ch == '.' }
                            errorMsg = null
                        },
                        label = { Text("Amount to Deposit") },
                        placeholder = { Text("0.00") },
                        supportingText = {
                            Text("Available in Wallet: ${Formatters.formatCentsToCurrency(walletBalanceCents)}")
                        },
                        leadingIcon = { Text("$", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    // Quick chip presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2500L, 5000L, 10000L, 20000L).forEach { cents ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        depositInput = (cents / 100).toString()
                                        errorMsg = null
                                    }
                            ) {
                                Text(
                                    text = "+${Formatters.formatCentsToCurrency(cents)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val cents = (depositInput.toDoubleOrNull()?.let { it * 100 })?.toLong() ?: 0L
                            if (cents <= 0L) {
                                errorMsg = "Please enter an amount greater than $0."
                                return@Button
                            }
                            if (cents > walletBalanceCents) {
                                errorMsg = "Insufficient wallet balance."
                                return@Button
                            }
                            onDeposit(cents, "Saved to ${goal.name}")
                            depositInput = ""
                        },
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(text = "Transfer to Vault", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Tab 1: Withdraw
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (goal.isLocked && !goal.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarningAmber.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = PocketPayIcons.Lock,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "This vault is locked until the target amount is reached. Premature withdrawal is restricted.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = withdrawInput,
                        onValueChange = {
                            withdrawInput = it.filter { ch -> ch.isDigit() || ch == '.' }
                            errorMsg = null
                        },
                        label = { Text("Amount to Cashout") },
                        placeholder = { Text("0.00") },
                        supportingText = {
                            Text("Available in Vault: ${Formatters.formatCentsToCurrency(goal.currentAmountCents)}")
                        },
                        leadingIcon = { Text("$", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                pinInput = it
                                errorMsg = null
                            }
                        },
                        label = { Text("Security PIN (4 digits)") },
                        placeholder = { Text("••••") },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(imageVector = PocketPayIcons.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val cents = (withdrawInput.toDoubleOrNull()?.let { it * 100 })?.toLong() ?: 0L
                            if (cents <= 0L) {
                                errorMsg = "Please enter an amount greater than $0."
                                return@Button
                            }
                            if (cents > goal.currentAmountCents) {
                                errorMsg = "Amount exceeds vault balance."
                                return@Button
                            }
                            if (goal.isLocked && !goal.isCompleted) {
                                errorMsg = "This vault is locked until target amount is achieved."
                                return@Button
                            }
                            if (pinInput.length != 4) {
                                errorMsg = "Please enter your 4-digit security PIN."
                                return@Button
                            }
                            onWithdraw(cents, pinInput, "Cashout from ${goal.name}")
                            withdrawInput = ""
                            pinInput = ""
                        },
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(text = "Transfer to Wallet", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Error display
            errorMsg?.let { err ->
                Text(
                    text = err,
                    color = ExpenseRose,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Delete Vault Button
            TextButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Close Vault & Refund Balance",
                    color = ExpenseRose,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmDialog = false
                    deletePinInput = ""
                },
                title = { Text("Close Savings Vault?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Closing '${goal.name}' will immediately refund its full balance (${Formatters.formatCentsToCurrency(goal.currentAmountCents)}) to your primary wallet.",
                            fontSize = 13.sp
                        )
                        OutlinedTextField(
                            value = deletePinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { ch -> ch.isDigit() }) deletePinInput = it
                            },
                            label = { Text("Enter 4-digit PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (deletePinInput.length == 4) {
                                onDelete(deletePinInput)
                                showDeleteConfirmDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRose)
                    ) {
                        Text("Confirm & Close")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private fun getCategoryIcon(category: SavingsCategory): ImageVector {
    return when (category) {
        SavingsCategory.EMERGENCY -> PocketPayIcons.Shield
        SavingsCategory.TRAVEL -> PocketPayIcons.Flight
        SavingsCategory.TECH -> PocketPayIcons.Smartphone
        SavingsCategory.VEHICLE -> PocketPayIcons.DirectionsCar
        SavingsCategory.HOME -> PocketPayIcons.Home
        SavingsCategory.EDUCATION -> PocketPayIcons.School
        SavingsCategory.GENERAL -> PocketPayIcons.Savings
    }
}
