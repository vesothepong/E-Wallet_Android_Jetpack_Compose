package com.example.e_wallet.ui.bills

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.model.BillCategory
import com.example.e_wallet.model.Biller

/**
 * Modal bottom sheet wrapper for the complete Bill Payment workflow.
 * Allows instant contextual invocation from the Home Screen or Services section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillPaymentBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialCategory: BillCategory? = null,
    initialBiller: Biller? = null,
    repository: WalletRepository = WalletRepositoryProvider.repository
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        BillPaymentScreen(
            onBackClick = onDismiss,
            initialCategory = initialCategory,
            initialBiller = initialBiller,
            repository = repository,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        )
    }
}
