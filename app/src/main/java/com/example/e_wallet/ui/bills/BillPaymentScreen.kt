package com.example.e_wallet.ui.bills

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.BillCategory
import com.example.e_wallet.model.Biller
import com.example.e_wallet.ui.components.BottomNavTab
import com.example.e_wallet.ui.components.PocketPayBottomBar
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.LightTextPrimary
import com.example.e_wallet.ui.theme.LightTextSecondary
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.PurpleAccentDark
import com.example.e_wallet.ui.theme.SecondaryTeal
import com.example.e_wallet.ui.theme.TealAccent
import com.example.e_wallet.ui.theme.WarningAmber
import kotlinx.coroutines.launch

/**
 * Screen 12: Bill Payments Categories Hub
 * Matches Image 13 with:
 * - Search bar: "Search biller (e.g. Electric, Water)"
 * - 2x3 Grid of vibrant utility squircles (Electricity, Water, Internet, Mobile Top-Up, Tuition, Cable TV)
 * - Upcoming Bills / Auto-Pay card: "City Water Utility — Due in 3 days ($42.50)" with "1-tap Pay Now" button
 * - 4-item bottom navigation bar
 * - Clicking any category squircle opens Screen 13 (Biller Invoice Detail & Payment)
 */
@Composable
fun BillPaymentScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialCategory: BillCategory? = null,
    repository: WalletRepository = WalletRepositoryProvider.repository,
    onTabSelected: (BottomNavTab) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryForDetail by remember { mutableStateOf<BillCategory?>(initialCategory) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Header: Back Arrow + Pay Bills & Utilities Title
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
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Pay Bills & Utilities",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search biller (e.g. Electric, Water)",
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2x3 Grid of Vibrant Utility Squircles
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Row 1: Electricity & Water Utility
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UtilityGridCard(
                        title = "Electricity",
                        icon = PocketPayIcons.ElectricBolt,
                        backgroundColor = WarningAmber, // Amber
                        onClick = { selectedCategoryForDetail = BillCategory.ELECTRICITY },
                        modifier = Modifier.weight(1f)
                    )

                    UtilityGridCard(
                        title = "Water Utility",
                        icon = PocketPayIcons.Water,
                        backgroundColor = Color(0xFF3B82F6), // Blue
                        onClick = { selectedCategoryForDetail = BillCategory.WATER },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Internet & Fiber and Mobile Top-Up
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UtilityGridCard(
                        title = "Internet & Fiber",
                        icon = PocketPayIcons.Wifi,
                        backgroundColor = Color(0xFF6366F1), // Indigo
                        onClick = { selectedCategoryForDetail = BillCategory.INTERNET },
                        modifier = Modifier.weight(1f)
                    )

                    UtilityGridCard(
                        title = "Mobile Top-Up",
                        icon = PocketPayIcons.Smartphone,
                        backgroundColor = Color(0xFFA855F7), // Purple
                        onClick = { selectedCategoryForDetail = BillCategory.MOBILE_TOPUP },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Tuition & School and Cable TV
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UtilityGridCard(
                        title = "Tuition & School",
                        icon = PocketPayIcons.School,
                        backgroundColor = Color(0xFF0D9488), // Teal/Emerald
                        onClick = { selectedCategoryForDetail = BillCategory.EDUCATION },
                        modifier = Modifier.weight(1f)
                    )

                    UtilityGridCard(
                        title = "Cable TV",
                        icon = PocketPayIcons.Tv,
                        backgroundColor = Color(0xFFEF4444), // Coral/Red
                        onClick = { selectedCategoryForDetail = BillCategory.GOVERNMENT },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Upcoming Bills / Auto-Pay Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Upcoming Bills / Auto-Pay",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "City Water Utility — Due in 3 days",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "($42.50)",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    repository.payBill(
                                        billerId = "biller_water_01",
                                        billerName = "City Water Utility",
                                        category = BillCategory.WATER,
                                        accountNumber = "WTR-440182",
                                        amountCents = 4250L,
                                        note = "City Water Utility 1-tap Pay"
                                    )
                                    snackbarHostState.showSnackbar("City Water Utility ($42.50) paid successfully!")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
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
                                text = "1-tap Pay Now",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Screen 13: Biller Invoice Detail & Payment Modal Sheet
        if (selectedCategoryForDetail != null) {
            BillPaymentBottomSheet(
                initialCategory = selectedCategoryForDetail,
                onDismiss = { selectedCategoryForDetail = null },
                repository = repository
            )
        }
    }
}

/**
 * 2x3 Utility Grid Squircle Item
 */
@Composable
private fun UtilityGridCard(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shadowElevation = 1.dp,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Squircle Icon Container (64x64)
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = backgroundColor.copy(alpha = 0.35f)
                    ),
                shape = RoundedCornerShape(22.dp),
                color = backgroundColor
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
