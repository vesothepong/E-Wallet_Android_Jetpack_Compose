package com.example.e_wallet.ui.send

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.Beneficiary
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.LightTextPrimary
import com.example.e_wallet.ui.theme.LightTextSecondary
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.util.Formatters
import kotlinx.coroutines.launch

/**
 * Screen 5: Send Money Modal (Bottom Sheet)
 * Matches Image 5 with:
 * - Recent Beneficiaries horizontal scroll (Sarah, David, Alex, Emily, + New)
 * - Recipient Wallet Number or Phone input
 * - Large editable Amount input with quick chips ($10, $50, $100, $250)
 * - Transfer Note (80 max)
 * - Breakdown Card (Transfer Amount, Fee, Wallet Balance, Balance After)
 * - Review & Send CTA button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialBeneficiary: Beneficiary? = null,
    repository: WalletRepository = WalletRepositoryProvider.repository
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val beneficiaries by repository.beneficiaries.collectAsState()
    val wallet by repository.currentWallet.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedBeneficiary by remember { mutableStateOf(initialBeneficiary ?: beneficiaries.firstOrNull()) }
    var recipientInput by remember {
        mutableStateOf(selectedBeneficiary?.walletNumber ?: "PKP-1049-5521")
    }
    var amountText by remember { mutableStateOf("250.00") }
    var noteText by remember { mutableStateOf("Dinner share") }
    var isSuccess by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val amountDouble = amountText.toDoubleOrNull() ?: 0.0
    val amountCents = (amountDouble * 100).toLong()
    val currentBalanceCents = wallet.currentBalance
    val balanceAfterCents = (currentBalanceCents - amountCents).coerceAtLeast(0L)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: Title and Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Send Money",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = PocketPayIcons.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Recent Beneficiaries
            Text(
                text = "Recent Beneficiaries",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(beneficiaries) { b ->
                    val isSelected = selectedBeneficiary?.beneficiaryId == b.beneficiaryId
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            selectedBeneficiary = b
                            recipientInput = b.walletNumber
                        }
                    ) {
                        Box(modifier = Modifier.size(54.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else Color(0xFFEDE9FE))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) PrimaryBlue else Color(0xFFE2E8F0),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = b.avatarInitials.ifBlank { b.displayName.take(2).uppercase() },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }

                            // Green online dot
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(1.5.dp)
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

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = b.displayName.split(" ").firstOrNull() ?: b.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // "+ New" avatar
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            selectedBeneficiary = null
                            recipientInput = ""
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, LightBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PocketPayIcons.Add,
                                contentDescription = "Add new",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "New",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Recipient Wallet Number or Phone
            Text(
                text = "Recipient Wallet Number or Phone",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = recipientInput,
                onValueChange = { recipientInput = it },
                placeholder = { Text("Recipient Wallet Number or Phone", fontSize = 14.sp) },
                trailingIcon = {
                    Row(modifier = Modifier.padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PocketPayIcons.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = PocketPayIcons.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = LightBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Amount Section
            Text(
                text = "Amount",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$ ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    BasicTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Quick Chips: $10, $50, $100, $250
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf("10", "50", "100", "250")
                for (chip in chips) {
                    val isSelected = amountText == chip || amountText == "$chip.00"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryBlue else LightBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { amountText = "$chip.00" }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$$chip",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Note Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Note",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "(80 max)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = { if (it.length <= 80) noteText = it },
                placeholder = { Text("Transfer Note (e.g. Dinner, Rent)", fontSize = 14.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = LightBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Breakdown Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Breakdown card",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    BreakdownRow(label = "Transfer Amount:", value = "$${String.format("%.2f", amountDouble)}")
                    BreakdownRow(label = "Fee:", value = "Free ($0.00)")
                    BreakdownRow(label = "Wallet Balance:", value = Formatters.formatCentsToCurrency(currentBalanceCents))
                    HorizontalDivider(color = LightBorder, thickness = 0.5.dp)
                    BreakdownRow(
                        label = "Balance After:",
                        value = Formatters.formatCentsToCurrency(balanceAfterCents),
                        isBold = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Review & Send CTA (52px tall pill)
            Button(
                onClick = {
                    if (amountCents > 0) {
                        isProcessing = true
                        scope.launch {
                            repository.sendMoney(
                                receiverWalletNumber = recipientInput,
                                receiverName = selectedBeneficiary?.displayName ?: "Recipient",
                                receiverId = selectedBeneficiary?.receiverUserId ?: "rec_${System.currentTimeMillis()}",
                                amountCents = amountCents,
                                note = noteText
                            )
                            isProcessing = false
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ),
                enabled = !isProcessing && amountCents > 0
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Review & Send $${String.format("%.2f", amountDouble)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
