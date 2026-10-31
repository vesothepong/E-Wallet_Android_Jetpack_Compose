package com.example.e_wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.LightTextSecondary
import com.example.e_wallet.ui.theme.PrimaryBlue

enum class BottomNavTab(val title: String) {
    HOME("Home"),
    TRANSFER("Transfer"),
    CARDS("Cards"),
    PROFILE("Profile");

    // Compatibility aliases for any legacy references
    companion object {
        val WALLET = CARDS
        val HISTORY = TRANSFER
        val SCAN = HOME
    }
}

@Composable
fun PocketPayBottomBar(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        BottomNavTab.HOME to PocketPayIcons.Home,
        BottomNavTab.TRANSFER to PocketPayIcons.SwapHoriz,
        BottomNavTab.CARDS to PocketPayIcons.CreditCard,
        BottomNavTab.PROFILE to PocketPayIcons.Person
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp, spotColor = Color(0x1A0F172A)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { (tab, icon) ->
                    val isSelected = tab == currentTab
                    val contentColor = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTabSelected(tab) }
                            )
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.title,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = tab.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}
