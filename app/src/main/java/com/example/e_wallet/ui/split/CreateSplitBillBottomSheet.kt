package com.example.e_wallet.ui.split

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.model.Beneficiary
import com.example.e_wallet.model.SplitBillCategory
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.util.Formatters

enum class SplitMethod(val label: String) {
    EQUAL("Split Equally"),
    CUSTOM("Custom / Unequal Split"),
    PERCENTAGE("By Percentages")
}

/**
 * Screen 15: Create New Split Bill Modal
 * Matches Image 16/18 and specifications:
 * - Title: "Create New Split Bill"
 * - Input 1: Bill Title "e.g. Weekend BBQ Party"
 * - Input 2: Total Bill Amount "$160.00"
 * - Contact Picker: Horizontal chips of added friends (Sarah Chen, David Miller, Alex Vance, "+ Add Friend")
 * - Split Method Selector:
 *   1. "Split Equally" ($40.00 each) — Selected
 *   2. "Custom / Unequal Split"
 *   3. "By Percentages"
 * - Summary Box: "You will pay $160.00 now and request $40.00 from 3 friends."
 * - Bottom CTA: 52px primary indigo button labeled "Send Split Requests"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSplitBillBottomSheet(
    beneficiaries: List<Beneficiary>,
    isProcessing: Boolean,
    onCreate: (String, SplitBillCategory, Long, List<String>, Boolean, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var billTitle by remember { mutableStateOf("Weekend BBQ Party") }
    var totalAmountInput by remember { mutableStateOf("160.00") }
    var selectedMethod by remember { mutableStateOf(SplitMethod.EQUAL) }

    val defaultFriends = listOf(
        "Sarah Chen",
        "David Miller",
        "Alex Vance"
    )

    val selectedFriends = remember {
        mutableStateListOf<String>().apply {
            addAll(defaultFriends)
        }
    }

    val amountDouble = totalAmountInput.toDoubleOrNull() ?: 160.0
    val totalCents = (amountDouble * 100).toLong()

    // Host + selected friends
    val totalPeople = selectedFriends.size + 1
    val eachAmount = if (totalPeople > 0) amountDouble / totalPeople else 0.0
    val eachAmountFormatted = String.format("$%.2f", eachAmount)

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
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create New Split Bill",
                    fontSize = 18.sp,
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

            Spacer(modifier = Modifier.height(18.dp))

            // Bill Title Input
            Text(
                text = "Bill Title",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = billTitle,
                onValueChange = { billTitle = it },
                placeholder = {
                    Text(
                        text = "e.g. Weekend BBQ Party",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

            Spacer(modifier = Modifier.height(16.dp))

            // Total Bill Amount Input
            Text(
                text = "Total Bill Amount",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = totalAmountInput,
                onValueChange = { totalAmountInput = it.filter { char -> char.isDigit() || char == '.' } },
                prefix = {
                    Text(
                        text = "$ ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Contact Picker (Horizontal chips of added friends)
            Text(
                text = "Split with Friends (${selectedFriends.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                selectedFriends.forEach { friendName ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = PrimaryBlue.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = friendName.take(1),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = friendName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }

                // + Add Friend Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.clickable {
                        val newFriend = "Friend ${selectedFriends.size + 1}"
                        if (!selectedFriends.contains(newFriend)) {
                            selectedFriends.add(newFriend)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = PocketPayIcons.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Friend",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Split Method Selector
            Text(
                text = "Split Method",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    SplitMethod.entries.forEach { method ->
                        val isSelected = method == selectedMethod
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedMethod = method }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedMethod = method },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (method == SplitMethod.EQUAL) "${method.label} ($eachAmountFormatted each)" else method.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Summary Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PrimaryBlue.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = PocketPayIcons.Info,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "You will pay $${String.format("%.2f", amountDouble)} now and request $eachAmountFormatted from ${selectedFriends.size} friends.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom CTA
            Button(
                onClick = {
                    val beneficiaryIds = beneficiaries.take(selectedFriends.size).map { it.beneficiaryId }
                    val finalIds = if (beneficiaryIds.isNotEmpty()) beneficiaryIds else listOf("ben_sarah", "ben_david", "ben_alex")
                    onCreate(
                        billTitle,
                        SplitBillCategory.DINING,
                        totalCents,
                        finalIds,
                        true,
                        "Split via PocketPay"
                    )
                    onDismiss()
                },
                enabled = !isProcessing && billTitle.isNotBlank() && totalCents > 0,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Send Split Requests",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
