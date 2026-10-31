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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.model.Wallet
import com.example.e_wallet.ui.theme.CyanAccent
import com.example.e_wallet.ui.theme.ExpenseRoseSubtext
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SecondaryTeal
import com.example.e_wallet.util.Formatters

/**
 * Screen 4 Hero Card: Total Balance Card
 * Visual Specs:
 * - Gradient from #4F46E5 to #3B82F6 (Electric Blue)
 * - 26dp rounded corner radius, soft blue drop shadow
 * - "TOTAL BALANCE" + Eye toggle icon
 * - Large tabular numerals ($12,450.80) with +2.4% cyan pill badge
 * - White divider line (15% alpha)
 * - Two-column sub-metrics: Income (Oct) $4,820.00 and Expenses (Oct) $1,635.20 (in salmon rose #FDA4AF)
 */
@Composable
fun WalletCard(
    wallet: Wallet?,
    isBalanceVisible: Boolean,
    totalReceivedCents: Long,
    totalSentCents: Long,
    onToggleBalanceVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayBalance = if (isBalanceVisible) {
        Formatters.formatCentsToCurrency(wallet?.currentBalance ?: 1245080L, wallet?.currency ?: "USD")
    } else {
        "••••••••"
    }

    val displayIncome = if (isBalanceVisible) {
        Formatters.formatCentsToCurrency(if (totalReceivedCents > 0) totalReceivedCents else 482000L, wallet?.currency ?: "USD")
    } else {
        "••••••"
    }

    val displayExpenses = if (isBalanceVisible) {
        Formatters.formatCentsToCurrency(if (totalSentCents > 0) totalSentCents else 163520L, wallet?.currency ?: "USD")
    } else {
        "••••••"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.35f)
            ),
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4F46E5), // Indigo
                            Color(0xFF3B82F6)  // Electric Blue
                        )
                    )
                )
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Line: "TOTAL BALANCE" and Eye toggle icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onToggleBalanceVisibility)
                    ) {
                        Text(
                            text = "TOTAL BALANCE",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isBalanceVisible) PocketPayIcons.Visibility else PocketPayIcons.VisibilityOff,
                            contentDescription = if (isBalanceVisible) "Hide balance" else "Show balance",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hero Amount Row with +2.4% cyan pill badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = displayBalance,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x3838BDF8) // Translucent Cyan
                    ) {
                        Text(
                            text = "+2.4%",
                            color = Color(0xFFE0F2FE),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Thin divider line
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.16f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Two-Column Sub-Metrics (Income vs Expenses)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column 1: Income (Oct)
                    Column {
                        Text(
                            text = "Income (Oct)",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayIncome,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Column 2: Expenses (Oct)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Expenses (Oct)",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayExpenses,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRoseSubtext // Soft Coral Salmon (#FDA4AF)
                        )
                    }
                }
            }
        }
    }
}
