package com.example.e_wallet.ui.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.e_wallet.model.BillCategory
import com.example.e_wallet.model.Biller
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.ui.theme.WarningAmber
import kotlinx.coroutines.launch

/**
 * Screen 13: Biller Invoice Detail & Payment
 * Matches Image 14 and specifications:
 * - Header: Electricity Bill — National Power Co. (or selected category biller)
 * - Meter Number: MTR-9921401 with verified green check
 * - Bill Month: October 2026
 * - Invoice Summary Card:
 *   - Electricity Usage: 340 kWh
 *   - Energy Charges: $68.00
 *   - Government Tax (10%): $6.80
 *   - Late Fee: $0.00
 *   - Total Amount Due: $74.80
 * - Toggle switch: Enable Auto-Pay for this utility
 * - Bottom CTA: 52px primary indigo button "Pay $74.80 from PocketPay"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillPaymentBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialCategory: BillCategory? = BillCategory.ELECTRICITY,
    initialBiller: Biller? = null,
    repository: WalletRepository = WalletRepositoryProvider.repository,
    onPaymentSuccess: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var meterNumber by remember { mutableStateOf("MTR-9921401") }
    var billMonth by remember { mutableStateOf("October 2026") }
    var autoPayEnabled by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var paymentCompleted by remember { mutableStateOf(false) }

    val category = initialCategory ?: BillCategory.ELECTRICITY
    val billerName = when (category) {
        BillCategory.ELECTRICITY -> "National Power Co."
        BillCategory.WATER -> "City Water Utility"
        BillCategory.INTERNET -> "Metro Fiber Net"
        BillCategory.MOBILE_TOPUP -> "Telecom Direct"
        BillCategory.EDUCATION -> "Central State Tuition"
        BillCategory.GOVERNMENT -> "Prime Cable & Stream"
        else -> "National Power Co."
    }

    val totalDueFormatted = "$74.80"
    val totalDueCents = 7480L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        if (paymentCompleted) {
            // Success Receipt State
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = SuccessGreen.copy(alpha = 0.15f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = PocketPayIcons.CheckCircle,
                            contentDescription = "Success",
                            tint = SuccessGreen,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bill Payment Completed!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$billerName • $meterNumber",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "-$totalDueFormatted",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SuccessGreen
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onPaymentSuccess()
                    },
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
                        text = "Done",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarningAmber,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = PocketPayIcons.ElectricBolt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Electricity Bill",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = billerName,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = PocketPayIcons.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Account / Meter Number
                Text(
                    text = "Customer Account / Meter Number",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = meterNumber,
                    onValueChange = { meterNumber = it },
                    trailingIcon = {
                        Icon(
                            imageVector = PocketPayIcons.CheckCircle,
                            contentDescription = "Verified",
                            tint = SuccessGreen,
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

                Spacer(modifier = Modifier.height(14.dp))

                // Bill Month
                Text(
                    text = "Bill Month",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = billMonth,
                    onValueChange = { billMonth = it },
                    singleLine = true,
                    readOnly = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Invoice Summary Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Invoice Summary",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        InvoiceSummaryRow(label = "Electricity Usage", value = "340 kWh")
                        Spacer(modifier = Modifier.height(10.dp))
                        InvoiceSummaryRow(label = "Energy Charges", value = "$68.00")
                        Spacer(modifier = Modifier.height(10.dp))
                        InvoiceSummaryRow(label = "Government Tax (10%)", value = "$6.80")
                        Spacer(modifier = Modifier.height(10.dp))
                        InvoiceSummaryRow(label = "Late Fee", value = "$0.00")

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Amount Due",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = totalDueFormatted,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Enable Auto-Pay Toggle
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Auto-Pay",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Automatically deduct when new invoice arrives",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = autoPayEnabled,
                            onCheckedChange = { autoPayEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom CTA
                Button(
                    onClick = {
                        isProcessing = true
                        scope.launch {
                            repository.payBill(
                                billerId = "biller_elec_01",
                                billerName = billerName,
                                category = category,
                                accountNumber = meterNumber,
                                amountCents = totalDueCents,
                                note = "Electricity Bill Payment"
                            )
                            isProcessing = false
                            paymentCompleted = true
                        }
                    },
                    enabled = !isProcessing,
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
                        text = if (isProcessing) "Processing..." else "Pay $totalDueFormatted from PocketPay",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceSummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
