package com.example.e_wallet.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.ui.home.HomeQuickAction
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SecondaryTeal
import com.example.e_wallet.ui.theme.SuccessGreen

@Composable
fun QuickActionsSection(
    onActionClick: (HomeQuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickActionButton(
            label = "Send",
            icon = PocketPayIcons.ArrowUp,
            iconTint = PrimaryBlue,
            containerColor = PrimaryBlue.copy(alpha = 0.12f),
            onClick = { onActionClick(HomeQuickAction.SEND) }
        )

        QuickActionButton(
            label = "Receive",
            icon = PocketPayIcons.ArrowDown,
            iconTint = SuccessGreen,
            containerColor = SuccessGreen.copy(alpha = 0.12f),
            onClick = { onActionClick(HomeQuickAction.RECEIVE) }
        )

        QuickActionButton(
            label = "Deposit",
            icon = PocketPayIcons.Add,
            iconTint = SecondaryTeal,
            containerColor = SecondaryTeal.copy(alpha = 0.12f),
            onClick = { onActionClick(HomeQuickAction.DEPOSIT) }
        )

        QuickActionButton(
            label = "Withdraw",
            icon = PocketPayIcons.Send,
            iconTint = ExpenseRose,
            containerColor = ExpenseRose.copy(alpha = 0.12f),
            onClick = { onActionClick(HomeQuickAction.WITHDRAW) }
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = containerColor,
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
