package com.example.e_wallet.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.model.User
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.LightTextSecondary
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen

/**
 * Screen 4: Top App Header
 * Left: Purple avatar circle 'K' with anchored green status dot, "Welcome back,", "Kantumruy Dev"
 * Right: Circular theme toggle (Sun icon) and Notification bell with red badge dot
 */
@Composable
fun HomeTopBar(
    user: User?,
    unreadNotificationsCount: Int,
    onNotificationClick: () -> Unit,
    onThemeToggleClick: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Avatar + Greeting
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onProfileClick)
        ) {
            Box(modifier = Modifier.size(46.dp)) {
                // Purple/Indigo Avatar Circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C3AED)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "K",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                // Anchored Emerald Green Status Dot (10x10)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Welcome back,",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = user?.fullName?.ifBlank { "Kantumruy Dev" } ?: "Kantumruy Dev",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Right: Theme Toggle & Notification Bell
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme Toggle Button (Sun)
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onThemeToggleClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = PocketPayIcons.Sun,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Notification Bell with Red Badge Dot
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onNotificationClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = PocketPayIcons.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )

                    if (unreadNotificationsCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .padding(end = 4.dp, top = 4.dp)
                                .clip(CircleShape)
                                .background(ExpenseRose)
                        )
                    }
                }
            }
        }
    }
}
