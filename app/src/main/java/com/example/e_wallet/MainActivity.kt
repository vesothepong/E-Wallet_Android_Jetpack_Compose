package com.example.e_wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.ui.navigation.AppNavGraph
import com.example.e_wallet.ui.theme.EWalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = WalletRepositoryProvider.repository
            val isDarkTheme by repository.isDarkTheme.collectAsState()
            EWalletTheme(darkTheme = isDarkTheme) {
                AppNavGraph()
            }
        }
    }
}
