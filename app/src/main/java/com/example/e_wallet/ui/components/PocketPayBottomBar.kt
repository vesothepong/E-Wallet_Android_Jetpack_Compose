package com.example.e_wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.ui.theme.PrimaryBlue

enum class BottomNavTab(val title: String) {
    HOME("Home"),
    WALLET("Wallet"),
    SCAN("Scan"),
    HISTORY("History"),
    PROFILE("Profile")
}

@Composable
fun PocketPayBottomBar(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        BottomNavTab.HOME to PocketPayIcons.Home,
        BottomNavTab.WALLET to PocketPayIcons.Wallet,
        BottomNavTab.SCAN to PocketPayIcons.QrCode,
        BottomNavTab.HISTORY to PocketPayIcons.History,
        BottomNavTab.PROFILE to PocketPayIcons.Person
    )

    NavigationBar(
        modifier = modifier.shadow(8.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        tabs.forEach { (tab, icon) ->
            val isSelected = tab == currentTab
            val isScan = tab == BottomNavTab.SCAN

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (isScan) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(PrimaryBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.title,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.title
                        )
                    }
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = if (isScan) Color.Transparent else PrimaryBlue.copy(alpha = 0.12f)
                )
            )
        }
    }
}
