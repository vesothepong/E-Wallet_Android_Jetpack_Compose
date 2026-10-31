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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.ui.home.HomeQuickAction
import com.example.e_wallet.ui.theme.LightTextPrimary
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.PurpleAccent
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.ui.theme.WarningAmber

/**
 * Screen 4: 4-Icon Quick Actions
 * Auto-Layout horizontal row with 4 vibrant squircle buttons:
 * 1. Send: Indigo (#4F46E5) + Paper Plane
 * 2. Receive: Emerald Green (#10B981) + QR Code
 * 3. Scan & Pay: Warm Amber (#F59E0B) + Viewfinder
 * 4. Top Up: Vivid Purple (#8B5CF6) + Wallet
 */
@Composable
fun QuickActionsSection(
    onActionClick: (HomeQuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Send
        QuickActionSquircle(
            label = "Send",
            icon = PocketPayIcons.Send,
            backgroundColor = PrimaryBlue,
            onClick = { onActionClick(HomeQuickAction.SEND) }
        )

        // 2. Receive
        QuickActionSquircle(
            label = "Receive",
            icon = PocketPayIcons.QrCode,
            backgroundColor = SuccessGreen,
            onClick = { onActionClick(HomeQuickAction.RECEIVE) }
        )

        // 3. Scan & Pay
        QuickActionSquircle(
            label = "Scan & Pay",
            icon = PocketPayIcons.CropFree,
            backgroundColor = WarningAmber,
            onClick = { onActionClick(HomeQuickAction.SCAN_QR) }
        )

        // 4. Top Up
        QuickActionSquircle(
            label = "Top Up",
            icon = PocketPayIcons.Wallet,
            backgroundColor = PurpleAccent,
            onClick = { onActionClick(HomeQuickAction.DEPOSIT) }
        )
    }
}

@Composable
private fun QuickActionSquircle(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(58.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(18.dp),
                    spotColor = backgroundColor.copy(alpha = 0.35f)
                ),
            shape = RoundedCornerShape(18.dp),
            color = backgroundColor
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(58.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
